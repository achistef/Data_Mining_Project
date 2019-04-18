package post;

import java.util.List;

/**
 * A Post class that contains the ID of the post as well as the tags of it
 */
public class TagPost {
    private final int id;
    private final List<String> tags;

    public TagPost(int id, List<String> tags){
        this.id  = id;
        this.tags = tags;
    }

    public int getId() {
        return id;
    }

    public List<String> getTags() {
        return tags;
    }
}
