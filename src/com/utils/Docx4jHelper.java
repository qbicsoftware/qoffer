package com.utils;

import java.math.BigInteger;
import java.util.List;

import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblPrBase.TblStyle;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.Color;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcMar;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.TcBorders;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;

public class Docx4jHelper {

  private ObjectFactory factory;

  public Docx4jHelper() {
    this.factory = new ObjectFactory();
  }

  public Tbl createTableWithContent(List<String> header, List<List<String>> data) {

    Tbl table = factory.createTbl();

    TblPr tblPr = new TblPr();
    TblStyle tblStyle = new TblStyle();
    tblStyle.setVal("TableGrid");
    tblPr.setTblStyle(tblStyle);
    table.setTblPr(tblPr);

    Tr tableRow = factory.createTr();

    // a default table cell style
    DocxStyle defStyle = new DocxStyle();
    defStyle.setBold(false);
    defStyle.setItalic(false);
    defStyle.setUnderline(false);
    defStyle.setFontSize("28");
    defStyle.setHorizAlignment(JcEnumeration.CENTER);

    // header row style
    DocxStyle headerStyle = new DocxStyle();
    headerStyle.setBold(true);
    headerStyle.setFontSize("28");
    headerStyle.setHorizAlignment(JcEnumeration.CENTER);

    for (String cell : header) {
      addTableCell(tableRow, cell, 2000, headerStyle, 1, null);
    }
    // addTableCell(tableRow, "Code", 2200, style, 1, null);
    // addTableCell(tableRow, "Name", 3200, style, 1, null);
    // addTableCell(tableRow, "External ID", 2100, style, 1, null);
    // addTableCell(tableRow, "Species", 2200, style, 1, null);
    // if (datasets)
    // addTableCell(tableRow, "Datasets", 1000, style, 1, null);
    table.getContent().add(tableRow);

    for (List<String> row : data) {
      tableRow = factory.createTr();
      for (String cell : row) {
        addTableCell(tableRow, cell, 2000, defStyle, 1, null);
      }
      // addTableCell(tableRow, row.get(1), 3200, defStyle, 1, null);
      // addTableCell(tableRow, row.get(2), 2100, defStyle, 1, null);
      // addTableCell(tableRow, row.get(3), 2200, defStyle, 1, null);
      // if (datasets)
      // addTableCell(tableRow, row.get(4), 1000, defStyle, 1, null);
      table.getContent().add(tableRow);
    }
    return table;
  }

  private void addTableCell(Tr tableRow, String content, int width,

  DocxStyle style, int horizontalMergedCells, String verticalMergedVal) {
    Tc tableCell = factory.createTc();
    addCellStyle(tableCell, content, style);
    setCellWidth(tableCell, width);
    setCellVMerge(tableCell, verticalMergedVal);
    setCellHMerge(tableCell, horizontalMergedCells);
    if (style.isNoWrap()) {
      setCellNoWrap(tableCell);
    }
    tableRow.getContent().add(tableCell);
  }

  public P createParagraph(String content, boolean bold, boolean underlined, String fontSize) {
    P paragraph = factory.createP();

    Text text = factory.createText();
    text.setValue(content);

    R run = factory.createR();
    run.getContent().add(text);

    paragraph.getContent().add(run);

    RPr runProperties = factory.createRPr();

    if (bold)
      addBoldStyle(runProperties);
    if (underlined)
      addUnderlineStyle(runProperties);

    setFontSize(runProperties, fontSize);
    run.setRPr(runProperties);

    return paragraph;
  }

  private void addCellStyle(Tc tableCell, String content, DocxStyle style) {
    if (style != null) {

      P paragraph = factory.createP();

      Text text = factory.createText();
      text.setValue(content);

      R run = factory.createR();
      run.getContent().add(text);

      paragraph.getContent().add(run);

      setHorizontalAlignment(paragraph, style.getHorizAlignment());

      RPr runProperties = factory.createRPr();

      if (style.isBold()) {
        addBoldStyle(runProperties);
      }
      if (style.isItalic()) {
        addItalicStyle(runProperties);
      }
      if (style.isUnderline()) {
        addUnderlineStyle(runProperties);
      }

      setFontSize(runProperties, style.getFontSize());
      setFontColor(runProperties, style.getFontColor());
      setFontFamily(runProperties, style.getFontFamily());

      setCellMargins(tableCell, style.getTop(), style.getRight(),

      style.getBottom(), style.getLeft());
      setCellColor(tableCell, style.getBackground());
      setVerticalAlignment(tableCell, style.getVerticalAlignment());

      setCellBorders(tableCell, style.isBorderTop(), style.isBorderRight(),

      style.isBorderBottom(), style.isBorderLeft());

      run.setRPr(runProperties);

      tableCell.getContent().add(paragraph);
    }
  }

  private void setCellBorders(Tc tableCell, boolean borderTop, boolean borderRight,

  boolean borderBottom, boolean borderLeft) {

    TcPr tableCellProperties = tableCell.getTcPr();
    if (tableCellProperties == null) {
      tableCellProperties = new TcPr();
      tableCell.setTcPr(tableCellProperties);
    }

    CTBorder border = new CTBorder();
    // border.setColor("auto");
    border.setColor("0000FF");
    border.setSz(new BigInteger("20"));
    border.setSpace(new BigInteger("0"));
    border.setVal(STBorder.SINGLE);

    TcBorders borders = new TcBorders();
    if (borderBottom) {
      borders.setBottom(border);
    }
    if (borderTop) {
      borders.setTop(border);
    }
    if (borderLeft) {
      borders.setLeft(border);
    }
    if (borderRight) {
      borders.setRight(border);
    }
    tableCellProperties.setTcBorders(borders);
  }

