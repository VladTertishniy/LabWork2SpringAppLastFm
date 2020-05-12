package ua.edu.sumdu.labwork2.springapp.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Artist {
    private String name;
    private String url;

    public Artist(String artist) {
        this.name = artist;
    }
}
