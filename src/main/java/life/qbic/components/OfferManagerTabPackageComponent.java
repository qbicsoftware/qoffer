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
import com.vaadin.data.util.sqlcontainer.query.FreeformQuery;
import com.vaadin.event.SelectionEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import life.qbic.dbase.Database;
import life.qbic.utils.CsvParserUtils;
import life.qbic.utils.RefreshableGrid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static life.qbic.utils.qOfferManagerUtils.displayNotification;

final class OfferManagerTabPackageComponent {

  private ArrayList<Float> discountPerSampleSize;
  private RefreshableGrid selectedPacksInOfferGrid;
  private OfferManagerTab offerManagerTab;
  private qOfferManager qOfferManager;

  private static final Logger LOG = LogManager.getLogger(OfferManagerTabPackageComponent.class);

  public OfferManagerTabPackageComponent(qOfferManager qom, OfferManagerTab omt) {
    offerManagerTab = omt;
    qOfferManager = qom;
    InputStream fileStream = OfferManagerTabPackageComponent.class.getClassLoader()
        .getResourceAsStream("discount_per_sample_size.csv");

    discountPerSampleSize = CsvParserUtils.parseCsvFile(fileStream, ",", true);
  }

  /**
   * creates the component showing the packages of the respective package type of the currently
   * selected offer in a grid and enables the user to add and remove packages from the offer
   * 
   * @param offerGridContainer: sql container of all the offers
   * @param selectedOfferID: id of the currently selected offer
   * @param packagesType: type of the packages: "All", "Bioinformatics Analysis",
   *        "Project Management", "Sequencing", "Mass spectrometry", "Other"; what type of packages
   *        the grid should display
   * @return vaadin component
   * @throws SQLException :
   */
  Component createOfferManagerTabPackageComponent(SQLContainer offerGridContainer,
      String selectedOfferID, String packagesType) throws SQLException {

    Database db = qOfferManager.getDb();

    VerticalLayout packQuantityLayout = new VerticalLayout();
    packQuantityLayout.setMargin(true);
    packQuantityLayout.setSpacing(true);
    packQuantityLayout.setSizeFull();
    HorizontalLayout packSettingsLayout = new HorizontalLayout();


    ComboBox packageQuantityComboBox = new ComboBox("Select Quantity");

    for (int i = 1; i <= 1000; i++)
      packageQuantityComboBox.addItem(i);

    Button updateQuantityButton = new Button("Update quantity");
    updateQuantityButton.setIcon(FontAwesome.SPINNER);
    updateQuantityButton.setDescription("Updates the quantity of the current package.");

    Button removePackageButton = new Button("Remove");
    removePackageButton.setIcon(FontAwesome.TRASH_O);
    removePackageButton.setDescription("Removes the selected package from the current offer.");

    // we are only displaying the packages for the current package group
    ComboBox packagesAvailableForOfferComboBox = new ComboBox("Select package to add");
    packagesAvailableForOfferComboBox.setFilteringMode(FilteringMode.CONTAINS);

    String selectedPackageGroup = offerManagerTab.getPackageGroupComboBoxValue();
    if (selectedPackageGroup.equals("All")) {
      packagesAvailableForOfferComboBox.addItems(db.getPackageIdsAndNames());
    } else {
      packagesAvailableForOfferComboBox.addItems(db.getPackageIdsAndNames(selectedPackageGroup));
    }

    Button addPackageButton = new Button("Add");
    addPackageButton.setIcon(FontAwesome.PLUS);
    addPackageButton.setDescription("Adds a package to the current offer.");

    ComboBox externalInternalPriceComboBox = new ComboBox("External/Internal Price");
    externalInternalPriceComboBox
        .setDescription("Select here whether the internal, the external academical or the "
            + "external commercial price should be used for the current selected package.");
    externalInternalPriceComboBox.addItems("Internal", "External academic", "External commercial");

    Button externalInternalButton = new Button("Update price type");
    externalInternalButton.setIcon(FontAwesome.SPINNER);
    externalInternalButton
        .setDescription("Updates the package price type (internal/external academic/external "
            + "commercial) to use.");

    packSettingsLayout.addComponent(packageQuantityComboBox);
    packSettingsLayout.addComponent(updateQuantityButton);
    packSettingsLayout.addComponent(removePackageButton);
    packSettingsLayout.addComponent(packagesAvailableForOfferComboBox);
    packSettingsLayout.addComponent(addPackageButton);
    packSettingsLayout.addComponent(externalInternalPriceComboBox);
    packSettingsLayout.addComponent(externalInternalButton);

    packSettingsLayout.setComponentAlignment(updateQuantityButton, Alignment.BOTTOM_CENTER);
    packSettingsLayout.setComponentAlignment(removePackageButton, Alignment.BOTTOM_CENTER);
    packSettingsLayout.setComponentAlignment(addPackageButton, Alignment.BOTTOM_CENTER);
    packSettingsLayout.setComponentAlignment(externalInternalPriceComboBox,
        Alignment.MIDDLE_CENTER);
    packSettingsLayout.setComponentAlignment(externalInternalButton, Alignment.BOTTOM_CENTER);

    packSettingsLayout.setSpacing(true);

    // we need different freeform queries if 'All' package groups are selected or e.g. only
    // 'Bioinformatics' package groups
    String freeformQueryString = "SELECT * " + "FROM offers "
        + "INNER JOIN offers_packages ON offers.`offer_id` = offers_packages.`offer_id` "
        + "INNER JOIN packages ON packages.`package_id` = offers_packages.`package_id`"
        + "WHERE offers.offer_id = " + selectedOfferID + " AND packages.`package_group` = '"
        + packagesType + "'";
    if (Objects.equals(packagesType, "All")) {
      freeformQueryString = "SELECT * " + "FROM offers "
          + "INNER JOIN offers_packages ON offers.`offer_id` = offers_packages.`offer_id` "
          + "INNER JOIN packages ON packages.`package_id` = offers_packages.`package_id`"
          + "WHERE offers.offer_id = " + selectedOfferID;
    }

    FreeformQuery query =
        new FreeformQuery(freeformQueryString, db.getDatabaseInstanceAlternative());

    SQLContainer packsContainer = new SQLContainer(query);
    packsContainer.setAutoCommit(true);

    selectedPacksInOfferGrid = new RefreshableGrid(packsContainer);

    // add tooltips to the cells
    selectedPacksInOfferGrid.setCellDescriptionGenerator((Grid.CellDescriptionGenerator) cell -> {
      if (cell.getValue() == null)
        return null;
      return cell.getValue().toString();
    });

    // update the array lists holding the information about the packages of the current offer
    updatePackageArrays(packsContainer);

    addListeners(offerGridContainer, selectedOfferID, db, packageQuantityComboBox,
        updateQuantityButton, removePackageButton, packagesAvailableForOfferComboBox,
        addPackageButton, packsContainer, externalInternalPriceComboBox, externalInternalButton);

    // remove unimportant columns from the grid
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
    selectedPacksInOfferGrid.removeColumn("discount");
    selectedPacksInOfferGrid.removeColumn("internal");

    // rename the header caption
    selectedPacksInOfferGrid.getColumn("package_id").setHeaderCaption("Id");
    selectedPacksInOfferGrid.getColumn("package_addon_price")
        .setHeaderCaption("Package total price (€)");
    selectedPacksInOfferGrid.getColumn("package_count").setHeaderCaption("Quantity");
    selectedPacksInOfferGrid.getColumn("package_discount").setHeaderCaption("Discount");
    selectedPacksInOfferGrid.getColumn("package_name").setHeaderCaption("Package Name")
        .setWidth(200);
    selectedPacksInOfferGrid.getColumn("package_facility").setHeaderCaption("Package Facility");
    selectedPacksInOfferGrid.getColumn("package_description").setHeaderCaption("Description")
        .setWidth(300);
    selectedPacksInOfferGrid.getColumn("package_group").setHeaderCaption("Group");
    selectedPacksInOfferGrid.getColumn("package_price_internal")
        .setHeaderCaption("Internal base price (€)");
    selectedPacksInOfferGrid.getColumn("package_price_external_academic")
        .setHeaderCaption("External academic base price (€)");
    selectedPacksInOfferGrid.getColumn("package_price_external_commercial")
        .setHeaderCaption("External commercial base price (€)");
    selectedPacksInOfferGrid.getColumn("package_unit_type").setHeaderCaption("Unit Type");
    selectedPacksInOfferGrid.getColumn("package_price_type").setHeaderCaption("Package price type");

    selectedPacksInOfferGrid.setColumnOrder("package_id", "package_name", "package_description",
        "package_addon_price", "package_count", "package_discount", "package_group",
        "package_facility", "package_price_internal", "package_price_external_academic",
        "package_price_external_commercial", "package_price_type", "package_unit_type");

    // we don't want the packages to be be editable, because this would change the package in other
    // offers as well
    selectedPacksInOfferGrid.sort("package_id", SortDirection.ASCENDING);
    selectedPacksInOfferGrid.setEditorEnabled(false);
    selectedPacksInOfferGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
    selectedPacksInOfferGrid.setWidth("100%");

    // label showing the packages group currently displayed (e.g. "All", "Project Management", etc.)
    Label packagesGroupLabel = new Label("<b><u>" + packagesType + ":</u></b>", ContentMode.HTML);
    packQuantityLayout.addComponent(packagesGroupLabel);
    packQuantityLayout.addComponent(selectedPacksInOfferGrid);
    packQuantityLayout.addComponent(packSettingsLayout);

    return packQuantityLayout;
  }


