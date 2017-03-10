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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.corext.refactoring.CollectingSearchRequestor;
import org.xml.sax.SAXException;

/**
 * Validator for web.xml.
 */
public class WebXmlValidator extends AbstractXmlValidator {
  
  private static final Logger logger = Logger.getLogger(
    WebXmlValidator.class.getName());
  
  /**
   * Clears all problem markers from the resource, then adds a marker in 
   * web.xml for every {@link BannedElement} found in the file.
   */
  @Override
  protected void validate(IResource resource, byte[] bytes) 
      throws CoreException, IOException, ParserConfigurationException {
    try {
      deleteMarkers(resource);
      SaxParserResults parserResults = WebXmlSaxParser.readXml(bytes);
      Map<BannedElement, Integer> bannedElementOffsetMap =
          ValidationUtils.getOffsetMap(bytes, parserResults);
      for (Map.Entry<BannedElement, Integer> entry : bannedElementOffsetMap.entrySet()) {
        String servletClassMarker =
            "com.google.cloud.tools.eclipse.appengine.validation.undefinedServletMarker";
        if (servletClassMarker.equals(entry.getKey().getMarkerId())) {
          UndefinedServletElement element = (UndefinedServletElement) entry.getKey();
          String servletClass = element.getServletClass();
          if (findClass(servletClass, resource.getProject()) == null) {
            createMarker(resource, entry.getKey(), entry.getValue());
          }
        } else {
          createMarker(resource, entry.getKey(), entry.getValue());
        }
      }
    } catch (SAXException ex) {
      createSaxErrorMessage(resource, ex);
    }
  }
  
  /**
   * Checks for the given class name within the project.
   */
  static IType findClass(String typeName, IProject project) {
    if (typeName == null || "".equals(typeName)) {
      return null;
    }
    SearchPattern pattern = SearchPattern.createPattern(typeName, 
        IJavaSearchConstants.CLASS,
        IJavaSearchConstants.DECLARATIONS,
        SearchPattern.R_EXACT_MATCH | SearchPattern.R_ERASURE_MATCH);
    IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
        new IJavaElement[] { (IJavaElement) JavaCore.create(project) });
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
