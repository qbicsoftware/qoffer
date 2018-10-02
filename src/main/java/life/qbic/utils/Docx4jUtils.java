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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.Docx4J;
import org.docx4j.model.datastorage.RemovalHandler;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;

import java.io.File;

public final class Docx4jUtils {

  private static final Logger LOG = LogManager.getLogger(Docx4jUtils.class);
  /**
   * Applies the bindings from the Document holding the content control (a xml file) to the template file and returns
   * the WordprocessingMLPackage for it.
   * @param contentControlFilename: w3c document holding the content controls (modified xml file
   *                              (see contentControlTemplate.xml for an example))
   * @param templateFilename: file name of the .docx file holding the template (here: YYYYMMDD_PiName_QXXXX_resizedTable.docx)
   * @return WordprocessingMLPackage which allows us to save the generated .docx file
   */
  public static WordprocessingMLPackage applyBindings(org.w3c.dom.Document contentControlFilename, String templateFilename) {

    // get the template file
    WordprocessingMLPackage wordProcessor = null;
    try {
      File templateFile = new File(templateFilename);
      wordProcessor = WordprocessingMLPackage.load(templateFile);
    } catch (Docx4JException e) {
      e.printStackTrace();
    }

    // apply the bindings to the template file
    try {
      assert wordProcessor != null;
      Docx4J.bind(wordProcessor, contentControlFilename, Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML);
    } catch (Docx4JException e) {
      e.printStackTrace();
    }

    // remove the bindings
    try {
      removeSDTs(wordProcessor);
    } catch (Docx4JException e) {
      e.printStackTrace();
    }

    return wordProcessor;
  }

  /**
   * removes the content control bindings from the wordprocessingMLPackage
   * @param wmlPackage: wordprocessingMLPackage to remove the bindings from
   * @throws Docx4JException:
   */
  private static void removeSDTs(WordprocessingMLPackage wmlPackage)throws Docx4JException {
    RemovalHandler removalHandler;
    removalHandler = new RemovalHandler();
    removalHandler.removeSDTs(wmlPackage.getMainDocumentPart(), RemovalHandler.Quantifier.ALL, (String[])null);
    for (Part part:wmlPackage.getParts().getParts().values()) {
      if (part instanceof HeaderPart) {
        removalHandler.removeSDTs((HeaderPart)part, RemovalHandler.Quantifier.ALL, (String[])null);
      }
      else if (part instanceof FooterPart) {
        removalHandler.removeSDTs((FooterPart)part, RemovalHandler.Quantifier.ALL, (String[])null);
      }
    }
  }
}
