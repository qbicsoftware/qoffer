/*******************************************************************************
 * QBiC Offer Generator provides an infrastructure for creating offers using QBiC portal and
 * infrastructure. Copyright (C) 2017 Aydın Can Polatkan
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

package com.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.docx4j.Docx4J;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.ProtectDocument;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.STDocProtect;

import com.dbase.DBManager;
import com.dbase.Database;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.shared.Position;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.Grid.SingleSelectionModel;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

public class packageManager extends CustomComponent {

  final GridLayout gridLayout = new GridLayout(6, 6);
  Grid selectedPacksInOfferGrid = new Grid();

  private static Database db;
  private String[] values;
  private String descriptionText;
  private float totalPrice;
  private float packageUnitPrice;
  private final TabSheet managerTabs = new TabSheet();

  Date dNow = new Date();
  SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
  SimpleDateFormat offerNumber = new SimpleDateFormat("yyyyMMdd");

  String dateToday = offerNumber.format(dNow);
  String queryFreeform =
      "SELECT * FROM z_offers INNER JOIN z_offers_packages ON z_offers.`offer_id` = z_offers_packages.`offer_id` INNER JOIN z_packages ON z_packages.`package_id` = z_offers_packages.`package_id` WHERE z_offers.offer_id = 8";

  /**
   * 
   */
  private static final long serialVersionUID = -3847280734052653158L;

  public packageManager() {

    init();

  }

  private void init() {

    DBManager.getDatabaseInstance();
    db = Database.Instance;

    System.out.println(ft.format(dNow) + "  INFO  Package Manager accessed! - User: "
        + LiferayAndVaadinUtils.getUser().getScreenName());

    // final TabSheet managerTabs = new TabSheet();

    managerTabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
    managerTabs.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);

    // selectUser.addItems(DBManager.getDatabaseInstance().getPackages());

    try {

      managerTabs.addTab(addToOffer(), "Offer Generator");
      managerTabs.addTab(offerGrid(), "Offer Manager");
      managerTabs.addTab(packageGrid(), "Package Manager");

    } catch (SQLException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    try {
      gridLayout.addComponent(managerTabs, 0, 1, 5, 1);
      // gridLayout.addComponent(selectUser, 0, 2, 5, 2);
    } catch (OverlapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (OutOfBoundsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    gridLayout.setSizeFull();
    setCompositionRoot(gridLayout);

  }

  // private Component packageGrid(String dateStart, String dateEnd) {
  private Component packageGrid() throws SQLException {

    VerticalLayout packManLayout = new VerticalLayout();
    // packManLayout.setCaption("Package Manager");

    String buttonTitle = "Refresh";
    Button refresh = new Button(buttonTitle);
    refresh.setDescription("Click here to reload the data from the database!");

    String addPackButtonTitle = "New Package";
    Button addPackButton = new Button(addPackButtonTitle);
    addPackButton.setIcon(FontAwesome.PLUS);
    addPackButton
        .setDescription("Click here to add a new package but don't forget to update the details");

    packManLayout.setMargin(true);
    packManLayout.setSpacing(true);
    packManLayout.setSizeFull();

    TableQuery tq = new TableQuery("z_packages", DBManager.getDatabaseInstanceAlternative());
    tq.setVersionColumn("OPTLOCK");
    SQLContainer container = new SQLContainer(tq);

    container.setAutoCommit(isEnabled());

    Grid packageGrid = new Grid(container);

    /*
     * packageGrid.setColumnOrder("user_id", "user_ldap", "user_name", "email", "phone",
     * "workgroup_id", "group_id", "kostenstelle", "project", "admin_panel");
     * packageGrid.sort("user_name", SortDirection.ASCENDING);
     * 
     * // usersGrid.removeColumn("workgroup_id"); packageGrid.removeColumn("group_id");
     * packageGrid.removeColumn("admin_panel");
     */

    addPackButton.addClickListener(new ClickListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 8181926819540586585L;

      @Override
      public void buttonClick(ClickEvent event) {
        DBManager.getDatabaseInstance().addNewPack("*** New Package - double click to edit ***");
        Notification(
            "New Package Added",
            "Please remind to edit the package details! If the details are not complete incompatibility issues may expected to happen.",
            "success");
        packageGrid.clearSortOrder();
      }

    });

    // packageGrid.getColumn("package_id").setHeaderCaption("ID");
    packageGrid.getColumn("package_name").setHeaderCaption("Name");
    packageGrid.getColumn("package_facility").setHeaderCaption("Facility");
    packageGrid.getColumn("package_description").setHeaderCaption("Description").setWidth(300);
    packageGrid.getColumn("package_group").setHeaderCaption("Group");
    packageGrid.getColumn("package_price").setHeaderCaption("Internal Price (€)");
    packageGrid.getColumn("package_price_external").setHeaderCaption("External Price (€)");
    packageGrid.getColumn("package_unit_type").setHeaderCaption("Unit Type");
    packageGrid.getColumn("package_date").setHeaderCaption("Date");

    packageGrid.removeColumn("last_edited");
    packageGrid.removeColumn("package_id");

    packageGrid.sort("package_name", SortDirection.ASCENDING);

    packageGrid.setWidth("100%");
    packageGrid.setSelectionMode(SelectionMode.SINGLE);
    packageGrid.setEditorEnabled(true);

    packManLayout.addComponent(packageGrid);
    packManLayout.addComponent(addPackButton);
    // packManLayout.addComponent(refresh);

    return packManLayout;

  }

  // private Component packageGrid(String dateStart, String dateEnd) {

  private Component addToOffer() {

    /*
     * ComboBox selectedUser = new ComboBox("Prospect");
     * selectedUser.setInputPrompt("No user selected!");
     * selectedUser.setDescription("Please select a user or a project");
     * selectedUser.addItems(db.getUsernames());
     */

    ComboBox selectedProject = new ComboBox("Select Project");

    selectedProject.setInputPrompt("No project selected!");
    selectedProject.setDescription("Please select a project before its too late! :P");
    selectedProject.addItems(db.getProjects());
    selectedProject.setWidth("300px");

    VerticalLayout addPackLayout = new VerticalLayout();
    // packManLayout.setCaption("Package Manager");

    String buttonTitle = "Complete";
    Button complete = new Button(buttonTitle);
    complete.setDescription("Click here to finalize the offer and save it into the DB!");
    complete.setIcon(FontAwesome.CHECK_CIRCLE);
    complete.setEnabled(false);

    addPackLayout.setMargin(true);
    addPackLayout.setSpacing(true);

    // container.setAutoCommit(isEnabled());

    CheckBox externalPriceSelected = new CheckBox("Internal Price", true);
    externalPriceSelected.setDescription("Please uncheck to add a package with an external price!");
    externalPriceSelected.addStyleName(ValoTheme.CHECKBOX_LARGE);

    TwinColSelect selectPackages = new TwinColSelect();

    selectPackages.addItems(db.getPackageNames());
    selectPackages.setSizeFull();
    selectPackages.setNullSelectionAllowed(true);
    selectPackages.setMultiSelect(true);
    selectPackages.setImmediate(true);
    selectPackages.setLeftColumnCaption("Available Packages");
    selectPackages.setRightColumnCaption("Selected Packages");

    Panel packageDescriptionPanel = new Panel("Package Details");

    VerticalLayout right = new VerticalLayout();
    right.setSpacing(true);
    right.setMargin(true);
    packageDescriptionPanel.setContent(right);

    @SuppressWarnings("deprecation")
    Label label = new Label("Package details will appear here!", Label.CONTENT_XHTML);
    label.addStyleName(ValoTheme.LABEL_BOLD);
    right.addComponent(label);

    // selectPackages.addValueChangeListener(e -> Notification.show("Contained Packages:",
    // String.valueOf(e.getProperty().getValue()), Type.TRAY_NOTIFICATION));

    selectedProject.addValueChangeListener(new ValueChangeListener() {

      /**
       * 
       */
      private static final long serialVersionUID = 6871999698387032993L;

      @Override
      public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

        if (selectedProject.equals(null)) {
          Notification(
              "oO! No project selected!",
              "Please select a project to proceed, otherwise we are going to have a situation here! ;)",
              "error");
        } else {
          addPackLayout.addComponent(selectPackages);
          addPackLayout.addComponent(externalPriceSelected);
          addPackLayout.addComponent(packageDescriptionPanel);
          addPackLayout.addComponent(complete);
        }

      }

    });

    selectPackages.addValueChangeListener(new ValueChangeListener() {

      String cvsSplitBy = ", ";

      ArrayList<String> selectedPacksArray = new ArrayList<String>();

      /**
       * 
       */
      private static final long serialVersionUID = -5813954665588509117L;

      @Override
      public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

        selectedPacksArray.add(selectPackages.getValue().toString());

        values = selectPackages.getValue().toString().split(cvsSplitBy);

        if (!selectedProject.getValue().equals(null))
          complete.setEnabled(true);

        descriptionText = "";
        totalPrice = 0;

        for (int i = 0; i < values.length; i++) {

          values[i] = values[i].replace("[", "");
          values[i] = values[i].replace("]", "");

          // System.out.println(i + " " + values[i]);

          descriptionText =
              descriptionText
                  + "<tr><td><p><b>"
                  + values[i]
                  + "</b><br>"
                  + db.getPackDescriptionFromPackName(values[i].toString())
                  + "</td><td align='right' valign='top'>€"
                  + db.getPriceInfoFromPackageName(values[i].toString(),
                      externalPriceSelected.getValue()) + "</td>" + "</p></tr>";

          totalPrice =
              totalPrice
                  + db.getPriceInfoFromPackageName(values[i].toString(),
                      externalPriceSelected.getValue());

          if (values[i].length() != 0)
            label
                .setValue("<table width='100%'><tr><td><p style='color:red;'><b>Package Name and Description</b></p></td><td align='right'><p style='color:red;'><b>Price</b></p></td></tr><tr> </tr>"
                    + descriptionText
                    + "<tr><td><p style='color:red;'><b>Grand Total</b> (excl. Taxes)</p></td><td align='right'><p style='color:red;'><b>€"
                    + totalPrice + "</b></p></td></tr></table>");
          else
            label.setValue("No description available!");
        }

      }

    });

    complete.addClickListener(new ClickListener() {

      int packageId = 0;
      int offerId = 0;
      String offerNumber = "N/A";
      String offerProjectReference = "N/A";
      String offerFacility = "Test";
      String offerName = "N/A";
      String offerDescription = "currently not available in the userDB";
      // TODO take the offer ID value autonomous

      /**
       * 
       */
      private static final long serialVersionUID = 8181926819540586585L;

      @Override
      public void buttonClick(ClickEvent event) {

        // offerProjectReference = selectedProject.getValue().toString().replaceAll("/.*?/", "");
        offerProjectReference = selectedProject.getValue().toString();
        offerNumber = dateToday + "_" + offerProjectReference;

        offerFacility = db.getPIFromProjectRef(offerProjectReference);
        offerName = db.getShortTitleFromProjectRef(offerProjectReference);
        offerDescription = db.getLongDescFromProjectRef(offerProjectReference);

        System.out.println("offerProjectReference: " + offerProjectReference + " offerName: "
            + offerName + " offerDescription: " + offerDescription);

        offerId =
            db.registerNewOffer(offerNumber, offerProjectReference, offerFacility, offerName,
                offerDescription, totalPrice, dNow,
                LiferayAndVaadinUtils.getUser().getScreenName(), externalPriceSelected.getValue());

        if (offerId == 0) {
          Notification(
              "oops! This offer already exits!",
              "Please take a good look at the grid below, you will find the existing offer. Feel free to edit it.",
              "error");
          managerTabs.setSelectedTab(1);

        } else {

          for (int i = 0; i < values.length; i++) {
            packageId = db.getPackIDFromPackName(values[i]);
            packageUnitPrice =
                db.getPriceInfoFromPackageName(values[i].toString(),
                    externalPriceSelected.getValue());
            System.out.println(i + "Package Unit Price: " + packageUnitPrice);
            db.insertOrUpdateOffersPackages(offerId, packageId, packageUnitPrice);
            // System.out.println("PackageID: " + packageId + "OfferID: " + offerId);
            // System.out.println(i + " " + values[i]);
          }

          Notification(
              "Perfect! Offer succesfully saved in the DB!",
              "Next step is to modify, finalize and send it to the customer. Keep in mind that the description of the offer is still missing. Please go ahead and complete it. Fingers crossed!",
              "success");
          managerTabs.setSelectedTab(1);
        }
      }

    });

    // addPackLayout.addComponent(selectedUser);
    addPackLayout.addComponent(selectedProject);

    return addPackLayout;

  }

  private Component packageQuantityGrid(String queryEnd) throws SQLException {

    VerticalLayout packQuantityLayout = new VerticalLayout();
    HorizontalLayout packSettingsLayout = new HorizontalLayout();

    packQuantityLayout.setMargin(true);
    packQuantityLayout.setSpacing(true);
    packQuantityLayout.setSizeFull();

    ComboBox addQuantity = new ComboBox("Select Quantity");
    ComboBox updateQuantityDiscount = new ComboBox("Select Discount");

    updateQuantityDiscount.addItem("0%");
    updateQuantityDiscount.addItem("10%");
    updateQuantityDiscount.addItem("20%");
    updateQuantityDiscount.addItem("30%");
    updateQuantityDiscount.addItem("40%");
    updateQuantityDiscount.addItem("50%");

    addQuantity.addItem("1");
    addQuantity.addItem("2");
    addQuantity.addItem("3");
    addQuantity.addItem("4");
    addQuantity.addItem("5");
    addQuantity.addItem("6");
    addQuantity.addItem("7");
    addQuantity.addItem("8");
    addQuantity.addItem("9");
    addQuantity.addItem("10");
    addQuantity.addItem("11");
    addQuantity.addItem("12");

    Button updateQuantityButton = new Button("Update");
    updateQuantityButton.setIcon(FontAwesome.SPINNER);

    packSettingsLayout.addComponent(addQuantity);
    packSettingsLayout.addComponent(updateQuantityDiscount);
    packSettingsLayout.addComponent(updateQuantityButton);

    packSettingsLayout.setComponentAlignment(updateQuantityButton, Alignment.BOTTOM_CENTER);

    packSettingsLayout.setSpacing(true);

    queryFreeform =
        "SELECT * FROM z_offers INNER JOIN z_offers_packages ON z_offers.`offer_id` = z_offers_packages.`offer_id` INNER JOIN z_packages ON z_packages.`package_id` = z_offers_packages.`package_id` WHERE z_offers.offer_id = "
            + queryEnd;

    FreeformQuery query =
        new FreeformQuery(queryFreeform, DBManager.getDatabaseInstanceAlternative(), "package_id");
    SQLContainer packsContainer = new SQLContainer(query);

    // System.out.println("Print Container: " + container.size());
    packsContainer.setAutoCommit(isEnabled());

    selectedPacksInOfferGrid = new Grid(packsContainer);

    // Create a grid bound to it

    selectedPacksInOfferGrid.addSelectionListener(new SelectionListener() {

      /**
       * 
       */
      private static final long serialVersionUID = -1061272471352530723L;

      @Override
      public void select(SelectionEvent event) {

        addQuantity.select(db.getPackageCount(queryEnd, selectedPacksInOfferGrid.getSelectedRow()
            .toString()));

        updateQuantityDiscount.select(db.getPackageDiscount(queryEnd, selectedPacksInOfferGrid
            .getSelectedRow().toString()));

      }

    });

    updateQuantityButton.addClickListener(new ClickListener() {

      /**
         * 
         */
      private static final long serialVersionUID = 8910018717791341602L;

      @Override
      public void buttonClick(ClickEvent event) {

        // System.out.println("ValueStatus: " + updateStatus.getValue() + " Discount "
        // + updateDiscount.getValue());

        if (addQuantity.getValue() == null || updateQuantityDiscount.getValue() == null) {

          Notification("oOps! Forgot something?!",
              "Please make sure that you select an option for status and discount update.", "error");

        } else {

          System.out.println("Whats going inside Package Details? ");

          float percentage =
              Integer
                  .parseInt(updateQuantityDiscount.getValue().toString().trim().replace("%", ""));

          System.out.println("Dropdown: " + addQuantity.getValue().toString() + " offer id: "
              + queryEnd + "package id: " + selectedPacksInOfferGrid.getSelectedRow().toString());

          db.updateQuantityQuery(addQuantity.getValue().toString(), queryEnd,
              selectedPacksInOfferGrid.getSelectedRow().toString(), db.internalOfferCheck(queryEnd));

          // TODO: Query Internal & External
          // container.getItem(selected).getItemProperty("offer_id").toString());
          // offerGrid.getContainerDataSource().getItem(selected).getItemProperty("offer_id").toString());

          db.updateQuantityDiscountQuery(updateQuantityDiscount.getValue().toString(), queryEnd,
              selectedPacksInOfferGrid.getSelectedRow().toString());

          packsContainer.refresh();


        }
      }
    });

    selectedPacksInOfferGrid.removeColumn("offer_id");
    selectedPacksInOfferGrid.removeColumn("offer_number");
    selectedPacksInOfferGrid.removeColumn("offer_project_reference");
    selectedPacksInOfferGrid.removeColumn("offer_facility");
    selectedPacksInOfferGrid.removeColumn("offer_name");
    selectedPacksInOfferGrid.removeColumn("offer_description");
    selectedPacksInOfferGrid.removeColumn("offer_group");
    selectedPacksInOfferGrid.removeColumn("offer_price");
    selectedPacksInOfferGrid.removeColumn("offer_extra_price");
    selectedPacksInOfferGrid.removeColumn("offer_total");
    selectedPacksInOfferGrid.removeColumn("offer_date");
    selectedPacksInOfferGrid.removeColumn("offer_status");
    selectedPacksInOfferGrid.removeColumn("last_edited");
    selectedPacksInOfferGrid.removeColumn("added_by");
    selectedPacksInOfferGrid.removeColumn("package_date");
    selectedPacksInOfferGrid.removeColumn("package_id");
    selectedPacksInOfferGrid.removeColumn("discount");


    selectedPacksInOfferGrid.getColumn("package_addon_price").setHeaderCaption(
        "Package Add-On Price (€)");
    selectedPacksInOfferGrid.getColumn("package_count").setHeaderCaption("Unit Count");
    selectedPacksInOfferGrid.getColumn("package_discount").setHeaderCaption("Discount");
    selectedPacksInOfferGrid.getColumn("package_name").setHeaderCaption("Package Name")
        .setWidth(200);
    selectedPacksInOfferGrid.getColumn("package_facility").setHeaderCaption("Package Facility");
    selectedPacksInOfferGrid.getColumn("package_description").setHeaderCaption("Description")
        .setWidth(300);
    selectedPacksInOfferGrid.getColumn("package_group").setHeaderCaption("Group");
    selectedPacksInOfferGrid.getColumn("package_price").setHeaderCaption("Internal Price (€)");
    selectedPacksInOfferGrid.getColumn("package_price_external").setHeaderCaption(
        "External Price (€)");
    selectedPacksInOfferGrid.getColumn("package_unit_type").setHeaderCaption("Unit Type");

    selectedPacksInOfferGrid.setEditorEnabled(false);
    selectedPacksInOfferGrid.setSelectionMode(SelectionMode.SINGLE);
    selectedPacksInOfferGrid.setWidth("100%");
    // selectedPacksInOfferGrid.setEditorEnabled(false);
    // selectedPacksInOfferGrid.setHeight("300px");

    packQuantityLayout.addComponent(selectedPacksInOfferGrid);
    packQuantityLayout.addComponent(packSettingsLayout);

    return packQuantityLayout;

  }

  // private Component packageGrid(String dateStart, String dateEnd) {
  private Component offerGrid() throws SQLException {

    VerticalLayout offerManLayout = new VerticalLayout();
    // packManLayout.setCaption("Package Manager");
    HorizontalLayout editSettingsLayout = new HorizontalLayout();
    HorizontalLayout detailsLayout = new HorizontalLayout();

    // detailsLayout.setMargin(true);
    // detailsLayout.setSizeFull();
    // detailsLayout.setSpacing(true);

    // editSettingsLayout.setMargin(true);
    // editSettingsLayout.setSizeFull();
    editSettingsLayout.setSpacing(true);
    detailsLayout.setSizeFull();

    /*
     * Panel packageDescriptionPanel = new Panel("Package Details");
     * 
     * VerticalLayout right = new VerticalLayout(); right.setSpacing(true); right.setMargin(true);
     * packageDescriptionPanel.setContent(right);
     * 
     * @SuppressWarnings("deprecation") Label label = new Label("Package details will appear here!",
     * Label.CONTENT_XHTML); label.addStyleName(ValoTheme.LABEL_BOLD); right.addComponent(label);
     * 
     * 
     * TextField packName = new TextField(""); TextField packDescription = new TextField("");
     * TextField packUnits = new TextField(""); TextField packPrice = new TextField("");
     * 
     * packName.setInputPrompt("N/A"); packDescription.setInputPrompt("N/A");
     * packUnits.setInputPrompt("N/A"); packPrice.setInputPrompt("N/A");
     * 
     * packName.setStyleName(ValoTheme.TEXTFIELD_LARGE);
     * packDescription.setStyleName(ValoTheme.TEXTFIELD_LARGE);
     * packUnits.setStyleName(ValoTheme.TEXTFIELD_LARGE);
     * packPrice.setStyleName(ValoTheme.TEXTFIELD_LARGE);
     * 
     * packDescription.setWidth("300px");
     * 
     * packName.setReadOnly(true); packDescription.setReadOnly(true);
     * 
     * detailsLayout.addComponent(packName); detailsLayout.addComponent(packDescription);
     * detailsLayout.addComponent(packUnits); detailsLayout.addComponent(packPrice);
     */

    ComboBox updateStatus = new ComboBox("Select Status");
    ComboBox updateDiscount = new ComboBox("Select Discount");

    Button updateButton = new Button("Update");
    updateButton.setIcon(FontAwesome.SPINNER);

    updateStatus.addItem("In Progress");
    updateStatus.addItem("Sent");
    updateStatus.addItem("Accepted");
    updateStatus.addItem("Rejected");

    updateDiscount.addItem("0%");
    updateDiscount.addItem("10%");
    updateDiscount.addItem("20%");
    updateDiscount.addItem("30%");
    updateDiscount.addItem("40%");

    editSettingsLayout.addComponent(updateStatus);
    editSettingsLayout.addComponent(updateDiscount);
    editSettingsLayout.addComponent(updateButton);

    editSettingsLayout.setComponentAlignment(updateButton, Alignment.BOTTOM_CENTER);

    Button refresh = new Button("Refresh");
    refresh.setDescription("Click here to reload the data from the database!");

    String printOfferButtonTitle = "Print Offer";
    Button printOfferButton = new Button(printOfferButtonTitle);
    printOfferButton.setIcon(FontAwesome.PRINT);
    printOfferButton.setDescription("Select an offer from the grid then click here to modify!");

    offerManLayout.setMargin(true);
    offerManLayout.setSpacing(true);
    offerManLayout.setSizeFull();

    TableQuery tq = new TableQuery("z_offers", DBManager.getDatabaseInstanceAlternative());
    tq.setVersionColumn("OPTLOCK");
    SQLContainer container = new SQLContainer(tq);

    container.setAutoCommit(isEnabled());

    Grid offerGrid = new Grid(container);

    /*
     * packageGrid.setColumnOrder("user_id", "user_ldap", "user_name", "email", "phone",
     * "workgroup_id", "group_id", "kostenstelle", "project", "admin_panel");
     * packageGrid.sort("user_name", SortDirection.ASCENDING);
     * 
     * // usersGrid.removeColumn("workgroup_id"); packageGrid.removeColumn("group_id");
     * packageGrid.removeColumn("admin_panel");
     */

    offerGrid.addSelectionListener(selectionEvent -> { // Java 8
          // Get selection from the selection model
          Object selected = ((SingleSelectionModel) offerGrid.getSelectionModel()).getSelectedRow();

          if (selected != null) {

            updateStatus.select(db.getOfferStatus(container.getItem(selected)
                .getItemProperty("offer_id").toString()));

            updateDiscount.select(db.getOfferDiscount(container.getItem(selected)
                .getItemProperty("offer_id").toString()));

            // offerGrid.getContainerDataSource().getItem(selected).getItemProperty("offer_id").toString()));
            // updateDiscount.select(db.getOfferDiscount(offerGrid.getContainerDataSource().getItem(selected).getItemProperty("offer_id").toString()));

            Notification.show("Selected "
                + db.getOfferStatus(container.getItem(selected).getItemProperty("offer_id")
                    .toString()));

            try {
              detailsLayout.removeAllComponents();
              detailsLayout.addComponent(packageQuantityGrid(container.getItem(selected)
                  .getItemProperty("offer_id").toString()));
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }

          updateButton.addClickListener(new ClickListener() {

            /**
             * 
             */
            private static final long serialVersionUID = 8910018717791341602L;

            @Override
            public void buttonClick(ClickEvent event) {

              // System.out.println("ValueStatus: " + updateStatus.getValue() + " Discount "
              // + updateDiscount.getValue());

              if (updateStatus.getValue() == null || updateDiscount.getValue() == null) {

                Notification("oOps! Forgot something?!",
                    "Please make sure that you select an option for status and discount update.",
                    "error");

              } else {

                // System.out.println("Selected Row: " + offerGrid.getSelectedRow().toString());
                // db.updateStatus(updateStatus.getValue().toString(), offerGrid.getSelectedRow()
                // .toString());
                // container.getItem(selected).getItemProperty("offer_id").toString());
                // offerGrid.getContainerDataSource().getItem(selected).getItemProperty("offer_id").toString());
                System.out.println("Whats going on here?");
                float percentage =
                    Integer.parseInt(updateDiscount.getValue().toString().trim().replace("%", ""));

                db.updateDiscount(updateDiscount.getValue().toString(), offerGrid.getSelectedRow()
                    .toString(), percentage);
                // container.getItem(selected).getItemProperty("offer_id").toString());
                // offerGrid.getContainerDataSource().getItem(selected).getItemProperty("offer_id").toString());

                // TODO: Set Status

                container.refresh();

              }
            }
          });


          printOfferButton.addClickListener(new ClickListener() {


            /**
             * 
             */
            private static final long serialVersionUID = 9170993096605292649L;

            @Override
            public void buttonClick(ClickEvent event) {

              Notification("Test", "Test", "");

              WordprocessingMLPackage wordMLPackage = null;
              try {
                wordMLPackage = WordprocessingMLPackage.createPackage();
              } catch (InvalidFormatException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
              MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

              String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

              // Image as a file resource
              File resource = new File(basepath + "/WEB-INF/images/header.png");

              java.io.InputStream is = null;
              try {
                is = new java.io.FileInputStream(resource);
              } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
              long length = resource.length();
              // You cannot create an array using a long type.
              // It needs to be an int type.
              if (length > Integer.MAX_VALUE) {
                System.out.println("File too large!!");
              }
              byte[] bytes = new byte[(int) length];
              int offset = 0;
              int numRead = 0;
              try {
                while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                  offset += numRead;
                }
              } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }
              // Ensure all the bytes have been read in
              if (offset < bytes.length) {
                System.out.println("Could not completely read file " + resource.getName());
              }
              try {
                is.close();
              } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
              }

              String filenameHint = null;
              String altText = null;
              int id1 = 0;
              int id2 = 1;

              // Image 1: no width specified
              org.docx4j.wml.P p;
              try {
                p = newImage(wordMLPackage, bytes, filenameHint, altText, id1, id2);
                wordMLPackage.getMainDocumentPart().addObject(p);
              } catch (Exception e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
              }

              /*
               * // Image 2: width 3000 org.docx4j.wml.P p2 = newImage(wordMLPackage, bytes,
               * filenameHint, altText, id1, id2, 3000);
               * wordMLPackage.getMainDocumentPart().addObject(p2);
               * 
               * // Image 3: width 6000 org.docx4j.wml.P p3 = newImage(wordMLPackage, bytes,
               * filenameHint, altText, id1, id2, 6000);
               * wordMLPackage.getMainDocumentPart().addObject(p3);
               */

              // Now save it
              /*
               * try { wordMLPackage.save(new java.io.File(System.getProperty("user.dir") +
               * "/OUT_AddImage.docx")); } catch (Docx4JException e1) { // TODO Auto-generated catch
               * block e1.printStackTrace(); }
               */

              mdp.addParagraphOfText("hello world");


              ProtectDocument protection = new ProtectDocument(wordMLPackage);
              protection.restrictEditing(STDocProtect.READ_ONLY, "foobaa");


              String filename = System.getProperty("user.dir") + "/offer.docx";
              try {
                Docx4J.save(wordMLPackage, new java.io.File(filename), Docx4J.FLAG_SAVE_ZIP_FILE);
              } catch (Docx4JException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }

              // To save encrypted, you'd use FLAG_SAVE_ENCRYPTED_AGILE, for example:
              // Docx4J.save(wordMLPackage, new java.io.File(filename),
              // Docx4J.FLAG_SAVE_ENCRYPTED_AGILE, "foo");


              System.out.println("Saved " + filename);

              /*
               * factory = Context.getWmlObjectFactory(); docxHelper = new Docx4jHelper(); try {
               * wordMLPackage = WordprocessingMLPackage.createPackage(); } catch
               * (InvalidFormatException e) { // TODO Auto-generated catch block
               * e.printStackTrace(); } //paragraphs P p =
               * docxHelper.createParagraph("Summary for project " + projectCode, true, false,
               * "40"); wordMLPackage.getMainDocumentPart().addObject(p); P p1 =
               * docxHelper.createParagraph(expHeadline, true, false, "32");
               * mainDocumentPart.addObject(p1);
               * 
               * //table
               * wordMLPackage.getMainDocumentPart().addObject(docxHelper.createTableWithContent
               * (header, data)); wordMLPackage.getMainDocumentPart().addObject(new Br());
               * 
               * //save file String docxPath = tmpFolder + projectCode + "_" + createTimeStamp() +
               * ".docx"; try { wordMLPackage.save(new java.io.File(docxPath)); } catch
               * (Docx4JException e) { // TODO Auto-generated catch block e.printStackTrace();
               */
              // writeDocx();
            }
          });
        });

    /*
     * updateDiscount.addValueChangeListener(new ValueChangeListener() {
     * 
     * 
     * private static final long serialVersionUID = -8555301175966822768L;
     * 
     * Object selectedDiscount = ((SingleSelectionModel) offerGrid.getSelectionModel())
     * .getSelectedRow();
     * 
     * @Override public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
     * 
     * System.out.println("updateDiscount: " + updateDiscount.getValue().toString() + " " +
     * offerGrid.getContainerDataSource().getItem(selectedDiscount)
     * .getItemProperty("offer_id").toString());
     * 
     * db.updateDiscount(updateDiscount.getValue().toString(), offerGrid.getContainerDataSource()
     * .getItem(selectedDiscount).getItemProperty("offer_id").toString());
     * 
     * } });
     * 
     * updateStatus.addValueChangeListener(new ValueChangeListener() {
     * 
     * 
     * private static final long serialVersionUID = -8555301175966822768L;
     * 
     * Object selectedStatus = ((SingleSelectionModel) offerGrid.getSelectionModel())
     * .getSelectedRow();
     * 
     * @Override public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
     * 
     * System.out.println("updateStatus: " + updateStatus.getValue().toString() + " " +
     * offerGrid.getContainerDataSource().getItem(selectedStatus)
     * .getItemProperty("offer_id").toString());
     * 
     * db.updateDiscount(updateStatus.getValue().toString(), offerGrid.getContainerDataSource()
     * .getItem(selectedStatus).getItemProperty("offer_id").toString());
     * 
     * } });
     */

    /*
     * offerGrid.setColumnOrder("offer_number", "offer_project_reference", "offer_name",
     * "offer_facility", "offer_description", "offer_price", "discount", "offer_status",
     * "offer_date");
     */

    // offerGrid.getColumn("offer_id").setHeaderCaption("ID");
    offerGrid.getColumn("offer_number").setHeaderCaption("Quotation Number");
    offerGrid.getColumn("offer_project_reference").setHeaderCaption("Project Reference");
    offerGrid.getColumn("offer_name").setHeaderCaption("Name").setWidth(200);
    offerGrid.getColumn("offer_facility").setHeaderCaption("Prospect");
    offerGrid.getColumn("offer_description").setHeaderCaption("Description").setWidth(300);
    offerGrid.getColumn("offer_group").setHeaderCaption("Group");
    offerGrid.getColumn("offer_total").setHeaderCaption("Price (€)").setEditable(false);
    offerGrid.getColumn("discount").setHeaderCaption("Discount").setEditable(false);
    offerGrid.getColumn("offer_status").setHeaderCaption("Status").setEditable(false);
    offerGrid.getColumn("offer_date").setHeaderCaption("Date");
    offerGrid.getColumn("internal").setHeaderCaption("Internal Offer");

    offerGrid.removeColumn("offer_price");
    offerGrid.removeColumn("offer_extra_price");
    offerGrid.removeColumn("offer_group");
    offerGrid.removeColumn("last_edited");
    offerGrid.removeColumn("offer_id");
    offerGrid.removeColumn("added_by");
    offerGrid.removeColumn("offer_date");
    offerGrid.removeColumn("internal");

    offerGrid.sort("offer_date", SortDirection.ASCENDING);

    offerGrid.setWidth("100%");
    offerGrid.setSelectionMode(SelectionMode.SINGLE);
    offerGrid.setEditorEnabled(true);

    offerManLayout.addComponent(offerGrid);
    offerManLayout.addComponent(editSettingsLayout);
    offerManLayout.addComponent(detailsLayout);
    offerManLayout.addComponent(printOfferButton);
    // packManLayout.addComponent(refresh);

    return offerManLayout;

  }

  // private void fillPackageGrid(Grid grid, String dateStart, String dateEnd) {
  /*
   * private void fillPackageGrid(Grid grid) {
   * 
   * // List<packageBean> packBeans = DBManager.getDatabaseInstance().getPackages(dateStart, //
   * dateEnd); List<packageBean> packBeans = DBManager.getDatabaseInstance().getPackages();
   * 
   * if (packBeans.size() > 0) Notification("Attention! Entries with No Costs!",
   * "Costs of some entries couldn't be calculated! Check the 'No Costs' tab!", "error");
   * 
   * }
   */


  /**
   * Create image, without specifying width
   */
  public static org.docx4j.wml.P newImage(WordprocessingMLPackage wordMLPackage, byte[] bytes,
      String filenameHint, String altText, int id1, int id2) throws Exception {

    BinaryPartAbstractImage imagePart =
        BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);

    Inline inline = imagePart.createImageInline(filenameHint, altText, id1, id2, false);

    // Now add the inline in w:p/w:r/w:drawing
    org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
    org.docx4j.wml.P p = factory.createP();
    org.docx4j.wml.R run = factory.createR();
    p.getContent().add(run);
    org.docx4j.wml.Drawing drawing = factory.createDrawing();
    run.getContent().add(drawing);
    drawing.getAnchorOrInline().add(inline);

    return p;

  }

  private void writeDocx() {
    try {
      int count = 0;
      String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
      XWPFDocument document = new XWPFDocument();
      XWPFDocument docx =
          new XWPFDocument(new FileInputStream(basepath + "/WEB-INF/documents/template1000.docx"));
      XWPFWordExtractor we = new XWPFWordExtractor(docx);
      String text = we.getText();
      if (text.contains("Hello")) {
        text = text.replace("Hello", "Hi");
        System.out.println(text);
      }
      char[] c = text.toCharArray();
      for (int i = 0; i < c.length; i++) {

        if (c[i] == '\n') {
          count++;
        }
      }
      System.out.println(c[0]);
      StringTokenizer st = new StringTokenizer(text, "\n");

      XWPFParagraph para = document.createParagraph();
      para.setAlignment(ParagraphAlignment.CENTER);
      XWPFRun run = para.createRun();
      run.setBold(true);
      run.setFontSize(36);
      run.setText("Apache POI works well!");

      List<XWPFParagraph> paragraphs = new ArrayList<XWPFParagraph>();
      List<XWPFRun> runs = new ArrayList<XWPFRun>();
      int k = 0;
      for (k = 0; k < count + 1; k++) {
        paragraphs.add(document.createParagraph());
      }
      k = 0;
      while (st.hasMoreElements()) {
        paragraphs.get(k).setAlignment(ParagraphAlignment.LEFT);
        paragraphs.get(k).setSpacingAfter(0);
        paragraphs.get(k).setSpacingBefore(0);
        run = paragraphs.get(k).createRun();
        run.setText(st.nextElement().toString());
        k++;
      }

      document.write(new FileOutputStream("template2000.docx"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void Notification(String title, String description, String type) {
    com.vaadin.ui.Notification notify = new com.vaadin.ui.Notification(title, description);
    notify.setPosition(Position.TOP_CENTER);
    if (type.equals("error")) {
      notify.setDelayMsec(16000);
      notify.setIcon(FontAwesome.FROWN_O);
      notify.setStyleName(ValoTheme.NOTIFICATION_ERROR + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    } else if (type.equals("success")) {
      notify.setDelayMsec(8000);
      notify.setIcon(FontAwesome.SMILE_O);
      notify.setStyleName(ValoTheme.NOTIFICATION_SUCCESS + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    } else {
      notify.setDelayMsec(8000);
      notify.setIcon(FontAwesome.MEH_O);
      notify.setStyleName(ValoTheme.NOTIFICATION_TRAY + " " + ValoTheme.NOTIFICATION_CLOSABLE);
    }
    notify.show(Page.getCurrent());
  }

}
