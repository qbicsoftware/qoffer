package life.qbic.CustomWindow;

import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;

public interface IWindow {

    /**
     * This Method creates a Vaadin Component that will be inserted into the window with the WindowFactory
     * @return
     */
    Component createContent();
    Layout getContentLayout();

}
