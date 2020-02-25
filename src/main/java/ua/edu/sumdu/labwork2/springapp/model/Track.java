package ua.edu.sumdu.labwork2.springapp.model;

public class Track {
    String name;
    String url;
    int duration;

    public Track(String name, String url, int duration) {
        this.name = name;
        this.url = url;
        this.duration = duration;
    }

    public Track() {
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
