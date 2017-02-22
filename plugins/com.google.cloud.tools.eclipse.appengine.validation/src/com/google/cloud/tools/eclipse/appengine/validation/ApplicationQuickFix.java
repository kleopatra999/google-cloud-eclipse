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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import com.google.cloud.tools.eclipse.util.Xslt;
import com.google.common.io.CharStreams;

/**
 * Applies application.xsl to appengine-web.xml to remove an <application/> element.
 */
public class ApplicationQuickFix implements IMarkerResolution {
  
  private static final Logger logger = Logger.getLogger(
      ApplicationQuickFix.class.getName());

  @Override
  public String getLabel() {
    return Messages.getString("remove.application.element");
  }

  /**
   * Attempts to edit the {@link IDocument} in the open editor. If the editor is not open,
   * reads the file from memory and transforms in place.
   */
  @Override
  public void run(IMarker marker) {
    try {
      IFile file = (IFile) marker.getResource();
      IDocument document = getCurrentDocument(file);
      URL xslPath = ApplicationQuickFix.class.getResource("/xslt/application.xsl");
      if (document != null) {
        String currentContents = document.get();
        String encoding = file.getCharset();
        InputStream documentStream = new ByteArrayInputStream(currentContents.getBytes(encoding));
        InputStream transformed = Xslt.applyXslt(documentStream, xslPath.openStream());
        String newDoc = convertStreamToString(transformed, encoding);
        document.set(newDoc);
      } else {
        Xslt.transformInPlace(file, xslPath);
      }
    } catch (IOException | TransformerException | CoreException ex) {
      logger.log(Level.SEVERE, ex.getMessage());
    }
  }
  
  /**
   * Returns {@link IDocument} in the open editor, or null if the editor
   * is not open.
   */
  IDocument getCurrentDocument(IFile file) {
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
    IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
    IEditorPart editorPart = ResourceUtil.findEditor(activePage, file);
    if (editorPart != null) {
      IDocument document = (IDocument) editorPart.getAdapter(IDocument.class);
      return document;
    }
    return null;
  }
  
  public static String convertStreamToString(InputStream is, String charset) throws IOException {
    String result = CharStreams.toString(new InputStreamReader(is, charset));
    return result;
  }
  
}
