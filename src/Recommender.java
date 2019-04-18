import post.TagPost;

import java.io.File;
import java.util.*;

public class Recommender {

    private final List<TagPost> tagPosts;
    private final CountMap<String> tagCounter;
    private final File posts_file;
    private final File answers_file;

    public Recommender(String postsFile, String answersFile) throws Exception {
        this.tagPosts = new ArrayList<>();
        this.tagCounter = new CountMap<>();
        this.posts_file = new File(postsFile);
        this.answers_file = new File(answersFile);

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
            this.tagPosts.add(new TagPost(postID, tagList));
        }

    }

    private void findBestMatches(Set<String> tags, int howMany) throws Exception{
        MyTreeMap<Float, Integer> treeMap = new MyTreeMap<>(howMany);

        int inputWeight = calcWeight(tags);

        for(TagPost tagPost : tagPosts){

            int intersectionWeight = 0;
            for(String tag : tagPost.getTags()){
                if(tags.contains(tag)){
                    intersectionWeight += tagCounter.get(tag);
                }
            }

            if(intersectionWeight != 0){
                int arrayWeight = calcWeight(tagPost.getTags());
                float jaccardSimilarity = (float)intersectionWeight / (inputWeight + arrayWeight - intersectionWeight);
                treeMap.put(jaccardSimilarity, tagPost.getId());
            }
        }

        List<Integer> relativePosts =  treeMap.getValues();
        List<Integer> answerPosts = new ArrayList<>();

        Scanner scanner = new Scanner(posts_file);
        scanner.nextLine(); //ignore headers
        while (scanner.hasNext()) {
            String next = scanner.nextLine();
            if (next.length() == 0) continue;
            String[] split = next.split("\t");
            int postID = Integer.parseInt(split[0]);
            if(relativePosts.contains(postID)){
                answerPosts.add(Integer.parseInt(split[1]));
                relativePosts.remove((Integer)postID);
                if(relativePosts.isEmpty()) break;
            }
        }

        List<Integer> users = new ArrayList<>();

        scanner = new Scanner(answers_file);
        scanner.nextLine(); //ignore headers
        while (scanner.hasNext()) {
            String next = scanner.nextLine();
            if (next.length() == 0) continue;
            String[] split = next.split("\t");
            int postID = Integer.parseInt(split[0]);
            if(answerPosts.contains(postID)){
                users.add(Integer.parseInt(split[4]));
                answerPosts.remove((Integer)postID);
                if(answerPosts.isEmpty()) break;
            }
        }

    }

    private int calcWeight(Collection<String> collection){
        int result = 0;
        for(String tag : collection){
            result += tagCounter.get(tag);
        }
        return result;
    }


    public static void main(String[] args) throws Exception {

        String posts = args[0];
        String answers = args[1];
        int howMany = 2;
        Recommender sg = new Recommender(posts, answers);
        Set<String> set = new HashSet<>();
        set.add("c#");
        set.add(".net");
        sg.findBestMatches(set,howMany);
    }

}
