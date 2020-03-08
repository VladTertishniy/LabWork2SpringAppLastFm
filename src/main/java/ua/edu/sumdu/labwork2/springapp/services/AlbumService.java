package ua.edu.sumdu.labwork2.springapp.services;

import ua.edu.sumdu.labwork2.springapp.model.Album;

import java.io.File;


public interface AlbumService {
    void saveToFile (Album album, File image);
    Album parseFromString (String httpRequestResult);
}
