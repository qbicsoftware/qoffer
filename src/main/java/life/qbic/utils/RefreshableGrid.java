package life.qbic.utils;

import com.vaadin.data.Container;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.server.Extension;
import com.vaadin.server.communication.data.RpcDataProviderExtension;
import com.vaadin.ui.Grid;

import java.util.Collection;

/**
 * Implements a grid component for displaying tabular data, where the grid is automatically refreshed if something
 * changes.
 *
 * See https://vaadin.com/forum/thread/9683271
 *
 */
public class RefreshableGrid extends Grid {

  public RefreshableGrid() {
    super();
  }

  public RefreshableGrid(final Container.Indexed dataSource) {
    super(dataSource);
  }

  @Override
  public void saveEditor() throws FieldGroup.CommitException {
    super.saveEditor();
    refreshVisibleRows();
  }

  private void refreshVisibleRows() {
    Collection<Extension> extensions = getExtensions();
    for (Extension extension : extensions) {
      if (extension instanceof RpcDataProviderExtension) {
        ((RpcDataProviderExtension) extension).refreshCache();
      }
    }
  }
}
