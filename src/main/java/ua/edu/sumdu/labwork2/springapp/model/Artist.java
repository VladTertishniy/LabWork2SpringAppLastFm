package ua.edu.sumdu.labwork2.springapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Artist {
    private String name;
    private String url;

    public Artist(String artist) {
        this.name = artist;
    }
}
