package ua.edu.sumdu.labwork2.springapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    private URL url;
    private String size;
}
