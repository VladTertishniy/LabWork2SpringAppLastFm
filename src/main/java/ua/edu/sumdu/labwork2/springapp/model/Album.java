package ua.edu.sumdu.labwork2.springapp.model;

import lombok.*;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Album {
    private String name;
    private Artist artist;
    private int listeners;
    private int playcount;
    private String url;
    private String published;
    private String summary;
    private Collection<Track> tracks = new ArrayList<>();
    private Collection<Tag> tags = new ArrayList<>();
    private Collection<Image> images = new ArrayList<>();
}
