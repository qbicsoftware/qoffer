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

import java.util.concurrent.*;

import com.vaadin.data.Property;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.server.*;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import life.qbic.CustomWindow.WindowFactory;
import life.qbic.dbase.Database;
import life.qbic.utils.Docx4jUtils;
import life.qbic.utils.RefreshableGrid;
import life.qbic.utils.TimeUtils;
import life.qbic.utils.qOfferManagerUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.vaadin.gridutil.cell.GridCellFilter;

import java.io.*;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static life.qbic.utils.XMLUtils.*;
import static life.qbic.utils.qOfferManagerUtils.*;

final class OfferManagerTab {

  private FileDownloader fileDownloader;
  private FileDownloader exportFileDownloader;
  private RefreshableGrid offerManagerGrid;
  private VerticalLayout detailsLayout;
  private ComboBox packageGroupComboBox;
  private qOfferManager qOfferManager;
  private OfferManagerTabPackageComponent offerManagerTabPackageComponent;
  private Button generateOfferButton;
  private Window validationWindow;
  private Window deleteWarning;
  private Layout notificationLayout;
  private SQLContainer offersContainer;


  private static final Logger LOG = LogManager.getLogger(OfferManagerTab.class);

  RefreshableGrid getOfferManagerGrid() {
    return offerManagerGrid;
  }

  VerticalLayout getDetailsLayout() {
    return detailsLayout;
  }

  String getPackageGroupComboBoxValue() {
    return packageGroupComboBox.getValue().toString();
  }


  public OfferManagerTab(qOfferManager qom) {
    qOfferManager = qom;
    offerManagerTabPackageComponent = new OfferManagerTabPackageComponent(qom, this);
  }

