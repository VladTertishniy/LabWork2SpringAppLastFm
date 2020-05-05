package ua.edu.sumdu.labwork2.springapp.services.impl;

import lombok.NoArgsConstructor;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.jetbrains.annotations.NotNull;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.labwork2.springapp.model.*;
import ua.edu.sumdu.labwork2.springapp.services.AlbumToDocFileService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Iterator;

@NoArgsConstructor
@Service
public class AlbumToDocFileServiceImpl implements AlbumToDocFileService {

    final static Logger logger = Logger.getLogger(AlbumToDocFileServiceImpl.class);

    @Override
    public byte[] saveToFile(Album album) {

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

        //получаем URL картинки с максимальным размером
        Iterator<Image> imageIterator = album.getImages().iterator();
        URL maxImageSizeUrl = null;
        while (imageIterator.hasNext()) {
            maxImageSizeUrl = imageIterator.next().getUrl();
        }
        if (maxImageSizeUrl != null) {
            InputStream imageInputStream = getImageInputStream(maxImageSizeUrl);
            int format = XWPFDocument.PICTURE_TYPE_PNG;
            try {
                //добавляем картинку в параграф DOC документа
                createParagraph(docxModel).addPicture(imageInputStream, format, "image", Units.toEMU(300), Units.toEMU(300));
                if (imageInputStream != null) {
                    imageInputStream.close();
                }
            } catch (InvalidFormatException | IOException e) {
                System.out.println("Unexpected error!");
                logger.info("Adding picture to paragraph is failed!", e);
            }
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
        if (album.getTracks().size() > 0) {
            XWPFTable tracksTable = docxModel.createTable(album.getTracks().size() + 1,3);
            tracksTable.getRow(0).getCell(0).setText("name");
            tracksTable.getRow(0).getCell(1).setText("duration");
            tracksTable.getRow(0).getCell(2).setText("url");
            int rowCounter = 1;
            for (Track track : album.getTracks()) {
                tracksTable.getRow(rowCounter).getCell(0).setText(track.getName());
                tracksTable.getRow(rowCounter).getCell(1).setText(String.valueOf(track.getDuration()));
                tracksTable.getRow(rowCounter).getCell(2).setText(track.getUrl().toString());
                rowCounter++;
            }
        }

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try {
            docxModel.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray.toByteArray();
    }

    @Override
    public InputStream getImageInputStream(URL connection) {
        InputStream in;
        try {
            HttpURLConnection urlConnection;
            urlConnection = (HttpURLConnection) connection.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            in = urlConnection.getInputStream();
            return in;
        } catch (IOException e) {
            logger.info("Connection or saving image on disk failed!", e);
            System.out.println("Unexpected error!");
            return null;
        }
    }

    @Override
    public String getDocFileName (String artistName, String albumName) {
        String nameFileDoc = albumName +
                artistName +
                LocalDateTime.now().getYear() +
                LocalDateTime.now().getMonth() +
                LocalDateTime.now().getDayOfMonth();
        return nameFileDoc.replaceAll("[?*<>.\\\\/|:]", "_");
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
