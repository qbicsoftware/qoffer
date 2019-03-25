package life.qbic.CustomWindow;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;

/**
 * This class creates a Window based on the given type
 */
public class WindowFactory{

    private String title;
    private boolean modal = true;
    private Layout contentLayout;
    private Button anotherButton;
    private Button close;


    public WindowFactory isModal(boolean modal){
        this.modal = modal;
        return this;
    }

    public WindowFactory setTitle(String title){
        this.title = title;

        return this;
    }

    /**
     * This method as an external Button.
     * This is helpful so its functionality can be controlled from outside this class
     * @param button
     * @return
     */
    public WindowFactory addButton(Button button){
        anotherButton = button;
        return this;
    }

    /**
     * Call this method after the window is created to obtain the layout where you can add notifications
     * with the method addNotifications
     * @return
     */
    public Layout getContentLayout(){
        return contentLayout;
    }


    /**
     * Call this method to create a window of the desired type (NotificationWindow or WarningWindow)
     * On default the warningWindow style is created
     * @param notification
     * @return
     */
    public Window getWindow(boolean notification){
        Window window = new Window(title);

        //customize the style (these settings could be adjusted from outside this class
        window.center();
        window.setResizable(false);
        window.setWidth(25, Sizeable.Unit.PERCENTAGE);
        window.setHeight(40, Sizeable.Unit.PERCENTAGE);
        window.setModal(modal);

        //add content
        VerticalLayout wrapperLayout = new VerticalLayout();
        window.setContent(wrapperLayout);
        wrapperLayout.setSizeFull();

        HorizontalLayout buttonBarLayout = new HorizontalLayout();
        buttonBarLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        Component content;
        IWindow customWindow;

        if (notification){
            customWindow = new NotificationWindow();
            close = createButton("close");
            enableButton(close,false);
        }
        //default will be a warning window with only one notification
        else{
            customWindow = new WarningWindow();
            close = createButton("cancel");
            buttonBarLayout.addComponent(anotherButton);
        }

        content = customWindow.createContent();
        contentLayout = customWindow.getContentLayout();

        buttonBarLayout.setWidth(100, Sizeable.Unit.PERCENTAGE);
        buttonBarLayout.addComponent(close);
        closeWindow(close, window);

        //put layouts together into one
        wrapperLayout.addComponent(content);
        wrapperLayout.addComponent(buttonBarLayout);

        wrapperLayout.setExpandRatio(buttonBarLayout, 0.1f);
        wrapperLayout.setExpandRatio(content, 0.9f);

        wrapperLayout.setMargin(true);

        return window;
    }

    /**
     * Creates the button for closing the window
     * @param name
     * @return
     */
    private Button createButton(String name){

        Button close = new Button(name);
        close.setIcon(FontAwesome.TIMES_CIRCLE);

        return close;
    }

    /**
     * This function adds a listener to the button close.
     * The button then closes the current Window.
     * @param close
     * @param window
     */
    private void closeWindow(Button close, Window window){

        close.addClickListener((Button.ClickListener) e -> {
            window.close();
        });

    }

    public void enableButton(Button button, boolean enable){
        button.setEnabled(enable);
    }

    public Button getCloseButton(){
        return close;
    }

    /**
     * Choose the style of the label that is added to a window
     * warn, failure, spin, success
     * @param type
     * @param message
     * @param notificationLayout
     */
    public static void addNotification(String type, String message, Layout notificationLayout){
        //warn, failure, spin, success
        if(type.equals("warn") || type.equals("failure") || type.equals("spin") || type.equals("success")){
            Label notification = new Label(message);
            notification.setStyleName(type);
            notificationLayout.addComponent(notification);
        }
    }




}
