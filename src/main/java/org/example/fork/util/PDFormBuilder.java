package org.example.fork.util;

import org.apache.pdfbox.cos.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.*;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureTreeRoot;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDMarkedContent;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.schema.XmpSchemaException;
import org.apache.xmpbox.xml.XmpSerializer;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.*;
import org.example.fork.pojo.Cell;


public class PDFormBuilder {
  public PDDocument pdf = null;
  private PDAcroForm acroForm = null;
  private ArrayList<PDPage> pages = new ArrayList<>();
  private ArrayList<COSDictionary> annotDicts = new ArrayList<>();
  private PDFont defaultFont = null;
  private PDStructureElement rootElem = null;
  private PDStructureElement currentElem = null;
  private COSDictionary currentMarkedContentDictionary;
  private COSArray nums = new COSArray();
  private COSArray numDictionaries = new COSArray();
  private int currentMCID = 0;
  private int currentStructParent = 1;
  private final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
  public final float PAGE_WIDTH = PDRectangle.A4.getWidth();

  public PDFormBuilder(int initPages, String title) throws IOException, TransformerException, XmpSchemaException {
    //Setup new document
    pdf = new PDDocument();
    acroForm = new PDAcroForm(pdf);
    pdf.getDocumentInformation().setTitle(title);
    PDResources resources = setupAcroForm();
    addXMPMetadata(title);
    setupDocumentCatalog();
    initiatePages(initPages, resources);

  }

  public PDStructureElement drawElement(Cell textCell, float x, float y, float height,
      PDStructureElement parent, String structType, int pageIndex) throws IOException {

    setNextMarkedContentDictionary();
    currentElem = addContentToParent(
        COSName.ARTIFACT, structType, pages.get(pageIndex), parent, textCell.getText());

    try (var contents
        = new PDPageContentStream(
                pdf, pages.get(pageIndex), PDPageContentStream.AppendMode.APPEND, false)) {

      contents.beginMarkedContent(COSName.P, PDPropertyList.create(currentMarkedContentDictionary));
      drawCellText(textCell, x + 5, y + height + textCell.getFontSize(), contents);
      contents.endMarkedContent();
    }

    return currentElem;
  }

  private PDStructureElement addContentToParent(
      COSName name, String type, PDPage currentPage, PDStructureElement parent, String text) {
    // Create a structure element with text content if a type is provided.
    PDStructureElement structureElement = null;
    if (type != null) {
      structureElement = new PDStructureElement(type, parent);
      structureElement.setPage(currentPage);
      structureElement.setActualText(text);  // Set the text for accessibility
    }

    if (name != null) {
      COSDictionary numDict = new COSDictionary();
      numDict.setInt(COSName.K, currentMCID - 1);
      numDict.setString(COSName.LANG, "EN-US");
      numDict.setItem(COSName.PG, currentPage.getCOSObject());

      if (structureElement != null) {
        structureElement.appendKid(new PDMarkedContent(name, currentMarkedContentDictionary));
        numDict.setItem(COSName.P, structureElement.getCOSObject());
      } else {
        parent.appendKid(new PDMarkedContent(name, currentMarkedContentDictionary));
        numDict.setItem(COSName.P, parent.getCOSObject());
      }
      numDict.setName(COSName.S, name.getName());
      numDictionaries.add(numDict);
    }

    if (structureElement != null) {
      parent.appendKid(structureElement);
    }

    return structureElement;
  }

  //Adds a document structure element as the structure tree root.
  public PDStructureElement addRoot(int pageIndex) {
    rootElem = new PDStructureElement(StandardStructureTypes.DOCUMENT, null);
    rootElem.setTitle("PDF Document");
    rootElem.setPage(pages.get(pageIndex));
    rootElem.setLanguage("EN-US");
    return rootElem;
  }

  //Save the pdf to disk and close the stream
  public void saveAndClose(String filePath) throws IOException {
    addParentTree();
    pdf.save(filePath);
    pdf.close();
  }

