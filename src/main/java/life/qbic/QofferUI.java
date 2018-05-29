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
package life.qbic;

import life.qbic.components.qOfferManager;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

import javax.servlet.annotation.WebServlet;

@SuppressWarnings("serial")
@Theme("valo")
public class QofferUI extends UI {

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = true, ui = QofferUI.class)
  @Widgetset("life.qbic.qoffer.widgetset.QofferWidgetset")

  public static class Servlet extends VaadinServlet {
  }

  @Override
  protected void init(VaadinRequest request) {

    qOfferManager pManager = new qOfferManager();
    setContent(pManager);

  }

  @SuppressWarnings("unused")
  private Component errorView() {
    Label label = new Label();
    label.addStyleName(ValoTheme.LABEL_FAILURE);
    label.setIcon(FontAwesome.FROWN_O);
    label
        .setValue("Initialization has failed! Are you logged out? Please try to login! If the problem continues " +
            "please contact 'helpdesk@qbic.uni-tuebingen.de'");
    return label;
  }

}
