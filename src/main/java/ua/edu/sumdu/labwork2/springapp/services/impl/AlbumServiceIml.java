package ua.edu.sumdu.labwork2.springapp.services.impl;

import org.springframework.stereotype.Service;
import ua.edu.sumdu.labwork2.springapp.model.Album;
import ua.edu.sumdu.labwork2.springapp.services.AlbumService;

import java.io.File;

@Service
public class AlbumServiceIml implements AlbumService {
    @Override
    public void saveToFile(Album album, File file) {
        // todo
    }

    @Override
    public Album parsFromFile(File file) {
        return null; // todo
    }
}
