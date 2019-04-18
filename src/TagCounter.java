import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

public class TagCounter {

    public void count(String input, String output) throws Exception {
        Scanner scanner = new Scanner(new File(input));
        scanner.nextLine(); //ignore headers
        CountMap<String> cmap = new CountMap<>();
        while(scanner.hasNext()){
            String next = scanner.nextLine();
            if(next.length() == 0) continue;
            String[] tags = next.split("\t")[6].split(",");
            cmap.addAll(Arrays.asList(tags));
        }
        PrintWriter writer = new PrintWriter(output);
        Iterator<Map.Entry<String, Integer>> it = cmap.iterator();
        while(it.hasNext()){
            Map.Entry<String, Integer> entry = it.next();
            writer.print(entry.getKey()+ ","+ entry.getValue()+ "\n");
        }
        writer.close();
    }

    public static void main(String[] args) throws Exception {
        String inputFile = args[0];
        String outputFile = args[1];
        TagCounter tc = new TagCounter();
        tc.count(inputFile, outputFile);
    }
}
