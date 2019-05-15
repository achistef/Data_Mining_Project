import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class CommInfo {

    private final CountMap<String> countMap;

    public CommInfo(){
        this.countMap = new CountMap<>();
    }

    public void addAll(Collection<String> tokens){
            this.countMap.addAll(tokens);
    }

    public Iterator<Map.Entry<String, Integer>> iterator(){
        return this.countMap.iterator();
    }



}