  /**
   * adds the listeners to the package component of the offer manager tab
   * 
   * @param offerGridContainer: sql container holding the data for the offers
   * @param selectedOfferID: id of the selected offer
   * @param db: database instance to query
   * @param packageQuantityComboBox: combo box for selecting the package quantity
   * @param updateQuantityButton: button for updating the package quantity
   * @param removePackageButton: button for removing a package from the current offer
   * @param packagesAvailableForOfferComboBox: combo box for selecting the available packages to add
   *        to the current offer
   * @param addPackageButton: button for adding a package
   * @param packsContainer: sql container holding the data for the packages
   */
  private void addListeners(SQLContainer offerGridContainer, String selectedOfferID, Database db,
      ComboBox packageQuantityComboBox, Button updateQuantityButton, Button removePackageButton,
      ComboBox packagesAvailableForOfferComboBox, Button addPackageButton,
      SQLContainer packsContainer, ComboBox externalInternalPriceComboBox,
      Button externalInternalPriceButton) {

    selectedPacksInOfferGrid.addSelectionListener(new SelectionEvent.SelectionListener() {

      /**
       *
       */
      private static final long serialVersionUID = -1061272471352530723L;

      @Override
      public void select(SelectionEvent event) {

        Object selectedPackageId = selectedPacksInOfferGrid.getSelectedRow();
        if (selectedPackageId != null) {
          int packageCount = db.getPackageCount(selectedOfferID, selectedPackageId.toString());
          if (packageCount > 0)
            packageQuantityComboBox.select(packageCount);
        }
      }
    });

    updateQuantityButton.addClickListener(new Button.ClickListener() {

      /**
       *
       */
      private static final long serialVersionUID = 8910018717791341602L;

      @Override
      public void buttonClick(Button.ClickEvent event) {
        offerManagerTab.setEnableGenerateButton(false);

        if (selectedPacksInOfferGrid.getSelectedRow() == null) {
          displayNotification("oOps! Forgot something?!",
              "Please make sure that you select a package to update.", "error");
          return;
        }

        if (packageQuantityComboBox.getValue() == null) {
          displayNotification("oOps! Forgot something?!",
              "Please make sure that you select an option for the package quantity.", "error");
        } else {

          String rowId = selectedPacksInOfferGrid.getSelectedRow().toString();
          Object rowContainerId = packsContainer.getIdByIndex(Integer.parseInt(rowId) - 1);
          String packageId = packsContainer.getContainerProperty(rowContainerId, "package_id")
              .getValue().toString();

          LOG.info("updatePackageQuantityAndRecalculatePrice update quantitybutton");

          String packageGroup;
          LOG.info(packsContainer.getContainerPropertyIds());
          try {
            packageGroup = packsContainer.getContainerProperty(rowContainerId, "package_group")
                .getValue().toString();
          } catch (NullPointerException e) {
            packageGroup = "";
          }
          int packageCount = Integer.parseInt(packageQuantityComboBox.getValue().toString());
          float packageDiscount = 1.0f;
          // the package discount should only be applied to the bioinformatics analysis packages
          if (Objects.equals(packageGroup, "Bioinformatics Analysis")) {
            LOG.info("package is Bioinformatics Analysis");
            // get the package discount based on the number of samples
            packageDiscount = discountPerSampleSize.get(packageCount);
          } else {
            LOG.info("package is not bioinfo analysis, but: <" + packageGroup + ">");
          }

          Property packagePriceTypeProperty =
              packsContainer.getContainerProperty(rowContainerId, "package_price_type");
          String packagePriceType;
          if (packagePriceTypeProperty.getValue() == null) {
            packagePriceType = "internal";
          } else {
            packagePriceType = packagePriceTypeProperty.getValue().toString();
          }

          // update the database
          db.updatePackageQuantityAndRecalculatePrice(packageQuantityComboBox.getValue().toString(),
              selectedOfferID, packageId, packagePriceType, packageDiscount);

          packsContainer.refresh();
          offerGridContainer.refresh();
        }

        // update the array lists holding the information about the packages of the current offer
        updatePackageArrays(packsContainer);
      }
    });

    removePackageButton.addClickListener((Button.ClickListener) event -> {
      offerManagerTab.setEnableGenerateButton(false);

      Object selectedRow = selectedPacksInOfferGrid.getSelectedRow();
      if (selectedRow == null) {
        displayNotification("No package selected!",
            "Please select an package to remove from the offer.", "error");
        return;
      }

      int selectedPackageID = (int) selectedPacksInOfferGrid.getContainerDataSource()
          .getItem(selectedRow).getItemProperty("package_id").getValue();

      db.removePackageFromOffer(selectedPackageID, Integer.parseInt(selectedOfferID));
      packsContainer.refresh();

      // update the array lists holding the information about the packages of the current offer
      updatePackageArrays(packsContainer);

      // recalculate the total offer price and update the database
      updateOfferPrice(selectedOfferID, packsContainer);

      offerGridContainer.refresh();

      displayNotification("Package removed",
          "Package " + selectedPackageID + " successfully removed from " + "offer.", "success");
    });

    addPackageButton.addClickListener((Button.ClickListener) event -> {
      offerManagerTab.setEnableGenerateButton(false);

      Object packageToAdd = packagesAvailableForOfferComboBox.getValue();
      if (packageToAdd == null) {
        displayNotification("No package selected!", "Please select an package to add to the offer.",
            "error");
        return;
      }

      // the package id and name is stored in the combobox as such: <package_id>: <package_name>
      String packageName = packageToAdd.toString().split(": ", 2)[1];
      int packageId = Integer.parseInt(packageToAdd.toString().split(": ", 2)[0]);

      // get the package price as string, so we can check whether it's null
      String packageUnitPrice = db.getPriceFromPackageId(packageId, "internal");
      if (packageUnitPrice == null) {
        displayNotification("Price is null!",
            "The price for the current package is null, please fix the "
                + "database entry before adding the package!",
            "error");
        return;
      }

      // check if the package is already used for the offer
      boolean packageIsAlreadyInOffer =
          db.checkForPackageInOffer(Integer.parseInt(selectedOfferID), packageId);
      if (packageIsAlreadyInOffer) {
        displayNotification("Package already in offer!",
            "The package you tried to add is already used in "
                + "the offer. Please update the quantity instead.",
            "error");
        return;
      }

      db.insertOrUpdateOffersPackages(Integer.parseInt(selectedOfferID), packageId,
          new BigDecimal(packageUnitPrice));
      packsContainer.refresh();

      // update the array lists holding the information about the packages of the current offer
      updatePackageArrays(packsContainer);

      // recalculate the total offer price and update the database
      updateOfferPrice(selectedOfferID, packsContainer);

      offerGridContainer.refresh();
      displayNotification("Package added",
          "Package " + packageName + " successfully added to the " + "offer.", "success");
    });

    externalInternalPriceButton.addClickListener(event -> {
      offerManagerTab.setEnableGenerateButton(false);

      if (selectedPacksInOfferGrid.getSelectedRow() == null) {
        displayNotification("oOps! Forgot something?!",
            "Please make sure that you select a package to update.", "error");
        return;
      }

      if (externalInternalPriceComboBox.getValue() == null) {
        displayNotification("oOps! Forgot something?!",
            "Please make sure that you select an option for the package price type.", "error");
      } else {

        String rowId = selectedPacksInOfferGrid.getSelectedRow().toString();
        Object rowContainerId = packsContainer.getIdByIndex(Integer.parseInt(rowId) - 1);
        String packageId =
            packsContainer.getContainerProperty(rowContainerId, "package_id").getValue().toString();

        // database entry is an enum ["internal", "external_academic" and "external_commercial"], so
        // we need to
        // adjust the value we want to insert
        String packagePriceType = externalInternalPriceComboBox.getValue().toString();
        packagePriceType = packagePriceType.toLowerCase().replace(" ", "_");

        // check if we have a price for the selected package price type in the database
        if (packsContainer.getContainerProperty(rowContainerId, "package_price_" + packagePriceType)
            .getValue() == null) {
          displayNotification("Package price is null",
              "The " + packagePriceType + " for package " + packageId
                  + " is null. Please update the package in the package manager tab. Otherwise the price "
                  + "will be null",
              "error");
          return;
        }

        // update the package price type
        db.updatePackagePriceTypeForPackage(selectedOfferID, packageId, packagePriceType);

        // due to a lack of time we simply use the updatePriceAndRecalculateTotalPrices function to
        // recalculate the prices, although the quantity has not changed
        // TODO: write function to recalculate the price without the quantity to save some
        // computation power
        packsContainer.refresh();
        offerGridContainer.refresh();

        LOG.info("updatePackageQuantityAndRecalculatePrice for price type button");

        String packageDiscountString =
            packsContainer.getContainerProperty(rowContainerId, "package_discount").getValue()
                .toString().split("%")[0];
        String packageCount = packsContainer.getContainerProperty(rowContainerId, "package_count")
            .getValue().toString();

        db.updatePackageQuantityAndRecalculatePrice(packageCount, selectedOfferID, packageId,
            packagePriceType, 1 - Float.valueOf(packageDiscountString) / 100);

        packsContainer.refresh();
        offerGridContainer.refresh();

        updatePackageArrays(packsContainer);
      }
    });
  }