  //Add text at a given location starting from the top-left corner.
  private void drawCellText(Cell cell, float x, float y, PDPageContentStream contents) throws IOException {
    //Open up a stream to draw text at a given location.
    contents.beginText();
    contents.setFont(defaultFont, cell.getFontSize());
    contents.newLineAtOffset(x, PAGE_HEIGHT - y);
    contents.setNonStrokingColor(cell.getTextColor());
    String[] lines = cell.getText().split("\n");
    for (String s: lines) {
      contents.showText(s);
      contents.newLineAtOffset(0, -(cell.getFontSize() * 2));
    }
    contents.endText();
  }

  //Assign an id for the next marked content element.
  private void setNextMarkedContentDictionary() {
    currentMarkedContentDictionary = new COSDictionary();
    currentMarkedContentDictionary.setInt(COSName.MCID, currentMCID);
    currentMCID++;
  }

  private void addXMPMetadata(String title) throws TransformerException, IOException {
    //Add UA XMP metadata based on specs at https://taggedpdf.com/508-pdf-help-center/pdfua-identifier-missing/
    XMPMetadata xmp = XMPMetadata.createXMPMetadata();
    xmp.createAndAddDublinCoreSchema();
    xmp.getDublinCoreSchema().setTitle(title);
    xmp.getDublinCoreSchema().setDescription(title);
    xmp.createAndAddPDFAExtensionSchemaWithDefaultNS();
    xmp.getPDFExtensionSchema().addNamespace("http://www.aiim.org/pdfa/ns/schema#", "pdfaSchema");
    xmp.getPDFExtensionSchema().addNamespace("http://www.aiim.org/pdfa/ns/property#", "pdfaProperty");
    xmp.getPDFExtensionSchema().addNamespace("http://www.aiim.org/pdfua/ns/id/", "pdfuaid");
    XMPSchema uaSchema = new XMPSchema(XMPMetadata.createXMPMetadata(),
        "pdfaSchema", "pdfaSchema", "pdfaSchema");
    uaSchema.setTextPropertyValue("schema", "PDF/UA Universal Accessibility Schema");
    uaSchema.setTextPropertyValue("namespaceURI", "http://www.aiim.org/pdfua/ns/id/");
    uaSchema.setTextPropertyValue("prefix", "pdfuaid");
    XMPSchema uaProp = new XMPSchema(XMPMetadata.createXMPMetadata(),
        "pdfaProperty", "pdfaProperty", "pdfaProperty");
    uaProp.setTextPropertyValue("name", "part");
    uaProp.setTextPropertyValue("valueType", "Integer");
    uaProp.setTextPropertyValue("category", "internal");
    uaProp.setTextPropertyValue("description", "Indicates, which part of ISO 14289 standard is followed");
    uaSchema.addUnqualifiedSequenceValue("property", uaProp);
    xmp.getPDFExtensionSchema().addBagValue("schemas", uaSchema);
    xmp.getPDFExtensionSchema().setPrefix("pdfuaid");
    xmp.getPDFExtensionSchema().setTextPropertyValue("part", "1");
    XmpSerializer serializer = new XmpSerializer();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serializer.serialize(xmp, baos, true);
    PDMetadata metadata = new PDMetadata(pdf);
    metadata.importXMPMetadata(baos.toByteArray());
    pdf.getDocumentCatalog().setMetadata(metadata);
  }

