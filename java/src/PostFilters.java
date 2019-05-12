import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class PostFilters {


    public void filter_noAnswer(String input, String output) throws Exception{
        PrintWriter writer = new PrintWriter(output);

        Scanner sc = new Scanner(new File(input));
        while(sc.hasNext()){
            String next = sc.nextLine();
            if(!next.equals("")){
                String[] split = next.split("\t");
                if(!split[1].equals("null")){
                    writer.print(next);
                    writer.print("\n");
                }
            }
        }
        writer.close();
    }

    public void filter_byDate(String input, String date, String output) throws Exception{
        PrintWriter writer = new PrintWriter(output);
        Scanner sc = new Scanner(new File(input));
        sc.nextLine();
        while(sc.hasNext()){
            String next = sc.nextLine();
            if(!next.equals("")){
                String[] split = next.split("\t");
                if(split[2].startsWith(date)){
                    writer.print(next);
                    writer.print("\n");
                }
            }
        }
        writer.close();
    }

    public static void main(String[] args) throws Exception{

        String inputFile = args[0];
        String outputFile = args[1];
        String date = null;
        if (args.length > 2){
            date = args[2];
        }
        PostFilters filter = new PostFilters();
        if (date == null){
            filter.filter_noAnswer(inputFile, outputFile);
        }else{
            filter.filter_byDate(inputFile, date, outputFile);
        }
    }

}
