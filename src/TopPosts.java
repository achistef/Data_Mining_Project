import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

public class TopPosts {

    public void keepTop(String input, int howMany, String output) throws Exception{
        PrintWriter writer = new PrintWriter(output);
        Scanner sc = new Scanner(new File(input));
        MyTreeMap<Integer, String> treeMap = new MyTreeMap<>(howMany);
        while(sc.hasNext()){
            String next = sc.nextLine();
            if(!next.equals("")){
                String[] split = next.split("\t");
                int views = Integer.parseInt(split[4]);
                treeMap.put(views, next);
            }
        }

        List<String> topPosts = treeMap.getValues();
        for(String post : topPosts){
            writer.println(post);
        }

        writer.close();
    }

    public static void main(String[] args) throws Exception{
        String inputFile = args[0];
        String outputFile = args[1];
        Integer howMany = Integer.parseInt(args[2]);
        TopPosts tp = new TopPosts();
        tp.keepTop(inputFile, 1000, outputFile);
    }
}