  /**
   * creates the tab for displaying and modifying the offers in a vaadin grid
   * 
   * @return vaadin component
   * @throws SQLException:
   */
  Component createOfferManagerTab() throws SQLException {

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

    Button proceedButton = new Button("Yes");
    proceedButton.setIcon(FontAwesome.CHECK_CIRCLE);
    proceedButton.setDescription("Click here to proceed.");

    /*
     * Button okButton = new Button("Ok"); okButton.setIcon(FontAwesome.CHECK);
     * okButton.setDescription("Click here to permanently delete the selected offer.");
     */
    packageGroupComboBox = new ComboBox("Select package group");
    packageGroupComboBox.addItems("All", "Bioinformatics Analysis", "Mass spectrometry",
        "Project Management", "Sequencing", "Other");
    packageGroupComboBox.setValue("All");
    packageGroupComboBox.setNullSelectionAllowed(false);
    packageGroupComboBox
        .setDescription("Click here to select the package group for the packages displayed below.");

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

    final Button validateOfferButton = new Button("Validate offer");
    validateOfferButton.setDescription("Download button will be active once offer is validated");
    validateOfferButton.setIcon(FontAwesome.CHECK_CIRCLE);

    generateOfferButton = new Button("Download offer");
    generateOfferButton.setIcon(FontAwesome.DOWNLOAD);
    generateOfferButton.setDescription("Offer must be first validated!");
    generateOfferButton.setEnabled(false);
    fileDownloader = new FileDownloader(new FileResource(new File("temporary_name")));
    fileDownloader.extend(generateOfferButton);

    offerManLayout.setMargin(true);
    offerManLayout.setSpacing(true);
    offerManLayout.setSizeFull();

    TableQuery tq = new TableQuery("offers", db.getDatabaseInstanceAlternative());
    tq.setVersionColumn("OPTLOCK");
    offersContainer = new SQLContainer(tq);
    offersContainer.setAutoCommit(true);

    offerManagerGrid = new RefreshableGrid(offersContainer);
    offerManagerGrid.setImmediate(true);

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
    filter.setComboBoxFilter("offer_status",
        Arrays.asList("In Progress", "Sent", "Accepted", "Rejected"));

    offerManagerGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

    addListeners(db, updateStatus, updateButton, deleteOfferButton, generateOfferButton,
        offersContainer, exportTableButton, validateOfferButton, proceedButton);

    offerManagerGrid.getColumn("offer_id").setHeaderCaption("Id").setWidth(100).setEditable(false);
    offerManagerGrid.getColumn("offer_number").setHeaderCaption("Quotation Number").setWidth(200)
        .setEditable(false);
    offerManagerGrid.getColumn("offer_project_reference").setHeaderCaption("Project Reference")
        .setEditable(false);
    offerManagerGrid.getColumn("offer_name").setHeaderCaption("Offer Name").setWidth(200)
        .setEditable(true);
    offerManagerGrid.getColumn("offer_facility").setHeaderCaption("Prospect").setEditable(false);
    offerManagerGrid.getColumn("offer_description").setHeaderCaption("Description").setWidth(300)
        .setEditable(false);
    offerManagerGrid.getColumn("offer_total").setHeaderCaption("Price (€)").setEditable(false);
    offerManagerGrid.getColumn("offer_status").setHeaderCaption("Status").setEditable(false);
    offerManagerGrid.getColumn("offer_date").setHeaderCaption("Date").setEditable(false);
    offerManagerGrid.getColumn("last_edited").setHeaderCaption("Last edited").setEditable(false);
    offerManagerGrid.getColumn("added_by").setHeaderCaption("Added by").setEditable(false);
    offerManagerGrid.getColumn("estimated_delivery_weeks").setHeaderCaption("Estimated Delivery");


    offerManagerGrid.setColumnOrder("offer_id", "offer_project_reference", "offer_number",
        "offer_name", "offer_description", "offer_total", "offer_facility", "offer_status",
        "offer_date", "estimated_delivery_weeks", "last_edited", "added_by"); // "estimated_delivery_weeks",

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
      Grid.HeaderCell cell = offerManagerGrid.getDefaultHeaderRow().getCell(column.getPropertyId());
      String htmlWithTooltip =
          String.format("<span title=\"%s\">%s</span>", cell.getText(), cell.getText());
      cell.setHtml(htmlWithTooltip);
    }

    offerManLayout.addComponent(offerManagerGrid);
    offerManLayout.addComponent(editSettingsLayout);
    offerManLayout.addComponent(detailsLayout);

    final HorizontalLayout buttonLayout = new HorizontalLayout();
    buttonLayout.setSpacing(true);
    buttonLayout.addComponent(validateOfferButton);
    buttonLayout.addComponent(generateOfferButton);
    offerManLayout.addComponent(buttonLayout);

