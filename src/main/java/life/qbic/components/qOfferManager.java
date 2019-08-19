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

import com.vaadin.ui.*;
import com.vaadin.ui.GridLayout.OutOfBoundsException;
import com.vaadin.ui.GridLayout.OverlapsException;
import com.vaadin.ui.themes.ValoTheme;
import life.qbic.dbase.Database;
import life.qbic.portal.utils.ConfigurationManager;
import life.qbic.portal.utils.ConfigurationManagerFactory;
import life.qbic.portal.utils.PortalUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class qOfferManager extends CustomComponent {

  public static String tmpFolder;

  private static Database db;
  private TabSheet managerTabs;

  // TODO: use one list of packageBeans instead of multiple lists
  // also: setters or adders ffs! see offermanagertab for update of these lists..
  private List<String> packageNames = new ArrayList<>();
  private List<String> packageDescriptions = new ArrayList<>();
  private List<String> packageCounts = new ArrayList<>();
  private List<String> packageUnitPrices = new ArrayList<>();
  private List<String> packageTotalPrices = new ArrayList<>();
  private List<String> packageIDs = new ArrayList<>();
  
  private List<Integer> discounts = new ArrayList<>();
  private List<String> discountedPrices = new ArrayList<>();


  private static final Logger LOG = LogManager.getLogger(qOfferManager.class);

  /**
   * 
   */
  private static final long serialVersionUID = -3847280734052653158L;

  public static Database getDb() {
    return db;
  }

  TabSheet getManagerTabs() {
    return managerTabs;
  }

  List<String> getPackageNames() {
    return packageNames;
  }

  List<String> getPackageDescriptions() {
    return packageDescriptions;
  }

  List<String> getPackageCounts() {
    return packageCounts;
  }

  List<String> getPackageUnitPrices() {
    return packageUnitPrices;
  }

  List<String> getPackageTotalPrices() {
    return packageTotalPrices;
  }

  List<String> getPackageIDs() {
    return packageIDs;
  }
  
  public List<Integer> getDiscounts() {
    return discounts;
  }

  public List<String> getDiscountedPrices() {
    return discountedPrices;
  }

  public qOfferManager() throws IOException {
    try {
      init();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void init() throws IOException {
    ConfigurationManager config = ConfigurationManagerFactory.getInstance();
    tmpFolder = config.getTmpFolder();

 // initialize tmp folder
    try {
      File folder = new File(tmpFolder);
      if (!folder.exists()) {
        folder.mkdirs();
      }
    } catch (Exception e) {
      LOG.warn("unsuccessfully tried to initialize temporary folder:"+tmpFolder);
      LOG.warn(e.getMessage());
    }
    
    db = Database.getInstance();
    managerTabs = new TabSheet();

    if (PortalUtils.isLiferayPortlet()) {
      LOG.info("Offer Manager accessed! - User: {}", PortalUtils.getUser().getScreenName());
    } else {
      LOG.info("Offer Manager accessed! This is not a liferay portlet, probably testing locally");
    }

    managerTabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
    managerTabs.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

    final GridLayout gridLayout = new GridLayout(6, 6);
    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);

    try {

      OfferGeneratorTab offerGeneratorTab = new OfferGeneratorTab(this);
      OfferManagerTab offerManagerTab = new OfferManagerTab(this);
      PackageManagerTab packageManagerTab = new PackageManagerTab(offerManagerTab);

      managerTabs.addTab(offerGeneratorTab.createOfferGeneratorTab(), "Offer Generator");
      LOG.info("Created Offer Generator");
      managerTabs.addTab(offerManagerTab.createOfferManagerTab(), "Offer Manager");
      LOG.info("Created Offer Manager");
      managerTabs.addTab(packageManagerTab.createPackageManagerTab(), "Package Manager");
      LOG.info("Created Package Manager");


      managerTabs.setSelectedTab(1); // show the offer manager first, since this will probably be
                                     // mostly in use

      // TODO: make this more elegant
      // if one changes the tab e.g. from the offer manager to the package manager, creates a new
      // package and goes
      // back to the offer manager tab, the package won't be updated -> workaround:
      // since the selected offer in the offer manager grid won't requery the database for the
      // information needed, we
      // deselect the current offer (if any has been selected), so the user has to select the offer
      // again -> information
      // for the database is queried again and e.g. the newly created packages are shown properly)
      managerTabs.addSelectedTabChangeListener((TabSheet.SelectedTabChangeListener) event -> {
        offerManagerTab.getOfferManagerGrid().deselectAll();
        offerManagerTab.getDetailsLayout().removeAllComponents();
      });

    } catch (SQLException e1) {
      LOG.error("could not create Tabs");
      LOG.error(e1.getMessage());
      e1.printStackTrace();
    }

    try {
      gridLayout.addComponent(managerTabs, 0, 1, 5, 1);
    } catch (OverlapsException | OutOfBoundsException e) {
      e.printStackTrace();
    }

    gridLayout.setSizeFull();
    setCompositionRoot(gridLayout);
  }
}
