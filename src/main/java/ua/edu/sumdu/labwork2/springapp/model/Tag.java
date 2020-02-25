package ua.edu.sumdu.labwork2.springapp.model;

public class Tag {
    String name;
    String url;

    public Tag(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public Tag() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
