package ua.edu.sumdu.labwork2.springapp.controller;


import com.fasterxml.jackson.databind.json.JsonMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import ua.edu.sumdu.labwork2.springapp.model.*;
import ua.edu.sumdu.labwork2.springapp.services.impl.AlbumServiceIml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;


@RestController
@RequestMapping(path = "/api")
public class MainController {

    public AlbumServiceIml albumServiceIml;

    @RequestMapping(
            path = "/test",
            method = RequestMethod.GET)
    public String test (@RequestParam(name = "album") String album, @RequestParam(name = "artist") String artist) {
        System.out.println("ok" + album + artist);
        return "ok" + artist + album;
    }

    @RequestMapping(path = "/test2/{artist}/{album}", method = RequestMethod.GET)
    public String getAlbumInfo (@PathVariable(name = "artist") String artist, @PathVariable(name = "album") String album) {

        HttpURLConnection connection = null;
        String result = "";
        try {
            connection = (HttpURLConnection) new URL("http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=b2abbb5334eaf31229236c6460ab5aec&artist=" + artist + "&album=" + album + "&format=json").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(250);
            connection.setReadTimeout(250);
            connection.connect();

            StringBuilder stringBuilder = new StringBuilder();

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append("\n");
                    }
                    result = stringBuilder.toString();
                    System.out.println(result);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        JSONObject albumJsonObject = new JSONObject(result).getJSONObject("album");

//       Object tracks = jsonObject.get("track");

        Album parsedAlbum = new Album();
        parsedAlbum.setArtist(new Artist(albumJsonObject.getString("artist")));
        parsedAlbum.setListeners(albumJsonObject.getInt("listeners"));
        parsedAlbum.setName(albumJsonObject.getString("name"));
        parsedAlbum.setPlaycount(albumJsonObject.getInt("playcount"));
        parsedAlbum.setPublished(parseDate(albumJsonObject.getJSONObject("wiki").getString("published")));
        parsedAlbum.setSummary(albumJsonObject.getJSONObject("wiki").getString("summary"));
        parsedAlbum.setUrl(albumJsonObject.getString("url"));

        JSONArray jsonArray = albumJsonObject.getJSONObject("tracks").getJSONArray("track");
        for (Object t : jsonArray) {
            JSONObject jsonTrack = (JSONObject) t;
            Track track = new Track();
            track.setDuration(jsonTrack.getInt("duration"));
            track.setName(jsonTrack.getString("name"));
            track.setUrl(jsonTrack.getString("url"));
            parsedAlbum.getTracks().add(track);
        }

        JSONArray jsonArrayTags = albumJsonObject.getJSONObject("tags").getJSONArray("tag");
        for (Object tagObject : jsonArrayTags) {
            JSONObject jsonTag = (JSONObject) tagObject;
            Tag tag = new Tag();
            tag.setName(jsonTag.getString("name"));
            tag.setUrl(jsonTag.getString("url"));
            parsedAlbum.getTags().add(tag);
        }

        JSONArray jsonImages = albumJsonObject.getJSONArray("image");
        for (Object imageObject : jsonImages) {
            JSONObject jsonImage = (JSONObject) imageObject;
            Image image = new Image();
            try {
                image.setUrl(new URL(jsonImage.getString("#text")));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            image.setSize(jsonImage.getString("size"));
            parsedAlbum.getImages().add(image);
        }

        albumServiceIml.saveToFile(parsedAlbum);


        try {
            return new JsonMapper().writer().writeValueAsString(parsedAlbum);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private LocalDateTime parseDate(String date) {
        // todo
        return LocalDateTime.now();
    }

    public MainController(AlbumServiceIml albumServiceIml) {
        this.albumServiceIml = albumServiceIml;
    }
}
