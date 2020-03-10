package ua.edu.sumdu.labwork2.springapp.services.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.labwork2.springapp.services.HTTPConnectionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class HTTPConnectionServiceImpl implements HTTPConnectionService {

    @Getter
    @Setter
    private String apiKey;

    final static Logger logger = Logger.getLogger(HTTPConnectionServiceImpl.class);

    public HTTPConnectionServiceImpl(@Value("${lastfm.apikey}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    @Cacheable("results")
    public String getRequestResult(String artistName, String albumName) {
        HttpURLConnection connection = null;
        String result = "";
        try {
            connection = (HttpURLConnection) new URL("http://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=" + apiKey + "&artist=" + artistName + "&album=" + albumName + "&format=json").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(500);
            connection.setReadTimeout(500);
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
            logger.info("Connection or reading result from server failed!", e);
            System.out.println("Unexpected error!");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }
}
