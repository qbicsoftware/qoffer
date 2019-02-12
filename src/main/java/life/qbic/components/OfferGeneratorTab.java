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

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import life.qbic.dbase.Database;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static life.qbic.utils.qOfferManagerUtils.displayNotification;
import life.qbic.portal.utils.PortalUtils;


final class OfferGeneratorTab {
	
	private static final Logger LOG = LogManager.getLogger(OfferGeneratorTab.class);

	/**
	 * creates the tab to generate the offers with the respective packages
	 * @return vaadin component holding the offer generator
	 */
	static Component createOfferGeneratorTab() {

		Database db = qOfferManager.getDb();
		TabSheet managerTabs = qOfferManager.getManagerTabs();

		ComboBox selectedProjectComboBox = new ComboBox("Select Project");
		selectedProjectComboBox.setInputPrompt("No project selected!");
		selectedProjectComboBox.setDescription("Please select a project before its too late! :P");
		selectedProjectComboBox.addItems(db.getProjects());
		selectedProjectComboBox.setWidth("300px");

		Button completeButton = new Button("Complete");
		completeButton.setDescription("Click here to finalize the offer and save it into the DB!");
		completeButton.setIcon(FontAwesome.CHECK_CIRCLE);
		completeButton.setEnabled(false);

		Button refreshButton = new Button("Refresh");
		refreshButton.setDescription("Refresh the listed available packages");
		refreshButton.setIcon(FontAwesome.SPINNER);

		// get the package ids and names as a bean container
		final BeanItemContainer<String> packageIdsAndNamesContainer = new BeanItemContainer<>(String.class);
		packageIdsAndNamesContainer.addAll(db.getPackageIdsAndNames());

		TwinColSelect selectPackagesTwinColSelect = new TwinColSelect();
		selectPackagesTwinColSelect.setContainerDataSource(packageIdsAndNamesContainer);
		selectPackagesTwinColSelect.setLeftColumnCaption("Available packages");
		selectPackagesTwinColSelect.setRightColumnCaption("Selected packages");
		selectPackagesTwinColSelect.setSizeFull();

		// text field which functions as a filter for the left side of the twin column select
		TextField twinColSelectFilter = new TextField();
		twinColSelectFilter.setCaption("Filter available packages");
		twinColSelectFilter.addTextChangeListener((FieldEvents.TextChangeListener) event -> {
			packageIdsAndNamesContainer.removeAllContainerFilters();
			packageIdsAndNamesContainer.addContainerFilter(new Container.Filter() {

				@Override
				public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
					return ((String) itemId).toLowerCase().contains(event.getText().toLowerCase())
							|| ((Collection) selectPackagesTwinColSelect.getValue()).contains(itemId);
				}

				@Override
				public boolean appliesToProperty(Object propertyId) {
					return true;
				}
			});
		});

		VerticalLayout right = new VerticalLayout();
		right.setSpacing(true);
		right.setMargin(true);

		VerticalLayout addPackLayout = new VerticalLayout();
		addPackLayout.setMargin(true);
		addPackLayout.setSpacing(true);

		Panel packageDescriptionPanel = new Panel("Package Details");
		packageDescriptionPanel.setContent(right);

		@SuppressWarnings("deprecation")
		Label packageDetailsLabel = new Label("Package details will appear here!", Label.CONTENT_XHTML);
		packageDetailsLabel.addStyleName(ValoTheme.LABEL_BOLD);
		right.addComponent(packageDetailsLabel);

		addListeners(db, managerTabs, selectedProjectComboBox, completeButton, addPackLayout,
				selectPackagesTwinColSelect, packageDescriptionPanel, packageDetailsLabel, twinColSelectFilter, refreshButton);

		addPackLayout.addComponent(selectedProjectComboBox);

