/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.eclipse.appengine.deploy;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.util.List;

/**
 * Holds de-serialized JSON output of gcloud app deploy. Don't change the field names
 * because Gson uses it for automatic de-serialization.
 */
// TODO: move into appengine-plugins-core
// TODO expand to include other Version attributes
public class AppEngineDeployOutput {
  private static class Version {
    String id;
    String service;
  }

  private List<Version> versions;

  /**
   * @return version, can be null
   */
  public String getVersion() {
    if (versions == null || versions.size() != 1) {
      return null;
    }
    return versions.get(0).id;
  }

  /**
   * @return service, can be null
   */
  public String getService() {
    if (versions == null || versions.size() != 1) {
      return null;
    }
    return versions.get(0).service;
  }

  /**
   * Parse the raw JSON output of the deployment.
   *
   * @return the output of gcloud app deploy
   * @throws JsonParseException if unable to extract the deploy output information needed
   */
  public static AppEngineDeployOutput parseDeployOutput(String jsonOutput) throws JsonParseException {
    AppEngineDeployOutput deployOutput = new Gson().fromJson(jsonOutput, AppEngineDeployOutput.class);
    if (deployOutput == null
        || deployOutput.versions == null || deployOutput.versions.size() != 1) {
      throw new JsonParseException("Cannot get app version: unexpected gcloud JSON output format");
    }
    return deployOutput;
  }
}
