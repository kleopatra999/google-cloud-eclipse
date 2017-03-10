/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.tools.eclipse.appengine.validation;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.CollectingSearchRequestor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.Locator2;

/**
 * Adds <web-app> element to {@link BannedElement} queue if the Servlet version is not 2.5.
 */
class WebXmlScanner extends AbstractScanner {

  private static final Logger logger = Logger.getLogger(WebXmlScanner.class.getName());
  private boolean insideServletClass;
  private StringBuilder servletClassContents;
  private DocumentLocation servletClassLocation;
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException {
    // Checks for expected namespace URI. Assume something else is going on if
    // web.xml has an unexpected root namespace.
    if ("web-app".equalsIgnoreCase(localName) && ("http://xmlns.jcp.org/xml/ns/javaee".equals(uri)
        || "http://java.sun.com/xml/ns/javaee".equals(uri))) {
      String version = attributes.getValue("version");
      if (!version.equals("2.5")) {
        Locator2 locator = getLocator();
        DocumentLocation start = new DocumentLocation(locator.getLineNumber(),
            locator.getColumnNumber());
        addToBlacklist(new JavaServletElement(Messages.getString("web.xml.version"), start, 0));
      }
    }
    if ("servlet-class".equalsIgnoreCase(localName)) {
      Locator2 locator = getLocator();
      insideServletClass = true;
      servletClassContents = new StringBuilder();
      servletClassLocation = new DocumentLocation(locator.getLineNumber(),
          locator.getColumnNumber() - localName.length() - 2);
    }
  }
  
  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    if (insideServletClass) {
      servletClassContents.append(ch, start, length);
    }
  }
  
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if ("servlet-class".equalsIgnoreCase(localName)) {
      insideServletClass = false;
      String servletClassName = servletClassContents.toString();
      if (findClass(servletClassName) == null) {
        addToBlacklist(new UndefinedServletElement(
            servletClassName, servletClassLocation, localName.length() + 2));
      }
    }
  }
  
  /**
   * Checks for the given class name within the project.
   */
  static IType findClass(String typeName) {
    if (typeName == null || "".equals(typeName)) {
      return null;
    }
    SearchPattern pattern = SearchPattern.createPattern(typeName, 
        IJavaSearchConstants.CLASS,
        IJavaSearchConstants.DECLARATIONS,
        SearchPattern.R_EXACT_MATCH | SearchPattern.R_ERASURE_MATCH);
    IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
    CollectingSearchRequestor requestor = new CollectingSearchRequestor();
    performSearch(pattern, scope, requestor, null);
    List<SearchMatch> results = requestor.getResults();
    return results.isEmpty() ? null : (IType) results.get(0).getElement();
  }
  
  /**
   * Performs the search for a class of a given pattern within the project.
   */
  static void performSearch(SearchPattern pattern, IJavaSearchScope scope, SearchRequestor requestor,
      IProgressMonitor monitor) {
    SearchEngine searchEngine = new SearchEngine();
    try {
      searchEngine.search(pattern,
          new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() },
          scope, requestor, monitor);
    } catch (CoreException ex) {
      logger.log(Level.SEVERE, ex.getMessage());
    }
  }
  
}
