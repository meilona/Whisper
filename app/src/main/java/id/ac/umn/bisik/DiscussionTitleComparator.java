package id.ac.umn.bisik;

import java.util.Comparator;

public class DiscussionTitleComparator implements Comparator<Discussions> {

    @Override
    public int compare(Discussions dis1, Discussions dis2) {
        return dis1.getTitle().compareTo(dis2.getTitle());
    }
}