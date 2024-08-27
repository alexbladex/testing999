package ByGuiInteraction;

public class AdItem {
    private Integer id;
    private String title;
    public AdItem(Integer id, String title) {
        this.id = id;
        this.title = title;
    }
    public Integer getId() { return id; }
    public String getTitle() { return title; }
    @Override
    public String toString() {
        return "AdItem {" +
                "id=" + id +
                ", title='" + title + "'" +
                "}";
    }
}