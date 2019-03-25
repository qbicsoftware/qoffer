package life.qbic.CustomWindow;

import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;

/**
 * A notification Window just displays information about a process
 */
public class NotificationWindow implements IWindow {

    private VerticalLayout notificationLayout;

    @Override
    public Component createContent(){
        notificationLayout = new VerticalLayout();

        notificationLayout.setHeightUndefined(); //make scrollable
        notificationLayout.setMargin(true);

        //multiple Notifications need a panel in order to be scrollable
        //note that the size is fixed for the panel and undefined for the notificationLayout
        Panel contentPanel = new Panel();
        contentPanel.setHeight(90, Sizeable.Unit.PERCENTAGE);
        contentPanel.setWidth(100, Sizeable.Unit.PERCENTAGE);
        contentPanel.setContent(notificationLayout);

        return contentPanel;
    }

    @Override
    public Layout getContentLayout(){
        return notificationLayout;
    }



}
