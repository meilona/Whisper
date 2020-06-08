package id.ac.umn.bisik;

public class DiscussionsDelete implements Comparable<DiscussionsDelete>{
    private String title;
    private String user;
    private String idDiscussion;
    private String picture;

    public DiscussionsDelete(String title, String user, String idDiscussion, String picture) {
        this.title = title;
        this.user = user;
        this.idDiscussion = idDiscussion;
        this.picture = picture;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getIdDiscussion() {
        return idDiscussion;
    }

    public void setIdDiscussion(String idDiscussion) {
        this.idDiscussion = idDiscussion;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int compareTo(DiscussionsDelete o) {
            return this.getTitle().compareTo(o.getTitle());
    }
}
