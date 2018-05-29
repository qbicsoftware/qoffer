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
import life.qbic.dbase.DBManager;
import life.qbic.dbase.Database;

import java.sql.SQLException;
import java.util.*;

import static life.qbic.components.OfferGeneratorTab.createOfferGeneratorTab;
import static life.qbic.components.OfferManagerTab.createOfferManagerTab;
import static life.qbic.components.PackageManagerTab.createPackageManagerTab;

public class qOfferManager extends CustomComponent {

  private static Database db;
  private static TabSheet managerTabs;

  // TODO: use one list of packageBeans instead of multiple lists
  private static List<String> packageNames = new ArrayList<>();
  private static List<String> packageDescriptions = new ArrayList<>();
  private static List<String> packageCounts = new ArrayList<>();
  private static List<String> packageUnitPrices = new ArrayList<>();
  private static List<String> packageTotalPrices = new ArrayList<>();

  /**
   * 
   */
  private static final long serialVersionUID = -3847280734052653158L;

  public static Database getDb() {
    return db;
  }

  static TabSheet getManagerTabs() {
    return managerTabs;
  }

  static List<String> getPackageNames() {
    return packageNames;
  }

  static List<String> getPackageDescriptions() {
    return packageDescriptions;
  }

  static List<String> getPackageCounts() {
    return packageCounts;
  }

  static List<String> getPackageUnitPrices() {
    return packageUnitPrices;
  }

  static List<String> getPackageTotalPrices() {
    return packageTotalPrices;
  }

  public qOfferManager() {
    init();
  }

  private void init() {

    DBManager.setCredentials();
    DBManager.getDatabaseInstance();
    db = Database.Instance;
    managerTabs = new TabSheet();

/*    System.out.println(ft.format(dNow) + "  INFO  Offer Manager accessed! - User: "
        + LiferayAndVaadinUtils.getUser().getScreenName());*/

    managerTabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
    managerTabs.addStyleName(ValoTheme.TABSHEET_EQUAL_WIDTH_TABS);

    final GridLayout gridLayout = new GridLayout(6, 6);
    gridLayout.setMargin(true);
    gridLayout.setSpacing(true);

    try {

      managerTabs.addTab(createOfferGeneratorTab(), "Offer Generator");
      managerTabs.addTab(createOfferManagerTab(), "Offer Manager");
      managerTabs.addTab(createPackageManagerTab(), "Package Manager");

      managerTabs.setSelectedTab(1);  // show the offer manager first, since this will probably be mostly in use

      // TODO: make this more elegant
      // if one changes the tab e.g. from the offer manager to the package manager, creates a new package and goes
      // back to the offer manager tab, the package won't be updated -> workaround:
      // since the selected offer in the offer manager grid won't requery the database for the information needed, we
      // deselect the current offer (if any has been selected), so the user has to select the offer again -> information
      // for the database is queried again and e.g. the newly created packages are shown properly)
      managerTabs.addSelectedTabChangeListener((TabSheet.SelectedTabChangeListener) event -> {
        OfferManagerTab.getOfferManagerGrid().deselectAll();
        OfferManagerTab.getDetailsLayout().removeAllComponents();
      });

    } catch (SQLException e1) {
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
