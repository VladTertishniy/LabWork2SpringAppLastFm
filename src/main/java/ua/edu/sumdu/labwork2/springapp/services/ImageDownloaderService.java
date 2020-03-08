package ua.edu.sumdu.labwork2.springapp.services;

import java.io.File;
import java.net.URL;

public interface ImageDownloaderService {
    File downloadImage(URL connection, int buffSize, String imageName);
}
