package gui.interaction;

public class AdItem {
    private Integer id;
    private String title;
    private String status;
    private String error;
    public AdItem(Integer id, String title, String status, String error) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.error = error;
    }
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getError() { return error; }
    @Override
    public String toString() {
        return "AdItem {" +
                "id=" + id +
                ", title='" + title + "'" +
                ", status='" + status + "'" +
                ", error='" + error + "'" +
                "}";
    }
}