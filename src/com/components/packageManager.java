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

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.Text;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.PageDimensions;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblPrBase.TblStyle;
import org.docx4j.wml.CTVerticalJc;
import org.docx4j.wml.Color;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STVerticalJc;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcMar;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.TcBorders;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Tr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;

import com.dbase.DBManager;
import com.dbase.Database;
import com.utils.Docx4jHelper;
import com.utils.DocxStyle;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FileResource;
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
  private String filename;
  private FileDownloader fileDownloader;
  private float totalPrice;
  private float packageUnitPrice;
  private final TabSheet managerTabs = new TabSheet();
  private Docx4jHelper docxHelper;
  private int packageGridSize = 0;

  org.docx4j.wml.P header;
  org.docx4j.wml.P footer;

  List<String> packageNames = new ArrayList<String>();
  List<String> packageDescriptions = new ArrayList<String>();
  List<String> packageCounts = new ArrayList<String>();
  List<String> packageUnitPrices = new ArrayList<String>();
  List<String> packageTotalPrices = new ArrayList<String>();

  Date dNow = new Date();
  SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
  SimpleDateFormat offerNumber = new SimpleDateFormat("yyyyMMdd");

  String dateToday = offerNumber.format(dNow);
  String queryFreeform =
      "SELECT * FROM offers INNER JOIN offers_packages ON offers.`offer_id` = offers_packages.`offer_id` INNER JOIN packages ON packages.`package_id` = offers_packages.`package_id` WHERE offers.offer_id = 8";

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

    System.out.println(ft.format(dNow) + "  INFO  Offer Manager accessed! - User: "
        + LiferayAndVaadinUtils.getUser().getScreenName());

    // final TabSheet managerTabs = new TabSheet();

    managerTabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
    managerTabs.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);

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

    TableQuery tq = new TableQuery("packages", DBManager.getDatabaseInstanceAlternative());
    tq.setVersionColumn("OPTLOCK");
    SQLContainer container = new SQLContainer(tq);

    container.setAutoCommit(isEnabled());

    Grid packageGrid = new Grid(container);

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

        // System.out.println("offerProjectReference: " + offerProjectReference + " offerName: "
        // + offerName + " offerDescription: " + offerDescription);

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
            // System.out.println(i + "Package Unit Price: " + packageUnitPrice);
            db.insertOrUpdateOffersPackages(offerId, packageId, packageUnitPrice);
            // System.out.println("PackageID: " + packageId + "OfferID: " + offerId);
            // System.out.println(i + " " + values[i]);
          }

          Notification(
              "Perfect! Offer succesfully saved in the Database!",
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

  private Component packageQuantityGrid(SQLContainer offerGridContainer, String queryEnd)
      throws SQLException {

    VerticalLayout packQuantityLayout = new VerticalLayout();
    HorizontalLayout packSettingsLayout = new HorizontalLayout();

    packQuantityLayout.setMargin(true);
    packQuantityLayout.setSpacing(true);
    packQuantityLayout.setSizeFull();

    ComboBox addQuantity = new ComboBox("Select Quantity");

    /*
     * ComboBox updateQuantityDiscount = new ComboBox("Select Discount");
     * updateQuantityDiscount.addItem("0%"); updateQuantityDiscount.addItem("10%");
     * updateQuantityDiscount.addItem("20%"); updateQuantityDiscount.addItem("30%");
     * updateQuantityDiscount.addItem("40%"); updateQuantityDiscount.addItem("50%");
     */

    for (int i = 1; i <= 50; i++)
      addQuantity.addItem(i);

    Button updateQuantityButton = new Button("Update");
    updateQuantityButton.setIcon(FontAwesome.SPINNER);

    packSettingsLayout.addComponent(addQuantity);
    // packSettingsLayout.addComponent(updateQuantityDiscount);
    packSettingsLayout.addComponent(updateQuantityButton);

    packSettingsLayout.setComponentAlignment(updateQuantityButton, Alignment.BOTTOM_CENTER);

    packSettingsLayout.setSpacing(true);

    queryFreeform =
        "SELECT * FROM offers INNER JOIN offers_packages ON offers.`offer_id` = offers_packages.`offer_id` INNER JOIN packages ON packages.`package_id` = offers_packages.`package_id` WHERE offers.offer_id = "
            + queryEnd;

    FreeformQuery query =
        new FreeformQuery(queryFreeform, DBManager.getDatabaseInstanceAlternative(), "package_id");
    SQLContainer packsContainer = new SQLContainer(query);

    packageGridSize = packsContainer.size();

    // System.out.println("Print Container: " + container.size());
    packsContainer.setAutoCommit(isEnabled());

    selectedPacksInOfferGrid = new Grid(packsContainer);

    packageNames.clear();
    packageDescriptions.clear();
    packageCounts.clear();
    packageUnitPrices.clear();
    packageTotalPrices.clear();

    // System.out.println(selectedPacksInOfferGrid.getColumns());
    DecimalFormat myFormatter = new DecimalFormat("###,###.###");

    for (Object itemIdInvoiced : packsContainer.getItemIds()) {
      packageNames.add(packsContainer.getContainerProperty(itemIdInvoiced, "package_name")
          .getValue().toString());
      packageDescriptions.add(packsContainer
          .getContainerProperty(itemIdInvoiced, "package_description").getValue().toString());
      packageCounts.add(packsContainer.getContainerProperty(itemIdInvoiced, "package_count")
          .getValue().toString());
      packageUnitPrices.add(myFormatter.format(
          packsContainer.getContainerProperty(itemIdInvoiced, "package_price").getValue())
          .toString());
      packageTotalPrices.add(myFormatter.format(
          packsContainer.getContainerProperty(itemIdInvoiced, "package_addon_price").getValue())
          .toString());

    }

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

        // updateQuantityDiscount.select(db.getPackageDiscount(queryEnd, selectedPacksInOfferGrid
        // .getSelectedRow().toString()));

      }

    });

    updateQuantityButton.addClickListener(new ClickListener() {

      /**
         * 
         */
      private static final long serialVersionUID = 8910018717791341602L;

      @Override
      public void buttonClick(ClickEvent event) {

        /*
         * System.out.println("ValueStatus: " + updateStatus.getValue() + " Discount " // +
         * updateDiscount.getValue()); if (addQuantity.getValue() == null ||
         * updateQuantityDiscount.getValue() == null) { }
         */

        if (addQuantity.getValue() == null) {
          Notification("oOps! Forgot something?!",
              "Please make sure that you select an option for status and discount update.", "error");

        } else {

          /*
           * float percentage = Integer
           * .parseInt(updateQuantityDiscount.getValue().toString().trim().replace("%", ""));
           * 
           * System.out.println("Dropdown: " + addQuantity.getValue().toString() + " offer id: " +
           * queryEnd + "package id: " + selectedPacksInOfferGrid.getSelectedRow().toString());
           */

          db.updateQuantityQuery(addQuantity.getValue().toString(), queryEnd,
              selectedPacksInOfferGrid.getSelectedRow().toString(), db.internalOfferCheck(queryEnd));

          /*
           * TODO: Query Internal & External
           * container.getItem(selected).getItemProperty("offer_id").toString());
           * offerGrid.getContainerDataSource
           * ().getItem(selected).getItemProperty("offer_id").toString());
           * 
           * db.updateQuantityDiscountQuery(updateQuantityDiscount.getValue().toString(), queryEnd,
           * selectedPacksInOfferGrid.getSelectedRow().toString());
           */

          packsContainer.refresh();
          offerGridContainer.refresh();

        }


        packageNames.clear();
        packageDescriptions.clear();
        packageCounts.clear();
        packageUnitPrices.clear();
        packageTotalPrices.clear();

        // System.out.println(selectedPacksInOfferGrid.getColumns());
        DecimalFormat myFormatter = new DecimalFormat("###,###.###");

        for (Object itemIdInvoiced : packsContainer.getItemIds()) {
          packageNames.add(packsContainer.getContainerProperty(itemIdInvoiced, "package_name")
              .getValue().toString());
          packageDescriptions.add(packsContainer
              .getContainerProperty(itemIdInvoiced, "package_description").getValue().toString());
          packageCounts.add(packsContainer.getContainerProperty(itemIdInvoiced, "package_count")
              .getValue().toString());
          packageUnitPrices.add(myFormatter.format(
              packsContainer.getContainerProperty(itemIdInvoiced, "package_price").getValue())
              .toString());
          packageTotalPrices.add(myFormatter
              .format(
                  packsContainer.getContainerProperty(itemIdInvoiced, "package_addon_price")
                      .getValue()).toString());

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

    String printOfferButtonTitle = "Generate Offer";
    Button printOfferButton = new Button(printOfferButtonTitle);
    printOfferButton.setIcon(FontAwesome.PRINT);
    printOfferButton.setDescription("Select an offer from the grid then click here to modify!");

    String downloadOfferButtonTitle = "Download";
    Button downloadOfferButton = new Button(downloadOfferButtonTitle);
    downloadOfferButton.setIcon(FontAwesome.DOWNLOAD);
    downloadOfferButton.setDescription("Download the printed document!");
    downloadOfferButton.setEnabled(false);

    offerManLayout.setMargin(true);
    offerManLayout.setSpacing(true);
    offerManLayout.setSizeFull();

    TableQuery tq = new TableQuery("offers", DBManager.getDatabaseInstanceAlternative());
    tq.setVersionColumn("OPTLOCK");
    SQLContainer container = new SQLContainer(tq);

    container.setAutoCommit(isEnabled());

    Grid offerGrid = new Grid(container);

    offerGrid.setSelectionMode(SelectionMode.SINGLE);

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


          // System.out.println("Selected Row -> " + selected.toString());

          if (selected != null) {

            updateStatus.select(db.getOfferStatus(container.getItem(selected)
                .getItemProperty("offer_id").toString()));

            // System.out.println(db.getOfferStatus(container.getItem(selected)
            // .getItemProperty("offer_id").toString()));

            updateDiscount.select(db.getOfferDiscount(container.getItem(selected)
                .getItemProperty("offer_id").toString()));

            // offerGrid.getContainerDataSource().getItem(selected).getItemProperty("offer_id").toString()));
            // updateDiscount.select(db.getOfferDiscount(offerGrid.getContainerDataSource().getItem(selected).getItemProperty("offer_id").toString()));

            Notification.show("Selected "
                + db.getOfferStatus(container.getItem(selected).getItemProperty("offer_id")
                    .toString()));

            try {
              detailsLayout.removeAllComponents();
              detailsLayout.addComponent(packageQuantityGrid(container, container.getItem(selected)
                  .getItemProperty("offer_id").toString()));
            } catch (Exception e) {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
          }


        });


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
              "Please make sure that you select an option for status and discount update.", "error");

        } else {

          // System.out.println("Selected Row: " + offerGrid.getSelectedRow().toString());
          db.updateStatus(updateStatus.getValue().toString(), offerGrid.getSelectedRow().toString());
          // container.getItem(selected).getItemProperty("offer_id").toString());
          // offerGrid.getContainerDataSource().getItem(selected).getItemProperty("offer_id").toString());

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

      // WordprocessingMLPackage template;

      /**
       * 
       */
      private static final long serialVersionUID = 9170993096605292649L;

      @Override
      public void buttonClick(ClickEvent event) {

        Notification("File Saved!", offerGrid.getSelectedRow().toString() + ", "
            + container.getItem(offerGrid.getSelectedRow()).getItemProperty("offer_number"), "");

        WordprocessingMLPackage wordMLPackage = null;
        try {
          wordMLPackage = WordprocessingMLPackage.createPackage();
        } catch (InvalidFormatException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        MainDocumentPart mdp = wordMLPackage.getMainDocumentPart();

        // list.add(p);
        // spc.getContent().add(rspc);
        // wordMLPackage.getMainDocumentPart().addObject(p);

        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();

        // Image as a file resource
        File resourceHeader = new File(basepath + "/WEB-INF/images/header.png");
        java.io.InputStream isHeader = null;

        try {
          isHeader = new java.io.FileInputStream(resourceHeader);
        } catch (FileNotFoundException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        long lengthHeader = resourceHeader.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        if (lengthHeader > Integer.MAX_VALUE) {
          System.out.println("File too large!!");
        }
        byte[] bytesHeader = new byte[(int) lengthHeader];
        int offsetHeader = 0;
        int numReadHeader = 0;
        try {
          while (offsetHeader < bytesHeader.length
              && (numReadHeader =
                  isHeader.read(bytesHeader, offsetHeader, bytesHeader.length - offsetHeader)) >= 0) {
            offsetHeader += numReadHeader;
          }
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        // Ensure all the bytes have been read in
        if (offsetHeader < bytesHeader.length) {
          System.out.println("Could not completely read file " + resourceHeader.getName());
        }
        try {
          isHeader.close();
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        String filenameHintHeader = null;
        String altTextHeader = null;
        int id1Header = 0;
        int id2Header = 1;

        // Image 1: no width specified
        // org.docx4j.wml.P p;
        // org.docx4j.wml.P p;

        try {
          header =
              newImage(wordMLPackage, bytesHeader, filenameHintHeader, altTextHeader, id1Header,
                  id2Header, 7000);
          // wordMLPackage.getMainDocumentPart().addObject(p);
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }


        File resourceFooter = new File(basepath + "/WEB-INF/images/footer.png");

        java.io.InputStream isFooter = null;
        try {
          isFooter = new java.io.FileInputStream(resourceFooter);
        } catch (FileNotFoundException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        long lengthFooter = resourceFooter.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        if (lengthFooter > Integer.MAX_VALUE) {
          System.out.println("File too large!!");
        }
        byte[] bytesFooter = new byte[(int) lengthFooter];
        int offsetFooter = 0;
        int numReadFooter = 0;
        try {
          while (offsetFooter < bytesFooter.length
              && (numReadFooter =
                  isFooter.read(bytesFooter, offsetFooter, bytesFooter.length - offsetFooter)) >= 0) {
            offsetFooter += numReadFooter;
          }
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        // Ensure all the bytes have been read in
        if (offsetFooter < bytesFooter.length) {
          System.out.println("Could not completely read file " + resourceFooter.getName());
        }
        try {
          isFooter.close();
        } catch (IOException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        String filenameHintFooter = null;
        String altTextFooter = null;
        int id1Footer = 0;
        int id2Footer = 1;

        try {
          footer =
              newImage(wordMLPackage, bytesFooter, filenameHintFooter, altTextFooter, id1Footer,
                  id2Footer, 2000);
          // wordMLPackage.getMainDocumentPart().addObject(p);
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        DecimalFormat myFormatter = new DecimalFormat("###,###.###");

        String clientAddress =
            container.getItem(offerGrid.getSelectedRow()).getItemProperty("offer_facility")
                .toString();

        String offerNumber =
            container.getItem(offerGrid.getSelectedRow()).getItemProperty("offer_number")
                .toString();

        String quotationNumber = offerNumber.substring(0, 6);

        String projectReference = offerNumber.substring(offerNumber.lastIndexOf('_') + 1);

        String projectScientist = db.getUserEmail(LiferayAndVaadinUtils.getUser().getScreenName());

        String projectTitle =
            container.getItem(offerGrid.getSelectedRow()).getItemProperty("offer_name").toString();

        String projectDescription =
            container.getItem(offerGrid.getSelectedRow()).getItemProperty("offer_description")
                .toString();

        String offerPrice =
            myFormatter.format(Float.valueOf(container.getItem(offerGrid.getSelectedRow())
                .getItemProperty("offer_price").toString()));

        String discount =
            container.getItem(offerGrid.getSelectedRow()).getItemProperty("discount").toString();

        String offerTotal =
            myFormatter.format(Float.valueOf(container.getItem(offerGrid.getSelectedRow())
                .getItemProperty("offer_total").toString()));

        String deliveryDetails =
            "An exact delivery time can be announced at the date of sample submission.";

        String notesDetails = "We are looking forward to a good cooperation with you.";

        String agreementText =
            "The invoice will be issued after completion of the project. Quality control at all steps of the data processing will guarantee that the processed data is in accordance to DFG (German research foundation) guidance for good scientific practice. All project related data will be kept securely on our local infrastructure. If the data generated through the project as outlined in this offer is subject to publication, QBiC offers to collaboratively contribute to the compilation of the manuscript and its scientific discussion (e.g., with respect to bioinformatics methods, visualisation of results and their interpretation). If such collaborative efforts lead to a significant scientific contribution, co-authorships on reports/manuscripts for QBiC scientist(s) involved in the study are expected. Offer expires in 30 days. If you agree with this offer, please return a signed copy.";

        Tbl quotation =
            createQuotationPage(header, clientAddress, offerNumber, projectReference,
                projectScientist, projectTitle, projectDescription, offerTotal, deliveryDetails,
                notesDetails, agreementText, footer);

        Tbl quotationDetails =
            createQuotationDetailsPage(header, clientAddress, offerNumber, projectReference,
                projectScientist, projectTitle, projectDescription, offerTotal, offerPrice,
                discount, deliveryDetails, notesDetails, agreementText, footer);

        wordMLPackage.getMainDocumentPart().addObject(quotation);

        mdp.addParagraphOfText("");

        wordMLPackage.getMainDocumentPart().addObject(quotationDetails);

        Body body;
        try {
          body = wordMLPackage.getMainDocumentPart().getContents().getBody();
          PageDimensions page = new PageDimensions();
          PgMar pgMar = page.getPgMar();
          pgMar.setBottom(BigInteger.valueOf(pixelsToDxa(40)));
          pgMar.setTop(BigInteger.valueOf(pixelsToDxa(40)));
          pgMar.setLeft(BigInteger.valueOf(pixelsToDxa(40)));
          pgMar.setRight(BigInteger.valueOf(pixelsToDxa(40)));
          SectPr sectPr = factory.createSectPr();
          body.setSectPr(sectPr);
          sectPr.setPgMar(pgMar);
        } catch (Docx4JException e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

        // TODO: create a check box and give the user options to save as read-only or
        // editable.
        // ProtectDocument protection = new ProtectDocument(wordMLPackage);
        // protection.restrictEditing(STDocProtect.READ_ONLY, "foobaa");

        String basepathVaadin = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        // save file example - date_projectid.docx - 20170530_QMARI.docx
        /*
         * filename = System.getProperty("user.dir") + "/" +
         * container.getItem(offerGrid.getSelectedRow()).getItemProperty("offer_number") + ".docx";
         */
        filename =
            "/home/tomcat-liferay/liferay_production/tmp/"
                + container.getItem(offerGrid.getSelectedRow()).getItemProperty("offer_number")
                + ".docx";

        try {
          Docx4J.save(wordMLPackage, new java.io.File(filename), Docx4J.FLAG_SAVE_ZIP_FILE);
        } catch (Docx4JException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

        System.out.println("Saved: " + filename);


        if (fileDownloader != null)
          downloadOfferButton.removeExtension(fileDownloader);
        FileResource offer = new FileResource(new File(filename));
        fileDownloader = new FileDownloader(offer);
        fileDownloader.extend(downloadOfferButton);
        downloadOfferButton.setEnabled(true);
        Notification("Ready for Download!", "The generated docx file, saved under " + filename
            + " is ready for download. Fire it up, click download now!", "success");
        downloadOfferButton.setEnabled(true);

      }
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
     * 
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
    offerManLayout.addComponent(downloadOfferButton);
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

  private WordprocessingMLPackage wordMLPackage;

  private static org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

  private boolean bold;
  private boolean italic;
  private boolean underline;
  private String fontSize;
  private String fontColor;
  private String fontFamily;

  // cell margins
  private int left;
  private int bottom;
  private int top;
  private int right;

  private String background;
  private STVerticalJc verticalAlignment;
  private JcEnumeration horizAlignment;

  private boolean borderLeft;
  private boolean borderRight;
  private boolean borderTop;
  private boolean borderBottom;
  private boolean noWrap;


  private void setPageMargins() {
    try {
      Body body = wordMLPackage.getMainDocumentPart().getContents().getBody();
      PageDimensions page = new PageDimensions();
      PgMar pgMar = page.getPgMar();
      pgMar.setBottom(BigInteger.valueOf(pixelsToDxa(20)));
      pgMar.setTop(BigInteger.valueOf(pixelsToDxa(20)));
      pgMar.setLeft(BigInteger.valueOf(pixelsToDxa(20)));
      pgMar.setRight(BigInteger.valueOf(pixelsToDxa(20)));
      SectPr sectPr = factory.createSectPr();
      body.setSectPr(sectPr);
      sectPr.setPgMar(pgMar);
    } catch (Docx4JException e) {
      e.printStackTrace();
    }
  }

  // get dots per inch
  protected static int getDPI() {
    return GraphicsEnvironment.isHeadless() ? 96 : Toolkit.getDefaultToolkit()
        .getScreenResolution();
  }

  private int pixelsToDxa(int pixels) {
    return (1440 * pixels / getDPI());
  }


  private void initDocx4J() {

    docxHelper = new Docx4jHelper();
    try {
      wordMLPackage = WordprocessingMLPackage.createPackage();
    } catch (InvalidFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private WordprocessingMLPackage getTemplate(String name) throws Docx4JException,
      FileNotFoundException {
    WordprocessingMLPackage template =
        WordprocessingMLPackage.load(new FileInputStream(new File(name)));
    return template;
  }

  private static List<Object> getAllElementFromObject(Object obj, Class<?> toSearch) {
    List<Object> result = new ArrayList<Object>();
    if (obj instanceof JAXBElement)
      obj = ((JAXBElement<?>) obj).getValue();

    if (obj.getClass().equals(toSearch))
      result.add(obj);
    else if (obj instanceof ContentAccessor) {
      List<?> children = ((ContentAccessor) obj).getContent();
      for (Object child : children) {
        result.addAll(getAllElementFromObject(child, toSearch));
      }

    }
    return result;
  }

  private void replacePlaceholder(WordprocessingMLPackage template, String name, String placeholder) {
    List<Object> texts = getAllElementFromObject(template.getMainDocumentPart(), Text.class);

    for (Object text : texts) {
      Text textElement = (Text) text;
      if (textElement.getValue().equals(placeholder)) {
        textElement.setValue(name);
      }
    }
  }

  private void writeDocxToStream(WordprocessingMLPackage template, String target)
      throws IOException, Docx4JException {
    File f = new File(target);
    template.save(f);
  }


  private void replaceParagraph(String placeholder, String textToAdd,
      WordprocessingMLPackage template, ContentAccessor addTo) {
    // 1. get the paragraph
    List<Object> paragraphs = getAllElementFromObject(template.getMainDocumentPart(), P.class);

    P toReplace = null;
    for (Object p : paragraphs) {
      List<Object> texts = getAllElementFromObject(p, Text.class);
      for (Object t : texts) {
        Text content = (Text) t;
        if (content.getValue().equals(placeholder)) {
          toReplace = (P) p;
          break;
        }
      }
    }

    // we now have the paragraph that contains our placeholder: toReplace
    // 2. split into seperate lines
    String as[] = StringUtils.splitPreserveAllTokens(textToAdd, '\n');

    for (int i = 0; i < as.length; i++) {
      String ptext = as[i];

      // 3. copy the found paragraph to keep styling correct
      P copy = XmlUtils.deepCopy(toReplace);

      // replace the text elements from the copy
      List<?> texts = getAllElementFromObject(copy, Text.class);
      if (texts.size() > 0) {
        Text textToReplace = (Text) texts.get(0);
        textToReplace.setValue(ptext);
      }

      // add the paragraph to the document
      addTo.getContent().add(copy);
    }

    // 4. remove the original one
    ((ContentAccessor) toReplace.getParent()).getContent().remove(toReplace);

  }

  private void replaceTable(String[] placeholders, List<Map<String, String>> textToAdd,
      WordprocessingMLPackage template) throws Docx4JException, JAXBException {
    List<Object> tables = getAllElementFromObject(template.getMainDocumentPart(), Tbl.class);

    // 1. find the table
    Tbl tempTable = getTemplateTable(tables, placeholders[0]);
    List<Object> rows = getAllElementFromObject(tempTable, Tr.class);

    // first row is header, second row is content
    if (rows.size() == 2) {
      // this is our template row
      Tr templateRow = (Tr) rows.get(1);

      for (Map<String, String> replacements : textToAdd) {
        // 2 and 3 are done in this method
        addRowToTable(tempTable, templateRow, replacements);
      }

      // 4. remove the template row
      tempTable.getContent().remove(templateRow);
    }
  }

  private Tbl getTemplateTable(List<Object> tables, String templateKey) throws Docx4JException,
      JAXBException {
    for (java.util.Iterator<Object> iterator = tables.iterator(); iterator.hasNext();) {
      Object tbl = iterator.next();
      List<?> textElements = getAllElementFromObject(tbl, Text.class);
      for (Object text : textElements) {
        Text textElement = (Text) text;
        if (textElement.getValue() != null && textElement.getValue().equals(templateKey))
          return (Tbl) tbl;
      }
    }
    return null;
  }

  private static void addRowToTable(Tbl reviewtable, Tr templateRow,
      Map<String, String> replacements) {
    Tr workingRow = XmlUtils.deepCopy(templateRow);
    List<?> textElements = getAllElementFromObject(workingRow, Text.class);
    for (Object object : textElements) {
      Text text = (Text) object;
      String replacementValue = replacements.get(text.getValue());
      if (replacementValue != null)
        text.setValue(replacementValue);
    }
    reviewtable.getContent().add(workingRow);
  }

  private static org.docx4j.wml.P createParagraph(String paragraphContent, boolean addNewLine,
      boolean bold, boolean italic) {
    org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
    org.docx4j.wml.P p = factory.createP();

    org.docx4j.wml.R run = factory.createR();
    p.getContent().add(run);

    org.docx4j.wml.Text text = factory.createText();
    text.setValue(paragraphContent);
    run.getContent().add(text);

    if (bold) {
      org.docx4j.wml.RPr rpr = factory.createRPr();
      org.docx4j.wml.BooleanDefaultTrue b = new org.docx4j.wml.BooleanDefaultTrue();
      b.setVal(true);
      rpr.setB(b);
      run.setRPr(rpr);
    }

    if (italic) {
      org.docx4j.wml.RPr rpr = factory.createRPr();
      org.docx4j.wml.BooleanDefaultTrue b = new org.docx4j.wml.BooleanDefaultTrue();
      b.setVal(true);
      rpr.setI(b);
      run.setRPr(rpr);
    }

    if (bold && italic) {
      org.docx4j.wml.RPr rpr = factory.createRPr();
      org.docx4j.wml.BooleanDefaultTrue b = new org.docx4j.wml.BooleanDefaultTrue();
      b.setVal(true);
      rpr.setB(b);
      rpr.setI(b);
      run.setRPr(rpr);
    }

    if (addNewLine) {
      run.getContent().add(factory.createBr());
    }

    return p;
  }

  private Tbl createQuotationPage(org.docx4j.wml.P headerImage, String clientAddress,
      String quotationNumber, String projectReference, String projectScientist,
      String projectTitle, String projectDescription, String offerTotal, String deliveryDetails,
      String notesDetails, String agreementText, org.docx4j.wml.P footerImage) {

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("dd MMMM yyyy");

    DocxStyle leftBlack11 = new DocxStyle();
    leftBlack11.setBold(false);
    leftBlack11.setItalic(false);
    leftBlack11.setUnderline(false);
    leftBlack11.setFontSize("22");
    leftBlack11.setFontFamily("OpenSans");
    leftBlack11.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle addressLeftCenterBlack10 = new DocxStyle();
    addressLeftCenterBlack10.setBold(false);
    addressLeftCenterBlack10.setItalic(false);
    addressLeftCenterBlack10.setUnderline(false);
    addressLeftCenterBlack10.setFontFamily("OpenSans");
    addressLeftCenterBlack10.setFontSize("20");
    addressLeftCenterBlack10.setHorizAlignment(JcEnumeration.LEFT);
    addressLeftCenterBlack10.setVerticalAlignment(STVerticalJc.CENTER);

    DocxStyle titleLeftGray16 = new DocxStyle();
    titleLeftGray16.setBold(false);
    titleLeftGray16.setItalic(false);
    titleLeftGray16.setUnderline(false);
    titleLeftGray16.setFontColor("7A7A7A");
    titleLeftGray16.setFontFamily("OpenSans");
    titleLeftGray16.setFontSize("32");
    titleLeftGray16.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle titleLeftBlack14 = new DocxStyle();
    titleLeftBlack14.setBold(false);
    titleLeftBlack14.setItalic(false);
    titleLeftBlack14.setUnderline(false);
    titleLeftBlack14.setFontFamily("OpenSans");
    titleLeftBlack14.setFontSize("28");
    titleLeftBlack14.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle subtitleLeftGray11 = new DocxStyle();
    subtitleLeftGray11.setBold(false);
    subtitleLeftGray11.setItalic(false);
    subtitleLeftGray11.setUnderline(false);
    subtitleLeftGray11.setFontColor("7A7A7A");
    subtitleLeftGray11.setFontFamily("OpenSans");
    subtitleLeftGray11.setFontSize("22");
    subtitleLeftGray11.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle subtitleLeftBlack11 = new DocxStyle();
    subtitleLeftBlack11.setBold(false);
    subtitleLeftBlack11.setItalic(false);
    subtitleLeftBlack11.setUnderline(false);
    subtitleLeftBlack11.setFontFamily("OpenSans");
    subtitleLeftBlack11.setFontSize("22");
    subtitleLeftBlack11.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle rightBottomGray11 = new DocxStyle();
    rightBottomGray11.setBold(false);
    rightBottomGray11.setItalic(false);
    rightBottomGray11.setUnderline(false);
    rightBottomGray11.setFontFamily("OpenSans");
    rightBottomGray11.setFontColor("7A7A7A");
    rightBottomGray11.setHorizAlignment(JcEnumeration.RIGHT);
    rightBottomGray11.setVerticalAlignment(STVerticalJc.BOTTOM);

    DocxStyle rightBottomBlack11 = new DocxStyle();
    rightBottomBlack11.setBold(false);
    rightBottomBlack11.setItalic(false);
    rightBottomBlack11.setUnderline(false);
    rightBottomBlack11.setFontFamily("OpenSans");
    rightBottomBlack11.setFontSize("22");
    rightBottomBlack11.setFontColor("FFFFFF");
    rightBottomBlack11.setHorizAlignment(JcEnumeration.RIGHT);
    rightBottomBlack11.setVerticalAlignment(STVerticalJc.BOTTOM);
    rightBottomBlack11.setBackground("52A8F8");

    DocxStyle leftGray9 = new DocxStyle();
    leftGray9.setBold(false);
    leftGray9.setItalic(false);
    leftGray9.setUnderline(false);
    leftGray9.setFontColor("7A7A7A");
    leftGray9.setFontFamily("OpenSans");
    leftGray9.setFontSize("18");
    leftGray9.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle justifiedGray9 = new DocxStyle();
    justifiedGray9.setBold(false);
    justifiedGray9.setItalic(false);
    justifiedGray9.setUnderline(false);
    justifiedGray9.setFontFamily("OpenSans");
    justifiedGray9.setFontColor("7A7A7A");
    justifiedGray9.setFontSize("18");
    justifiedGray9.setHorizAlignment(JcEnumeration.BOTH);

    DocxStyle justifiedBlack9 = new DocxStyle();
    justifiedBlack9.setBold(false);
    justifiedBlack9.setItalic(false);
    justifiedBlack9.setUnderline(false);
    justifiedBlack9.setFontFamily("OpenSans");
    justifiedBlack9.setFontSize("18");
    justifiedBlack9.setHorizAlignment(JcEnumeration.BOTH);

    DocxStyle style = new DocxStyle();
    style.setBold(false);
    style.setItalic(true);
    style.setUnderline(true);
    style.setFontSize("40");
    style.setFontColor("FF0000");
    style.setFontFamily("OpenSans");
    style.setTop(300);
    style.setBackground("CCFFCC");
    style.setHorizAlignment(JcEnumeration.LEFT);
    style.setNoWrap(false);

    Tbl tableQuotation = factory.createTbl();

    // for TEST: this adds borders to all cells
    TblPr tblPr = new TblPr();
    TblStyle tblStyle = new TblStyle();
    tblStyle.setVal("TableGrid");
    tblPr.setTblStyle(tblStyle);
    tableQuotation.setTblPr(tblPr);


    Tr tableRow = factory.createTr();

    /*
     * tableRow = factory.createTr(); addTableCell(tableRow, "Address", 4000, addressStyle, 1,
     * null); addTableCell(tableRow, "QBiC Photo", 7000, defStyle, 1, null);
     * table.getContent().add(tableRow);
     * 
     * tableRow = factory.createTr(); addTableCell(tableRow, "", 10000, defStyle, 3, null);
     * table.getContent().add(tableRow);
     */

    tableRow = factory.createTr();
    addTableCell(tableRow, clientAddress, 4000, addressLeftCenterBlack10, 1, null);
    addTableCell(tableRow, headerImage, 8000, rightBottomGray11, 1, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 12000, leftGray9, 2, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Quotation", 4000, titleLeftGray16, 1, null);
    addTableCell(tableRow, ft.format(dNow), 8000, rightBottomGray11, 1, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 12000, leftGray9, 2, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, projectTitle, 8000, titleLeftBlack14, 1, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Quotation Number", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, "Project Description", 8000, subtitleLeftBlack11, 1, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, quotationNumber, 4000, leftGray9, 1, null);
    addTableCell(tableRow, projectDescription, 8000, justifiedBlack9, 1, "restart");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Project Reference", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, projectReference, 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Project Scientist", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, projectScientist, 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Delivery Time", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, deliveryDetails, 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Notes", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, notesDetails, 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 4000, leftGray9, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, "close");
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 12000, leftGray9, 2, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Estimated Total (Netto): " + offerTotal + " €", 12000,
        rightBottomBlack11, 2, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 12000, leftGray9, 2, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Agreement", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, "", 8000, leftGray9, 1, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, agreementText, 12000, justifiedGray9, 2, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 12000, leftGray9, 2, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Place and date of issue", 4000, leftBlack11, 1, null);
    addTableCell(tableRow, "Signature", 4000, leftBlack11, 1, null);
    tableQuotation.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, footerImage, 12000, rightBottomGray11, 2, null);
    tableQuotation.getContent().add(tableRow);
    tableRow = factory.createTr();

    // start vertical merge for Filed 2 and Field 3 on 3 rows
    // addTableCell(tableRow, "Field 2", 3500, defStyle, 1, "restart");
    // addTableCell(tableRow, "Field 3", 1500, defStyle, 1, "resart");


    // add an image horizontally merged on 3 cells
    /*
     * String filenameHint = null; String altText = null; int id1 = 0; int id2 = 1; byte[] bytes =
     * getImageBytes(); P pImage; try { pImage = newImage(wordMLPackage, bytes, filenameHint,
     * altText, id1, id2, 8500); tableRow = factory.createTr(); addTableCell(tableRow, pImage, 8500,
     * defStyle, 3, null); table.getContent().add(tableRow); } catch (Exception e) {
     * e.printStackTrace(); }
     */

    return tableQuotation;
  }


  private Tbl createQuotationDetailsPage(org.docx4j.wml.P headerImage, String clientAddress,
      String quotationNumber, String projectReference, String projectScientist,
      String projectTitle, String projectDescription, String offerTotal, String offerPrice,
      String discount, String deliveryDetails, String notesDetails, String agreementText,
      org.docx4j.wml.P footerImage) {

    Date dNow = new Date();
    SimpleDateFormat ft = new SimpleDateFormat("dd MMMM yyyy");

    DocxStyle leftBlack11 = new DocxStyle();
    leftBlack11.setFontSize("22");
    leftBlack11.setBold(false);
    leftBlack11.setItalic(false);
    leftBlack11.setUnderline(false);
    leftBlack11.setFontFamily("OpenSans");
    leftBlack11.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle addressLeftCenterBlack10 = new DocxStyle();
    addressLeftCenterBlack10.setBold(false);
    addressLeftCenterBlack10.setItalic(false);
    addressLeftCenterBlack10.setUnderline(false);
    addressLeftCenterBlack10.setFontFamily("OpenSans");
    addressLeftCenterBlack10.setFontSize("20");
    addressLeftCenterBlack10.setHorizAlignment(JcEnumeration.LEFT);
    addressLeftCenterBlack10.setVerticalAlignment(STVerticalJc.CENTER);

    DocxStyle titleLeftGray16 = new DocxStyle();
    titleLeftGray16.setBold(false);
    titleLeftGray16.setItalic(false);
    titleLeftGray16.setUnderline(false);
    titleLeftGray16.setFontColor("7A7A7A");
    titleLeftGray16.setFontFamily("OpenSans");
    titleLeftGray16.setFontSize("32");
    titleLeftGray16.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle subtitleLeftGray11 = new DocxStyle();
    subtitleLeftGray11.setBold(false);
    subtitleLeftGray11.setItalic(false);
    subtitleLeftGray11.setUnderline(false);
    subtitleLeftGray11.setFontColor("7A7A7A");
    subtitleLeftGray11.setFontFamily("OpenSans");
    subtitleLeftGray11.setFontSize("22");
    subtitleLeftGray11.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle subtitleLeftBlack11 = new DocxStyle();
    subtitleLeftBlack11.setBold(false);
    subtitleLeftBlack11.setItalic(false);
    subtitleLeftBlack11.setUnderline(false);
    subtitleLeftBlack11.setFontFamily("OpenSans");
    subtitleLeftBlack11.setFontSize("22");
    subtitleLeftBlack11.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle rightBottomGray11 = new DocxStyle();
    rightBottomGray11.setBold(false);
    rightBottomGray11.setItalic(false);
    rightBottomGray11.setUnderline(false);
    rightBottomGray11.setFontFamily("OpenSans");
    rightBottomGray11.setFontSize("22");
    rightBottomGray11.setFontColor("7A7A7A");
    rightBottomGray11.setHorizAlignment(JcEnumeration.RIGHT);
    rightBottomGray11.setVerticalAlignment(STVerticalJc.BOTTOM);

    DocxStyle rightBottomBlack11 = new DocxStyle();
    rightBottomBlack11.setBold(false);
    rightBottomBlack11.setItalic(false);
    rightBottomBlack11.setUnderline(false);
    rightBottomBlack11.setFontFamily("OpenSans");
    rightBottomBlack11.setFontSize("22");
    rightBottomBlack11.setFontColor("FFFFFF");
    rightBottomBlack11.setHorizAlignment(JcEnumeration.RIGHT);
    rightBottomBlack11.setVerticalAlignment(STVerticalJc.BOTTOM);
    rightBottomBlack11.setBackground("52A8F8");

    DocxStyle leftGray9 = new DocxStyle();
    leftGray9.setBold(false);
    leftGray9.setItalic(false);
    leftGray9.setUnderline(false);
    leftGray9.setFontColor("7A7A7A");
    leftGray9.setFontFamily("OpenSans");
    leftGray9.setFontSize("18");
    leftGray9.setHorizAlignment(JcEnumeration.LEFT);

    DocxStyle justifiedGray9 = new DocxStyle();
    justifiedGray9.setBold(false);
    justifiedGray9.setItalic(false);
    justifiedGray9.setUnderline(false);
    justifiedGray9.setFontFamily("OpenSans");
    justifiedGray9.setFontColor("7A7A7A");
    justifiedGray9.setFontSize("18");
    justifiedGray9.setHorizAlignment(JcEnumeration.BOTH);

    DocxStyle amountStyle = new DocxStyle();
    amountStyle.setBold(false);
    amountStyle.setItalic(false);
    amountStyle.setUnderline(false);
    amountStyle.setFontColor("7A7A7A");
    amountStyle.setFontFamily("OpenSans");
    amountStyle.setFontSize("18");
    amountStyle.setHorizAlignment(JcEnumeration.RIGHT);

    // a address field cell style
    DocxStyle detailsBlackStyle = new DocxStyle();
    detailsBlackStyle.setBold(false);
    detailsBlackStyle.setItalic(false);
    detailsBlackStyle.setUnderline(false);
    detailsBlackStyle.setFontFamily("OpenSans");
    detailsBlackStyle.setFontSize("18");
    detailsBlackStyle.setHorizAlignment(JcEnumeration.BOTH);

    // a specific table cell style
    DocxStyle style = new DocxStyle();
    style.setBold(false);
    style.setItalic(true);
    style.setUnderline(true);
    style.setFontSize("40");
    style.setFontColor("FF0000");
    style.setFontFamily("OpenSans");
    style.setTop(300);
    style.setBackground("CCFFCC");
    style.setHorizAlignment(JcEnumeration.LEFT);
    style.setNoWrap(false);

    Tbl tableQuotationDetails = factory.createTbl();

    // for TEST: this adds borders to all cells
    TblPr tblPr = new TblPr();
    TblStyle tblStyle = new TblStyle();
    tblStyle.setVal("TableGrid");
    tblPr.setTblStyle(tblStyle);
    tableQuotationDetails.setTblPr(tblPr);

    Tr tableRow = factory.createTr();

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 4000, addressLeftCenterBlack10, 1, null);
    addTableCell(tableRow, headerImage, 8000, rightBottomGray11, 4, null);
    tableQuotationDetails.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 12000, leftGray9, 5, null);
    tableQuotationDetails.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Quotation Details", 4000, titleLeftGray16, 1, null);
    addTableCell(tableRow, ft.format(dNow), 8000, rightBottomGray11, 4, null);
    tableQuotationDetails.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 12000, leftGray9, 5, null);
    tableQuotationDetails.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "Work Package", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, "Description", 4000, subtitleLeftGray11, 1, null);
    addTableCell(tableRow, "Quantity", 1000, amountStyle, 1, null);
    addTableCell(tableRow, "Unit Price", 1000, amountStyle, 1, null);
    addTableCell(tableRow, "Amount", 2000, amountStyle, 1, null);
    tableQuotationDetails.getContent().add(tableRow);

    for (int i = 0; i < packageGridSize; i++) {

      tableRow = factory.createTr();
      addTableCell(tableRow, packageNames.get(i), 4000, leftGray9, 1, null);
      addTableCell(tableRow, packageDescriptions.get(i), 4000, justifiedGray9, 1, null);
      addTableCell(tableRow, packageCounts.get(i), 1000, amountStyle, 1, null);
      addTableCell(tableRow, packageUnitPrices.get(i) + " €", 1200, amountStyle, 1, null);
      addTableCell(tableRow, packageTotalPrices.get(i) + " €", 1800, amountStyle, 1, null);
      tableQuotationDetails.getContent().add(tableRow);

      System.out.println(i + " Name: " + packageNames.get(i) + " Desc: "
          + packageDescriptions.get(i) + " Count: " + packageCounts.get(i));

    }

    if (!discount.equals("0%")) {

      tableRow = factory.createTr();
      addTableCell(tableRow, "Estimated Sum " + offerPrice + " €", 12000, rightBottomGray11, 5,
          null);
      tableQuotationDetails.getContent().add(tableRow);

      tableRow = factory.createTr();
      addTableCell(tableRow, "Discount: " + discount, 12000, rightBottomGray11, 5, null);
      tableQuotationDetails.getContent().add(tableRow);

    }

    tableRow = factory.createTr();
    addTableCell(tableRow, "Estimated Total (Netto): " + offerTotal + " €", 12000,
        rightBottomBlack11, 5, null);
    tableQuotationDetails.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, "", 12000, leftGray9, 5, null);
    tableQuotationDetails.getContent().add(tableRow);

    tableRow = factory.createTr();
    addTableCell(tableRow, footerImage, 12000, rightBottomGray11, 5, null);
    tableQuotationDetails.getContent().add(tableRow);

    tableRow = factory.createTr();

    return tableQuotationDetails;
  }

  /*
   * private byte[] getImageBytes() { // TODO Auto-generated method stub return null; }
   */

  private void addTableCell(Tr tableRow, P image, int width, DocxStyle style,
      int horizontalMergedCells, String verticalMergedVal) {
    Tc tableCell = factory.createTc();
    addImageCellStyle(tableCell, image, style);
    setCellWidth(tableCell, width);
    setCellVMerge(tableCell, verticalMergedVal);
    setCellHMerge(tableCell, horizontalMergedCells);
    tableRow.getContent().add(tableCell);
  }

  private void addTableCell(Tr tableRow, String content, int width, DocxStyle style,
      int horizontalMergedCells, String verticalMergedVal) {
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

  private void addCellStyle(Tc tableCell, String content, DocxStyle style) {
    if (style != null) {

      P paragraph = factory.createP();

      org.docx4j.wml.Text text = factory.createText();
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

      setCellMargins(tableCell, style.getTop(), style.getRight(), style.getBottom(),
          style.getLeft());
      setCellColor(tableCell, style.getBackground());
      setVerticalAlignment(tableCell, style.getVerticalAlignment());

      setCellBorders(tableCell, style.isBorderTop(), style.isBorderRight(), style.isBorderBottom(),
          style.isBorderLeft());

      run.setRPr(runProperties);

      tableCell.getContent().add(paragraph);
    }
  }

  private void addImageCellStyle(Tc tableCell, P image, DocxStyle style) {
    setCellMargins(tableCell, style.getTop(), style.getRight(), style.getBottom(), style.getLeft());
    setCellColor(tableCell, style.getBackground());
    setVerticalAlignment(tableCell, style.getVerticalAlignment());
    setHorizontalAlignment(image, style.getHorizAlignment());
    setCellBorders(tableCell, style.isBorderTop(), style.isBorderRight(), style.isBorderBottom(),
        style.isBorderLeft());
    tableCell.getContent().add(image);
  }

  public P newImage(WordprocessingMLPackage wordMLPackage, byte[] bytes, String filenameHint,
      String altText, int id1, int id2, long cx) throws Exception {
    BinaryPartAbstractImage imagePart =
        BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);
    Inline inline = imagePart.createImageInline(filenameHint, altText, id1, id2, cx, false);
    // Now add the inline in w:p/w:r/w:drawing
    org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
    P p = factory.createP();
    R run = factory.createR();
    p.getContent().add(run);
    Drawing drawing = factory.createDrawing();
    run.getContent().add(drawing);
    drawing.getAnchorOrInline().add(inline);
    return p;
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
    border.setColor("FFFFFF");
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
        // System.out.println(text);
      }
      char[] c = text.toCharArray();
      for (int i = 0; i < c.length; i++) {

        if (c[i] == '\n') {
          count++;
        }
      }
      // System.out.println(c[0]);
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

  /*
   * public class Test {
   * 
   * static WordprocessingMLPackage wordMLPackage = null; static String inputfilepath; static
   * boolean save;
   * 
   * public static void main(String[] args) throws Exception { String inputfilepath;
   * 
   * try { // getInputFilePath(args); inputfilepath = System.getProperty("user.dir") +
   * "/CreateWordprocessingMLDocument_out.docx"; } catch (IllegalArgumentException e) {
   * 
   * inputfilepath = System.getProperty("user.dir") + "/CreateWordprocessingMLDocument_out.docx"; }
   * 
   * save = (inputfilepath == null ? false : true);
   * 
   * System.out.println("Creating package.."); wordMLPackage =
   * WordprocessingMLPackage.createPackage();
   * 
   * wordMLPackage.getMainDocumentPart().addStyledParagraphOfText("Title", "Hello world");
   * 
   * wordMLPackage.getMainDocumentPart().addParagraphOfText("from docx4j!");
   * org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
   * 
   * // Let's add a table int writableWidthTwips =
   * wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions()
   * .getWritableWidthTwips(); int cols = 5; int cellWidthTwips = new
   * Double(Math.floor((writableWidthTwips / cols))).intValue();
   * 
   * // Tbl tbl = TblFactory.createTable(3, 3, cellWidthTwips);
   * 
   * Tbl tbl = Context.getWmlObjectFactory().createTbl();
   * 
   * // /////////////////// String strTblPr = "<w:tblPr " + Namespaces.W_NAMESPACE_DECLARATION + ">"
   * + "<w:tblStyle w:val=\"TableGrid\"/>" + "<w:tblW w:w=\"0\" w:type=\"auto\"/>" +
   * "<w:tblLook w:val=\"04A0\"/>" + "</w:tblPr>"; TblPr tblPr = null;
   * 
   * try { tblPr = (TblPr) XmlUtils.unmarshalString(strTblPr); }
   * 
   * catch (JAXBException e) { // Shouldn't happen e.printStackTrace(); } tbl.setTblPr(tblPr);
   * 
   * TblGrid tblGrid = Context.getWmlObjectFactory().createTblGrid(); tbl.setTblGrid(tblGrid);
   * 
   * int writableWidthTwips1 =
   * wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions()
   * .getWritableWidthTwips(); int cellWidthTwips1 = new Double(Math.floor((writableWidthTwips /
   * cols))).intValue();
   * 
   * for (int i = 0; i < cols; i++) { TblGridCol gridCol =
   * Context.getWmlObjectFactory().createTblGridCol();
   * gridCol.setW(BigInteger.valueOf(cellWidthTwips)); tblGrid.getGridCol().add(gridCol); }
   * 
   * List<String> ls = new ArrayList<String>(); ls.add("rohit"); ls.add("Dwivedi"); ls.add("hi");
   * ls.add("hello"); ls.add("Byee");
   * 
   * Tc tc = null; Tr tr = null; int rows = 3; for (int j = 0; j < rows; j++) { tr =
   * Context.getWmlObjectFactory().createTr(); // tbl.getEGContentRowContent().add(tr); // for (int
   * i = 1; i <= cols; i++) { for (int i = 0; i < cols; i++) { tc =
   * Context.getWmlObjectFactory().createTc();
   * 
   * TcPr tcPr = Context.getWmlObjectFactory().createTcPr(); tc.setTcPr(tcPr); TblWidth cellWidth =
   * Context.getWmlObjectFactory().createTblWidth(); tcPr.setTcW(cellWidth);
   * cellWidth.setType("dxa"); cellWidth.setW(BigInteger.valueOf(cellWidthTwips));
   * org.docx4j.wml.ObjectFactory factory1 = Context.getWmlObjectFactory(); org.docx4j.wml.P p1 =
   * factory.createP(); org.docx4j.wml.Text t1 = factory.createText(); // ls.add("val :" + i);
   * t1.setValue(ls.get(i)); org.docx4j.wml.R run1 = factory.createR();
   * run1.getRunContent().add(t1);
   * 
   * p1.getParagraphContent().add(run1); tc.getEGBlockLevelElts().add(p1);
   * tr.getEGContentCellContent().add(tc);
   * 
   * }
   * 
   * tbl.getEGContentRowContent().add(tr); }
   * 
   * wordMLPackage.getMainDocumentPart().addObject(tbl);
   * 
   * if (save) { wordMLPackage.save(new java.io.File(inputfilepath)); System.out.println("Saved " +
   * inputfilepath); }
   * 
   * System.out.println("Done.");
   * 
   * }
   * 
   * }
   */



}
