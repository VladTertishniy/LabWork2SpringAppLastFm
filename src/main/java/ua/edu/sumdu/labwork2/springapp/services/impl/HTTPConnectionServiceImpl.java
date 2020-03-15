package ua.edu.sumdu.labwork2.springapp.services.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import ua.edu.sumdu.labwork2.springapp.services.HTTPConnectionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@Service
public class HTTPConnectionServiceImpl implements HTTPConnectionService {

    @Getter
    @Setter
    private String apiKey;
    private String scheme;
    private String host;
    private String method;
    private String format;

    final static Logger logger = Logger.getLogger(HTTPConnectionServiceImpl.class);

    public HTTPConnectionServiceImpl(@Value("${lastfm.apikey}") String apiKey,
                                     @Value("${lastfm.scheme}") String scheme,
                                     @Value("${lastfm.host}") String host,
                                     @Value("${lastfm.method}") String method,
                                     @Value("${lastfm.format}") String format) {
        this.apiKey = apiKey;
        this.scheme = scheme;
        this.host = host;
        this.method = method;
        this.format = format;
    }

    @Override
    @Cacheable("results")
    public String getRequestResult(URL urlConnection) {

        HttpURLConnection connection = null;
        String result = "";
        try {
            connection = (HttpURLConnection) urlConnection.openConnection();
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

    @Override
    public URL buildUrl (String artistName, String albumName) {
        try {
            return UriComponentsBuilder.newInstance().scheme(scheme).host(host)
                    .path("/2.0/")
                    .query("method={method}")
                    .query("api_key={apiKey}")
                    .query("artist={artistName}")
                    .query("album={albumName}")
                    .query("format={format}")
                    .buildAndExpand(method, apiKey, artistName, albumName, format).toUri().toURL();
        } catch (MalformedURLException e) {
            logger.info("Building url failed!", e);
            System.out.println("Unexpected error!");
        }
        return null;
    }
}
