import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class FindCommunityTags {

    private final File post_file;
    private final File comm_file;

    public  FindCommunityTags(String postFile, String commFile) throws Exception{
        this.post_file = new File(postFile);
        this.comm_file = new File(commFile);
    }

    public void findCommTags(int k, String outputFile) throws Exception{

        Map<Integer, Integer> commMap = new HashMap<>();
        Scanner sc = new Scanner(this.comm_file);
        sc.nextLine();
        while (sc.hasNext()){
            String next = sc.nextLine();
            if(!next.equals("")) {
                String[] tokens = next.split(",,,");
                Integer ID = Integer.parseInt(tokens[0]);
                Integer modularity = Integer.parseInt(tokens[1]);
                commMap.put(ID, modularity);
            }
        }

        Map<Integer, CommInfo> commInfoMap= new HashMap<>();
        sc = new Scanner(this.post_file);
        while (sc.hasNext()){
            String next = sc.nextLine();
            if(!next.equals("")) {
                String[] tokens = next.split("\t");
                Integer ID = Integer.parseInt(tokens[0]);
                if (commMap.containsKey(ID)) {
                    Integer modularity = commMap.get(ID);
                    CommInfo commInfo = null;
                    if (commInfoMap.containsKey(modularity)) {
                        commInfo = commInfoMap.get(modularity);
                    } else {
                        commInfo = new CommInfo();
                        commInfoMap.put(modularity, commInfo);
                    }

                    String[] tokensList = tokens[6].split(",");
                    final List<String> strings = Arrays.asList(tokensList);
                    commInfo.addAll(strings);
                }
            }
        }

        Map<Integer, MyTreeMap<Integer,String>> commTopTags = new HashMap<>();
        for(Map.Entry<Integer, CommInfo> entry: commInfoMap.entrySet()){

            Integer modularity = entry.getKey();
            CommInfo commInfo = entry.getValue();
            MyTreeMap<Integer, String> treeMap = new MyTreeMap<>(k);
            final Iterator<Map.Entry<String, Integer>> iterator = commInfo.iterator();
            while(iterator.hasNext()){
                final Map.Entry<String, Integer> next = iterator.next();
                treeMap.put(next.getValue(), next.getKey());
            }
            commTopTags.put(modularity, treeMap);
        }

        PrintWriter writer = new PrintWriter(new File(outputFile));
        for(Map.Entry<Integer, MyTreeMap<Integer,String>> entry: commTopTags.entrySet()){
            writer.println(entry);
        }
        writer.close();

    }

    public static void main(String[] args) throws Exception {
        String postFile = args[0];
        String commFile = args[1];
        String outputFile = args[2];
        int k = Integer.parseInt(args[3]);
        FindCommunityTags ft = new FindCommunityTags(postFile, commFile);
        ft.findCommTags(k, outputFile);
    }
}
