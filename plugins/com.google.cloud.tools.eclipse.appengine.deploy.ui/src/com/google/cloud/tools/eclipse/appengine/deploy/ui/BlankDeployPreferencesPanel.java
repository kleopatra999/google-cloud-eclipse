package com.google.cloud.tools.eclipse.appengine.deploy.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

public class BlankDeployPreferencesPanel extends DeployPreferencesPanel {

  private Runnable facetPageSelector;

  BlankDeployPreferencesPanel(Composite parent) {
    super(parent, SWT.NONE);
    Link addFacetLink = new Link(this, SWT.WRAP);
    addFacetLink.setText(Messages.getString("add.facet.link"));
    addFacetLink.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        facetPageSelector.run();
      }
    });
    GridLayoutFactory.fillDefaults().generateLayout(this);
  }

  public void setFacetPageSelector(Runnable runnable) {
    this.facetPageSelector = runnable;
  }

  @Override
  DataBindingContext getDataBindingContext() {
    return new DataBindingContext();
  }

  @Override
  void resetToDefaults() {
  }

  @Override
  boolean savePreferences() {
    return true;
  }
}
