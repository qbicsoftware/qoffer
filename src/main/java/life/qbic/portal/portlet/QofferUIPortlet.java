package life.qbic.portal.portlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Layout;
import life.qbic.components.qOfferManager;
import com.vaadin.ui.HorizontalLayout;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for portlet qoffer-portlet. This class derives from {@link QBiCPortletUI}, which is
 * found in the {@code portal-utils-lib} library.
 * 
 * @see <a href="https://github.com/qbicsoftware/portal-utils-lib">portal-utils-lib</a>
 */
@Theme("mytheme")
@SuppressWarnings("serial")
@Widgetset("life.qbic.portal.portlet.AppWidgetSet")
public class QofferUIPortlet extends QBiCPortletUI {

  private static final Logger LOG = LogManager.getLogger(QofferUIPortlet.class);

  @Override
  protected Layout getPortletContent(final VaadinRequest request) {
    LOG.info("Generating content for {}", QofferUIPortlet.class);

    HorizontalLayout pLayout = new HorizontalLayout();

    try {
      qOfferManager pManager = new qOfferManager();
      pManager.setSizeFull();
      pLayout = new HorizontalLayout(pManager);
      pLayout.setWidth("100%");

    } catch (IOException e) {
      LOG.error("could not create UI");
      LOG.error(e.getMessage());
      // TODO Auto-generated catch block
      e.printStackTrace();
    }


    return pLayout;
  }
}
