import post.TagPost;

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
 * all tags based on weighted Jaccard similarity.
 */
public class TagJaccardGraph {

    // input file with posts
    private final File posts_file;
    // list - contains the posts in main memory
    private final List<TagPost> tagPosts;
    // countMap - for counting the occurrence of each tag
    private final CountMap<String> tagCounter;
    private final List<String> tagList;



    public TagJaccardGraph(String inputFile) throws Exception {
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
            TagPost post = new TagPost(postID, tagList);
            this.tagPosts.add(post);
        }

        this.tagList = new ArrayList<>(tagCounter.keySet());

    }

    // multithreaded execution
    private void findSimilaritiesParallel(final double threshold, String outputFile) throws Exception {

        AtomicInteger countdown = new AtomicInteger(0);
        int numOfThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numOfThreads);
        final int top = tagList.size();

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
                        String tag = tagList.get(counter);
                        sb.append(tag);

                        CountMap<String> localCountMap = new CountMap<>();
                        for(TagPost post : tagPosts){
                            if(post.getTags().contains(tag)){
                                localCountMap.addAll(post.getTags());
                            }
                        }

                        for (int j = counter + 1; j < top; j++) {
                            String tag2 = tagList.get(j);
                            int intersection = localCountMap.get(tag2);
                            if(intersection != 0){
                                int tagCount = tagCounter.get(tag);
                                int tag2Count = tagCounter.get(tag2);
                                float jaccardSimilarity = (float) intersection / (tagCount + tag2Count - intersection);

                                if(jaccardSimilarity >= threshold){
                                    sb.append(" ");
                                    sb.append(tag2);
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


    public static void main(String[] args) throws Exception {
        String inputFile = args[0];
        String outputFile = args[1];
        double threshold = 0.8;
        TagJaccardGraph sg = new TagJaccardGraph(inputFile);
        sg.findSimilaritiesParallel(threshold, outputFile);
    }

}
