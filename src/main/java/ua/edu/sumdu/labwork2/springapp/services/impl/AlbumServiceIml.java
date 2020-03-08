package ua.edu.sumdu.labwork2.springapp.services.impl;

import org.apache.poi.xwpf.model.XWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.edu.sumdu.labwork2.springapp.model.Album;
import ua.edu.sumdu.labwork2.springapp.model.Tag;
import ua.edu.sumdu.labwork2.springapp.model.Track;
import ua.edu.sumdu.labwork2.springapp.services.AlbumService;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;

@Service
public class AlbumServiceIml implements AlbumService {

    private String dirPath;

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public AlbumServiceIml(@Value("${lastfm.dir}") String dirPath) {
        this.dirPath = dirPath;
    }

    @Override
    public void saveToFile(Album album) {

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
                tracksTable.getRow(RowCounter).getCell(2).setText(String.valueOf(track.getUrl())); // todo
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

    private static CTP createFooterModel(String footerContent) {
        // создаем футер или нижний колонтитул
        CTP ctpFooterModel = CTP.Factory.newInstance();
        CTR ctrFooterModel = ctpFooterModel.addNewR();
        CTText cttFooter = ctrFooterModel.addNewT();

        cttFooter.setStringValue(footerContent);
        return ctpFooterModel;
    }

    private static CTP createHeaderModel(String headerContent) {
        // создаем хедер или верхний колонтитул
        CTP ctpHeaderModel = CTP.Factory.newInstance();
        CTR ctrHeaderModel = ctpHeaderModel.addNewR();
        CTText cttHeader = ctrHeaderModel.addNewT();

        cttHeader.setStringValue(headerContent);
        return ctpHeaderModel;
    }

    private static XWPFRun createParagraph (XWPFDocument docxModel) {
        XWPFParagraph bodyParagraph = docxModel.createParagraph();
        bodyParagraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun paragraphConfig = bodyParagraph.createRun();
        paragraphConfig.setItalic(true);
        paragraphConfig.setFontSize(16);
        // HEX цвет без решетки #
        paragraphConfig.setColor("4036d6");
        return paragraphConfig;
    }
}
