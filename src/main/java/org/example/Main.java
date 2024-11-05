package org.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.example.accessiblepdf.AccessiblePdf;

public class Main {
  private static int mcid = 0;
  private static final Map<Integer, PDStructureElement> parentTreeMap = new HashMap<>();

  public static void main(String[] args) throws IOException {
    AccessiblePdf pdf = new AccessiblePdf();
    pdf.generate();
  }

//  private static void writeText(PDPageContentStream contentStream, PDType0Font font,
//      float fontSize, String text, float x, float y) throws IOException {
//    contentStream.beginText();
//    contentStream.setFont(font, fontSize);
//    contentStream.newLineAtOffset(x, y);
//    contentStream.showText(text);
//    contentStream.endText();
//  }
//
//  private static PDPropertyList createMCIDPropertyList(int mcid) {
//    COSDictionary dict = new COSDictionary();
//    dict.setInt(COSName.MCID, mcid);
//    return PDPropertyList.create(dict);
//  }
//
//  private static void createSection(PDDocument document, PDStructureElement parentElement,
//      PDPageContentStream contentStream, PDType0Font font, String sectionTitle,
//      String paragraphLeft, String paragraphRight, int x, int y, PDPage page) throws IOException {
//    createHeading(document, parentElement, contentStream, font, sectionTitle, x, y, StandardStructureTypes.H2, page);
//
//    PDStructureElement sectionElement = new PDStructureElement(StandardStructureTypes.SECT, parentElement);
//    parentElement.appendKid(sectionElement);
//    sectionElement.setPage(page);
//
//    createParagraph(document, sectionElement, contentStream, font, "Paragraph Left", paragraphLeft, x, y - 30, page);
//
//    createParagraph(document, sectionElement, contentStream, font, "Paragraph Right", paragraphRight, x + 200, y - 30, page);
//  }
//
//  private static void createHeading(PDDocument document, PDStructureElement parentElement,
//      PDPageContentStream contentStream, PDType0Font font, String headingText,
//      int x, int y, String headingLevel, PDPage page) throws IOException {
//    PDStructureElement headingElement = new PDStructureElement(headingLevel, parentElement);
//    parentElement.appendKid(headingElement);
//    headingElement.setPage(page);
//    parentElement.setAlternateDescription("alt");
//
//    PDPropertyList propertyList = createMCIDPropertyList(mcid);
//    contentStream.beginMarkedContent(COSName.MCID, propertyList);
//    parentTreeMap.put(mcid, headingElement);
//
//    writeText(contentStream, font, headingLevel.equals(StandardStructureTypes.H1) ? 24 : 18,
//        headingText, x, y);
//
//    contentStream.endMarkedContent();
//    mcid++;
//  }
//
//  private static void createParagraph(PDDocument document, PDStructureElement parentElement,
//      PDPageContentStream contentStream, PDType0Font font, String paragraphTitle,
//      String paragraphContent, int x, int y, PDPage page) throws IOException {
//    PDStructureElement paragraphElement = new PDStructureElement(StandardStructureTypes.P, parentElement);
//    parentElement.appendKid(paragraphElement);
//    paragraphElement.setPage(page);
//
//    PDPropertyList propertyList = createMCIDPropertyList(mcid);
//    contentStream.beginMarkedContent(COSName.MCID, propertyList);
//    parentTreeMap.put(mcid, paragraphElement);
//
//    writeText(contentStream, font, 12, paragraphTitle, x, y);
//
//    contentStream.endMarkedContent();
//    mcid++;
//
//    propertyList = createMCIDPropertyList(mcid);
//    contentStream.beginMarkedContent(COSName.MCID, propertyList);
//    parentTreeMap.put(mcid, paragraphElement);
//
//    writeText(contentStream, font, 10, paragraphContent, x, y - 15);
//
//    contentStream.endMarkedContent();
//    mcid++;
//  }
}