  /**
   * updates the total offer price for the offer with offer id selectedOfferId
   * 
   * @param selectedOfferID: id of the offer to update the price for
   * @param packsContainer: holds all the information about the offer
   */
  private void updateOfferPrice(String selectedOfferID, SQLContainer packsContainer) {
    offerManagerTab.setEnableGenerateButton(false);

    Database db = qOfferManager.getDb();

    BigDecimal totalOfferPrice = BigDecimal.ZERO;

    for (Object itemID : packsContainer.getItemIds()) {
      // get the package id
      Property<?> packageIdProperty = packsContainer.getContainerProperty(itemID, "package_id");
      int packageIdInGrid = Integer.parseInt(packageIdProperty.getValue().toString());

      // get the package price type (internal, external, etc.)
      Property<?> packagePriceTypeProperty =
          packsContainer.getContainerProperty(itemID, "package_price_type");
      String packagePriceType = packagePriceTypeProperty.getValue().toString();

      Property<?> packageCountProperty =
          packsContainer.getContainerProperty(itemID, "package_count");
      int packageCount = Integer.parseInt(packageCountProperty.getValue().toString());

      // get the discount as floating point number (82% -> 0.18)
      float discount = getDiscountForRow(packsContainer, itemID);

      String packagePrice = db.getPriceFromPackageId(packageIdInGrid, packagePriceType);

      if (packagePrice == null) {
        packagePrice = "0";
        displayNotification("Package price is null!", "The package price for the package "
            + packageIdInGrid + " is null. Please update the price in the package manager tab.",
            "warning");
      }
      totalOfferPrice = totalOfferPrice.add(computeDiscount(packagePrice, packageCount, discount));
    }
    // update total offer price in db
    db.updateTotalOfferPrice(selectedOfferID, totalOfferPrice);
  }

