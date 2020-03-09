package ua.edu.sumdu.labwork2.springapp.controller;


import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import ua.edu.sumdu.labwork2.springapp.model.*;
import ua.edu.sumdu.labwork2.springapp.services.impl.AlbumServiceImpl;
import ua.edu.sumdu.labwork2.springapp.services.impl.HTTPConnectionServiceImpl;
import ua.edu.sumdu.labwork2.springapp.services.impl.ImageDownloaderServiceImpl;

import java.io.File;
import java.net.URL;
import java.util.Iterator;


@RestController
@RequestMapping(path = "/api")
public class MainController {

    public AlbumServiceImpl albumServiceImpl;
    public HTTPConnectionServiceImpl httpConnectionServiceImpl;
    public ImageDownloaderServiceImpl imageDownloaderServiceImpl;
    final static Logger logger = Logger.getLogger(MainController.class);

    @RequestMapping(path = "/test2/{artist}/{album}", method = RequestMethod.GET)
    public String getAlbumInfo (@PathVariable(name = "artist") String artistName, @PathVariable(name = "album") String albumName) {

        logger.info("New request. Artist: " + artistName + ", album: " + albumName);
        String result = httpConnectionServiceImpl.getRequestResult(artistName, albumName);
        Album parsedAlbum = albumServiceImpl.parseFromString(result);
        if (parsedAlbum == null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", "error");
            jsonObject.put("message", "Album not found");
            logger.info(jsonObject.toString());
            return jsonObject.toString();
        }

        Iterator<Image> imageIterator = parsedAlbum.getImages().iterator();
        URL maxImageSizeUrl = null;
        while (imageIterator.hasNext()) {
            maxImageSizeUrl = imageIterator.next().getUrl();
        }
        if (maxImageSizeUrl != null) {
            File image = imageDownloaderServiceImpl.downloadImage(maxImageSizeUrl, 512, parsedAlbum.getName() + parsedAlbum.getArtist().getName());
            albumServiceImpl.saveToFile(parsedAlbum, image);
        }

        try {
            return new JsonMapper().writer().writeValueAsString(parsedAlbum);
        } catch (Throwable e) {
            logger.info("Data display failed!", e);
            throw new RuntimeException(e);
        }
    }

    public MainController(AlbumServiceImpl albumServiceImpl, HTTPConnectionServiceImpl httpConnectionServiceImpl, ImageDownloaderServiceImpl imageDownloaderServiceImpl) {
        this.albumServiceImpl = albumServiceImpl;
        this.httpConnectionServiceImpl = httpConnectionServiceImpl;
        this.imageDownloaderServiceImpl = imageDownloaderServiceImpl;
    }
}
