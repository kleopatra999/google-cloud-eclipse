package com.google.cloud.tools.eclipse.util.service;

import com.google.cloud.tools.eclipse.util.status.StatusUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class ServiceContextFactory implements IExecutableExtensionFactory, IExecutableExtension {

  private Class<?> clazz;
  private IConfigurationElement configElement;

  @Override
  public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
      throws CoreException {
    if (data == null || !(data instanceof String)) {
      throw new CoreException(StatusUtil.error(getClass(), "Data must be a class name"));
    }
    configElement = config;
    String className = (String) data;
    String bundleSymbolicName = config.getNamespaceIdentifier();
    Bundle bundle = Platform.getBundle(bundleSymbolicName);
    if (bundle == null) {
      throw new CoreException(StatusUtil.error(this, "Missing bundle " + bundleSymbolicName));
    }
    try {
      clazz = bundle.loadClass(className);
    } catch (ClassNotFoundException ex) {
      throw new CoreException(StatusUtil.error(this,
          "Could not load class " + className + " from bundle " + bundle.getSymbolicName(), ex));
    }
  }

  @Override
  public Object create() throws CoreException {
    BundleContext bundleContext = FrameworkUtil.getBundle(clazz).getBundleContext();
    IEclipseContext serviceContext = EclipseContextFactory.getServiceContext(bundleContext);
    IEclipseContext staticContext = EclipseContextFactory.create();
    staticContext.set(IConfigurationElement.class, configElement);
    try {
      return ContextInjectionFactory.make(clazz, serviceContext, staticContext);
    } finally {
      staticContext.dispose();
    }
  }
}
