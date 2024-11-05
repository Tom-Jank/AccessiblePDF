package org.example.accessiblepdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDNumberTreeNode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

public class AccessiblePdf {
  private static final String TITLE = "Accessible PDF Document";
  private static final String CREATOR = "Tom Jank";
  private static final String LANGUAGE = "en-US";

  public void generate() {
    try (var document = new PDDocument()) {
      var catalog = document.getDocumentCatalog();

      setLanguageAndInformation(document, catalog);
      setXmpMetadata(document, catalog);
      markAsTagged(catalog);

      var font = loadFont(document);
      var structureTreeRoot = createStructureTreeRoot(catalog);

      setViewerPreferences(catalog);
      createDocumentOutline(catalog);

      var page = addPage(document);
      var rootElement = createRootElement(structureTreeRoot, catalog);

      try (var contentStream = new PDPageContentStream(document, page)) {
        PageElementFactory.addHeading(rootElement, contentStream, font, "Title", 100, 700, StandardStructureTypes.H1, page);
        PageElementFactory.addSection(rootElement, contentStream, font, "Left", "Right", 100, 600, page);
        PageElementFactory.addSection(rootElement, contentStream, font, "Left", "Right", 100, 400, page);
      }

      PDNumberTreeNode parentTree = new PDNumberTreeNode(PDStructureElement.class);
      parentTree.setNumbers(PageElementFactory.parentTreeMap);
      structureTreeRoot.setParentTree(parentTree);
      structureTreeRoot.setParentTreeNextKey(PageElementFactory.mcid);

      document.save("Accessible PDF.pdf");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  PDPage addPage(PDDocument document) {
    var page = new PDPage(PDRectangle.A4);
    document.addPage(page);
    return page;
  }

  private void setLanguageAndInformation(PDDocument document, PDDocumentCatalog catalog) {
    catalog.setLanguage(LANGUAGE);

    var information = new PDDocumentInformation();
    information.setTitle(TITLE);
    information.setCreator(CREATOR);
    information.setCreationDate(Calendar.getInstance());

    document.setDocumentInformation(information);
  }

  private void setXmpMetadata(PDDocument document, PDDocumentCatalog catalog) {
    var xmp = XMPMetadata.createXMPMetadata();
    try {
      setDocumentSchemas(xmp);
      setDocumentIdentification(xmp);

      var metadata = new PDMetadata(document);
      metadata.importXMPMetadata(serializeXmpMetadata(xmp));

      catalog.setMetadata(metadata);
    } catch (BadFieldValueException | TransformerException | IOException e) {
      throw new RuntimeException("Error setting metadata: " + e.getMessage());
    }
  }

  private void setDocumentSchemas(XMPMetadata xmp) {
    xmp.createAndAddDublinCoreSchema();
    var dublinCoreSchema = xmp.getDublinCoreSchema();
    dublinCoreSchema.setTitle(TITLE);
    dublinCoreSchema.addLanguage(LANGUAGE);
    dublinCoreSchema.setIdentifier("PDF/UA");

    var basicSchema = xmp.createAndAddXMPBasicSchema();
    basicSchema.setCreatorTool(CREATOR);

    var pdfUASchema = xmp.createAndAddDefaultSchema("pdfuaid", "http://www.aiim.org/pdfua/ns/id/");
    pdfUASchema.setTextPropertyValueAsSimple("part", "1");
  }

  private byte[] serializeXmpMetadata(XMPMetadata xmp) throws TransformerException {
    var serializer = new XmpSerializer();
    var out = new ByteArrayOutputStream();
    serializer.serialize(xmp, out, true);

    return out.toByteArray();
  }

  private void setDocumentIdentification(XMPMetadata xmp) throws BadFieldValueException {
    var identification = xmp.createAndAddPDFAIdentificationSchema();
    identification.setPart(1);
    identification.setConformance("U");
  }

  private void markAsTagged(PDDocumentCatalog catalog) {
    catalog.setMarkInfo(new PDMarkInfo());
    catalog.getMarkInfo().setMarked(true);
  }

  private PDType0Font loadFont(PDDocument document) {
    try {
      return PDType0Font.load(document,
          new File(
              "C:\\Users\\Tomek\\Desktop\\AccessiblePDF\\src\\main\\resources\\fonts\\helvetica.ttf"));
    } catch (IOException e) {
      throw new RuntimeException("Error loading font: " + e.getMessage());
    }
  }

  private PDStructureTreeRoot createStructureTreeRoot(PDDocumentCatalog catalog) {
    var structureTreeRoot = new PDStructureTreeRoot();
    catalog.setStructureTreeRoot(structureTreeRoot);
    return structureTreeRoot;
  }

  private void setViewerPreferences(PDDocumentCatalog catalog) {
    catalog.setViewerPreferences(new PDViewerPreferences(new COSDictionary()));
    catalog.getViewerPreferences().setDisplayDocTitle(true);
  }

  private void createDocumentOutline(PDDocumentCatalog catalog) {
    catalog.setDocumentOutline(new PDDocumentOutline());
  }

  private PDStructureElement createRootElement(
      PDStructureTreeRoot structureTreeRoot, PDDocumentCatalog catalog) {
    var root = new PDStructureElement(StandardStructureTypes.DOCUMENT, structureTreeRoot);
    catalog.setStructureTreeRoot(structureTreeRoot);
    return root;
  }
}
