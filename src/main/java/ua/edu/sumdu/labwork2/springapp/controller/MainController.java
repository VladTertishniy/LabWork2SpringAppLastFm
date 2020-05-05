package ua.edu.sumdu.labwork2.springapp.controller;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import ua.edu.sumdu.labwork2.springapp.model.*;
import ua.edu.sumdu.labwork2.springapp.services.AlbumToDocFileService;
import ua.edu.sumdu.labwork2.springapp.services.HTTPConnectionService;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/api")
@AllArgsConstructor
public class MainController {

    private final AlbumToDocFileService albumToDocFileService;
    private final HTTPConnectionService httpConnectionService;
    private final Converter<String, Album> stringToAlbumConverter;
    final static Logger logger = Logger.getLogger(MainController.class);

    @RequestMapping(path = "/searchAlbumInfo/{artist}/{album}", produces = { "application/json", "application/xml" }, method = RequestMethod.GET)
    @Async
    public CompletableFuture<Album> getAlbumInfo (@PathVariable(name = "artist") String artistName, @PathVariable(name = "album") String albumName) {
        logger.info("New request. Artist: " + artistName + ", album: " + albumName);
        Album parsedAlbum = getParsedAlbum(artistName, albumName);
        try {
            return CompletableFuture.completedFuture(parsedAlbum);
        } catch (Throwable e) {
            logger.info("Data display failed!", e);
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(path = "/saveAlbumInfoToDoc/{artist}/{album}", produces = { "application/json", "application/xml" }, method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getAlbumDoc (@PathVariable(name = "artist") String artistName, @PathVariable(name = "album") String albumName) {
        logger.info("New request. Artist: " + artistName + ", album: " + albumName);
        Album parsedAlbum = getParsedAlbum(artistName, albumName);
        if (parsedAlbum == null) {
            return null;
        }
        byte[] byteArray = albumToDocFileService.saveToFile(parsedAlbum);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(byteArray));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + albumToDocFileService.getDocFileName(artistName, albumName) + ".docx")
                .body(resource);
    }

    private Album getParsedAlbum(String artistName, String albumName) {
        URL urlConnection = httpConnectionService.buildUrl(artistName, albumName);
        if (urlConnection == null) {
            return null;
        }
        String result = httpConnectionService.getRequestResult(urlConnection);
        return stringToAlbumConverter.convert(result);
    }
}
