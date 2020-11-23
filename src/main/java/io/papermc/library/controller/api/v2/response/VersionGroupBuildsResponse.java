/*
 * This file is part of library, licensed under the MIT License.
 *
 * Copyright (c) 2019-2020 PaperMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.papermc.library.controller.api.v2.response;

import io.papermc.library.database.document.Change;
import io.papermc.library.database.document.Download;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Schema
public class VersionGroupBuildsResponse {
  @Schema(
    name = "project_id",
    pattern = "[a-z]+",
    example = "paper"
  )
  public String projectId;
  @Schema(
    name = "project_name",
    example = "Paper"
  )
  public String projectName;
  @Schema(
    name = "version_group",
    pattern = "[0-9pre.-]+",
    example = "1.16"
  )
  public String versionGroup;
  @Schema(
    name = "versions",
    pattern = "[0-9pre.-]+",
    example = "[\"1.16.1\", \"1.16.2\", \"1.16.3\", \"1.16.4\"]"
  )
  public List<String> versions;
  @Schema(
    name = "builds"
  )
  public List<VersionGroupBuild> builds;

  public static class VersionGroupBuild {
    @Schema(
      name = "version",
      pattern = "[0-9pre.-]+",
      example = "1.16.4"
    )
    private String version;
    @Schema(
      name = "build",
      pattern = "\\d+",
      example = "274"
    )
    public int build;
    @Schema(
      name = "time"
    )
    public Instant time;
    @Schema(
      name = "changes"
    )
    public List<Change> changes;
    @Schema(
      name = "downloads",
      example = "{\"application\": {\"name\": \"paper-1.16.4-274.jar\", \"sha256\": \"a167fddcb40d50d1e8c913ed83bc21365691f0c006d51a38e17535fa6ecf2e53\"}}"
    )
    public Map<String, Download> downloads;
  }
}
