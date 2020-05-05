package ua.edu.sumdu.labwork2.springapp.services;

import ua.edu.sumdu.labwork2.springapp.model.Album;

import java.io.InputStream;
import java.net.URL;


public interface AlbumToDocFileService {
    byte[] saveToFile (Album album);
    String getDocFileName (String artistName, String albumName);
    InputStream getImageInputStream(URL connection);
}
