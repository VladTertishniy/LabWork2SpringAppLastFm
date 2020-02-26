package ua.edu.sumdu.labwork2.springapp.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


@RestController
@RequestMapping(path = "/api")
public class MainController {

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
        JSONObject jsonObject = new JSONObject(result);
//        Object tracks = jsonObject.get("track");
        return "test2 " + artist + album;
    }
}
