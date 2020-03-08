package ua.edu.sumdu.labwork2.springapp.services;

import java.net.URL;

public interface ImageDownloaderService {
    void downloadFiles(URL connection, String strPath, int buffSize, String imageName);
}