  private void setCellWidth(Tc tableCell, int width) {
    if (width > 0) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }
      TblWidth tableWidth = new TblWidth();
      tableWidth.setType("dxa");
      tableWidth.setW(BigInteger.valueOf(width));
      tableCellProperties.setTcW(tableWidth);
    }
  }

  private void setCellNoWrap(Tc tableCell) {
    TcPr tableCellProperties = tableCell.getTcPr();
    if (tableCellProperties == null) {
      tableCellProperties = new TcPr();
      tableCell.setTcPr(tableCellProperties);
    }
    BooleanDefaultTrue b = new BooleanDefaultTrue();
    b.setVal(true);
    tableCellProperties.setNoWrap(b);
  }

  private void setCellVMerge(Tc tableCell, String mergeVal) {
    if (mergeVal != null) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }
      VMerge merge = new VMerge();
      if (!"close".equals(mergeVal)) {
        merge.setVal(mergeVal);
      }
      tableCellProperties.setVMerge(merge);
    }
  }

  private void setCellHMerge(Tc tableCell, int horizontalMergedCells) {
    if (horizontalMergedCells > 1) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }

      GridSpan gridSpan = new GridSpan();
      gridSpan.setVal(new BigInteger(String.valueOf(horizontalMergedCells)));

      tableCellProperties.setGridSpan(gridSpan);
      tableCell.setTcPr(tableCellProperties);
    }
  }

  private void setCellColor(Tc tableCell, String color) {
    if (color != null) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }
      CTShd shd = new CTShd();
      shd.setFill(color);
      tableCellProperties.setShd(shd);
    }
  }

  private void setCellMargins(Tc tableCell, int top, int right, int bottom, int left) {
    TcPr tableCellProperties = tableCell.getTcPr();
    if (tableCellProperties == null) {
      tableCellProperties = new TcPr();
      tableCell.setTcPr(tableCellProperties);
    }
    TcMar margins = new TcMar();

    if (bottom > 0) {
      TblWidth bW = new TblWidth();
      bW.setType("dxa");
      bW.setW(BigInteger.valueOf(bottom));
      margins.setBottom(bW);
    }

    if (top > 0) {
      TblWidth tW = new TblWidth();
      tW.setType("dxa");
      tW.setW(BigInteger.valueOf(top));
      margins.setTop(tW);
    }

    if (left > 0) {
      TblWidth lW = new TblWidth();
      lW.setType("dxa");
      lW.setW(BigInteger.valueOf(left));
      margins.setLeft(lW);
    }

    if (right > 0) {
      TblWidth rW = new TblWidth();
      rW.setType("dxa");
      rW.setW(BigInteger.valueOf(right));
      margins.setRight(rW);
    }

    tableCellProperties.setTcMar(margins);
  }

  private void setVerticalAlignment(Tc tableCell, STVerticalJc align) {
    if (align != null) {
      TcPr tableCellProperties = tableCell.getTcPr();
      if (tableCellProperties == null) {
        tableCellProperties = new TcPr();
        tableCell.setTcPr(tableCellProperties);
      }

      CTVerticalJc valign = new CTVerticalJc();
      valign.setVal(align);

      tableCellProperties.setVAlign(valign);
    }
  }

  private void setFontSize(RPr runProperties, String fontSize) {
    if (fontSize != null && !fontSize.isEmpty()) {
      HpsMeasure size = new HpsMeasure();
      size.setVal(new BigInteger(fontSize));
      runProperties.setSz(size);
      runProperties.setSzCs(size);
    }

  }


  private void setFontFamily(RPr runProperties, String fontFamily) {
    if (fontFamily != null) {
      RFonts rf = runProperties.getRFonts();
      if (rf == null) {
        rf = new RFonts();
        runProperties.setRFonts(rf);
      }
      rf.setAscii(fontFamily);
    }

  }

  private void setFontColor(RPr runProperties, String color) {
    if (color != null) {
      Color c = new Color();
      c.setVal(color);
      runProperties.setColor(c);
    }
  }

  private void setHorizontalAlignment(P paragraph, JcEnumeration hAlign) {
    if (hAlign != null) {
      PPr pprop = new PPr();
      Jc align = new Jc();
      align.setVal(hAlign);
      pprop.setJc(align);
      paragraph.setPPr(pprop);
    }
  }

  private void addBoldStyle(RPr runProperties) {
    BooleanDefaultTrue b = new BooleanDefaultTrue();
    b.setVal(true);
    runProperties.setB(b);
  }

  private void addItalicStyle(RPr runProperties) {
    BooleanDefaultTrue b = new BooleanDefaultTrue();
    b.setVal(true);
    runProperties.setI(b);
  }

  private void addUnderlineStyle(RPr runProperties) {
    U val = new U();
    val.setVal(UnderlineEnumeration.SINGLE);
    runProperties.setU(val);
  }
}
