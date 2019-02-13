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

import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public final class qOfferManagerUtils {

  /**
   * Both DBManager and OpenBisProxy require this path to obtain configuration settings.
   */
  public static final String PROPERTIES_FILE_PATH = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/WEB-INF/resourceFiles/Credentials.properties";

  /**
   * Formats a given string to X,XXX.XX €.
   * @param currencyToFormat: string we want to format
   * @return currency in X,XXX.XX € format
   */
  public static String formatCurrency(String currencyToFormat) {

    // TODO: check if this works all the time
    // format should be something like X.XXX,XX
    if (currencyToFormat.contains(",")) {
      String[] temp = currencyToFormat.split(",");
      // format is weird, so we return it without changing it..
      if (temp.length > 2) {
        return currencyToFormat;
      }
      // price is e.g. X.XXX,X, so we add a single 0 at the end to it
      if (temp[temp.length-1].length() == 1) {
        currencyToFormat += "0";
      }

      // replace commas with dots and vice versa
      currencyToFormat = currencyToFormat.replace(".", "@");
      currencyToFormat = currencyToFormat.replace(",", "?");
      currencyToFormat = currencyToFormat.replace("@", ",");
      currencyToFormat = currencyToFormat.replace("?", ".");

      return currencyToFormat + " €";
    } else
      // format is X.XXX, so we change it to X,XXX.00 €
      return currencyToFormat.replace(".", ",") + ".00 €";
  }

  /**
   * Displays a vaadin Notification with the respective title, description and type.
   * @param title: title of the displayNotification window
   * @param description: description of the displayNotification Window
   * @param type: one of "error", "success" and "warning". Changes the style and the delay of the displayNotification.
   */
  public static void displayNotification(String title, String description, String type) {
    com.vaadin.ui.Notification notify = new com.vaadin.ui.Notification(title, description);
    notify.setPosition(Position.TOP_CENTER);
    switch (type) {
      case "error": //16000
        notify.setDelayMsec(16000);
        notify.setIcon(FontAwesome.FROWN_O);
        notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
        break;
      case "success":
        notify.setDelayMsec(8000);
        notify.setIcon(FontAwesome.SMILE_O);
        notify.setStyleName(ValoTheme.NOTIFICATION_SUCCESS + " " + ValoTheme.NOTIFICATION_CLOSABLE);
        break;
      case "warning":
        notify.setDelayMsec(16000);
        notify.setIcon(FontAwesome.MEH_O);
        notify.setStyleName(ValoTheme.NOTIFICATION_WARNING + " " + ValoTheme.NOTIFICATION_CLOSABLE);
        break;
      default:
        notify.setDelayMsec(16000);
        notify.setIcon(FontAwesome.MEH_O);
        notify.setStyleName(ValoTheme.NOTIFICATION_TRAY + " " + ValoTheme.NOTIFICATION_CLOSABLE);
        break;
    }
    // make sure that, if we are running this from a background thread, we are accessing the UI in a thread-safe way
    UI.getCurrent().access(() -> {
        notify.show(Page.getCurrent());
    });

  }

  /**
   * returns the contents of the SQLContainer (including the header) as delimiter separated string
   * @param container: SQLContainer to get the data from
   * @param delimiter: how the items should be separated, e.g. "," for .csv or "\t" for .tsv
   * @return delimiter separated string
   */
  private static String getContainerContents(SQLContainer container, String delimiter) {

    StringBuilder containerContents = new StringBuilder();

    // write the header row
    for (Object columnId: container.getContainerPropertyIds()) {
      containerContents.append(columnId.toString()).append(delimiter);
    }
    // remove last delimiter
    containerContents = new StringBuilder(containerContents.substring(0, containerContents.length() - delimiter.length()));
    containerContents.append("\n");

    // iterate over the row ids
    for (Object rowId: container.getItemIds()) {
      // iterate over the column ids
      for (Object columnId: container.getContainerPropertyIds()) {
        try {
          containerContents.append(container.getContainerProperty(rowId, columnId.toString()).getValue().toString()).append(delimiter);
        } catch (NullPointerException e) {
          containerContents.append("null").append(delimiter);
        }
      }
      // remove last delimiter
      containerContents = new StringBuilder(containerContents.substring(0, containerContents.length() - delimiter.length()));
      containerContents.append("\n");
    }

    return containerContents.toString();
  }

  /**
   * creates the content for exporting the tables as .csv file
   * @param container: SQLContainer which should be exported as .csv
   * @param exportFileName: filename of the .csv file
   * @param fileDownloader: file downloader for downloading the .csv file
   */
  public static void createExportContent(SQLContainer container, String exportFileName, FileDownloader fileDownloader)
  {
    try {
      // get the contents of the container as a comma separated string
      String containerContents = getContainerContents(container, ",");

      // write the csv file
      try (PrintStream ps = new PrintStream(exportFileName)) {
        ps.print(containerContents);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      fileDownloader.setFileDownloadResource(new FileResource(new File(exportFileName)));
    } catch (Exception e) {
      throw new RuntimeException("Error exporting!", e);
    }
  }

}
