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

import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.sqlcontainer.SQLContainer;
import com.vaadin.data.util.sqlcontainer.query.TableQuery;
import com.vaadin.server.*;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.ui.*;
import life.qbic.dbase.DBManager;
import life.qbic.dbase.Database;
import life.qbic.utils.RefreshableGrid;
import org.vaadin.gridutil.cell.GridCellFilter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;

//import static life.qbic.components.OfferManagerTab.getPathOnServer;
import static life.qbic.utils.qOfferManagerUtils.createExportContent;
import static life.qbic.utils.qOfferManagerUtils.displayNotification;

final class PackageManagerTab {

  // price modifiers for project management and bioinformatics analysis
  private static BigDecimal externalAcademicPriceModifier = new BigDecimal("1.3");
  private static BigDecimal externalCommercialPriceModifier = new BigDecimal("2.0");

  // price modifier for sequencing and mass spectrometry packages
  private static BigDecimal externalPriceModifier = new BigDecimal("1.1");

  private static FileDownloader fileDownloader;
  //private static String pathOnServer = getPathOnServer();

  /**
   * creates the tab for creating new packages
   * @return vaadin component
   * @throws SQLException:
 * @throws IOException 
   */
  static Component createPackageManagerTab() throws SQLException, IOException {

    Database db = qOfferManager.getDb();

    VerticalLayout packManVerticalLayout = new VerticalLayout();
    packManVerticalLayout.setMargin(true);
    packManVerticalLayout.setSpacing(true);
    packManVerticalLayout.setSizeFull();

    HorizontalLayout packManHorizontalLayout = new HorizontalLayout();
    packManHorizontalLayout.setSpacing(true);

    Button addPackageButton = new Button("New Package");
    addPackageButton.setIcon(FontAwesome.PLUS);
    addPackageButton.setDescription("Click here to add a new package but don't forget to update the details.");

    ComboBox updatePackageGroupComboBox = new ComboBox("Select package group");
    updatePackageGroupComboBox.addItems("Sequencing", "Project Management", "Bioinformatics Analysis",
        "Mass spectrometry", "Other");
    updatePackageGroupComboBox.setDescription("Select a package group for the currently selected package and hit the " +
        "update button.");

    Button updateSelectedPackageButton = new Button("Update");
    updateSelectedPackageButton.setIcon(FontAwesome.SPINNER);
    updateSelectedPackageButton.setDescription("Click here to update the currently selected package.");

    Button deleteSelectedPackageButton = new Button("Delete");
    deleteSelectedPackageButton.setIcon(FontAwesome.TRASH_O);
    deleteSelectedPackageButton.setDescription("Click here to delete the currently selected package.");

    Button exportTableButton = new Button("Export as .csv");
    exportTableButton.setIcon(FontAwesome.DOWNLOAD);
    exportTableButton.setDescription("Click here to export the table as .csv file.");

    CheckBox calculatePricesAutomaticallyCheckBox = new CheckBox("Auto-calculate external prices");
    calculatePricesAutomaticallyCheckBox.setDescription("Click here to enable/disable the automatic calculation of the " +
        "external prices based on the internal prices.");
    calculatePricesAutomaticallyCheckBox.setValue(true);

    TableQuery tq = new TableQuery("packages", DBManager.getDatabaseInstanceAlternative());
    tq.setVersionColumn("OPTLOCK");

    SQLContainer container = new SQLContainer(tq);
    container.setAutoCommit(true);

    GeneratedPropertyContainer gpcontainer = new GeneratedPropertyContainer(container);

    // create the column holding the offer_ids where the package is being used in
    // TODO: offer_ids currently not in use; since it's extremely slow..
/*
    gpcontainer.addGeneratedProperty("offer_ids",
        new PropertyValueGenerator<String>() {
          @Override
          public String getValue(Item item, Object itemId,
                                 Object propertyId) {

            int package_id = (Integer) item.getItemProperty("package_id").getValue();

            // query offers_packages for all offer_ids of the current package
            ArrayList<String> offerIds = db.getOfferIdsForPackage(package_id);

            return String.join(",", offerIds);
          }

          @Override
          public Class<String> getType() {
            return String.class;
          }
        });
*/

    RefreshableGrid packageGrid = new RefreshableGrid(gpcontainer);

    // add the filters to the grid
    GridCellFilter filter = new GridCellFilter(packageGrid);
    filter.setTextFilter("package_id", true, true);
    filter.setTextFilter("package_name", true, false);
    filter.setTextFilter("package_facility", true, false);
    filter.setTextFilter("package_description", true, false);
    filter.setTextFilter("package_group", true, false);
    filter.setTextFilter("package_unit_type", true, false);
    filter.setComboBoxFilter("package_group", Arrays.asList("Bioinformatics Analysis",
        "Mass spectrometry", "Project Management", "Sequencing", "Other"));

    packageGrid.getColumn("package_id").setHeaderCaption("Id").setWidth(100);
    packageGrid.getColumn("package_name").setHeaderCaption("Name");
    packageGrid.getColumn("package_facility").setHeaderCaption("Facility");
    packageGrid.getColumn("package_description").setHeaderCaption("Description").setWidth(300);
    packageGrid.getColumn("package_group").setHeaderCaption("Package Group").setEditable(false);
    packageGrid.getColumn("package_price_internal").setHeaderCaption("Internal Price (€)");
    packageGrid.getColumn("package_price_external_academic").setHeaderCaption("Ext. Academical Price (€)");
    packageGrid.getColumn("package_price_external_commercial").setHeaderCaption("Ext. Commercial Price (€)");
    // TODO: offer_ids currently not in use; since it's extremely slow..
    //packageGrid.getColumn("offer_ids").setHeaderCaption("Offer Id's");
    packageGrid.getColumn("package_unit_type").setHeaderCaption("Unit Type");

    /*
    // TODO: offer_ids currently not in use; since it's extremely slow..
    packageGrid.setColumnOrder("package_id", "package_name", "package_description", "package_group", "package_facility",
        "package_price_internal", "package_price_external_academic", "package_price_external_commercial",
        "package_unit_type", "offer_ids");*/

    packageGrid.setColumnOrder("package_id", "package_name", "package_description", "package_group", "package_facility",
        "package_price_internal", "package_price_external_academic", "package_price_external_commercial",
        "package_unit_type");

    packageGrid.removeColumn("added_by");
    packageGrid.removeColumn("package_grp");
    packageGrid.removeColumn("package_date");
    packageGrid.removeColumn("last_edited");

    packageGrid.sort("package_name", SortDirection.ASCENDING);
    packageGrid.setWidth("100%");
    packageGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
    packageGrid.setEditorEnabled(true);

    addAutomaticPriceCalculation(calculatePricesAutomaticallyCheckBox, container, packageGrid);

    // add tooltips to the cells
    packageGrid.setCellDescriptionGenerator((Grid.CellDescriptionGenerator) cell -> {
      if (cell.getValue() == null)
        return null;
      return cell.getValue().toString();
    });

    // add tooltips to the header row
    for (Grid.Column column : packageGrid.getColumns()) {
      Grid.HeaderCell cell = packageGrid.getDefaultHeaderRow().getCell(
          column.getPropertyId());
      String htmlWithTooltip = String.format(
          "<span title=\"%s\">%s</span>", cell.getText(),
          cell.getText());
      cell.setHtml(htmlWithTooltip);
    }

    addListeners(db, addPackageButton, updatePackageGroupComboBox, updateSelectedPackageButton, deleteSelectedPackageButton,
        container, packageGrid, exportTableButton);

    packManHorizontalLayout.addComponent(addPackageButton);
    packManHorizontalLayout.addComponent(updatePackageGroupComboBox);
    packManHorizontalLayout.addComponent(updateSelectedPackageButton);
    packManHorizontalLayout.addComponent(deleteSelectedPackageButton);
    packManHorizontalLayout.addComponent(exportTableButton);
    packManHorizontalLayout.addComponent(calculatePricesAutomaticallyCheckBox);

    packManHorizontalLayout.setComponentAlignment(addPackageButton, Alignment.BOTTOM_CENTER);
    packManHorizontalLayout.setComponentAlignment(updatePackageGroupComboBox, Alignment.MIDDLE_CENTER);
    packManHorizontalLayout.setComponentAlignment(updateSelectedPackageButton, Alignment.BOTTOM_CENTER);
    packManHorizontalLayout.setComponentAlignment(deleteSelectedPackageButton, Alignment.BOTTOM_CENTER);
    packManHorizontalLayout.setComponentAlignment(calculatePricesAutomaticallyCheckBox, Alignment.MIDDLE_CENTER);
    packManHorizontalLayout.setComponentAlignment(exportTableButton, Alignment.BOTTOM_CENTER);

    packManVerticalLayout.addComponent(packageGrid);
    packManVerticalLayout.addComponent(packManHorizontalLayout);

    return packManVerticalLayout;
  }

