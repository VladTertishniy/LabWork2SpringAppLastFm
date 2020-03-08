package ua.edu.sumdu.labwork2.springapp.controller;


import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.web.bind.annotation.*;
import ua.edu.sumdu.labwork2.springapp.model.*;
import ua.edu.sumdu.labwork2.springapp.services.impl.AlbumServiceIml;
import ua.edu.sumdu.labwork2.springapp.services.impl.HTTPConnectionServiceImp;
import ua.edu.sumdu.labwork2.springapp.services.impl.ImageDownloaderServiceImp;

import java.io.File;
import java.net.URL;
import java.util.Iterator;


@RestController
@RequestMapping(path = "/api")
public class MainController {

    public AlbumServiceIml albumServiceIml;
    public HTTPConnectionServiceImp httpConnectionServiceImp;
    public ImageDownloaderServiceImp imageDownloaderServiceImp;

    @RequestMapping(path = "/test2/{artist}/{album}", method = RequestMethod.GET)
    public String getAlbumInfo (@PathVariable(name = "artist") String artistName, @PathVariable(name = "album") String albumName) {

        String result = httpConnectionServiceImp.getRequestResult(artistName, albumName);
        Album parsedAlbum = albumServiceIml.parseFromString(result);

        Iterator<Image> imageIterator = parsedAlbum.getImages().iterator();
        URL maxImageSizeUrl = null;
        while (imageIterator.hasNext()) {
            maxImageSizeUrl = imageIterator.next().getUrl();
        }
        if (maxImageSizeUrl != null) {
            File image = imageDownloaderServiceImp.downloadImage(maxImageSizeUrl, 512, parsedAlbum.getName() + parsedAlbum.getArtist().getName());
            albumServiceIml.saveToFile(parsedAlbum, image);
        }

        try {
            return new JsonMapper().writer().writeValueAsString(parsedAlbum);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public MainController(AlbumServiceIml albumServiceIml, HTTPConnectionServiceImp httpConnectionServiceImp, ImageDownloaderServiceImp imageDownloaderServiceImp) {
        this.albumServiceIml = albumServiceIml;
        this.httpConnectionServiceImp = httpConnectionServiceImp;
        this.imageDownloaderServiceImp = imageDownloaderServiceImp;
    }
}
