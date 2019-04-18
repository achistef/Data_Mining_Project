import post.TagPostPreweighted;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates a "Jaccard" Graph. It creates edges by comparing
 * all nodes based on weighted Jaccard similarity.
 */
public class JaccardGraph {

    // input file with posts
    private final File posts_file;
    // list - contains the posts in main memory
    private final List<TagPostPreweighted> tagPosts;
    // countMap - for counting the occurrence of each tag
    private final CountMap<String> tagCounter;



    public JaccardGraph(String inputFile) throws Exception {
        this.tagPosts = new ArrayList<>();
        this.tagCounter = new CountMap<>();
        this.posts_file = new File(inputFile);

        // read tagPosts
        Scanner scanner = new Scanner(posts_file);
        scanner.nextLine(); //ignore headers
        while (scanner.hasNext()) {
            String next = scanner.nextLine();
            if (next.length() == 0) continue;
            String[] split = next.split("\t");
            int postID = Integer.parseInt(split[0]);
            String[] tags = split[6].split(",");
            List<String> tagList = Arrays.asList(tags);
            this.tagCounter.addAll(tagList);
            TagPostPreweighted post = new TagPostPreweighted(postID, tagList);
            this.tagPosts.add(post);
        }

        for (TagPostPreweighted post : tagPosts) {
            post.setWeight(calcWeight(post.getTags()));
        }

    }

    // multithreaded execution
    private void findSimilaritiesParallel(String outputFile) throws Exception {

        AtomicInteger countdown = new AtomicInteger(0);
        int numOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        final int top = tagPosts.size();

        PrintWriter writer = new PrintWriter(new File(outputFile));
        DecimalFormat df = new DecimalFormat("#.00");

        for (int i = 0; i < numOfThreads; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int counter;
                    StringBuilder sb = new StringBuilder();

                    while ((counter = countdown.getAndIncrement()) < top) {
                        if(counter % 1000 == 0 ){
                            System.out.println(counter);
                        }
                        sb.setLength(0);
                        TagPostPreweighted post1 = tagPosts.get(counter);
                        final List<String> tags1 = post1.getTags();
                        int w1 = post1.getWeight();
                        sb.append(post1.getId());

                        for (int j = counter + 1; j < top; j++) {

                            TagPostPreweighted post2 = tagPosts.get(j);
                            final List<String> tags2 = post2.getTags();
                            int w2 = post2.getWeight();

                            int w3 = 0; // intersection weight
                            for (String tag : tags2) {
                                if (tags1.contains(tag)) {
                                    w3 += tagCounter.get(tag);
                                }
                            }

                            if (w3 != 0) {
                                float jaccardSimilarity = (float) w3 / (w1 + w2 - w3);
                                if (jaccardSimilarity >= 0.2) {
                                    sb.append(" ");
                                    sb.append(post2.getId());
                                    sb.append(" ");
                                    sb.append(df.format(jaccardSimilarity));
                                }
                            }
                        }
                        writer.println(sb.toString());
                    }
                }
            };
            executor.submit(runnable);
        }
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        writer.close();
    }

    private void findSimilarities() throws Exception {
//        for (int i = 0; i < tagPosts.size(); i++) {
//            TagPostPreweighted post1 = tagPosts.get(i);
//            final List<String> tags1 = post1.getTags();
//            int w1 = post1.getWeight();
//
//            for (int j = i+1 ; j < tagPosts.size(); j++) {
//
//                TagPostPreweighted post2 = tagPosts.get(j);
//                final List<String> tags2 = post2.getTags();
//                int w2 = post2.getWeight();
//
//                int w3 = 0; // intersection weight
//                for(String tag : tags2){
//                    if(tags1.contains(tag)){
//                        w3 += tagCounter.get(tag);
//                    }
//                }
//
//                if(w3 != 0){
//                    float jaccardSimilarity = (float)w3 / (w1 + w2 - w3);
//                    if(jaccardSimilarity >= 0.2){
//                        //System.out.println(post1.getId() + " " + post2.getId() + "  " + jaccardSimilarity);
//                        //print to file
//                    }
//                }
//
//            }
//        }
    }

    /**
     * calculates the weight of a given collection. The weight of each tag is simply
     * the number of occurrences in the dataset.
     */
    private int calcWeight(Collection<String> collection) {
        int result = 0;
        for (String tag : collection) {
            result += tagCounter.get(tag);
        }
        return result;
    }


    public static void main(String[] args) throws Exception {
        String inputFile = args[0];
        String outputFile = args[1];
        JaccardGraph sg = new JaccardGraph(inputFile);
        sg.findSimilaritiesParallel(outputFile);
    }

}
