package com.thomasmore.blc.labflow.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.thomasmore.blc.labflow.entity.microbiology.Staal;
import com.thomasmore.blc.labflow.entity.microbiology.StaalTestVoedingsbodem;
import com.thomasmore.blc.labflow.entity.microbiology.Voedingsbodem;
import com.thomasmore.blc.labflow.repository.microbiology.StaalRepository;
import com.thomasmore.blc.labflow.repository.microbiology.StaalTestVoedingsbodemRepository;

import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Label PDFs for microbiology: standard label + one page per confirmed {@link Voedingsbodem}.
 */
@Service
@Transactional("microbiologyTransactionManager")
public class MicrobiologyPdfGeneratorService {

    private static final String GEBOORTE_STRING = "Geboorte: ";
    private static final String GESLACHT_STRING = "Geslacht: ";

    @Autowired
    @Qualifier("microbiologyStaalRepository")
    private StaalRepository staalRepository;

    @Autowired
    @Qualifier("microbiologyStaalTestVoedingsbodemRepository")
    private StaalTestVoedingsbodemRepository staalTestVoedingsbodemRepository;

    public byte[] generateLabelPdf(Long staalId) throws DocumentException {
        Staal staal = staalRepository.findById(staalId)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + staalId));
        Hibernate.initialize(staal.getStaalType());

        if (staal.getPatientGeboorteDatum() == null) {
            throw new DocumentException("Patient geboortedatum ontbreekt");
        }

        Long staalCode = staal.getStaalCode();
        String voornaam = nullToEmpty(staal.getPatientVoornaam());
        String achternaam = nullToEmpty(staal.getPatientAchternaam());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = staal.getPatientGeboorteDatum().toLocalDate().format(formatter);
        char geslacht = staal.getPatientGeslacht();

        List<StaalTestVoedingsbodem> links = staalTestVoedingsbodemRepository.findByStaalId(staalId);
        Set<Voedingsbodem> voedingsbodems = new LinkedHashSet<>();
        for (StaalTestVoedingsbodem link : links) {
            Hibernate.initialize(link.getVoedingsbodem());
            voedingsbodems.add(link.getVoedingsbodem());
        }
        List<Voedingsbodem> sortedVbs = voedingsbodems.stream()
                .sorted(Comparator.comparing(Voedingsbodem::getNaam, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        Document document = new Document(new Rectangle(210, 140));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfContentByte canvas = writer.getDirectContent();

        Paragraph nameParagraph = new Paragraph(voornaam + " " + achternaam, boldFont);
        nameParagraph.setAlignment(Element.ALIGN_LEFT);

        Barcode128 barcode = new Barcode128();
        barcode.setCode(String.valueOf(staalCode));
        barcode.setFont(null);
        Image barcodeImage = barcode.createImageWithBarcode(canvas, BaseColor.BLACK, BaseColor.BLACK);
        barcodeImage.setAbsolutePosition(70, 30);
        barcodeImage.scalePercent(100);

        drawStandardLabelPage(document, canvas, nameParagraph, regularFont, formattedDate, geslacht,
                staalCode, barcodeImage);

        for (Voedingsbodem vb : sortedVbs) {
            document.newPage();
            Rectangle newBorder = new Rectangle(10, 10, 200, 130);
            newBorder.setBorder(Rectangle.BOX);
            newBorder.setBorderWidth(1);
            canvas.rectangle(newBorder);
            canvas.stroke();

            barcodeImage.setAbsolutePosition(70, 30);
            barcodeImage.scalePercent(100);
            document.add(barcodeImage);

            nameParagraph.setAlignment(Element.ALIGN_LEFT);
            document.add(nameParagraph);
            document.add(new Paragraph(GEBOORTE_STRING + formattedDate, regularFont));
            document.add(new Paragraph(GESLACHT_STRING + formatGeslacht(geslacht), regularFont));
            document.add(Chunk.NEWLINE);

            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                    new Phrase(String.valueOf(staalCode), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)),
                    105, 15, 0);

            String vbNaam = vb.getNaam() != null ? vb.getNaam() : "";
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase(vbNaam, boldFont), 185, 60, 270);
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase("", boldFont), 170, 60, 270);
        }

        document.close();
        return out.toByteArray();
    }

    private void drawStandardLabelPage(Document document, PdfContentByte canvas, Paragraph nameParagraph,
            Font regularFont, String formattedDate, char geslacht, Long staalCode,
            Image barcodeImage) throws DocumentException {
        Rectangle border = new Rectangle(10, 10, 200, 130);
        border.setBorder(Rectangle.BOX);
        border.setBorderWidth(1);
        canvas.rectangle(border);
        canvas.stroke();

        nameParagraph.setAlignment(Element.ALIGN_LEFT);
        document.add(nameParagraph);
        document.add(new Paragraph(GEBOORTE_STRING + formattedDate, regularFont));
        document.add(new Paragraph(GESLACHT_STRING + formatGeslacht(geslacht), regularFont));
        document.add(Chunk.NEWLINE);

        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase(String.valueOf(staalCode), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)),
                105, 15, 0);

        barcodeImage.setAbsolutePosition(70, 30);
        barcodeImage.scalePercent(100);
        document.add(barcodeImage);
    }

    private static String formatGeslacht(char g) {
        if (g == 'M') {
            return "Man";
        }
        if (g == 'V') {
            return "Vrouw";
        }
        return "X";
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