  private BigDecimal computeDiscount(String packagePrice, int packageCount, float discountPercent) {
    return new BigDecimal(packagePrice).multiply(new BigDecimal(packageCount))
        .multiply(new BigDecimal(discountPercent));
  }

  private float getDiscountForRow(SQLContainer packsContainer, Object id) {
    Property<?> discountProperty = packsContainer.getContainerProperty(id, "package_discount");
    float discount =
        (100 - Float.parseFloat(discountProperty.getValue().toString().split("%")[0])) / 100;
    return discount;
  }

  /**
   * updates the array lists holding the package names, descriptions, counts, unit prices and total
   * prices
   * 
   * @param packsContainer: holds all the information of the currently used packages
   */
  private void updatePackageArrays(SQLContainer packsContainer) {

    // TODO: later: use one list of packageBeans instead of multiple lists of strings
    // get the lists for the packages holding the package names, descriptions, etc.
    List<String> packageNames = qOfferManager.getPackageNames();
    List<String> packageDescriptions = qOfferManager.getPackageDescriptions();
    List<String> packageCounts = qOfferManager.getPackageCounts();
    List<String> packageUnitPrices = qOfferManager.getPackageUnitPrices();
    List<String> packageTotalPrices = qOfferManager.getPackageTotalPrices();
    List<String> packageIDs = qOfferManager.getPackageIDs();

    // new infos
    List<Integer> discounts = qOfferManager.getDiscounts();
    List<String> discountedPrices = qOfferManager.getDiscountedPrices();

    // clear all the lists from the qOfferManager
    packageNames.clear();
    packageDescriptions.clear();
    packageCounts.clear();
    packageUnitPrices.clear();
    packageTotalPrices.clear();
    packageIDs.clear();

    discounts.clear();
    discountedPrices.clear();

    for (Object packsContainerRowId : packsContainer.getItemIds()) {

      String packageId = packsContainer.getContainerProperty(packsContainerRowId, "package_id")
          .getValue().toString();
      packageIDs.add(packageId);

      // package description can be empty, since it's not really needed
      Object packageDescription = packsContainer
          .getContainerProperty(packsContainerRowId, "package_description").getValue();
      if (packageDescription != null)
        packageDescriptions.add(packageDescription.toString());
      else
        packageDescriptions.add("");

      // deal with all properties which must not be null
      Object packageName =
          packsContainer.getContainerProperty(packsContainerRowId, "package_name").getValue();
      if (packageName == null) {
        displayNotification("Error parsing the package name for package " + packageId + "!",
            " package_name is null. Please fix the package in the package tab.", "error");
      } else {
        packageNames.add(packageName.toString());
      }

      Object packageCount =
          packsContainer.getContainerProperty(packsContainerRowId, "package_count").getValue();
      if (packageCount == null) {
        displayNotification("Error parsing the package count for package " + packageId + "!",
            " package_count is null. Please fix it in the offer manager tab.", "error");
      } else {
        packageCounts.add(packageCount.toString());
      }
      NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
      DecimalFormat decimalFormatter = (DecimalFormat) nf;
      decimalFormatter.applyLocalizedPattern("#.##");
      // DecimalFormat decimalFormatter = new DecimalFormat("###,###.###");

      // get the container property we need to check ("package_price_internal",
      // "package_price_external_academic", ..)
      Object packagePriceTypeObject =
          packsContainer.getContainerProperty(packsContainerRowId, "package_price_type").getValue();
      // deal with null and empty strings -> use "internal" instead
      String packagePriceType =
          packagePriceTypeObject == null ? "internal" : packagePriceTypeObject.toString();
      packagePriceType = packagePriceType.equals("") ? "internal" : packagePriceType;
      String containerPropertyToCheck = "package_price_" + packagePriceType;

      // get the respective package price (based on containerPropertyToCheck)
      Object packagePrice = packsContainer.getContainerProperty(packsContainerRowId, containerPropertyToCheck).getValue();

      LOG.info(String.format("Selected Package Price is: %s", packagePrice.toString()));
      LOG.info(String.format("Selected Package Price Type is: %s", containerPropertyToCheck));
      LOG.info(String.format("Selected Package Price Type in DB was: %s", packagePriceType));
      LOG.info(String.format("Selected Package Price Type in DB was: %s", packagePriceType));


      if (packagePrice == null) {
        displayNotification(
            "Error parsing the package price for package " + packsContainerRowId + "!",
            packageId + " is null. Please fix the package in the package tab.", "error");
      } else {
        packageUnitPrices.add(decimalFormatter.format(packagePrice));
      }

      float discountPercent = getDiscountForRow(packsContainer, packsContainerRowId);
      LOG.info(String.format("Selected Package Price Discount was: %f", discountPercent));
      int discount = (int) (100 * (new Float(1) - discountPercent));
      LOG.info(decimalFormatter.format(packagePrice));
      LOG.info(new BigDecimal(decimalFormatter.format(packagePrice)));
      LOG.info(new BigDecimal(discountPercent));
      BigDecimal res = new BigDecimal(decimalFormatter.format(packagePrice))
          .multiply(new BigDecimal(discountPercent));
//      System.out.println("---");
//      System.out.println(res.toString());
      String discountedUnitPrice = res.toString();
//      System.out.println(discountedUnitPrice);
//      discountedUnitPrice = decimalFormatter.format(res);
//      System.out.println(discountedUnitPrice);
//      System.out.println("---");

      discounts.add(discount);
      discountedPrices.add(discountedUnitPrice);

      Object packageAddonPrice = packsContainer
          .getContainerProperty(packsContainerRowId, "package_addon_price").getValue();
      if (packageAddonPrice == null) {
        displayNotification("Error parsing the package addon price for package " + packageId + "!",
            " package_addon_price is null. Please fix the package in the package tab.", "error");
      } else {
        packageTotalPrices.add(decimalFormatter.format(packageAddonPrice));
        // packageTotalPrices.add(nf.getCurrencyInstance().format(packageAddonPrice));
      }
    }

  }
}
