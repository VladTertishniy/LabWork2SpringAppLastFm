package ua.edu.sumdu.labwork2.springapp.services.impl;

import lombok.Getter;
import lombok.Setter;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;

@Service
public class AlbumServiceIml implements AlbumService {

    @Getter
    @Setter
    private String dirPath;

    public AlbumServiceIml(@Value("${lastfm.dir}") String dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public Album parseFromString(String httpRequestResult) {
        JSONObject albumJsonObject = new JSONObject(httpRequestResult).getJSONObject("album");
        Album parsedAlbum = new Album();
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
                e.printStackTrace();
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
                e.printStackTrace();
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
                e.printStackTrace();
            }
            image.setSize(jsonImage.getString("size"));
            parsedAlbum.getImages().add(image);
        }
        return parsedAlbum;
    }

    @Override
    public void saveToFile(Album album, File imageFile) {

        try {
            // создаем модель docx документа,
            // к которой будем прикручивать наполнение (колонтитулы, текст)
            XWPFDocument docxModel = new XWPFDocument();
            CTSectPr ctSectPr = docxModel.getDocument().getBody().addNewSectPr();
            // получаем экземпляр XWPFHeaderFooterPolicy для работы с колонтитулами
            XWPFHeaderFooterPolicy headerFooterPolicy = new XWPFHeaderFooterPolicy(docxModel, ctSectPr);

            // создаем верхний колонтитул Word файла
            CTP ctpHeaderModel = createHeaderModel(
                    "Terishniy Vladislav labwork №2 Spring Boot Application"
            );
            // устанавливаем сформированный верхний
            // колонтитул в модель документа Word
            XWPFParagraph headerParagraph = new XWPFParagraph(ctpHeaderModel, docxModel);
            headerFooterPolicy.createHeader(
                    XWPFHeaderFooterPolicy.DEFAULT,
                    new XWPFParagraph[]{headerParagraph}
            );

            // создаем нижний колонтитул docx файла
            CTP ctpFooterModel = createFooterModel("Netcracker course 2019-2020");
            // устанавливаем сформированый нижний
            // колонтитул в модель документа Word
            XWPFParagraph footerParagraph = new XWPFParagraph(ctpFooterModel, docxModel);
            headerFooterPolicy.createFooter(
                    XWPFHeaderFooterPolicy.DEFAULT,
                    new XWPFParagraph[]{footerParagraph}
            );
            int format = XWPFDocument.PICTURE_TYPE_PNG;
            createParagraph(docxModel).addPicture(new FileInputStream(imageFile.getAbsoluteFile()), format, imageFile.getName(), Units.toEMU(300), Units.toEMU(300));
            createParagraph(docxModel).setText("Name: " + album.getName());
            createParagraph(docxModel).setText("Artist: " + album.getArtist().getName());
            createParagraph(docxModel).setText("Listeners: " + album.getListeners());
            StringBuilder builderParagraphBody = new StringBuilder();
            builderParagraphBody.append("Tags: ");
            for (Tag tag : album.getTags()) {
                builderParagraphBody.append(tag.getName()).append(", ");
            }
            createParagraph(docxModel).setText(builderParagraphBody.toString());


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
                dir.mkdir();
            }
            String pathDocFile = dir.getPath() + "\\" + album.getName() + album.getArtist().getName() + LocalDateTime.now().getYear() + LocalDateTime.now().getMonth() + ".docx";
            File fileDoc = new File(pathDocFile);
            if (!fileDoc.exists()) {
                fileDoc.createNewFile();
            } else {
                fileDoc.delete();
                fileDoc = new File(pathDocFile);
                fileDoc.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(fileDoc);
            docxModel.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
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


    @NotNull
    private LocalDateTime parseDate(String date) {
        // todo
        return LocalDateTime.now();
    }
}
