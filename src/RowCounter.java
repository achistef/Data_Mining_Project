import java.io.File;
import java.util.Scanner;

public class RowCounter {

    public static void main(String[] args) throws Exception{

        Scanner sc = new Scanner(new File(args[0]));
        long c = 0;
        while(sc.hasNext()){
            sc.nextLine();
            c++;
        }
        System.out.println("Counted "+ c + " lines");

    }
}
