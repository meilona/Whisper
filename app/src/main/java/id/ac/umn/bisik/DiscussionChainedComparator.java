package id.ac.umn.bisik;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DiscussionChainedComparator implements Comparator<Discussions> {

    private List<Comparator<Discussions>> listComparators;

    @SafeVarargs
    public DiscussionChainedComparator(Comparator<Discussions>... comparators) {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(Discussions dis1, Discussions dis2) {
        for (Comparator<Discussions> comparator : listComparators) {
            int result = comparator.compare(dis1, dis1);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
//
//    @Override
//    public int compareTo(Discussions discussions) {
//        return this.compareTo(discussions);
//
//    }
}
