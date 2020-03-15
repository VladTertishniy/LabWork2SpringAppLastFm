package ua.edu.sumdu.labwork2.springapp.controller;

import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import ua.edu.sumdu.labwork2.springapp.model.*;
import ua.edu.sumdu.labwork2.springapp.services.StringToAlbumConverter;
import ua.edu.sumdu.labwork2.springapp.services.impl.SaveToDocFileServiceImpl;
import ua.edu.sumdu.labwork2.springapp.services.impl.HTTPConnectionServiceImpl;
import ua.edu.sumdu.labwork2.springapp.services.impl.ImageDownloaderServiceImpl;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class MainController {

    public SaveToDocFileServiceImpl saveToDocFileServiceImpl;
    public HTTPConnectionServiceImpl httpConnectionServiceImpl;
    public ImageDownloaderServiceImpl imageDownloaderServiceImpl;
    public StringToAlbumConverter stringToAlbumConverter;
    final static Logger logger = Logger.getLogger(MainController.class);

    @RequestMapping(path = "/searchAlbumInfo/{artist}/{album}", method = RequestMethod.GET)
    @Async
    public CompletableFuture<String> getAlbumInfo (@PathVariable(name = "artist") String artistName, @PathVariable(name = "album") String albumName) {

        logger.info("New request. Artist: " + artistName + ", album: " + albumName);
        URL urlConnection = httpConnectionServiceImpl.buildUrl(artistName, albumName);
        if (urlConnection == null) {
            return CompletableFuture.completedFuture("Unexpected error!");
        }
        String result = httpConnectionServiceImpl.getRequestResult(urlConnection);
        Album parsedAlbum = stringToAlbumConverter.convert(result);
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
            saveToDocFileServiceImpl.saveToFile(parsedAlbum, image);
        }

        try {
            return CompletableFuture.completedFuture(new JsonMapper().writer().writeValueAsString(parsedAlbum));
        } catch (Throwable e) {
            logger.info("Data display failed!", e);
            throw new RuntimeException(e);
        }
    }
}
