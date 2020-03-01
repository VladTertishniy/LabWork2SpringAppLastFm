package ua.edu.sumdu.labwork2.springapp.services;

import org.springframework.beans.factory.annotation.Value;
import ua.edu.sumdu.labwork2.springapp.model.Album;

import java.io.File;

public interface AlbumService {

    void saveToFile (Album album);
    //Album parsFromFile (File file);
}
