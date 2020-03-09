package ua.edu.sumdu.labwork2.springapp.services.impl;

import lombok.Getter;
import lombok.Setter;
import netscape.javascript.JSException;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.labwork2.springapp.model.*;
import ua.edu.sumdu.labwork2.springapp.services.AlbumService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

@Service
public class AlbumServiceImpl implements AlbumService {

    final static Logger logger = Logger.getLogger(AlbumServiceImpl.class);

    @Getter
    @Setter
    private String dirPath;

    public AlbumServiceImpl(@Value("${lastfm.dir}") String dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public Album parseFromString(String httpRequestResult) {
        Album parsedAlbum;
        JSONObject albumJsonObject;
        try {
            albumJsonObject = new JSONObject(httpRequestResult).getJSONObject("album");
        } catch (JSONException e) {
            logger.info("Http request result is error!", e);
            return null;
        }
        parsedAlbum = new Album();
        parsedAlbum.setArtist(new Artist(albumJsonObject.getString("artist")));
        parsedAlbum.setListeners(albumJsonObject.getInt("listeners"));
        parsedAlbum.setName(albumJsonObject.getString("name"));
        parsedAlbum.setPlaycount(albumJsonObject.getInt("playcount"));
        parsedAlbum.setPublished(albumJsonObject.getJSONObject("wiki").getString("published"));
        parsedAlbum.setSummary(albumJsonObject.getJSONObject("wiki").getString("summary"));
        parsedAlbum.setUrl(albumJsonObject.getString("url"));

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
            parsedAlbum.getTracks().add(track);
        }

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
            parsedAlbum.getTags().add(tag);
        }

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
            parsedAlbum.getImages().add(image);
        }
        logger.info("HTTP request result successfully parsed.");
        return parsedAlbum;
    }

    @Override
    public void saveToFile(Album album, File imageFile) {

        // создаем модель docx документа,
        // к которой будем прикручивать наполнение (колонтитулы, текст)
        XWPFDocument docxModel = new XWPFDocument();
        CTSectPr ctSectPr = docxModel.getDocument().getBody().addNewSectPr();
        // получаем экземпляр XWPFHeaderFooterPolicy для работы с колонтитулами
        XWPFHeaderFooterPolicy headerFooterPolicy = null;
        try {
            headerFooterPolicy = new XWPFHeaderFooterPolicy(docxModel, ctSectPr);
        } catch (IOException | XmlException e) {
            System.out.println("Unexpected error!");
            logger.info("Creating XWPFHeaderFooterPolicy is failed!", e);
        }

        // создаем верхний колонтитул Word файла
        CTP ctpHeaderModel = createHeaderModel("Terishniy Vladislav labwork №2 Spring Boot Application" + "\n" + LocalDateTime.now());
        // устанавливаем сформированный верхний
        // колонтитул в модель документа Word
        XWPFParagraph headerParagraph = new XWPFParagraph(ctpHeaderModel, docxModel);
        try {
            if (headerFooterPolicy != null) {
                headerFooterPolicy.createHeader(XWPFHeaderFooterPolicy.DEFAULT, new XWPFParagraph[]{headerParagraph});
            }
        } catch (IOException e) {
            System.out.println("Unexpected error!");
            logger.info("Creating header is failed!", e);
        }

        // создаем нижний колонтитул docx файла
        CTP ctpFooterModel = createFooterModel("Netcracker course 2019-2020"  + "\n" + LocalDateTime.now());
        // устанавливаем сформированый нижний
        // колонтитул в модель документа Word
        XWPFParagraph footerParagraph = new XWPFParagraph(ctpFooterModel, docxModel);
        try {
            if (headerFooterPolicy != null) {
                headerFooterPolicy.createFooter(XWPFHeaderFooterPolicy.DEFAULT, new XWPFParagraph[]{footerParagraph});
            }
        } catch (IOException e) {
            System.out.println("Unexpected error!");
            logger.info("Creating footer is failed!", e);
        }
        int format = XWPFDocument.PICTURE_TYPE_PNG;
        try {
            createParagraph(docxModel).addPicture(new FileInputStream(imageFile.getAbsoluteFile()), format, imageFile.getName(), Units.toEMU(300), Units.toEMU(300));
        } catch (InvalidFormatException | IOException e) {
            System.out.println("Unexpected error!");
            logger.info("Adding picture to paragraph is failed!", e);
        }
        createParagraph(docxModel).setText("Name: " + album.getName());
        createParagraph(docxModel).setText("Artist: " + album.getArtist().getName());
        createParagraph(docxModel).setText("Listeners: " + album.getListeners());
        StringBuilder tagsStringBuilder = new StringBuilder();
        tagsStringBuilder.append("Tags: ");
        for (Tag tag : album.getTags()) {
            tagsStringBuilder.append(tag.getName()).append(", ");
        }
        createParagraph(docxModel).setText(tagsStringBuilder.toString());

        //создаем таблицу
        XWPFTable tracksTable = docxModel.createTable(album.getTracks().size() + 1,3);
        tracksTable.getRow(0).getCell(0).setText("name");
        tracksTable.getRow(0).getCell(1).setText("duration");
        tracksTable.getRow(0).getCell(2).setText("url");
        int RowCounter = 1;
        for (Track track : album.getTracks()) {
            tracksTable.getRow(RowCounter).getCell(0).setText(track.getName());
            tracksTable.getRow(RowCounter).getCell(1).setText(String.valueOf(track.getDuration()));
            tracksTable.getRow(RowCounter).getCell(2).setText(track.getUrl().toString());
            RowCounter++;
        }

        // сохраняем модель docx документа в файл
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdir()) {
                logger.info("Directory " + dir.getAbsolutePath() + " is created!");
            }
        }
        String pathDocFile = dir.getPath() + "\\" +
                album.getName() +
                album.getArtist().getName() +
                LocalDateTime.now().getYear() +
                LocalDateTime.now().getMonth() +
                LocalDateTime.now().getDayOfMonth() +
                ".docx";
        File fileDoc = new File(pathDocFile);
        try {
            if (fileDoc.exists()) {
                if (fileDoc.delete()) {
                    logger.info("Deleted file: " + fileDoc.getAbsolutePath());
                }
                fileDoc = new File(pathDocFile);
            }
            if (fileDoc.createNewFile()) {
                logger.info("Created file: " + fileDoc.getAbsolutePath());
            }
            FileOutputStream outputStream = new FileOutputStream(fileDoc);
            docxModel.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Unexpected error!");
            logger.info("Saving doxc model to file is failed!", e);
        }
    }

    @NotNull
    private static CTP createFooterModel(String footerContent) {
        // создаем футер или нижний колонтитул
        CTP ctpFooterModel = CTP.Factory.newInstance();
        CTR ctrFooterModel = ctpFooterModel.addNewR();
        CTText cttFooter = ctrFooterModel.addNewT();

        cttFooter.setStringValue(footerContent);
        return ctpFooterModel;
    }

    @NotNull
    private static CTP createHeaderModel(String headerContent) {
        // создаем хедер или верхний колонтитул
        CTP ctpHeaderModel = CTP.Factory.newInstance();
        CTR ctrHeaderModel = ctpHeaderModel.addNewR();
        CTText cttHeader = ctrHeaderModel.addNewT();

        cttHeader.setStringValue(headerContent);
        return ctpHeaderModel;
    }

    @NotNull
    private static XWPFRun createParagraph (@NotNull XWPFDocument docxModel) {
        XWPFParagraph bodyParagraph = docxModel.createParagraph();
        bodyParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun paragraphConfig = bodyParagraph.createRun();
        paragraphConfig.setItalic(true);
        paragraphConfig.setFontSize(16);
        // HEX цвет без решетки #
        paragraphConfig.setColor("4036d6");
        return paragraphConfig;
    }
}
