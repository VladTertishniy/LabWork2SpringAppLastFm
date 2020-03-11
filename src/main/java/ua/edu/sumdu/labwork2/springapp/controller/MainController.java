package ua.edu.sumdu.labwork2.springapp.controller;


import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import ua.edu.sumdu.labwork2.springapp.model.*;
import ua.edu.sumdu.labwork2.springapp.services.impl.AlbumServiceImpl;
import ua.edu.sumdu.labwork2.springapp.services.impl.HTTPConnectionServiceImpl;
import ua.edu.sumdu.labwork2.springapp.services.impl.ImageDownloaderServiceImpl;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping(path = "/api")
public class MainController {

    public AlbumServiceImpl albumServiceImpl;
    public HTTPConnectionServiceImpl httpConnectionServiceImpl;
    public ImageDownloaderServiceImpl imageDownloaderServiceImpl;
    final static Logger logger = Logger.getLogger(MainController.class);

    @RequestMapping(path = "/test2/{artist}/{album}", method = RequestMethod.GET)
    @Async/*("workExecutor")*/
    public CompletableFuture<String> getAlbumInfo (@PathVariable(name = "artist") String artistName, @PathVariable(name = "album") String albumName) {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("New request. Artist: " + artistName + ", album: " + albumName);
        String result = httpConnectionServiceImpl.getRequestResult(artistName, albumName);
        Album parsedAlbum = albumServiceImpl.parseFromString(result);
        if (parsedAlbum == null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", "error");
            jsonObject.put("message", "Album not found");
            logger.info(jsonObject.toString());
            return CompletableFuture.completedFuture(jsonObject.toString());
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
            return CompletableFuture.completedFuture(new JsonMapper().writer().writeValueAsString(parsedAlbum));
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
