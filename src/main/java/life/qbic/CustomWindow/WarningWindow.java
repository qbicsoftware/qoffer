package life.qbic.CustomWindow;

import com.vaadin.ui.*;

/**
 * A warning window asks the user if he wants to proceed.
 * This is useful if irreversible actions should performed
 */
public class WarningWindow implements IWindow {

    private VerticalLayout content;

    @Override
    public Component createContent(){
        content = new VerticalLayout();
        content.setMargin(true);

        return content;
    }

    @Override
    public Layout getContentLayout(){
        return content;
    }

}
