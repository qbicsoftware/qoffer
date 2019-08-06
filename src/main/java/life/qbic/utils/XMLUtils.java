/*******************************************************************************
 * QBiC Offer Generator provides an infrastructure for creating offers using QBiC portal and
 * infrastructure. Copyright (C) 2018 Benjamin Sailer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package life.qbic.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;

/**
 * This class holds several functions to modify the .xml document which holds the content for the bindings of the
 * template .docx file:
 *      - {@link XMLUtils#addRowToTable(Document, int, String, String, String, String, String)}: Adds a new row to
 *      the table within the xml document
 *      - {@link XMLUtils#removeRowInTable(Document, int)}: Removes the row with index rowIndex from the table
 *      - {@link XMLUtils#changeRowInTable(Document, int, String, String, String, String, String)}: Changes the text
 *      content of the nodes for the specified row to the provided values
 *      - {@link XMLUtils#readXMLFile(String)}: Reads the xml file and returns the Document of it.
 *      - {@link XMLUtils#changeNodeTextContent(Document, String, String)}: Changes the text content of the first
 *      found node in the document.
 *      - {@link XMLUtils#writeXMLDocumentToFile(Document, String)}: Writes the xml document to the specified output
 *      file.
 */
public class XMLUtils {


  /**
   * Adds a new row to the table within the xml document
   * @param doc: xml document to modify
   * @param rowIndex: at which position the new row should be inserted
   * @param workPackageName: text content of the work package name node
   * @param workPackageQuantity: text content of the work package quantity node
   * @param workPackageUnitPrice: text content of the work package unit price node
   * @param workPackageAmount: text content of the work package amount node
   */
  public static void addRowToTable(Document doc, int rowIndex, String workPackageID, String workPackageName, String workPackageQuantity,
                                   String workPackageUnitPrice, String workPackageAmount, int discount, String discountedPrice) {
    
    // create the element we want to insert
    Element rowToInsert = doc.createElement("row");

    // add the row elements to the row element we want to insert
    Element workPackageIDElement = doc.createElement("work_package_id");
    workPackageIDElement.setTextContent(workPackageID);
    rowToInsert.appendChild(workPackageIDElement);

    // add the row elements to the row element we want to insert
    Element workPackageNameElement = doc.createElement("work_package_name");
    // add discount information, if there is a discount
    if(discount>0) {
      workPackageName+= " ("+workPackageQuantity+" samples result in a "+discount+"% discount)";
      workPackageUnitPrice = discountedPrice;
    }
    workPackageNameElement.setTextContent(workPackageName);
    rowToInsert.appendChild(workPackageNameElement);

    Element workPackageQuantityElement = doc.createElement("work_package_quantity");
    workPackageQuantityElement.setTextContent(workPackageQuantity);
    rowToInsert.appendChild(workPackageQuantityElement);

    Element workPackageUnitPriceElement = doc.createElement("work_package_unit_price");
    workPackageUnitPriceElement.setTextContent(workPackageUnitPrice);
    rowToInsert.appendChild(workPackageUnitPriceElement);

    Element workPackageAmountElement = doc.createElement("work_package_amount");
    workPackageAmountElement.setTextContent(workPackageAmount);
    rowToInsert.appendChild(workPackageAmountElement);

/*    Element workPackageNumberElement = doc.createElement("work_package_number");
    workPackageNumberElement.setTextContent(workPackageNumber);
    rowToInsert.appendChild(workPackageNumberElement); --> no longer within new Template*/

    // Insert the "row" into the "table"
    insertRowToTable(doc, rowIndex, rowToInsert);
  }

  /**
   * inserts the given element to the table at the specified rowIndex
   * @param doc: xml document to modify
   * @param rowIndex: index of the row where the new row should be inserted
   * @param rowToInsert: row to insert
   */
  private static void insertRowToTable(Document doc, int rowIndex, Element rowToInsert) {

    // for inserting on specific positions we need xpath
    XPathFactory xfactory = XPathFactory.newInstance();
    XPath xpath = xfactory.newXPath();

    // create the x path expression like this, so we can use a variable for the position
    String xPathExpression = "/yourxml/work_packages_table/*[position()=" + rowIndex + "]";

    // get the node for the position
    Node positionSpecificNode = null;
    try {
      positionSpecificNode = (Node)xpath.evaluate(xPathExpression, doc, XPathConstants.NODE);
    } catch (XPathExpressionException e) {
      e.printStackTrace();
    }

    // insert the row into the table
    assert positionSpecificNode != null;
    positionSpecificNode.getParentNode().insertBefore(rowToInsert, positionSpecificNode);
  }

  /**
   * Removes the row with index rowIndex from the table
   * @param doc: xml document to modify
   * @param rowIndex: index of row to be removed
   */
  public static void removeRowInTable(Document doc, int rowIndex) {

    // get the node representing the row
    Node rowNode = doc.getElementsByTagName("row").item(rowIndex);
    if (rowNode == null) {
      System.err.println("Error: No corresponding row found! Row index invalid!");
      return;
    }

    // remove the row node
    rowNode.getParentNode().removeChild(rowNode);
  }

