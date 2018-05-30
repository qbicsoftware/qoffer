/*******************************************************************************
 * QBiC Offer Generator provides an infrastructure for creating offers using QBiC portal and
 * infrastructure. Copyright (C) 2017 Aydın Can Polatkan, 2018 Benjamin Sailer
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

package life.qbic.components;

import com.vaadin.data.Property;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.server.*;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import life.qbic.dbase.DBManager;
import life.qbic.dbase.Database;
import life.qbic.portal.liferayandvaadinhelpers.main.LiferayAndVaadinUtils;
import life.qbic.utils.Docx4jUtils;
import life.qbic.utils.RefreshableGrid;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.vaadin.gridutil.cell.GridCellFilter;

import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static life.qbic.components.OfferManagerTabPackageComponent.createOfferManagerTabPackageComponent;
import static life.qbic.utils.XMLUtils.*;
import static life.qbic.utils.qOfferManagerUtils.*;

final class OfferManagerTab {

  private static FileDownloader fileDownloader;
  private static FileDownloader exportFileDownloader;
  private static RefreshableGrid offerManagerGrid;
  private static VerticalLayout detailsLayout;
  private static ComboBox packageGroupComboBox;

  static RefreshableGrid getOfferManagerGrid() {
    return offerManagerGrid;
  }

  static VerticalLayout getDetailsLayout() {
    return detailsLayout;
  }

  static String getPackageGroupComboBoxValue() {
    return packageGroupComboBox.getValue().toString();
  }

  /**
   * creates the tab for displaying and modifying the offers in a vaadin grid
   * @return vaadin component
   * @throws SQLException:
   */
  static Component createOfferManagerTab() throws SQLException {

    Database db = qOfferManager.getDb();

    VerticalLayout offerManLayout = new VerticalLayout();
    HorizontalLayout editSettingsLayout = new HorizontalLayout();
    detailsLayout = new VerticalLayout();

    editSettingsLayout.setSpacing(true);
    detailsLayout.setSizeFull();

    ComboBox updateStatus = new ComboBox("Select Status");
    updateStatus.addItem("In Progress");
    updateStatus.addItem("Sent");
    updateStatus.addItem("Accepted");
    updateStatus.addItem("Rejected");

    Button updateButton = new Button("Update");
    updateButton.setIcon(FontAwesome.SPINNER);
    updateButton.setDescription("Click here to update the currently selected offer.");

    Button deleteOfferButton = new Button("Delete");
    deleteOfferButton.setIcon(FontAwesome.TRASH_O);
    deleteOfferButton.setDescription("Click here to delete the currently selected offer.");

    packageGroupComboBox = new ComboBox("Select package group");
    packageGroupComboBox.addItems("All", "Bioinformatics Analysis", "Mass spectrometry", "Project Management",
        "Sequencing", "Other");
    packageGroupComboBox.setValue("All");
    packageGroupComboBox.setNullSelectionAllowed(false);
    packageGroupComboBox.setDescription("Click here to select the package group for the packages displayed below.");

    Button exportTableButton = new Button("Export as .csv");
    exportTableButton.setIcon(FontAwesome.DOWNLOAD);
    exportTableButton.setDescription("Click here to export the table as .csv file.");

    editSettingsLayout.addComponent(updateStatus);
    editSettingsLayout.addComponent(updateButton);
    editSettingsLayout.addComponent(deleteOfferButton);
    editSettingsLayout.addComponent(packageGroupComboBox);
    editSettingsLayout.addComponent(exportTableButton);

    editSettingsLayout.setComponentAlignment(updateButton, Alignment.BOTTOM_CENTER);
    editSettingsLayout.setComponentAlignment(deleteOfferButton, Alignment.BOTTOM_CENTER);
    editSettingsLayout.setComponentAlignment(packageGroupComboBox, Alignment.BOTTOM_CENTER);
    editSettingsLayout.setComponentAlignment(exportTableButton, Alignment.BOTTOM_CENTER);

    Button generateOfferButton = new Button("Download offer");
    generateOfferButton.setIcon(FontAwesome.DOWNLOAD);
    generateOfferButton.setDescription("Select an offer from the grid then click here to download it as .docx!");
    generateOfferButton.setEnabled(false);

    offerManLayout.setMargin(true);
    offerManLayout.setSpacing(true);
    offerManLayout.setSizeFull();

    TableQuery tq = new TableQuery("offers", DBManager.getDatabaseInstanceAlternative());
    tq.setVersionColumn("OPTLOCK");
    SQLContainer container = new SQLContainer(tq);
    container.setAutoCommit(true);

    offerManagerGrid = new RefreshableGrid(container);

    // add the filters to the grid
    GridCellFilter filter = new GridCellFilter(offerManagerGrid);
    filter.setTextFilter("offer_id", true, true);
    filter.setTextFilter("offer_number", true, false);
    filter.setTextFilter("offer_project_reference", true, false);
    filter.setTextFilter("offer_facility", true, false);
    filter.setTextFilter("offer_name", true, false);
    filter.setTextFilter("offer_description", true, false);
    filter.setDateFilter("offer_date");
    filter.setDateFilter("last_edited");
    filter.setComboBoxFilter("offer_status", Arrays.asList("In Progress",
        "Sent", "Accepted", "Rejected"));

    offerManagerGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

    addListeners(db, updateStatus, updateButton, deleteOfferButton, generateOfferButton, container,
        exportTableButton);

    offerManagerGrid.getColumn("offer_id").setHeaderCaption("Id").setWidth(100).setEditable(false);
    offerManagerGrid.getColumn("offer_number").setHeaderCaption("Quotation Number").setWidth(200).setEditable(false);
    offerManagerGrid.getColumn("offer_project_reference").setHeaderCaption("Project Reference").setEditable(false);
    offerManagerGrid.getColumn("offer_name").setHeaderCaption("Offer Name").setWidth(200);
    offerManagerGrid.getColumn("offer_facility").setHeaderCaption("Prospect");
    offerManagerGrid.getColumn("offer_description").setHeaderCaption("Description").setWidth(300);
    offerManagerGrid.getColumn("offer_total").setHeaderCaption("Price (€)").setEditable(false);
    offerManagerGrid.getColumn("offer_status").setHeaderCaption("Status").setEditable(false);
    offerManagerGrid.getColumn("offer_date").setHeaderCaption("Date").setEditable(false);
    offerManagerGrid.getColumn("last_edited").setHeaderCaption("Last edited").setEditable(false);
    offerManagerGrid.getColumn("added_by").setHeaderCaption("Added by").setEditable(false);

    offerManagerGrid.setColumnOrder("offer_id", "offer_project_reference", "offer_number", "offer_name",
        "offer_description", "offer_total", "offer_facility", "offer_status", "offer_date", "last_edited", "added_by");

    offerManagerGrid.removeColumn("discount");
    offerManagerGrid.removeColumn("internal");
    offerManagerGrid.removeColumn("offer_group");
    offerManagerGrid.removeColumn("offer_extra_price");
    offerManagerGrid.removeColumn("offer_price");

    offerManagerGrid.sort("offer_date", SortDirection.DESCENDING);
    offerManagerGrid.setWidth("100%");
    offerManagerGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
    offerManagerGrid.setEditorEnabled(true);

    // add tooltips to the cells
    offerManagerGrid.setCellDescriptionGenerator((Grid.CellDescriptionGenerator) cell -> {
      if (cell.getValue() == null)
        return null;
      return cell.getValue().toString();
    });

    // add tooltips to the header row
    for (Grid.Column column : offerManagerGrid.getColumns()) {
      Grid.HeaderCell cell = offerManagerGrid.getDefaultHeaderRow().getCell(
              column.getPropertyId());
      String htmlWithTooltip = String.format(
              "<span title=\"%s\">%s</span>", cell.getText(),
              cell.getText());
      cell.setHtml(htmlWithTooltip);
    }

    offerManLayout.addComponent(offerManagerGrid);
    offerManLayout.addComponent(editSettingsLayout);
    offerManLayout.addComponent(detailsLayout);
    offerManLayout.addComponent(generateOfferButton);

    return offerManLayout;
  }

  /**
   * adds the listeners to the offer manager tab
   * @param db: database instance to query
   * @param updateStatusComboBox: combo box for selecting the status of the offer
   * @param updateButton: button for updating the status of the offer
   * @param deleteOfferButton: button for deleting an offer
   * @param generateOfferButton: button for printing an offer
   * @param container: sql container holding the data from the database
   * @param exportTableButton: button for exporting the grid as csv
   */
  private static void addListeners(Database db, ComboBox updateStatusComboBox, Button updateButton,
                                   Button deleteOfferButton, Button generateOfferButton, SQLContainer container,
                                   Button exportTableButton) {

    // several lists holding the package names, descriptions, prices, etc. for the current offer
    // TODO: change to one list of packageBeans
    List<String> packageNames = qOfferManager.getPackageNames();
    List<String> packageDescriptions = qOfferManager.getPackageDescriptions();
    List<String> packageCounts = qOfferManager.getPackageCounts();
    List<String> packageUnitPrices = qOfferManager.getPackageUnitPrices();
    List<String> packageTotalPrices = qOfferManager.getPackageTotalPrices();

    offerManagerGrid.addSelectionListener(selectionEvent -> {

      // Get selection from the selection model
      Object selected = ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow();

      if (selected != null) {

        // check if any of the packages in the current offer has no package_grp (e.g. "Bioinformatics Analysis",
        // "Project Management", etc.) associated with it and display a warning for the user
        ArrayList<String> packageIdsWithoutPackageGroup =
            db.getPackageIdsWithoutPackageGroup(container.getItem(selected).getItemProperty("offer_id").getValue().toString());
        if (packageIdsWithoutPackageGroup.size() > 0) {
          String firstPackageName = db.getPackageNameFromPackageId(packageIdsWithoutPackageGroup.get(0));
          displayNotification("Package not associated to package group",
              "The package " + firstPackageName + " with id " + packageIdsWithoutPackageGroup.get(0) +
                  " has no package group associated with it. Please consider assigning the package to a " +
                  "package group.", "warning");
        }

        updateStatusComboBox.select(db.getOfferStatus(container.getItem(selected)
            .getItemProperty("offer_id").getValue().toString()));

        // enable the print offer button
        generateOfferButton.setEnabled(true);

        Notification.show("Selected "
            + db.getOfferStatus(container.getItem(selected).getItemProperty("offer_id").getValue()
            .toString()));

        detailsLayout.removeAllComponents();
        try {
          detailsLayout.addComponent(createOfferManagerTabPackageComponent(container, container.getItem(selected)
              .getItemProperty("offer_id").getValue().toString(), "All"));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });

    updateButton.addClickListener(new Button.ClickListener() {

      /**
       *
       */
      private static final long serialVersionUID = 8910018717791341602L;

      @Override
      public void buttonClick(Button.ClickEvent event) {

        if (offerManagerGrid.getSelectedRow() == null) {
          displayNotification("oOps! Forgot something?!",
              "Please make sure that you select an offer to update.", "error");
        }
        else if (updateStatusComboBox.getValue() == null) {
          displayNotification("oOps! Forgot something?!",
              "Please make sure that you select an option for status update.", "error");
        }
        else {
          // update the status of the offer (sent, accepted, declined, etc.)
          db.updateStatus(updateStatusComboBox.getValue().toString(), offerManagerGrid.getSelectedRow().toString());

          container.refresh();
        }
      }
    });

    deleteOfferButton.addClickListener((Button.ClickListener) event -> {

      Object selectedRow = offerManagerGrid.getSelectedRow();
      if (selectedRow == null) {
        displayNotification("No offer selected!",
            "Please select an offer to delete.", "error");
        return;
      }

      int selectedOfferId = (int) offerManagerGrid.getContainerDataSource().getItem(selectedRow)
          .getItemProperty("offer_id").getValue();

      db.deleteOffer(selectedOfferId);

      // since refreshing the rows doesn't work properly; we force an update of the grid by setting the sort direction
      // of the package name column
      offerManagerGrid.sort("offer_id", SortDirection.ASCENDING);
      displayNotification("Offer deleted", "Offer " + selectedOfferId + " " +
          "successfully deleted.", "success");

    });

    packageGroupComboBox.addValueChangeListener((Property.ValueChangeListener) event -> {

      Object selected = ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow();

      if (selected == null) {
        displayNotification("No offer selected.",
            "Please select an offer to display", "error");
        return;
      }

      // e.g. "All", "Sequencing", or "Project Management"
      String selectedPackageGroup = packageGroupComboBox.getValue().toString();

      // change the view to display only the packages for the selected package group
      detailsLayout.removeAllComponents();
      try {
        detailsLayout.addComponent(createOfferManagerTabPackageComponent(container, container.getItem(selected)
            .getItemProperty("offer_id").getValue().toString(),  selectedPackageGroup));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    });

    // adds the file creation and the export functionality to the print offer button
    try {
      setupOfferFileExportFunctionality(db, generateOfferButton, container, packageNames, packageDescriptions, packageCounts,
          packageUnitPrices, packageTotalPrices);
    } catch (IOException e) {
      displayNotification("Whoops, something went wrong.", "A file could not be found, please try" +
          "again.", "error");
      e.printStackTrace();
    }

    try {
      setupTableExportFunctionality(container, exportTableButton);
    } catch (IOException e) {
      displayNotification("Whoops, something went wrong.", "A file could not be found, please try" +
          "again.", "error");
      e.printStackTrace();
    }
  }

  /**
   * adds the functionality of exporting the offer grid to the exportGridButton
   * @param container: SQLContainer holding the data
   * @param exportGridButton: button the export functionality should be added to
   * @throws IOException:
   */
  private static void setupTableExportFunctionality(SQLContainer container, Button exportGridButton) throws IOException {
    // setup the export as .csv file functionality
    String exportOffersFileName = "offers.csv";
    exportFileDownloader = new FileDownloader(new FileResource(new File(exportOffersFileName)))
    {
      @Override
      public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException
      {
        createExportContent(container, exportOffersFileName, exportFileDownloader);
        return super.handleConnectorRequest(request, response, path);
      }
    };
    exportFileDownloader.extend(exportGridButton);
  }

  /**
   * adds the functionality of generating the offer file and exporting it to the printOfferButton
   * @param db: database to connect to
   * @param printOfferButton: button where the functionality should be added
   * @param container: SQLContainer holding the data
   * @param packageNames: list of all the package names in the current offer
   * @param packageDescriptions: list of all the package descriptions in the current offer
   * @param packageCounts: list of all the package counts in the current offer
   * @param packageUnitPrices: list of all the package prices in the current offer
   * @param packageTotalPrices: list of all the package total prices in the current offer
   * @throws IOException:
   */
  private static void setupOfferFileExportFunctionality(Database db, Button printOfferButton, SQLContainer container,
                                                        List<String> packageNames, List<String> packageDescriptions,
                                                        List<String> packageCounts, List<String> packageUnitPrices,
                                                        List<String> packageTotalPrices) throws IOException {
    // init with some non-existent file
    fileDownloader = new FileDownloader(new FileResource(new File("temp"))) {
      @Override
      public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException {

        // fails if no offer has been selected
        boolean success = generateOfferFile(container, db, packageNames, packageDescriptions, packageCounts, packageUnitPrices,
            packageTotalPrices, fileDownloader);

        // offer file could not be generated, so we return nothing
        if (!success) {
          return false;
        }
        // handle the download of the file
        return super.handleConnectorRequest(request, response, path);
      }
    };
    fileDownloader.extend(printOfferButton);
  }

  /**
   * generates the .docx file for the offer
   * @param container: sql container holding the offers
   * @param db: database instance
   * @param packageNames: list of the package names in the offer
   * @param packageDescriptions: list of the package descriptions in the offer
   * @param packageCounts: list of the package counts in the offer
   * @param packageUnitPrices: list of the package unit prices (=price for one package)
   * @param packageTotalPrices: list of the total package prices (package_unit_price*count)*discount
   * @param fileDownloader: file downloader for enabling the download of the file
   * @return whether or not creating the file has worked
   */
  private static boolean generateOfferFile(SQLContainer container, Database db, List<String> packageNames,
                                        List<String> packageDescriptions, List<String> packageCounts,
                                        List<String> packageUnitPrices, List<String> packageTotalPrices,
                                        FileDownloader fileDownloader) {

    if (offerManagerGrid.getSelectedRow() == null) {
      displayNotification("oOps! Forgot something?!",
          "Please make sure that you select an offer.", "error");
      return false;
    }

    displayNotification("File is being generated", "Please wait a few seconds while the file is " +
            "being generated..", "warning");

    // since we take the package specific values from the grid showing the packages for the current offers,
    // we need to check whether all packages are displayed or e.g. only the sequencing packages
    String selectedPackageGroup = packageGroupComboBox.getValue().toString();
    if (!selectedPackageGroup.equals("All")) {
      packageGroupComboBox.setValue("All");
    }

    String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

    // file holding the content controls for the bindings
    String contentControlFilename = basepath + "/WEB-INF/resourceFiles/contentControlTemplate.xml";
    // template .docx file containing the bindings
    String templateFileName = basepath + "/WEB-INF/resourceFiles/Template.docx";

    String clientName =
        container.getItem(offerManagerGrid.getSelectedRow()).getItemProperty("offer_facility").getValue()
            .toString();

    String offerNumber =
        container.getItem(offerManagerGrid.getSelectedRow()).getItemProperty("offer_number").getValue()
            .toString();

    String[] address = db.getAddressForPerson(clientName);
    String groupAcronym = null;
    String institute = null;
    String umbrellaOrganization = null;
    String street = null;
    String cityZipCodeAndCounty = null;
    String zipCode;
    String city;
    String country;

    // deal with the potential errors; address[0] contains a more detailed error message and tells the user how to fix the issue
    if (address.length == 1) {
      displayNotification("Database entry not found!", address[0], "warning");
    } else {
      groupAcronym = address[0];
      institute = address[1];
      umbrellaOrganization = address[2];
      street = address[3];
      zipCode = address[4];
      city = address[5];
      country = address[6];

      // e.g. D - 72076 Tübingen, Germany
      // TODO: country in english (database entry is in german..), postal code of country (is not in the database)
      cityZipCodeAndCounty = zipCode + " " + city + ", " + country;
    }

    String projectReference = offerNumber.substring(offerNumber.indexOf('_') + 1);

    String clientEmail = db.getClientEmailFromProjectRef(projectReference);

    // TODO: for liferay it probably needs some adjustments, since I couldn't test this properly..
    String projectManager;
    String projectManagerMail;
    try {
      projectManager = LiferayAndVaadinUtils.getUser().getScreenName();
      projectManagerMail = db.getUserEmail(projectManager);
    } catch (NullPointerException e) {
      projectManager = "Project manager not found";
      projectManagerMail = "Mail not found";
    }

    String projectTitle =
        container.getItem(offerManagerGrid.getSelectedRow()).getItemProperty("offer_name").getValue().toString();
    if (projectTitle == null) {
      displayNotification("Offer name is null", "Warning: The offer name for the current offer is null." +
          "The respective fields in the .docx file will thus hold the placeholder values. Please consider " +
          "setting the offer name in the Offer Manager tab.", "warning");
    }

    Object projectDescriptionObject = container.getItem(offerManagerGrid.getSelectedRow())
        .getItemProperty("offer_description").getValue();
    String projectDescription = projectDescriptionObject == null ? null : projectDescriptionObject.toString();
    if (projectDescription == null) {
      displayNotification("Offer description is null.", "Warning: The offer description for the current " +
          "offer is null. The respective fields in the .docx file will thus hold the placeholder values. Please " +
          "consider setting the offer name in the Offer Manager tab.", "warning");
    }

    DecimalFormat offerPriceFormatter = new DecimalFormat("###,###.###");
    String offerTotal =
        offerPriceFormatter.format(Float.valueOf(container.getItem(offerManagerGrid.getSelectedRow())
            .getItemProperty("offer_total").getValue().toString()));

    String clientSurname = clientName.split(" ")[clientName.split(" ").length-1];
    String dateToday = new SimpleDateFormat("yyyyMMdd").format(new Date());
    String projectQuotationNumber = dateToday + "_"  + clientSurname + "_" + projectReference;

    SimpleDateFormat currentDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH);
    String currentDate = currentDateFormat.format(new Date());

    // get the xml document holding the content for the bindings in the docx template file
    org.w3c.dom.Document contentControlDocument = readXMLFile(contentControlFilename);

    // change the fields of the content control document according to the values obtained in the grid
    changeNodeTextContent(contentControlDocument, "client_name", clientName);
    changeNodeTextContent(contentControlDocument, "client_organization", groupAcronym);
    changeNodeTextContent(contentControlDocument, "client_department", institute);
    changeNodeTextContent(contentControlDocument, "client_university", umbrellaOrganization);
    changeNodeTextContent(contentControlDocument, "client_address", street);
    changeNodeTextContent(contentControlDocument, "client_address_town", cityZipCodeAndCounty);
    changeNodeTextContent(contentControlDocument, "client_email", clientEmail);
    changeNodeTextContent(contentControlDocument, "project_reference", projectReference);
    changeNodeTextContent(contentControlDocument, "project_quotation_number", projectQuotationNumber);
    changeNodeTextContent(contentControlDocument, "name", projectManager);
    changeNodeTextContent(contentControlDocument, "email", projectManagerMail);
    changeNodeTextContent(contentControlDocument, "project_title", projectTitle);
    changeNodeTextContent(contentControlDocument, "objective", projectDescription);
    changeNodeTextContent(contentControlDocument, "estimated_total", formatCurrency(offerTotal));
    changeNodeTextContent(contentControlDocument, "date", currentDate);

    // iterate over the packages and add them to the content control .xml file
    for (int i = packageNames.size()-1; i >= 0; i--) {
      addRowToTable(contentControlDocument, 1, packageNames.get(i) + ": "+
              packageDescriptions.get(i), packageCounts.get(i), formatCurrency(packageUnitPrices.get(i)),
          formatCurrency(packageTotalPrices.get(i)), String.valueOf(i+1));
    }

    // remove the placeholder row in the .xml file
    removeRowInTable(contentControlDocument, packageNames.size());

    // apply the bindings to the .docx template file
    WordprocessingMLPackage wordProcessor = Docx4jUtils.applyBindings(contentControlDocument, templateFileName);

    // TODO: output filename needs probably to be changed when running on the server
    String outputFilename = projectQuotationNumber + ".docx";
/*
        String outputFilename = "/home/tomcat-liferay/liferay_production/tmp/" + projectQuotationNumber + ".docx";
*/

    // save updated document to output file
    try {
      assert wordProcessor != null;
      wordProcessor.save(new File(outputFilename), Docx4J.FLAG_SAVE_ZIP_FILE);
    } catch (Docx4JException e) {
      e.printStackTrace();
    }
    fileDownloader.setFileDownloadResource(new FileResource(new File(outputFilename)));

    return true;
  }
}