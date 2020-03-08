package ua.edu.sumdu.labwork2.springapp.services.impl;

import org.springframework.stereotype.Service;
import ua.edu.sumdu.labwork2.springapp.services.ImageDownloaderService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;


@Service
public class ImageDownloaderServiceImp implements ImageDownloaderService {
    @Override
    public void downloadFiles(URL connection, String strPath, int buffSize, String imageName) {
        try {
            File dir = new File(strPath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            String pathImageFile = dir.getPath() + "\\" + imageName + LocalDateTime.now().getYear() + LocalDateTime.now().getMonth() + ".png";
            File fileImage = new File(pathImageFile);
            if (!fileImage.exists()) {
                fileImage.createNewFile();
            } else {
                fileImage.delete();
                fileImage = new File(pathImageFile);
                fileImage.createNewFile();
            }
//            URL connection = new URL(strURL);
            HttpURLConnection urlconn;
            urlconn = (HttpURLConnection) connection.openConnection();
            urlconn.setRequestMethod("GET");
            urlconn.connect();
            InputStream in;
            in = urlconn.getInputStream();
            OutputStream writer = new FileOutputStream(fileImage);
            byte[] buffer = new byte[buffSize];
            int c = in.read(buffer);
            while (c > 0) {
                writer.write(buffer, 0, c);
                c = in.read(buffer);
            }
            writer.flush();
            writer.close();
            in.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
