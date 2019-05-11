package post;

import java.util.List;

/**
 * A Post class that contains the ID of the post, its tags and a weight.
 */
public class TagPostPreweighted extends TagPost {

    private int weight;

    public TagPostPreweighted(int id, List<String> tags){
        super(id, tags);
        this.weight = weight;

    }

    public void setWeight(int weight){
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
