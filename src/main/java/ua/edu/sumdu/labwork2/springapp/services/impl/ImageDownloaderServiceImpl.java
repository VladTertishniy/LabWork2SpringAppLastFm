package ua.edu.sumdu.labwork2.springapp.services.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.labwork2.springapp.services.ImageDownloaderService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;


@Service
public class ImageDownloaderServiceImpl implements ImageDownloaderService {

    @Getter
    @Setter
    private String dirPath;

    final static Logger logger = Logger.getLogger(ImageDownloaderServiceImpl.class);

    public ImageDownloaderServiceImpl(@Value("${lastfm.dir}") String dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public File downloadImage(URL connection, int buffSize, String imageName) {
        File fileImage = null;
        try {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                if (dir.mkdir()) {
                    logger.info("Directory " + dir.getAbsolutePath() + " is created!");
                }
            }
            String pathImageFile = dir.getPath() + "\\" + imageName + LocalDateTime.now().getYear() + LocalDateTime.now().getMonth() + ".png";
            fileImage = new File(pathImageFile);
            if (fileImage.exists()) {
                if (fileImage.delete()) {
                    logger.info("Deleted file: " + fileImage.getAbsolutePath());
                }
                fileImage = new File(pathImageFile);
            }
            if (fileImage.createNewFile()) {
                logger.info("Created file: " + fileImage.getAbsolutePath());
            }
//            URL connection = new URL(strURL);
            HttpURLConnection urlConnection;
            urlConnection = (HttpURLConnection) connection.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream in;
            in = urlConnection.getInputStream();
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
            return fileImage;
        } catch (IOException e) {
            logger.info("Connection or saving image on disk failed!", e);
            System.out.println("Unexpected error!");
        }
        return fileImage;
    }
}