    return offerManLayout;
  }

  /**
   * adds the listeners to the offer manager tab
   * 
   * @param db: database instance to query
   * @param updateStatusComboBox: combo box for selecting the status of the offer
   * @param updateButton: button for updating the status of the offer
   * @param deleteOfferButton: button for deleting an offer
   * @param generateOfferButton: button for printing an offer
   * @param container: sql container holding the data from the database
   * @param exportTableButton: button for exporting the grid as csv
   * @param validateOfferButton: button to validate offers
   */
  private void addListeners(Database db, ComboBox updateStatusComboBox, Button updateButton,
      Button deleteOfferButton, Button generateOfferButton, SQLContainer container,
      Button exportTableButton, Button validateOfferButton, Button proceed) {

    // several lists holding the package names, descriptions, prices, etc. for the current offer
    List<String> packageNames = qOfferManager.getPackageNames();
    List<String> packageDescriptions = qOfferManager.getPackageDescriptions();
    List<String> packageCounts = qOfferManager.getPackageCounts();
    List<String> packageUnitPrices = qOfferManager.getPackageUnitPrices();
    List<String> packageTotalPrices = qOfferManager.getPackageTotalPrices();
    List<String> packageIDs = qOfferManager.getPackageIDs();
    List<Integer> discounts = qOfferManager.getDiscounts();
    List<String> discountedPrices = qOfferManager.getDiscountedPrices();

    offerManagerGrid.addSelectionListener(selectionEvent -> {
      generateOfferButton.setEnabled(false);

      // Get selection from the selection model
      Object selected =
          ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow();

      if (selected != null) {
        // check if any of the packages in the current offer has no package_grp (e.g.
        // "Bioinformatics Analysis",
        // "Project Management", etc.) associated with it and display a warning for the user
        ArrayList<String> packageIdsWithoutPackageGroup = db.getPackageIdsWithoutPackageGroup(
            container.getItem(selected).getItemProperty("offer_id").getValue().toString());
        if (packageIdsWithoutPackageGroup.size() > 0) {
          String firstPackageName =
              db.getPackageNameFromPackageId(packageIdsWithoutPackageGroup.get(0));
          displayNotification("Package not associated to package group",
              "The package " + firstPackageName + " with id " + packageIdsWithoutPackageGroup.get(0)
                  + " has no package group associated with it. Please consider assigning the package to a "
                  + "package group.",
              "warning");
        }

        updateStatusComboBox.select(db.getOfferStatus(
            container.getItem(selected).getItemProperty("offer_id").getValue().toString()));

        Notification.show("Selected " + db.getOfferStatus(
            container.getItem(selected).getItemProperty("offer_id").getValue().toString()));

        detailsLayout.removeAllComponents();
        try {
          detailsLayout.addComponent(
              offerManagerTabPackageComponent.createOfferManagerTabPackageComponent(container,
                  container.getItem(selected).getItemProperty("offer_id").getValue().toString(),
                  "All"));

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
        } else if (updateStatusComboBox.getValue() == null) {
          displayNotification("oOps! Forgot something?!",
              "Please make sure that you select an option for status update.", "error");
        } else {
          // update the status of the offer (sent, accepted, declined, etc.)
          db.updateStatus(updateStatusComboBox.getValue().toString(),
              offerManagerGrid.getSelectedRow().toString());

          container.refresh();
        }
      }
    });

    deleteOfferButton.addClickListener((Button.ClickListener) event -> {

      Object selectedRow = offerManagerGrid.getSelectedRow();
      if (selectedRow == null) {
        displayNotification("No offer selected!", "Please select an offer to delete.", "error");
        return;
      }

      WindowFactory warning = new WindowFactory();
      deleteWarning =
          warning.setTitle("Delete this offer?").isModal(true).addButton(proceed).getWindow(false);
      Layout infoLayout = warning.getContentLayout();
      WindowFactory.addNotification("warn", "Do you really want to delete this offer?", infoLayout);

      UI.getCurrent().addWindow(deleteWarning);

    });

    proceed.addClickListener((Button.ClickListener) event -> {
      Object selectedRow = offerManagerGrid.getSelectedRow();

      int selectedOfferId = (int) offerManagerGrid.getContainerDataSource().getItem(selectedRow)
          .getItemProperty("offer_id").getValue();

      db.deleteOffer(selectedOfferId);

      // since refreshing the rows doesn't work properly; we force an update of the grid by setting
      // the sort direction
      // of the package name column
      offerManagerGrid.sort("offer_id", SortDirection.ASCENDING);

      displayNotification("Offer deleted",
          "Offer " + selectedOfferId + " " + "successfully deleted.", "success");

      deleteWarning.close();

    });


    packageGroupComboBox.addValueChangeListener((Property.ValueChangeListener) event -> {

      Object selected =
          ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow();

      if (selected == null) {
        displayNotification("No offer selected.", "Please select an offer to display", "error");
        return;
      }

      // e.g. "All", "Sequencing", or "Project Management"
      String selectedPackageGroup = packageGroupComboBox.getValue().toString();

      // change the view to display only the packages for the selected package group
      detailsLayout.removeAllComponents();
      try {
        detailsLayout.addComponent(
            offerManagerTabPackageComponent.createOfferManagerTabPackageComponent(container,
                container.getItem(selected).getItemProperty("offer_id").getValue().toString(),
                selectedPackageGroup));

      } catch (SQLException e) {
        e.printStackTrace();
      }
    });

    validateOfferButton.addClickListener(e -> {

      WindowFactory validation = new WindowFactory().isModal(true).setTitle("Validating Offer");
      validationWindow = validation.getWindow(true);
      notificationLayout = validation.getContentLayout(); // can only be returned when Window is
                                                          // returned and Content is created
      WindowFactory.addNotification("spin", "The offer is being validated please wait",
          notificationLayout);

      generateOfferButton.setEnabled(false);
      validateOfferButton.setEnabled(false);

      UI.getCurrent().setPollInterval(100);

      UI.getCurrent().addWindow(validationWindow);

      CompletableFuture.supplyAsync(() -> generateOfferFile(container, db, packageNames,
          packageDescriptions, packageCounts, packageUnitPrices, packageTotalPrices, packageIDs,
          discounts, discountedPrices, fileDownloader)).thenAcceptAsync(success -> {
            if (success) {
              UI.getCurrent().access(() -> generateOfferButton.setEnabled(true));
            }
            UI.getCurrent().access(() -> validateOfferButton.setEnabled(true));
            UI.getCurrent()
                .access(() -> validation.enableButton(validation.getCloseButton(), true));
            UI.getCurrent().setPollInterval(-1);
          });

    });

    try {
      setupTableExportFunctionality(container, exportTableButton);
    } catch (IOException e) {
      displayNotification("Whoops, something went wrong.",
          "A file could not be found, please try" + "again.", "error");
      e.printStackTrace();
    }
  }


  /**
   * adds the functionality of exporting the offer grid to the exportGridButton
   * 
   * @param container: SQLContainer holding the data
   * @param exportGridButton: button the export functionality should be added to
   * @throws IOException:
   */
  private void setupTableExportFunctionality(SQLContainer container, Button exportGridButton)
      throws IOException {
    // setup the export as .csv file functionality

    String timeStamp = TimeUtils.getCurrentTimestampString();
    String filePath = Paths.get(qOfferManager.tmpFolder, "offers_" + timeStamp + ".csv").toString();
    File tempFile = new File(filePath);

    // File tempFile = File.createTempFile("offers", ".csv");
    // String filePath = tempFile.getAbsolutePath();

    // set cache time to zero so that each time the download button is pressed the file is generated
    // new and no cached information
    // is saved (like quantity for table content
    FileResource resource = new FileResource(tempFile);
    resource.setCacheTime(1000);

    exportFileDownloader = new FileDownloader(resource) {
      @Override
      public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response,
          String path) throws IOException {
        createExportContent(container, filePath, exportFileDownloader);
        return super.handleConnectorRequest(request, response, path);
      }
    };
    exportFileDownloader.extend(exportGridButton);
  }

  /**
   * generates the .docx file for the offer
   * 
   * @param container: sql container holding the offers
   * @param db: database instance
   * @param packageNames: list of the package names in the offer
   * @param packageDescriptions: list of the package descriptions in the offer
   * @param packageCounts: list of the package counts in the offer
   * @param packageUnitPrices: list of the package unit prices (=price for one package)
   * @param packageTotalPrices: list of the total package prices (package_unit_price*count)*discount
   * @param fileDownloader: file downloader for enabling the download of the file
   * @return whether or not creating the file has worked
   * @throws IOException
   */
  private boolean generateOfferFile(SQLContainer container, Database db, List<String> packageNames,
      List<String> packageDescriptions, List<String> packageCounts, List<String> packageUnitPrices,
      List<String> packageTotalPrices, List<String> packageIDs, List<Integer> discounts,
      List<String> discountedPrices, FileDownloader fileDownloader) {
    if (offerManagerGrid.getSelectedRow() == null) {
      UI.getCurrent().access(() -> WindowFactory.addNotification("failure",
          "Please make sure that you select an offer.", notificationLayout));
      return false;
    }

    // since we take the package specific values from the grid showing the packages for the current
    // offers,
    // we need to check whether all packages are displayed or e.g. only the sequencing packages
    String selectedPackageGroup = packageGroupComboBox.getValue().toString();
    if (!selectedPackageGroup.equals("All")) {
      packageGroupComboBox.setValue("All");
    }

    // String basePath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
    // TODO for templates change files here:
    ClassLoader loader = OfferManagerTab.class.getClassLoader();
    // file holding the content controls for the bindings

    String contentControlFilename = loader.getResource("contentControlTemplate.xml").getPath();
    // String contentControlFilename = basePath +
    // "/WEB-INF/resourceFiles/contentControlTemplate.xml";
    // template .docx file containing the bindings
    String templateFileName =
        loader.getResource("YYYYMMDD_PiName_QXXXX_TEMPLATE_NEW_LOGO.docx").getPath();
    // String templateFileName = basePath +
    // "/WEB-INF/resourceFiles/YYYYMMDD_PiName_QXXXX_TEMPLATE_NEW_LOGO.docx";

    String clientName = container
        .getItem(
            ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow())
        .getItemProperty("offer_facility").getValue().toString();

    // be careful when testing for non-existent entries in the database. Some are null and others
    // are just empty strings!
    if (clientName == null | clientName.equals("NULL") | clientName.trim().equals("")) {
      UI.getCurrent().access(() -> WindowFactory.addNotification("failure",
          "The prospect field is empty thus no client can be found!", notificationLayout));
      return false;
    }

    String offerNumber = container
        .getItem(
            ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow())
        .getItemProperty("offer_number").getValue().toString();

    String estimatedDeliveryWeeks = null;
    if (container
        .getItem(
            ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow())
        .getItemProperty("estimated_delivery_weeks").getValue() != null) {
      estimatedDeliveryWeeks =
          container
              .getItem(((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel())
                  .getSelectedRow())
              .getItemProperty("estimated_delivery_weeks").getValue().toString() + " weeks";
    }

    UI.getCurrent().access(() -> WindowFactory.addNotification("warn",
        "Searching in database for client", notificationLayout));
    String[] address = db.getAddressForPerson(clientName);
    String groupAcronym = null;
    String institute = null;
    String umbrellaOrganization = null;
    String street = null;
    String cityZipCodeAndCounty = null;
    String zipCode = null;
    String city = null;
    String country = null;

    // deal with the potential missing values and display them in the notification window
    if (address.length == 1) {
      UI.getCurrent().access(() -> WindowFactory.addNotification("failure",
          "Database entry for address not found!", notificationLayout));
      return false;
    } else {
      groupAcronym = address[0];
      groupAcronym = checkAddressValidity(groupAcronym, "group");

      institute = address[1];
      institute = checkAddressValidity(institute, "institute");

      umbrellaOrganization = address[2];
      umbrellaOrganization = checkAddressValidity(umbrellaOrganization, "organization");

      street = address[3];
      street = checkAddressValidity(street, "street");

      zipCode = address[4];
      zipCode = checkAddressValidity(zipCode, "zip code");

      city = address[5];
      city = checkAddressValidity(city, "city");

      country = address[6];
      country = checkAddressValidity(country, "country");


      // e.g. D - 72076 Tübingen, Germany
      // TODO: country in english (database entry is in german..), postal code of country (is not in
      // the database)
      cityZipCodeAndCounty = zipCode + " " + city + ", " + country;
    }

    String projectReference = offerNumber.substring(offerNumber.indexOf('_') + 1).split("_")[0];

    // find the project manager based on the offer_id
    String offer_id = container
        .getItem(
            ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow())
        .getItemProperty("offer_id").getValue().toString();
    UI.getCurrent().access(() -> WindowFactory.addNotification("warn",
        "Searching the project manager in the database", notificationLayout));
    String personResult = db.getProjectManager(offer_id);
    String projectManager;
    String projectManagerMail;

    if (personResult != "no person found") {
      String[] projectManagerPerson = personResult.split(",");

      projectManager = projectManagerPerson[0] + " " + projectManagerPerson[1];
      projectManagerMail = projectManagerPerson[2];

    } else {
      projectManager = "Project manager not found";
      projectManagerMail = "Mail not found";
      UI.getCurrent()
          .access(() -> WindowFactory.addNotification("warn",
              "Project Manager entry not found in Database! "
                  + "You may want to change the information in the generated offer.",
              notificationLayout));
    }

    String projectID = container
        .getItem(
            ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow())
        .getItemProperty("offer_id").getValue().toString();
    if (projectID == null | projectID.equals("null") | projectID.trim().equals("")) {
      UI.getCurrent().access(() -> WindowFactory.addNotification("failure",
          "The offer ID for the current offer is null.", notificationLayout));

      // added to prevent fail if ID is null -> no download should be triggered
      return false;
    }

    String projectTitle = container
        .getItem(
            ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow())
        .getItemProperty("offer_name").getValue().toString();

    if (projectTitle == null | projectTitle.equals("null") | projectTitle.trim().equals("")) {
      UI.getCurrent().access(() -> WindowFactory.addNotification("failure",
          "The offer name for the current offer is null.", notificationLayout));
      // added to prevent fail if titel is null -> no download should be triggered
      return false;
    }

    Object projectDescriptionObject = container
        .getItem(
            ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow())
        .getItemProperty("offer_description").getValue();
    String projectDescription =
        projectDescriptionObject == null ? null : projectDescriptionObject.toString();

    if (projectDescription == null | projectDescription.equals("null")
        | projectDescription.trim().equals("")) {
      UI.getCurrent().access(() -> WindowFactory.addNotification("failure",
          "The offer description for the current offer is null.", notificationLayout));
      // added to prevent fail if description is null -> no download should be triggered
      return false;
    }


    String totalVal = container
        .getItem(
            ((Grid.SingleSelectionModel) offerManagerGrid.getSelectionModel()).getSelectedRow())
        .getItemProperty("offer_total").getValue().toString();

    // DecimalFormat offerPriceFormatter = new DecimalFormat("###,###.###");
    // String offerTotal =
    // offerPriceFormatter
    // .format(totalVal);

    String clientSurname = clientName.split(" ")[clientName.split(" ").length - 1];
    String dateToday = new SimpleDateFormat("yyyyMMdd").format(new Date());
    String projectQuotationNumber = dateToday + "_" + clientSurname + "_" + projectReference;

    SimpleDateFormat currentDateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH);
    String currentDate = currentDateFormat.format(new Date());

    // get the xml document holding the content for the bindings in the docx template file
    org.w3c.dom.Document contentControlDocument = readXMLFile(contentControlFilename);
    // change the fields of the content control document according to the values obtained in the
    // grid
    changeNodeTextContent(contentControlDocument, "client_name", clientName);
    changeNodeTextContent(contentControlDocument, "client_organization", groupAcronym);
    changeNodeTextContent(contentControlDocument, "client_department", institute);
    changeNodeTextContent(contentControlDocument, "client_university", umbrellaOrganization);
    changeNodeTextContent(contentControlDocument, "client_address", street);
    changeNodeTextContent(contentControlDocument, "client_town", cityZipCodeAndCounty);
    // changeNodeTextContent(contentControlDocument, "client_email", clientEmail);
    changeNodeTextContent(contentControlDocument, "project_reference", projectReference);
    changeNodeTextContent(contentControlDocument, "quotation_number", projectQuotationNumber);
    changeNodeTextContent(contentControlDocument, "name", projectManager);
    changeNodeTextContent(contentControlDocument, "email", projectManagerMail);
    changeNodeTextContent(contentControlDocument, "project_title", projectTitle);
    changeNodeTextContent(contentControlDocument, "objective", projectDescription);
    changeNodeTextContent(contentControlDocument, "estimated_total", formatCurrency(totalVal));
    changeNodeTextContent(contentControlDocument, "date", currentDate);

    if (estimatedDeliveryWeeks != null) {
      changeNodeTextContent(contentControlDocument, "delivery_time",
          "Approx. " + estimatedDeliveryWeeks + " upon data retrieval.");
    } else {
      // the default value will be written
      // but give a warning!
      UI.getCurrent()
          .access(() -> WindowFactory.addNotification("warn",
              "The estimated delivery time is not entered and thus will be set to the default value.",
              notificationLayout));
    }

    // iterate over the packages and add them to the content control .xml file
    for (int i = packageNames.size() - 1; i >= 0; i--) {
      addRowToTable(contentControlDocument, 1, packageIDs.get(i),
          packageNames.get(i) + ": " + packageDescriptions.get(i), packageCounts.get(i),
          formatCurrency(packageUnitPrices.get(i)), formatCurrency(packageTotalPrices.get(i)),
          discounts.get(i), formatCurrency(discountedPrices.get(i)));
    }

    // remove the placeholder rows in the .xml file
    removeRowInTable(contentControlDocument, packageNames.size());

    if (contentControlDocument.getDoctype() != null) {
      throw new NullPointerException();
    }

    // apply the bindings to the .docx template file
    WordprocessingMLPackage wordProcessor =
        Docx4jUtils.applyBindings(contentControlDocument, templateFileName); // TODO error here!

    // save updated document to output file
    try {
      // File tempFile = File.createTempFile(projectQuotationNumber, ".docx");

      String timeStamp = TimeUtils.getCurrentTimestampString();
      String filePath = Paths
          .get(qOfferManager.tmpFolder, projectQuotationNumber + timeStamp + ".docx").toString();
      File tempFile = new File(filePath);

      assert wordProcessor != null;
      wordProcessor.save(tempFile, Docx4J.FLAG_SAVE_ZIP_FILE);

      LOG.info("SAVE FILE: done saving the File");
      StreamResource sr = new StreamResource(new StreamResource.StreamSource() {
        @Override
        public InputStream getStream() {
          try {
            return new FileInputStream(tempFile);
          } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not save offer", e);
          }
        }
      }, projectQuotationNumber + ".docx");

      // also set the cache time of the Resource of the actually downloaded file to zero to obtain
      // the newly generated file
      sr.setCacheTime(1000);

      fileDownloader.setFileDownloadResource(sr);

      UI.getCurrent().access(() -> WindowFactory.addNotification("success",
          "File is ready to download.", notificationLayout));

      return true;

    } catch (Docx4JException e) {
      UI.getCurrent().access(() -> WindowFactory.addNotification("failure",
          "Could not generate offer file", notificationLayout));
      throw new RuntimeException("Could not generate offer file", e);
    }
  }

  /**
   * Enable the manipulation of the enabled-status of the generateOfferButton
   * 
   * @param enable
   */
  public void setEnableGenerateButton(boolean enable) {
    generateOfferButton.setEnabled(enable);
  }

  private String checkAddressValidity(String address, String type) {
    if (address == null | address.equals("") | address.equals(" ")) {
      UI.getCurrent()
          .access(() -> WindowFactory.addNotification("failure",
              "Database entry for " + type + " is empty check the address in the downloaded offer!",
              notificationLayout));
      return " ";
    }
    return address;
  }

  public void refreshOffersContainer() {
    offersContainer.refresh();
  }

}
