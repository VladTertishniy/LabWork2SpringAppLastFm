package ua.edu.sumdu.labwork2.springapp.model;

import lombok.*;

import java.net.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Tag {
    private String name;
    private URL url;
}
