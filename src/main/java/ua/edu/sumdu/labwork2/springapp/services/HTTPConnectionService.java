package ua.edu.sumdu.labwork2.springapp.services;

import java.net.URL;

public interface HTTPConnectionService {
    String getRequestResult (URL urlConnection);
    URL buildUrl (String artistName, String albumName);
}
