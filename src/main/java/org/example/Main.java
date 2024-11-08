package org.example;

import java.awt.Color;
import java.io.IOException;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDStructureElement;
import org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes;
import org.example.fork.constants.PDConstants;
import org.example.fork.pojo.Cell;
import org.example.fork.util.PDFormBuilder;

public class Main {

  public static void main(String[] args) throws IOException, TransformerException {
    PDFormBuilder formBuilder = new PDFormBuilder(1, "Accessibility Example");

    PDStructureElement sec1 = formBuilder.addRoot(0);

    var struct = new PDStructureElement(StandardStructureTypes.SECT, sec1);

    var a = formBuilder.drawElement(
        new Cell("Heading", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        50, 25, 50, struct, StandardStructureTypes.H1, 0);

    var f = formBuilder.drawElement(
        new Cell("Footer", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        50, 600, 50, struct, StandardStructureTypes.P, 0);

    var header = formBuilder.drawElement(
        new Cell("Page title", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        250, 50, 50, sec1, StandardStructureTypes.H2, 0);

    formBuilder.saveAndClose("Accessible-Example.pdf");
  }
}