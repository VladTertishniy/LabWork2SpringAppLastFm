package ua.edu.sumdu.labwork2.springapp.services;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.labwork2.springapp.model.*;

import java.net.MalformedURLException;
import java.net.URL;

@Service
public class StringToAlbumConverter implements Converter<String, Album> {

    final static Logger logger = Logger.getLogger(StringToAlbumConverter.class);

    @Override
    @Cacheable("albums")
    public Album convert(@NotNull String source) {
        Album parsedAlbum;
        JSONObject albumJsonObject;
        try {
            albumJsonObject = new JSONObject(source).getJSONObject("album");
        } catch (JSONException e) {
            logger.info("Http request result is error!", e);
            return null;
        }
        parsedAlbum = new Album();
        parsedAlbum.setArtist(new Artist(albumJsonObject.getString("artist")));
        parsedAlbum.setListeners(albumJsonObject.getInt("listeners"));
        parsedAlbum.setName(albumJsonObject.getString("name"));
        parsedAlbum.setPlaycount(albumJsonObject.getInt("playcount"));
        parsedAlbum.setPublished(albumJsonObject.getJSONObject("wiki").getString("published"));
        parsedAlbum.setSummary(albumJsonObject.getJSONObject("wiki").getString("summary"));
        parsedAlbum.setUrl(albumJsonObject.getString("url"));

        JSONArray jsonArray = albumJsonObject.getJSONObject("tracks").getJSONArray("track");
        for (Object t : jsonArray) {
            JSONObject jsonTrack = (JSONObject) t;
            Track track = new Track();
            track.setDuration(jsonTrack.getInt("duration"));
            track.setName(jsonTrack.getString("name"));
            try {
                track.setUrl(new URL(jsonTrack.getString("url")));
            } catch (MalformedURLException e) {
                logger.info("Incorrect value of track url! + \n + Conversion String to URL impossible", e);
                System.out.println("Unexpected error!");
            }
            parsedAlbum.getTracks().add(track);
        }

        JSONArray jsonArrayTags = albumJsonObject.getJSONObject("tags").getJSONArray("tag");
        for (Object tagObject : jsonArrayTags) {
            JSONObject jsonTag = (JSONObject) tagObject;
            Tag tag = new Tag();
            tag.setName(jsonTag.getString("name"));
            try {
                tag.setUrl(new URL(jsonTag.getString("url")));
            } catch (MalformedURLException e) {
                logger.info("Incorrect value of tag url! + \n + Conversion String to URL impossible", e);
                System.out.println("Unexpected error!");
            }
            parsedAlbum.getTags().add(tag);
        }

        JSONArray jsonImages = albumJsonObject.getJSONArray("image");
        for (Object imageObject : jsonImages) {
            JSONObject jsonImage = (JSONObject) imageObject;
            Image image = new Image();
            try {
                image.setUrl(new URL(jsonImage.getString("#text")));
            } catch (MalformedURLException e) {
                logger.info("Incorrect value of image url! + \n + Conversion String to URL impossible", e);
                System.out.println("Unexpected error!");
            }
            image.setSize(jsonImage.getString("size"));
            parsedAlbum.getImages().add(image);
        }
        logger.info("HTTP request result successfully parsed.");
        return parsedAlbum;
    }
}
