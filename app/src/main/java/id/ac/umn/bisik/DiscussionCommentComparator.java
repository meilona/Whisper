package id.ac.umn.bisik;

import java.util.Comparator;

public class DiscussionCommentComparator implements Comparator<Discussions> {

    @Override
    public int compare(Discussions dis1, Discussions dis2) {
        return dis1.getComment() - dis2.getComment();
    }
}
