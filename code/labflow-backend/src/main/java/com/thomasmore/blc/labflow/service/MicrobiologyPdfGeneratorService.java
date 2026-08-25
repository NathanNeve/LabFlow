package com.thomasmore.blc.labflow.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyAntibiogramEntryDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyGramkleuringRowDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyNotebookResponse;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyStaalTestDto;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyVoedingsbodemLogEntry;
import com.thomasmore.blc.labflow.dto.microbiology.MicrobiologyVoedingsbodemNotebookDto;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Label PDFs for microbiology: standard label + one page per confirmed
 * {@link Voedingsbodem}.
 */
@Service
@Transactional("microbiologyTransactionManager")
public class MicrobiologyPdfGeneratorService {

    private static final String GEBOORTE_STRING = "Geboorte: ";
    private static final String GESLACHT_STRING = "Geslacht: ";
    private static final float SECTION_SUBTITLE_SPACING_AFTER = 12f;
    private static final float CULTUUR_COMMENT_SPACING_BEFORE = 10f;
    private static final float CULTUUR_COMMENT_SPACING_AFTER = 14f;
    private static final BaseColor WARNING_ROW_BG = new BaseColor(255, 243, 205);

    @Autowired
    @Qualifier("microbiologyStaalRepository")
    private StaalRepository staalRepository;

    @Autowired
    @Qualifier("microbiologyStaalTestVoedingsbodemRepository")
    private StaalTestVoedingsbodemRepository staalTestVoedingsbodemRepository;

    @Autowired
    private MicrobiologyNotebookService microbiologyNotebookService;

    public byte[] generateResultsPdf(Long staalId) throws DocumentException {
        Staal staal = staalRepository.findById(staalId)
                .orElseThrow(() -> new EntityNotFoundException("Staal not found with id: " + staalId));
        MicrobiologyNotebookResponse notebook = microbiologyNotebookService.getNotebook(staalId);
        List<String> activeSections = notebook.getActiveSections() != null
                ? notebook.getActiveSections()
                : List.of();

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("Microbiologie resultaten", titleFont));
        document.add(Chunk.NEWLINE);
        addPatientHeaderTable(document, staal, bodyFont, headerFont);
        document.add(new LineSeparator());
        document.add(Chunk.NEWLINE);

        if (notebook.getCommentaar() != null && !notebook.getCommentaar().isBlank()) {
            addSectionSubtitle(document, "Algemene commentaar", headerFont);
            document.add(new Paragraph(notebook.getCommentaar(), bodyFont));
            document.add(Chunk.NEWLINE);
        }

        if (activeSections.contains("algemene-testen")
                && notebook.getAlgemeneTesten() != null
                && !notebook.getAlgemeneTesten().isEmpty()) {
            addSectionSubtitle(document, "Algemene testen", headerFont);
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Test", headerFont);
            addHeaderCell(table, "Waarde", headerFont);
            addHeaderCell(table, "Commentaar", headerFont);
            for (MicrobiologyStaalTestDto test : notebook.getAlgemeneTesten()) {
                BaseColor rowBg = test.isFailed() ? WARNING_ROW_BG : null;
                addBodyCell(table, test.getTestNaam(), bodyFont, rowBg);
                addBodyCell(table, test.isFailed() ? "" : nullToEmpty(test.getWaarde()), bodyFont, rowBg);
                String commentaar = nullToEmpty(test.getCommentaar());
                addBodyCell(table, commentaar, bodyFont, rowBg);
            }
            document.add(table);
            document.add(Chunk.NEWLINE);
        }

        if (activeSections.contains("voedingsbodems")
                && notebook.getVoedingsbodems() != null) {
            for (MicrobiologyVoedingsbodemNotebookDto vb : notebook.getVoedingsbodems()) {
                addSectionSubtitle(document, "Cultuur: " + vb.getVoedingsbodemNaam(), headerFont);
                if (vb.getCommentaar() != null && !vb.getCommentaar().isBlank()) {
                    Paragraph comment = new Paragraph(vb.getCommentaar(), bodyFont);
                    comment.setSpacingBefore(CULTUUR_COMMENT_SPACING_BEFORE);
                    comment.setSpacingAfter(CULTUUR_COMMENT_SPACING_AFTER);
                    document.add(comment);
                }
                if (vb.getLogs() != null && !vb.getLogs().isEmpty()) {
                    PdfPTable logTable = new PdfPTable(4);
                    logTable.setWidthPercentage(100);
                    addHeaderCell(logTable, "Organisme", headerFont);
                    addHeaderCell(logTable, "Beoordeling", headerFont);
                    addHeaderCell(logTable, "STS", headerFont);
                    addHeaderCell(logTable, "Commentaar", headerFont);
                    for (MicrobiologyVoedingsbodemLogEntry log : vb.getLogs()) {
                        addBodyCell(logTable, nullToEmpty(log.getOrganisme()), bodyFont);
                        addBodyCell(logTable, nullToEmpty(log.getBeoordeling()), bodyFont);
                        addBodyCell(logTable, nullToEmpty(log.getSts()), bodyFont);
                        addBodyCell(logTable, nullToEmpty(log.getCommentaar()), bodyFont);
                    }
                    document.add(logTable);
                }
                document.add(Chunk.NEWLINE);
            }
        }

