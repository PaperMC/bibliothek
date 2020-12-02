/*
 * This file is part of bibliothek, licensed under the MIT License.
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
package io.papermc.bibliothek.controller.api.v2.response;

import io.papermc.bibliothek.database.document.Change;
import io.papermc.bibliothek.database.document.Download;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static io.papermc.bibliothek.controller.api.v2.FieldNames.BUILD;
import static io.papermc.bibliothek.controller.api.v2.FieldNames.CHANGES;
import static io.papermc.bibliothek.controller.api.v2.FieldNames.DOWNLOADS;
import static io.papermc.bibliothek.controller.api.v2.FieldNames.PROJECT_ID;
import static io.papermc.bibliothek.controller.api.v2.FieldNames.PROJECT_NAME;
import static io.papermc.bibliothek.controller.api.v2.FieldNames.TIME;
import static io.papermc.bibliothek.controller.api.v2.FieldNames.VERSION;

@Schema
public class BuildResponse {
  @Schema(
    name = PROJECT_ID,
    pattern = "[a-z]+",
    example = "paper"
  )
  public String projectId;
  @Schema(
    name = PROJECT_NAME,
    example = "Paper"
  )
  public String projectName;
  @Schema(
    name = VERSION,
    pattern = "[0-9pre.-]+",
    example = "1.16.4"
  )
  public String version;
  @Schema(
    name = BUILD,
    pattern = "\\d+",
    example = "274"
  )
  public int build;
  @Schema(
    name = TIME
  )
  public Instant time;
  @Schema(
    name = CHANGES
  )
  public List<Change> changes;
  @Schema(
    name = DOWNLOADS,
    example = "{\"application\": {\"name\": \"paper-1.16.4-274.jar\", \"sha256\": \"a167fddcb40d50d1e8c913ed83bc21365691f0c006d51a38e17535fa6ecf2e53\"}}"
  )
  public Map<String, Download> downloads;
}
