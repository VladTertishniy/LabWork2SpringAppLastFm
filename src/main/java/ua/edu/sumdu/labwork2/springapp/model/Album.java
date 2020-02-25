package ua.edu.sumdu.labwork2.springapp.model;

public class Album {

    String name;
    Artist artist;
    int listeners;
    int playcount;
    String url;
    String published;
    String summary;

    public Album(String name, Artist artist, int listeners, int playcount, String url, String published, String summary) {
        this.name = name;
        this.artist = artist;
        this.listeners = listeners;
        this.playcount = playcount;
        this.url = url;
        this.published = published;
        this.summary = summary;
    }

    public Album() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public int getListeners() {
        return listeners;
    }

    public void setListeners(int listeners) {
        this.listeners = listeners;
    }

    public int getPlaycount() {
        return playcount;
    }

    public void setPlaycount(int playcount) {
        this.playcount = playcount;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