  /**
   * Changes the text content of the nodes for the specified row to the provided values
   * @param doc: xml document to modify
   * @param rowIndex: which row to change
   * @param workPackageName: new text content of the work package name node
   * @param workPackageQuantity: new text content of the work package quantity node
   * @param workPackageUnitPrice: new text content of the work package unit price node
   * @param workPackageAmount: new text content of the work package amount node
   */
  private static void changeRowInTable(Document doc, int rowIndex, String workPackageName, String workPackageQuantity,
                                       String workPackageUnitPrice, String workPackageAmount, String workPackageNumber) {

    // get the node representing the row
    Node rowNode = doc.getElementsByTagName("row").item(rowIndex);
    if (rowNode == null) {
      System.err.println("Error: No corresponding row found! Row index invalid!");
      return;
    }

    if ( rowNode.getNodeType() == Node.ELEMENT_NODE ) {
      // convert node to element to enable use of getElementsByTagName
      Element nodeElement = (Element) rowNode;

      // set the text content of the different nodes according to the parameters provided
      nodeElement.getElementsByTagName("work_package_name").item(0).setTextContent(workPackageName);
      nodeElement.getElementsByTagName("work_package_quantity").item(0).setTextContent(workPackageQuantity);
      nodeElement.getElementsByTagName("work_package_unit_price").item(0).setTextContent(workPackageUnitPrice);
      nodeElement.getElementsByTagName("work_package_amount").item(0).setTextContent(workPackageAmount);
      //nodeElement.getElementsByTagName("work_package_number").item(0).setTextContent(workPackageNumber); --> not in new template

    } else {
      System.err.println("Error: Node is not an element!");
    }
  }


  /**
   * swaps the position of two rows in the table
   * @param doc: xml document to modify
   * @param rowIndex1: index of the first row to swap
   * @param rowIndex2: index of the second row to swap
   */
  private static void swapRowsInTable(Document doc, int rowIndex1, int rowIndex2) {

    if (rowIndex1 == rowIndex2) {
      System.err.println("Row indices should be different from each other");
      return;
    }

    // implementation requires rowIndex1 to be smaller than rowIndex2, so we simply change the values between them
    if (rowIndex1 > rowIndex2) {
      int temp = rowIndex1;
      rowIndex1 = rowIndex2;
      rowIndex2 = temp;
    }

    // get the first node representing the row
    Node rowNode1 = doc.getElementsByTagName("row").item(rowIndex1);
    if (rowNode1 == null) {
      System.err.println("Error: No corresponding row found! First row index invalid!");
      return;
    }
    // convert node to element
    Element rowElement1 = (Element) rowNode1;

    // get the second node representing the row
    Node rowNode2 = doc.getElementsByTagName("row").item(rowIndex2);
    if (rowNode2 == null) {
      System.err.println("Error: No corresponding row found! Second row index invalid!");
      return;
    }
    // convert node to element
    Element rowElement2 = (Element) rowNode2;

    // insert row 2 at the position of row 1
    insertRowToTable(doc, rowIndex1, rowElement2);

    // insert row 1 at the position of row 2
    insertRowToTable(doc, rowIndex2+1, rowElement1);
  }

  /**
   * Reads the xml file and returns the Document of it.
   * @param filename: file to parse
   * @return org.w3c.dom.Document of the xml file
   */
  public static Document readXMLFile(String filename) {

    // setup the document builder
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = null;
    try {
      dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    assert dBuilder != null;

    // parse the xml file
    File XMLFile = new File(filename);
    Document doc = null;
    try {
      doc = dBuilder.parse(XMLFile);
    } catch (SAXException | IOException e) {
      e.printStackTrace();
    }
    assert doc != null;

    return doc;
  }

  /**
   * Changes the text content of the first found node in the document.
   * @param doc: xml Document to modify
   * @param elementTagName: tagName of the node to modify
   * @param newTextContent: new text content
   */
  public static void changeNodeTextContent(Document doc, String elementTagName, String newTextContent) {

    // we can only set the text content if its not null
    if (newTextContent == null)
      return;

    Node node = doc.getElementsByTagName(elementTagName).item(0);
    node.setTextContent(newTextContent);
  }

  /**
   * Writes the xml document to the specified output file.
   * @param doc: xml document to write
   * @param outputFilename: filename the document should be saved to
   */
  private static void writeXMLDocumentToFile(Document doc, String outputFilename) {

    // setup the transformer factory and the transformer for writing the xml file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = null;
    try {
      transformer = transformerFactory.newTransformer();
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    }
    DOMSource domSource = new DOMSource(doc);

    // we want indentation for the generated xml file with 4 spaces
    assert transformer != null;
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

    // create the output file
    File outputFile = new File(outputFilename);
    try {
      outputFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // write the xml document to the output file
    StreamResult result = new StreamResult(outputFile.getAbsolutePath());
    try {
      transformer.transform(domSource, result);
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }
}
