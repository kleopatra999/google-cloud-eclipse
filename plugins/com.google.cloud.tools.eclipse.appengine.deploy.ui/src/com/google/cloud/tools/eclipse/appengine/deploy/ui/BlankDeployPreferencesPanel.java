package com.google.cloud.tools.eclipse.appengine.deploy.ui;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BlankDeployPreferencesPanel extends DeployPreferencesPanel {

  BlankDeployPreferencesPanel(Composite parent) {
    super(parent, SWT.NONE);
    Label noGAEProjectLabel = new Label(this, SWT.NONE);
    noGAEProjectLabel.setText("Not an App Engine Application");
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
