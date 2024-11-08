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
        new Cell("SBM Antragzusammenfastung", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        50, 25, 50, struct, StandardStructureTypes.H1, 0);

    var f = formBuilder.drawElement(
        new Cell("Footer", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        50, 600, 50, struct, StandardStructureTypes.P, 0);

    var header = formBuilder.drawElement(
        new Cell("Title", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        250, 50, 50, sec1, StandardStructureTypes.H2, 0);


    var section_title = formBuilder.drawElement(
        new Cell("Section", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        150, 75, 50, sec1, StandardStructureTypes.H3, 0);

    var structure = new PDStructureElement(StandardStructureTypes.SECT, sec1);

    var section_one = formBuilder.drawElement(
        new Cell("Left argument", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        150, 100, 50, sec1, StandardStructureTypes.H4, 0);

    var section_two = formBuilder.drawElement(
        new Cell("Right argument", new Color(229, 229, 229), Color.BLUE.darker(),
            12, formBuilder.PAGE_WIDTH - 100, PDConstants.LEFT_ALIGN),
        400, 100, 50, sec1, StandardStructureTypes.H4, 0);

    var numd = formBuilder.numDictionaries;

    System.out.println(formBuilder.numDictionaries);
    formBuilder.saveAndClose("Accessible-Example.pdf");
  }
}