		return addPackLayout;
	}

	/**
	 * adds all the listeners to the offer generator tab
	 * @param db: database instance to query
	 * @param managerTabs: TabSheet holding the three tabs (Offer Generator, Offer Manager and Package Manager)
	 * @param selectedProjectComboBox: ComboBox for selecting the project for the offer to generate
	 * @param completeButton: button to complete the offer
	 * @param addPackLayout: vertical layout which holds the twin select for selecting the packages, the package
	 *                     description panel and the button to complete the offer
	 * @param selectPackagesTwinColSelect: TwinSelect to select the packages for the current offer
	 * @param packageDescriptionPanel: panel holding the descriptions labels for the selected packages
	 * @param packageDescriptionLabel: label holding the description for the packages
	 * @param twinColSelectFilter: text field for filtering the left side of the TwinColSelect
	 */
	private static void addListeners(Database db, TabSheet managerTabs, ComboBox selectedProjectComboBox,
			Button completeButton, VerticalLayout addPackLayout,
			TwinColSelect selectPackagesTwinColSelect, Panel packageDescriptionPanel,
			Label packageDescriptionLabel, TextField twinColSelectFilter, Button refreshButton) {

		final float[] totalPrice = new float[1];
		final String[] descriptionText = new String[1];
		final String[][] offerGeneratorPackageNames = new String[1][1];
		final String[][] offerGeneratorPackageIds = new String[1][1];

		selectedProjectComboBox.addValueChangeListener(new Property.ValueChangeListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = 6871999698387032993L;

			@Override
			public void valueChange(Property.ValueChangeEvent event) {
				if (selectedProjectComboBox.getValue() == null) {
					displayNotification(
							"oO! No project selected!",
							"Please select a project to proceed, otherwise we are going to have a situation here! ;)",
							"error");
				} else {
					addPackLayout.addComponent(twinColSelectFilter);
					addPackLayout.addComponent(selectPackagesTwinColSelect);
					addPackLayout.addComponent(refreshButton);
					addPackLayout.addComponent(packageDescriptionPanel);
					addPackLayout.addComponent(completeButton);
				}
			}
		});

		selectPackagesTwinColSelect.addValueChangeListener(new Property.ValueChangeListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = -5813954665588509117L;

			@Override
			public void valueChange(Property.ValueChangeEvent event) {

				// get the packages (package_id: package_name) as comma separated string and remove the leading and trailing bracket
				String selectedPackages = selectPackagesTwinColSelect.getValue().toString().substring(1);
				selectedPackages = selectedPackages.substring(0, selectedPackages.length() - 1);

				// in case there is no package selected, we do nothing
				if (selectedPackages.length() == 0) {
					return;
				}

				// init the arrays holding the package names and package ids
				offerGeneratorPackageNames[0] = selectedPackages.split(",");
				offerGeneratorPackageIds[0] = selectedPackages.split(",");

				// split the package id and the package name
				for (int i = 0; i < offerGeneratorPackageNames[0].length; i++) {
					offerGeneratorPackageIds[0][i] = offerGeneratorPackageIds[0][i].split(": ", 2)[0];
					offerGeneratorPackageNames[0][i] = offerGeneratorPackageNames[0][i].split(": ", 2)[1];
				}

				// TODO: value change listener does not trigger if the last element from the twin col select gets deselected
				System.out.println("packages in twin col select:");
				System.out.println(selectPackagesTwinColSelect.getValue());

				// there is a package for the current project, so we enable the complete offer button
				if (selectPackagesTwinColSelect.getValue() != null) {
					completeButton.setEnabled(true);
				} else {
					completeButton.setEnabled(false);
				}

				assert offerGeneratorPackageNames[0].length == offerGeneratorPackageIds[0].length;

				// iterate over all of the packages for the current offer and add their price and description to the view
				descriptionText[0] = "";
				totalPrice[0] = 0;
				for (int i = 0; i < offerGeneratorPackageNames[0].length; i++) {

					int currentPackageId = Integer.valueOf(offerGeneratorPackageIds[0][i].trim());

					// calculate the total offer price
					try {
						totalPrice[0] +=
								Float.parseFloat(db.getPriceFromPackageId(currentPackageId, "internal"));
					} catch (NullPointerException e) {
						displayNotification("Package price is null!", "The package price of the package "
								+ offerGeneratorPackageIds[0][i] + ": " + offerGeneratorPackageNames[0][i] + " is null. Please remove the "
								+ "package from the offer or update the " + "package price on the package manager tab. Otherwise "
								+ "bad stuff is expected to happen..", "error");
					}

					// get the description for the current package
					descriptionText[0] =
							descriptionText[0]
									+ "<tr><td><p><b>"
									+ offerGeneratorPackageIds[0][i] + ": " + offerGeneratorPackageNames[0][i]
											+ "</b><br>"
											+ db.getPackageDescriptionFromPackageId(currentPackageId)
											+ "</td><td align='right' valign='top'>€"
											+ db.getPriceFromPackageId(currentPackageId, "internal") + "</td>" + "</p></tr>";

					// add the description and the total price to the view
					if (offerGeneratorPackageNames[0][i].length() != 0)
						packageDescriptionLabel
						.setValue("<table width='100%'><tr><td><p style='color:red;'><b>Package Name and Description</b></p>" +
								"</td><td align='right'><p style='color:red;'><b>Price</b></p></td></tr><tr> </tr>"
								+ descriptionText[0]
										+ "<tr><td><p style='color:red;'><b>Grand Total</b> (excl. Taxes)</p></td><td align='right'>" +
										"<p style='color:red;'><b>€"
										+ totalPrice[0] + "</b></p></td></tr></table>");
					else
						packageDescriptionLabel.setValue("No description available!");
				}
			}

		});

		refreshButton.addClickListener(new Button.ClickListener() {

			//private static final long serialVersionUID = 8181926819540586585L;


			@Override
			public void buttonClick(Button.ClickEvent event){
				//refresh the content of the left TwinCol (read DB again)
				final BeanItemContainer<String> packageIdsAndNamesContainer = new BeanItemContainer<>(String.class);
				ArrayList<String> completePackageList = db.getPackageIdsAndNames();

				packageIdsAndNamesContainer.addAll(completePackageList);
				Object selectedValues = selectPackagesTwinColSelect.getValue();

				//update list:
				selectPackagesTwinColSelect.setContainerDataSource(packageIdsAndNamesContainer);
				//select what was already selected
				selectPackagesTwinColSelect.setValue(selectedValues);

			}
		});

		completeButton.addClickListener(new Button.ClickListener() {

			/**
			 *
			 */
			private static final long serialVersionUID = 8181926819540586585L;

			@Override
			public void buttonClick(Button.ClickEvent event) {

				// get some fields for the current offer
				String offerProjectReference = selectedProjectComboBox.getValue().toString();
				String dateToday = new SimpleDateFormat("yyyyMMdd").format(new Date());
				String offerNumber = dateToday + "_" + offerProjectReference;
				String offerFacility = db.getPIFromProjectRef(offerProjectReference);
				String offerName = db.getShortTitleFromProjectRef(offerProjectReference);
				String offerDescription = db.getLongDescFromProjectRef(offerProjectReference);
				int offerId;
				
				LOG.info("is liferayPortlet()? "+PortalUtils.isLiferayPortlet());

				if(PortalUtils.isLiferayPortlet()) {
					offerId =
							db.registerNewOffer(offerNumber, offerProjectReference, offerFacility, offerName,
									offerDescription, totalPrice[0], new Date(),
									PortalUtils.getUser().getScreenName(), true);//externalPriceSelectedCheckBox.getValue() -> has no functionallity anymore
				}else {
					// register the new offer in the database; NOTE: internal has no functionality anymore, so we simply set it to
					// true
					offerId =
							db.registerNewOffer(offerNumber, offerProjectReference, offerFacility, offerName,
									offerDescription, totalPrice[0], new Date(), "temp", true);				
				}


				// iterate over the packages from the current offer and insert them also in the database
				for (String packageId : offerGeneratorPackageIds[0]) {
					//here NullPointer Exception
					LOG.info("packageID "+packageId);
					packageId = packageId.trim(); //remove whitespaces -> error
					int currentPackageId = Integer.valueOf(packageId);
					float packageUnitPrice =
							Float.valueOf(db.getPriceFromPackageId(currentPackageId, "internal"));
					db.insertOrUpdateOffersPackages(offerId, currentPackageId, packageUnitPrice);
				}

				displayNotification(
						"Perfect! Offer successfully saved in the Database!",
						"Next step is to modify, finalize and send it to the customer. Keep in mind that the " +
								"description of the offer is still missing. Please go ahead and complete it. Fingers crossed!",
						"success");

				// change view to offer manager
				managerTabs.setSelectedTab(1);
			}
		});
	}
}
