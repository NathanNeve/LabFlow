package com.thomasmore.blc.labflow.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.thomasmore.blc.labflow.entity.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfGeneratorService {
    // format strings
    String geboorteString = "Geboorte: ";
    String geslachtString = "Geslacht: ";
    String geenNotitieString = "Geen notitie";

    public byte[] generateLabelPdf(Staal staal) throws DocumentException {
        Document document = new Document(new Rectangle(210, 140));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        // alle nodige info uit staal
        Long staalCode = staal.getStaalCode();
        String voornaam = staal.getPatientVoornaam();
        String achternaam = staal.getPatientAchternaam();
        LocalDate geboortedatum = staal.getPatientGeboorteDatum();
        char geslacht = staal.getPatientGeslacht();

        // Filter unieke categorieën
        Set<Testcategorie> testcategorieSet = staal.getRegisteredTests().stream()
                .map(StaalTest::getTest)
                .map(Test::getTestcategorie)
                .filter(testcategorie -> testcategorie.getId() != 7)
                .collect(Collectors.toSet());

        // Format verjaardag
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = geboortedatum.format(formatter);

        // initialiseer fonts
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        // variable canvas voor tekenen
        PdfContentByte canvas = writer.getDirectContent();

        // zwarte box rond het label
        Rectangle border = new Rectangle(10, 10, 200, 130); // Define rectangle dimensions
        border.setBorder(Rectangle.BOX);
        border.setBorderWidth(1);
        canvas.rectangle(border);
        canvas.stroke();


        // Eerste standaard label
        // naam en voornaam toevoegen
        Paragraph nameParagraph = new Paragraph(voornaam + " " + achternaam, boldFont);
        nameParagraph.setAlignment(Element.ALIGN_LEFT);
        document.add(nameParagraph);

        // geboortedatum & geslacht
        document.add(new Paragraph(geboorteString + formattedDate, regularFont));
        document.add(new Paragraph(geslachtString + (geslacht == 'M' ? "Man" : "Vrouw"), regularFont));

        // spacing
        document.add(Chunk.NEWLINE);

        // staalcode
        ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                new Phrase(String.valueOf(staalCode), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)),
                105, 15, 0); // Center the text inside the black rectangle

        // Voeg de barcode toe
        Barcode128 barcode = new Barcode128();
        barcode.setCode(String.valueOf(staalCode)); // barcode representeert de staalCode
        barcode.setFont(null); // geen tekst onder de barcode
        Image barcodeImage = barcode.createImageWithBarcode(canvas, BaseColor.BLACK, BaseColor.BLACK);
        barcodeImage.setAbsolutePosition(70, 30); // absolute positie van de barcode
        barcodeImage.scalePercent(100); // grootte van de barcode
        document.add(barcodeImage);

        // voor elke staalcategorie een label aanmaken
        for (Testcategorie testcategorie : testcategorieSet) {
            // niewe pagina maken
            document.newPage(); // Add a new page for every label

            // zwarte box
            Rectangle newBorder = new Rectangle(10, 10, 200, 130); // Create a new rectangle for each page
            newBorder.setBorder(Rectangle.BOX);
            newBorder.setBorderWidth(1);
            canvas.rectangle(newBorder);
            canvas.stroke();

            // Voeg de barcode toe (defined bij het standaardlabel)
            barcodeImage.setAbsolutePosition(70, 30); // absolute positie van de barcode
            barcodeImage.scalePercent(100); // grootte van de barcode
            document.add(barcodeImage);

            // patiënt naam
            nameParagraph.setAlignment(Element.ALIGN_LEFT);
            document.add(nameParagraph);

            // geboortedatum & geslacht
            document.add(new Paragraph(geboorteString + formattedDate, regularFont));
            document.add(new Paragraph(geslachtString + (geslacht == 'M' ? "Man" : "Vrouw"), regularFont));

            // spacing
            document.add(Chunk.NEWLINE);

            // staalcode
            ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                    new Phrase(String.valueOf(staalCode), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK)),
                    105, 15, 0); // Center the text inside the black rectangle

            // test categorie naam
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase(testcategorie.getNaam(), boldFont), 185, 60, 270); // Rotated 90 degrees

            // test categorie kleurnaam
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase(testcategorie.getKleurnaam(), boldFont), 170, 60, 270);
        }

        // document sluiten en transformeren naar byte array
        document.close();
        return out.toByteArray();
    }




    public byte[] generateResultsPdf(Staal staal) throws DocumentException {
        // start een nieuw document
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfWriter.getInstance(document, out);
        document.open();

        // declareren van verschillende fonts
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 10);
        Font noteFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
        Font headerDataFont = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);

        // alle patient en staal info ophalen
        List<StaalTest> registeredTests = staal.getRegisteredTests();
        Long staalCode = staal.getStaalCode();
        String voornaam = staal.getPatientVoornaam();
        String achternaam = staal.getPatientAchternaam();
        LocalDate geboortedatum = staal.getPatientGeboorteDatum();
        char geslacht = staal.getPatientGeslacht();
        String laborant = staal.getLaborantNaam();
        String rNummer = staal.getLaborantRnummer();

        // geboortedatum formatteren
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = geboortedatum.format(formatter);
        // huidige datum formatteren
        String formattedCurrentDate = LocalDate.now().format(formatter);


        // hoofding tabel voor patient info
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new int[]{3, 1});  // Adjust the column widths as needed

        // Linkse kant (Patient details)
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);

        leftCell.addElement(new Paragraph("PATIËNT", headerFont));
        leftCell.addElement(new Paragraph(voornaam + " " + achternaam, bodyFont));
        leftCell.addElement(new Paragraph(geboorteString + formattedDate, bodyFont));
        leftCell.addElement(new Paragraph(geslachtString + geslacht, bodyFont));
        headerTable.addCell(leftCell);

        // Rechtse kant (Test code, datum, laborant, r-nummer)
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.addElement(new Paragraph("Testcode: " + staalCode, bodyFont));
        rightCell.addElement(new Paragraph("Datum: " + formattedCurrentDate, bodyFont));
        rightCell.addElement(new Paragraph("Laborant: " + laborant, bodyFont));
        rightCell.addElement(new Paragraph("R-nummer: " + rNummer, bodyFont));// Current date
        headerTable.addCell(rightCell);

        // spacing toevoegen
        document.add(headerTable);
        document.add(new Paragraph("\n"));

        // divider toevoegen
        document.add(new LineSeparator());

        // nieuwe tabel met layout voor hoofding collom
        PdfPTable headerDataTable = new PdfPTable(6);
        headerDataTable.setWidthPercentage(100);
        headerDataTable.setWidths(new int[]{1, 3, 1, 1, 3, 2}); // Adjust column widths

        // standaard hoofding collom
        headerDataTable.addCell(new PdfPCell(new Phrase("Code", headerDataFont)));
        headerDataTable.addCell(new PdfPCell(new Phrase("Naam", headerDataFont)));
        headerDataTable.addCell(new PdfPCell(new Phrase("Resultaat", headerDataFont)));
        headerDataTable.addCell(new PdfPCell(new Phrase("Eenheid", headerDataFont)));
        headerDataTable.addCell(new PdfPCell(new Phrase("referentie", headerDataFont)));
        headerDataTable.addCell(new PdfPCell(new Phrase("Categorie", headerDataFont)));

        document.add(headerDataTable);

        // spacing toevoegen
        document.add(new Paragraph("\n"));// Adding space after the header

        // alle testen ophalen en sorteren op categorie
        List<Test> testList = registeredTests.stream()
                .map(StaalTest::getTest)
                .sorted(Comparator.comparing(test -> test.getTestcategorie().getNaam()))
                .filter(test -> test.getTestcategorie().getId() != 7) // notitie test uit gefilterd
                .toList();

        // enkel notitie test ophalen
        Test notitie = registeredTests.stream()
                .map(StaalTest::getTest)
                .filter(test -> test.getTestcategorie().getId() == 7)
                .findFirst()
                .orElse(null);

        // tabel aanmaken en layout voor data tabel
        PdfPTable dataTable = new PdfPTable(6);
        dataTable.setWidthPercentage(100);
        dataTable.setWidths(new int[]{1, 3, 1, 1, 3, 2}); // Adjust column widths

        // eerst een notitie toevoegen (enkel wanneer deze bestaat)
        if (notitie != null) {
            // cell met testcode van de test
            PdfPCell testCodeHeader = new PdfPCell(new Phrase(notitie.getTestCode(), bodyFont));
            testCodeHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(testCodeHeader);

            // cell met naam van de test
            PdfPCell nameHeader = new PdfPCell(new Phrase(notitie.getNaam(), bodyFont));
            nameHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(nameHeader);

            // ophalen van het resultaat van de test
            String result = notitie.getStalen().stream()
                    .filter(staalTest -> staalTest.getStaal().getStaalCode().equals(staalCode))
                    .map(StaalTest::getResult)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(geenNotitieString);


            // afhankelijk van het resultaat een cell toevoegen
            if (result.equals(geenNotitieString)) {
                PdfPCell resultHeader = new PdfPCell(new Phrase(geenNotitieString, bodyFont));
                resultHeader.setBorder(Rectangle.NO_BORDER);
                dataTable.addCell(resultHeader);
            } else {
                PdfPCell resultHeader = new PdfPCell(new Phrase(result, bodyFont));
                resultHeader.setBorder(Rectangle.NO_BORDER);
                dataTable.addCell(resultHeader);
            }

            // 3 volgende blokken zijn lege cellen aangezien deze niet in aanmerking komen voor een notitie
            PdfPCell unitHeader = new PdfPCell(new Phrase(""));
            unitHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(unitHeader);

            PdfPCell referenceHeader = new PdfPCell(new Phrase("", bodyFont));
            referenceHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(referenceHeader);

            PdfPCell categoryHeader = new PdfPCell(new Phrase("", bodyFont));
            categoryHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(categoryHeader);

            // ophalen van de nota van de notitie test
            String note = notitie.getStalen().stream()
                    .filter(staalTest -> staalTest.getStaal().getStaalCode().equals(staalCode))
                    .map(StaalTest::getNote)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            // als de test een notitie heeft, deze toevoegen aan de tabel
            if (note != null) {
                PdfPCell noteCell = new PdfPCell(new Phrase("Nota: " + note, noteFont));
                noteCell.setPaddingLeft(20);
                noteCell.setPaddingBottom(5);
                noteCell.setBackgroundColor(new BaseColor(239, 239, 239));
                noteCell.setBorder(Rectangle.NO_BORDER);
                noteCell.setColspan(6);
                dataTable.addCell(noteCell);
            }
        }

        // voor elke test in de lijst, een rij toevoegen aan de tabel
        for (Test test : testList) {
            // cell toevoegen met testcode
            PdfPCell testCodeHeader = new PdfPCell(new Phrase(test.getTestCode(), bodyFont));
            testCodeHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(testCodeHeader);

            // cell toevoegen met naam van de test
            PdfPCell nameHeader = new PdfPCell(new Phrase(test.getNaam(), bodyFont));
            nameHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(nameHeader);

            // ophalen van het resultaat van de test
            String result = test.getStalen().stream()
                    .filter(staalTest -> staalTest.getStaal().getStaalCode().equals(staalCode))
                    .map(StaalTest::getResult)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("No result");

            // ophalen van de nota van de test
            String note = test.getStalen().stream()
                    .filter(staalTest -> staalTest.getStaal().getStaalCode().equals(staalCode))
                    .map(StaalTest::getNote)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse("No note");

            // afhankelijk van het resultaat een cell toevoegen
            if (result.equals("No result")) {
                PdfPCell resultHeader = new PdfPCell(new Phrase("no result", bodyFont));
                resultHeader.setBorder(Rectangle.NO_BORDER);
                dataTable.addCell(resultHeader);
            } else {
                PdfPCell resultHeader = new PdfPCell(new Phrase(result, bodyFont));
                resultHeader.setBorder(Rectangle.NO_BORDER);
                dataTable.addCell(resultHeader);
            }

            // cell toevoegen met eenheid van de test
            PdfPCell unitHeader = new PdfPCell(new Phrase(test.getEenheid().getAfkorting(), bodyFont));
            unitHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(unitHeader);

            // alle referentiewaardes ophalen die bij test horen
            Set<Referentiewaarde> referentiewaardes = test.getReferentiewaardes();

            // afhankelijk van het aantal referentiewaardes, deze samenvoegen en toevoegen aan de tabel
            assert referentiewaardes != null;
            if (!referentiewaardes.isEmpty()) {
                // stream voor tussen elke referentiewaarde een '/' te plaatsen
                String referentieWaarden = referentiewaardes.stream()
                        .map(Referentiewaarde::getWaarde)
                        .collect(Collectors.joining(" / "));
                PdfPCell referenceHeader = new PdfPCell(new Phrase(referentieWaarden, bodyFont));
                referenceHeader.setBorder(Rectangle.NO_BORDER);
                dataTable.addCell(referenceHeader);
            } else {
                PdfPCell referenceHeader = new PdfPCell(new Phrase("No reference value", bodyFont));
                referenceHeader.setBorder(Rectangle.NO_BORDER);
                dataTable.addCell(referenceHeader);
            }

            // cell toevoegen met de naam van de categorie van de test
            PdfPCell categoryHeader = new PdfPCell(new Phrase(test.getTestcategorie().getNaam(), bodyFont));
            categoryHeader.setBorder(Rectangle.NO_BORDER);
            dataTable.addCell(categoryHeader);

            // cell toevoegen wanneer er een nota is
            if (!note.equals("No note")) {
                PdfPCell noteCell = new PdfPCell(new Phrase("Nota: " + note, noteFont));
                noteCell.setPaddingLeft(20);
                noteCell.setPaddingBottom(5);
                noteCell.setBackgroundColor(new BaseColor(239, 239, 239));
                noteCell.setBorder(Rectangle.NO_BORDER);
                noteCell.setColspan(6);
                dataTable.addCell(noteCell);
            }
        }

        document.add(dataTable);

        document.close();
        return out.toByteArray();
    }
}
