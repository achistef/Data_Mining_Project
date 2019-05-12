import java.util.*;
import java.util.Map.Entry;

/**
 * A map which counts the unique elements it contains
 *
 * @author achilles
 * @param <K> the type on which count map will operate
 * equals.
 */
public class CountMap<K extends Comparable<K>>
{

    private final HashMap<K, Integer> map;

    /**
     * Constructor
     */
    public CountMap()
    {
        //initialize the map
        this.map = new HashMap<>();
    }
    /**
     * Constructor
     *
     * @param collection the collection to be added
     */
    public CountMap(Collection<K> collection)
    {
        this();
        this.addAll(collection);
    }

    public Iterator<Map.Entry<K, Integer>> iterator(){
        return this.map.entrySet().iterator();
    }

    /**
     * Removes elements based on their value
     *
     * @param value the value to be deleted
     * @return a set containing all the removed elements
     */
    public Set<K> removeValues(Integer value)
    {
        HashSet<K> result = new HashSet<>();
        Iterator<Entry<K, Integer>> it = this.map.entrySet().iterator();
        while (it.hasNext())
        {
            Entry<K, Integer> entry = it.next();
            Integer x = entry.getValue();
            if (x.equals(value))
            {
                result.add(entry.getKey());
                it.remove();
            }
        }
        return result;
    }

    public Set<K> keySet(){
        return this.map.keySet();
    }

    /**
     * Clears the map. A clean map contains no elements
     */
    public void clearAll()
    {
        this.map.clear();
    }

    /**
     * Returns the times an element was added to this map
     *
     * @param key the element to be checked
     * @return the times the element was added
     */
    public Integer get(K key)
    {
        Integer value;
        if ((value = this.map.get(key)) != null)
        {
            return value;
        } else
        {
            return 0;
        }
    }

    /**
     * Adds an element to the map
     *
     * @param obj the element to be added
     */
    public void add(K obj)
    {
        Integer value;
        if ((value = this.map.get(obj)) != null)
        {
            this.map.replace(obj, value + 1);
        } else
        {
            this.map.put(obj, 1);
        }
    }

    /**
     * Adds a collection to the map
     *
     * @param collection the collection to be added
     */
    public final void addAll(Collection<K> collection)
    {
        Iterator<K> it = collection.iterator();
        while (it.hasNext())
        {
            K obj = it.next();
            this.add(obj);
        }

    }

    public int size()
    {
        return this.map.size();
    }

    public int numOfItems()
    {
        int counter = 0;
        for (Integer entry : this.map.values())
        {
            counter += entry;
        }
        return counter;
    }

    public String toString(){
        return this.map.toString();
    }
}

