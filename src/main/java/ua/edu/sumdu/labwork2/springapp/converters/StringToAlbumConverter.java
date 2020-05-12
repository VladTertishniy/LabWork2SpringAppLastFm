package ua.edu.sumdu.labwork2.springapp.converters;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class StringToAlbumConverter implements Converter<String, Album> {

    final static Logger logger = Logger.getLogger(StringToAlbumConverter.class);

    @SuppressWarnings("rawtypes")
    private Map<Supplier, Consumer> map = new HashMap<>();

    private <T> void put(Supplier<T> supplier, Consumer<T> consumer) {
        map.put(supplier, consumer);
    }

    private Album initMap(JSONObject albumJsonObject) {
        Album album = new Album();
        map = new HashMap<>();
        put(() -> new Artist(albumJsonObject.getString("artist")), album::setArtist);
        put(() -> albumJsonObject.getInt("listeners"), album::setListeners);
        put(() -> albumJsonObject.getString("name"), album::setName);
        put(() -> albumJsonObject.getInt("playcount"), album::setPlaycount);
        put(() -> albumJsonObject.getString("published"), album::setPublished);
        put(() -> albumJsonObject.getString("summary"), album::setSummary);
        put(() -> albumJsonObject.getString("url"), album::setUrl);

        put(
            () -> {
                Collection<Track> trackArrayList = new ArrayList<>();
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
                    trackArrayList.add(track);
                }
                return trackArrayList;
            },
            album::setTracks
        );

        put(
                () -> {
                    Collection<Tag> tagArrayList = new ArrayList<>();
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
                        tagArrayList.add(tag);
                    }
                    return tagArrayList;
                },
                album::setTags
        );

        put(
                () -> {
                    Collection<Image> imageArrayList = new ArrayList<>();
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
                        imageArrayList.add(image);
                    }
                    return imageArrayList;
                },
                album::setImages
        );
        return album;
    }

    @Override
    @Cacheable("albums")
    public Album convert(@NotNull String source) throws JSONException {
        JSONObject albumJsonObject;
        try {
            albumJsonObject = new JSONObject(source).getJSONObject("album");
        } catch (JSONException e) {
            logger.info("Http request result is error!", e);
            return null;
        }
        Album album = initMap(albumJsonObject);

        //noinspection rawtypes
        for (Map.Entry<Supplier, Consumer> entry : map.entrySet()) {
            try {
                //noinspection unchecked
                entry.getValue().accept(entry.getKey().get());
            } catch (JSONException e) {
                logger.error(e);
            }
        }
        logger.info("HTTP request result successfully parsed.");
        return album;
    }
}