        if (activeSections.contains("gramkleuring")
                && notebook.getGramkleuring() != null
                && hasGramkleuringContent(notebook)) {
            addSectionSubtitle(document, "Gramkleuring", headerFont);
            if (notebook.getGramkleuring().getCommentaar() != null
                    && !notebook.getGramkleuring().getCommentaar().isBlank()) {
                Paragraph gramComment = new Paragraph(notebook.getGramkleuring().getCommentaar(), bodyFont);
                gramComment.setSpacingAfter(CULTUUR_COMMENT_SPACING_AFTER);
                document.add(gramComment);
            }
            if (notebook.getGramkleuring().getRows() != null
                    && !notebook.getGramkleuring().getRows().isEmpty()) {
                PdfPTable gramTable = new PdfPTable(3);
                gramTable.setWidthPercentage(100);
                addHeaderCell(gramTable, "Bepaling", headerFont);
                addHeaderCell(gramTable, "Score", headerFont);
                addHeaderCell(gramTable, "Commentaar", headerFont);
                for (MicrobiologyGramkleuringRowDto row : notebook.getGramkleuring().getRows()) {
                    addBodyCell(gramTable, nullToEmpty(row.getBepaling()), bodyFont);
                    addBodyCell(gramTable, nullToEmpty(row.getScore()), bodyFont);
                    addBodyCell(gramTable, nullToEmpty(row.getCommentaar()), bodyFont);
                }
                document.add(gramTable);
            }
            document.add(Chunk.NEWLINE);
        }

        if (activeSections.contains("antibiogram")
                && notebook.getAntibiogram() != null
                && !notebook.getAntibiogram().isEmpty()) {
            addSectionSubtitle(document, "Antibiogram", headerFont);
            PdfPTable abTable = new PdfPTable(2);
            abTable.setWidthPercentage(100);
            addHeaderCell(abTable, "Antibioticum", headerFont);
            addHeaderCell(abTable, "Beoordeling", headerFont);
            for (MicrobiologyAntibiogramEntryDto entry : notebook.getAntibiogram()) {
                addBodyCell(abTable, nullToEmpty(entry.getAntibioticaNaam()), bodyFont);
                addBodyCell(abTable, nullToEmpty(entry.getBeoordeling()), bodyFont);
            }
            document.add(abTable);
        }

        document.close();
        return out.toByteArray();
    }

    private void addPatientHeaderTable(Document document, Staal staal, Font bodyFont, Font headerFont)
            throws DocumentException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedBirthDate = staal.getPatientGeboorteDatum() != null
                ? staal.getPatientGeboorteDatum().toLocalDate().format(dateFormatter)
                : "";
        String formattedCurrentDate = LocalDate.now().format(dateFormatter);

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[] { 3, 1 });

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("PATIËNT", headerFont));
        leftCell.addElement(new Paragraph(
                nullToEmpty(staal.getPatientVoornaam()) + " " + nullToEmpty(staal.getPatientAchternaam()),
                bodyFont));
        leftCell.addElement(new Paragraph(GEBOORTE_STRING + formattedBirthDate, bodyFont));
        leftCell.addElement(new Paragraph(GESLACHT_STRING + formatGeslacht(staal.getPatientGeslacht()), bodyFont));
        headerTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.addElement(new Paragraph("Testcode: " + staal.getStaalCode(), bodyFont));
        rightCell.addElement(new Paragraph("Datum: " + formattedCurrentDate, bodyFont));
        rightCell.addElement(new Paragraph("Laborant: " + nullToEmpty(staal.getLaborantNaam()), bodyFont));
        rightCell.addElement(new Paragraph("R-nummer: " + nullToEmpty(staal.getLaborantRnummer()), bodyFont));
        headerTable.addCell(rightCell);

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    private static void addSectionSubtitle(Document document, String text, Font headerFont) throws DocumentException {
        Paragraph subtitle = new Paragraph(text, headerFont);
        subtitle.setSpacingAfter(SECTION_SUBTITLE_SPACING_AFTER);
        document.add(subtitle);
    }

    private static boolean hasGramkleuringContent(MicrobiologyNotebookResponse notebook) {
        if (notebook.getGramkleuring().getCommentaar() != null
                && !notebook.getGramkleuring().getCommentaar().isBlank()) {
            return true;
        }
        if (notebook.getGramkleuring().getRows() == null) {
            return false;
        }
        return notebook.getGramkleuring().getRows().stream()
                .anyMatch(row -> (row.getScore() != null && !row.getScore().isBlank())
                        || (row.getCommentaar() != null && !row.getCommentaar().isBlank()));
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private static void addBodyCell(PdfPTable table, String text, Font font) {
        addBodyCell(table, text, font, null);
    }

    private static void addBodyCell(PdfPTable table, String text, Font font, BaseColor background) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        if (background != null) {
            cell.setBackgroundColor(background);
        }
        table.addCell(cell);
    }

    public byte[] generateLabelPdf(Long staalId, List<Long> voedingsbodemFilterIds) throws DocumentException {
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

        boolean onlySpecificVbs = voedingsbodemFilterIds != null && !voedingsbodemFilterIds.isEmpty();
        if (onlySpecificVbs) {
            Set<Long> filterSet = new LinkedHashSet<>(voedingsbodemFilterIds);
            sortedVbs = sortedVbs.stream()
                    .filter(vb -> vb.getId() != null && filterSet.contains(vb.getId()))
                    .toList();
            if (sortedVbs.isEmpty()) {
                throw new DocumentException("Geen geldige voedingsbodems voor labelgeneratie");
            }
        }

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

        if (!onlySpecificVbs) {
            drawStandardLabelPage(document, canvas, nameParagraph, regularFont, formattedDate, geslacht,
                    staalCode, barcodeImage);
        }

        for (int i = 0; i < sortedVbs.size(); i++) {
            Voedingsbodem vb = sortedVbs.get(i);
            if (!onlySpecificVbs || i > 0) {
                document.newPage();
            }
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
                    new Phrase(String.valueOf(staalCode),
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)),
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
                new Phrase(String.valueOf(staalCode),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)),
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