  /**
   * adds the automatic price calculation to the grid
   * @param calculatePricesAutomatically: checkbox whether the prices should be automatically calculated or not
   * @param container: sql container for getting the currently selected row
   * @param packageGrid: grid showing the packages
   */
  private static void addAutomaticPriceCalculation(CheckBox calculatePricesAutomatically, SQLContainer container,
                                                   RefreshableGrid packageGrid) {

    // TODO: make this more efficient + not a workaround any more
    // add the automatic price calculation for the external academic and external commercial prices to the package.
    // Since vaadin doesn't provide a proper ValueChangeListener for the grid, we have to do it this way..
    // Note: this recalculates the prices whenever some package has been edited by the user, which is pretty bad,
    // but I couldn't find a better way (a ValueChangeListener for the editor field of the internal_price column did
    // work even worse, so I decided to stick with this..).
    packageGrid.getEditorFieldGroup().addCommitHandler(new FieldGroup.CommitHandler() {
      @Override
      public void preCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {

      }

      @Override
      public void postCommit(FieldGroup.CommitEvent commitEvent) throws FieldGroup.CommitException {

        if (calculatePricesAutomatically.getValue()) {

          // Get the internal package price from the selected row in the grid
          Object selected = ((Grid.SingleSelectionModel) packageGrid.getSelectionModel()).getSelectedRow();
          if (selected == null) {
            displayNotification("Price could not be automatically calculated", "Vaadin couldn't get " +
                "the selected row, so the external prices have NOT been automatically calculated. If you wanted to " +
                "update the prices, please select the row via left click and then try shift + enter to open the edit " +
                "menu. Unfortunately this seems to be a little buggy..", "warning");
            return;
          }
          Item selectedRow = container.getItem(selected);
          String packagePriceInternalString = selectedRow.getItemProperty("package_price_internal").getValue().toString();

          // get the internal price as BigDecimal to deal with floating point issues
          BigDecimal packagePriceInternal = new BigDecimal(packagePriceInternalString);

          // check if we have a package group for the current package
          Object packageGroupObject = selectedRow.getItemProperty("package_group").getValue();
          if (packageGroupObject == null) {
            displayNotification("No package group", "Package has no package group associated with it. " +
                "Thus an automatic calculation of the external packages is NOT possible. Please update the package group" +
                    "or disable the Auto-calculate external prices checkbox.",
                "warning");
            return;
          }

          String packageGroup = packageGroupObject.toString();

          // based on the package group we have differnet price modifiers:
          Float packagePriceExternalAcademic;
          Float packagePriceExternalCommercial;
          switch (packageGroup) {
            case "Bioinformatics Analysis":
            case "Project Management":
              // recalculate the two external prices (*1.3 and *2.0)
              packagePriceExternalAcademic = packagePriceInternal.multiply(externalAcademicPriceModifier).floatValue();
              packagePriceExternalCommercial = packagePriceInternal.multiply(externalCommercialPriceModifier).floatValue();
              break;
            case "Sequencing":
            case "Mass spectrometry":
              // recalculate the two external prices (*1.1)
              packagePriceExternalAcademic = packagePriceInternal.multiply(externalPriceModifier).floatValue();
              packagePriceExternalCommercial = packagePriceInternal.multiply(externalPriceModifier).floatValue();
              break;
            default:  // package group is "Other"
              displayNotification("Wrong package group", "Package has package group \"Other\" " +
                      "associated with it. Thus an automatic calculation of the external packages is NOT possible. " +
                      "Please change the package group or disable the Auto-calculate external prices checkbox.",
                  "warning");
              return;
          }

          // set the respective fields in the grid, which also updates the database
          selectedRow.getItemProperty("package_price_external_academic").setValue(packagePriceExternalAcademic);
          selectedRow.getItemProperty("package_price_external_commercial").setValue(packagePriceExternalCommercial);
        }
      }
    });
  }