  private void setupDocumentCatalog() {
    //Adjust other document metadata
    PDDocumentCatalog documentCatalog = pdf.getDocumentCatalog();
    documentCatalog.setLanguage("English");
    documentCatalog.setViewerPreferences(new PDViewerPreferences(new COSDictionary()));
    documentCatalog.getViewerPreferences().setDisplayDocTitle(true);
    documentCatalog.getCOSObject().setString(COSName.LANG, "EN-US");
    documentCatalog.getCOSObject().setName(COSName.PAGE_LAYOUT, "OneColumn");
    documentCatalog.setAcroForm(acroForm);
    PDStructureTreeRoot structureTreeRoot = new PDStructureTreeRoot();
    HashMap<String, String> roleMap = new HashMap<>();
    roleMap.put("Annotation", "Span");
    roleMap.put("Artifact", "P");
    roleMap.put("Bibliography", "BibEntry");
    roleMap.put("Chart", "Figure");
    roleMap.put("Diagram", "Figure");
    roleMap.put("DropCap", "Figure");
    roleMap.put("EndNote", "Note");
    roleMap.put("FootNote", "Note");
    roleMap.put("InlineShape", "Figure");
    roleMap.put("Outline", "Span");
    roleMap.put("Strikeout", "Span");
    roleMap.put("Subscript", "Span");
    roleMap.put("Superscript", "Span");
    roleMap.put("Underline", "Span");
    structureTreeRoot.setRoleMap(roleMap);
    documentCatalog.setStructureTreeRoot(structureTreeRoot);
    PDMarkInfo markInfo = new PDMarkInfo();
    markInfo.setMarked(true);
    documentCatalog.setMarkInfo(markInfo);
  }

  private PDResources setupAcroForm() throws IOException {
    //Set AcroForm Appearance Characteristics
    PDResources resources = new PDResources();
    PDType1Font font = new PDType1Font(FontName.HELVETICA);
    defaultFont = PDType0Font.load(pdf,
        new PDTrueTypeFont(font.getCOSObject()).getTrueTypeFont(), true);
    resources.put(COSName.getPDFName("Helv"), defaultFont);
    acroForm.setNeedAppearances(true);
    acroForm.setXFA(null);
    acroForm.setFields(Collections.emptyList());
    acroForm.setDefaultResources(resources);
    acroForm.setDefaultAppearance("/Helv 10 Tf 0 g");
    return resources;
  }


  private void initiatePages(int initPages, PDResources resources) {
    //Create document initial pages
    COSArray cosArray = new COSArray();
    cosArray.add(COSName.getPDFName("PDF"));
    cosArray.add(COSName.getPDFName("Text"));
    COSArray boxArray = new COSArray();
    boxArray.add(new COSFloat(0.0f));
    boxArray.add(new COSFloat(0.0f));
    boxArray.add(new COSFloat(612.0f));
    boxArray.add(new COSFloat(792.0f));
    for (int i = 0; i < initPages; i++) {
      PDPage page = new PDPage(PDRectangle.LETTER);
      page.getCOSObject().setItem(COSName.getPDFName("Tabs"), COSName.S);
      page.setResources(resources);
      page.getResources().getCOSObject().setItem(COSName.PROC_SET, cosArray);
      page.getCOSObject().setItem(COSName.CROP_BOX, boxArray);
      page.getCOSObject().setItem(COSName.ROTATE, COSInteger.get(0));
      page.getCOSObject().setItem(COSName.STRUCT_PARENTS, COSInteger.get(0));
      pages.add(page);
      pdf.addPage(pages.get(pages.size() - 1));
    }
    nums.add(COSInteger.get(0));
  }

  //Adds the parent tree to root struct element to identify tagged content
  private void addParentTree() {
    COSDictionary dict = new COSDictionary();
    nums.add(numDictionaries);
    for (int i = 1; i < currentStructParent; i++) {
      nums.add(COSInteger.get(i));
      nums.add(annotDicts.get(i - 1));
    }
    dict.setItem(COSName.NUMS, nums);
    PDNumberTreeNode numberTreeNode = new PDNumberTreeNode(dict, dict.getClass());
    pdf.getDocumentCatalog().getStructureTreeRoot().setParentTreeNextKey(currentStructParent);
    pdf.getDocumentCatalog().getStructureTreeRoot().setParentTree(numberTreeNode);
    pdf.getDocumentCatalog().getStructureTreeRoot().appendKid(rootElem);
  }
}
