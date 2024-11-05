package org.example.accessiblepdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.pdfbox.pdmodel.font.PDType0Font;


class PageElementFactory {
  static final Map<Integer, PDStructureElement> parentTreeMap = new HashMap<>();
  static int mcid = 0;

  static void addSection(PDStructureElement parentElement, PDPageContentStream contentStream,
      PDType0Font font, String paragraphLeft, String paragraphRight, int x, int y, PDPage page) throws IOException {

    PDStructureElement sectionElement = new PDStructureElement(StandardStructureTypes.SECT, parentElement);
    parentElement.appendKid(sectionElement);
    sectionElement.setPage(page);

    addParagraph(sectionElement, contentStream, font, "Paragraph Left", paragraphLeft, x, y-30, page);
    addParagraph(sectionElement, contentStream, font, "Paragraph Right", paragraphRight, x+200, y-30, page);
  }

  static void addHeading(PDStructureElement parentElement, PDPageContentStream contentStream,
      PDType0Font font, String text, int x, int y, String headingLevel, PDPage page)
      throws IOException {
    var childElement = new PDStructureElement(headingLevel, parentElement);
    parentElement.appendKid(childElement);
    childElement.setPage(page);
    addMarkedContent(contentStream, childElement, font, text, x, y);
  }

  static void addParagraph(PDStructureElement parentElement, PDPageContentStream contentStream,
      PDType0Font font, String title, String content, int x, int y, PDPage page)
      throws IOException {
    var childElement = new PDStructureElement(StandardStructureTypes.P, parentElement);
    parentElement.appendKid(childElement);
    childElement.setPage(page);

    addMarkedContent(contentStream, childElement, font, title, x, y);
    addMarkedContent(contentStream, childElement, font, content, x, y - 15);
  }

  private static void addMarkedContent(PDPageContentStream contentStream,
      PDStructureElement childElement, PDType0Font font, String title, float x, float y)
      throws IOException {
    var propertyList = createMCIDPropertyList(mcid);

    contentStream.beginMarkedContent(COSName.MCID, propertyList);
    contentStream.beginText();
    contentStream.setFont(font, 12);
    contentStream.newLineAtOffset(x, y);
    contentStream.showText(title);
    contentStream.endText();
    contentStream.endMarkedContent();

    parentTreeMap.put(mcid, childElement);
    mcid++;
  }

  private static PDPropertyList createMCIDPropertyList(int mcid) {
    COSDictionary dict = new COSDictionary();
    dict.setInt(COSName.MCID, mcid);
    return PDPropertyList.create(dict);
  }

}