  /**
   * adds the listeners to the three buttons
   * @param db: database instance to query
   * @param addPackageButton: button for creating a new package
   * @param updatePackageGroupComboBox: combo box for selecting the package group
   * @param updateSelectedPackageButton: button for updating a package
   * @param deleteSelectedPackageButton: button for deleting a package
   * @param container: SQLContainer holding the data from the database
   * @param packageGrid: grid holding the packages
   * @param exportTableButton: button for exporting the grid as csv
 * @throws IOException 
   */
  private static void addListeners(Database db, Button addPackageButton, ComboBox updatePackageGroupComboBox,
                                   Button updateSelectedPackageButton, Button deleteSelectedPackageButton,
                                   SQLContainer container, RefreshableGrid packageGrid, Button exportTableButton) throws IOException {

    addPackageButton.addClickListener(new Button.ClickListener() {

      /**
       *
       */
      private static final long serialVersionUID = 8181926819540586585L;

      @Override
      public void buttonClick(Button.ClickEvent event) {
        DBManager.getDatabaseInstance().addNewPackage("*** New Package - double click to edit ***");
        displayNotification(
            "New Package Added",
            "Please edit the package details! If the details are not complete, incompatibility " +
                "issues are expected to happen.",
            "success");
        packageGrid.clearSortOrder();
        packageGrid.sort("package_name", SortDirection.ASCENDING);
      }
    });

    updateSelectedPackageButton.addClickListener((Button.ClickListener) event -> {

      if (packageGrid.getSelectedRow() == null) {
        displayNotification("oOps! Forgot something?!",
            "Please make sure that you select a package to update.", "error");
      }
      else if (updatePackageGroupComboBox.getValue() == null) {
        displayNotification("oOps! Forgot something?!",
            "Please make sure that you select an option for the package group.", "error");
      }
      else {
        String selectedPackageGroup = updatePackageGroupComboBox.getValue().toString();
        String packageId = packageGrid.getSelectedRow().toString();
        db.updatePackageGroupForPackage(selectedPackageGroup, packageId);
        container.refresh();
      }
    });

    deleteSelectedPackageButton.addClickListener((Button.ClickListener) event -> {
      Object selectedRow = packageGrid.getSelectedRow();
      if (selectedRow == null) {
        displayNotification("No package selected!", "Please select a package to delete.", "error");
        return;
      }

      int selectedPackageId = (int) packageGrid.getContainerDataSource().getItem(selectedRow)
          .getItemProperty("package_id").getValue();

      // check if package is used in a offer
      boolean isPackageSelected = db.isPackageSelectedForAnyOffer(selectedPackageId);
      if (isPackageSelected) {
        // get the first offer_id from the offers where the package is in use
        int offerId = db.getFirstOfferIdForPackageId(selectedPackageId);
        displayNotification("Package in use", "Package " + selectedPackageId + " is used by offer " +
                offerId + " ! Please remove the package from the offer before deleting it.",
            "error");
        return;
      }

      db.deletePackage(selectedPackageId);
      // since refreshing the rows doesn't work properly; we force an update of the grid by setting the sort direction
      // of the package name column
      packageGrid.sort("package_name", SortDirection.ASCENDING);
      displayNotification("Package deleted", "Package " + selectedPackageId + " successfully deleted.",
          "success");
    });

    // setup the export as .csv file functionality
    //String exportPackagesFileName =  pathOnServer + "packages.csv";
    File tempFile = File.createTempFile("packages", ".csv");
    String filePath = tempFile.getAbsolutePath();
    fileDownloader = new FileDownloader(new FileResource(tempFile))
    
    {
      @Override
      public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path) throws IOException
      {
        createExportContent(container, filePath, fileDownloader);
        return super.handleConnectorRequest(request, response, path);
      }
    };
    fileDownloader.extend(exportTableButton);

  }
}
