package id.ac.umn.bisik;

public class Discussions implements Comparable< Discussions >{
    private String title;
    private String user;
    private String idDiscussion;
    private String picture;
    private int comment;

    public int getComment() {
        return comment;
    }

    public void setComment(int comment) {
        this.comment = comment;
    }

    public Discussions(String title, String user, String idDiscussion, String picture, int comment) {
        this.title = title;
        this.user = user;
        this.idDiscussion = idDiscussion;
        this.picture = picture;
        this.comment = comment;
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

    public int compareTo(Discussions o) {
            return this.getTitle().compareTo(o.getTitle());
    }
}
