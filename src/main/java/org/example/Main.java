package org.example;

import java.awt.Color;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.xmpbox.schema.XmpSchemaException;
import org.example.fork.constants.PDConstants;
import org.example.fork.pojo.Cell;
import org.example.fork.util.PDFormBuilder;

public class Main {

  public static void main(String[] args) throws IOException, XmpSchemaException, TransformerException {
    PDFormBuilder formBuilder = new PDFormBuilder(1, "Accessibility Example");

    PDStructureElement sec1 = formBuilder.addRoot(0);

    var header = formBuilder.drawElement(
        new Cell("Header", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        50, 25, 50, sec1, StandardStructureTypes.H1, 0);

//
//    var header2 = formBuilder.drawElement(
//        new Cell("Header 2", new Color(229, 229, 229), Color.BLUE.darker(),
//            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
//        50, 50, 50, sec1, StandardStructureTypes.H2, 0);

    formBuilder.saveAndClose("Accessible-Example.pdf");
  }
}