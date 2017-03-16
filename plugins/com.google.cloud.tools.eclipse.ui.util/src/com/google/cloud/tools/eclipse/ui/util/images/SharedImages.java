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

package com.google.cloud.tools.eclipse.ui.util.images;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

public class SharedImages {

  private static final ImageDescriptor refreshPngImageDescriptor =
      ImageDescriptor.createFromFile(SharedImages.class, "/icons/refresh.png");

  /**
   * Creates an {@link Image} to be used to indicate a 'refresh' action.
   * <p>
   * The caller is responsible for disposing the Image once it's not needed anymore by
   * calling {@link Image#dispose()} on the image object.
   *
   * @return the image or null if the image was not found
   */
  public static Image createRefreshIcon(Device device) {
    if (refreshPngImageDescriptor != null) {
      return refreshPngImageDescriptor.createImage(device);
    } else {
      return null;
    }
  }
}
