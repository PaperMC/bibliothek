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

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema
public class ProjectResponse {
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
    name = "version_groups",
    pattern = "[0-9pre.-]+",
    example = "[\"1.15\", \"1.16\"]"
  )
  public List<String> versionGroups;
  @Schema(
    name = "versions",
    pattern = "[0-9pre.-]+",
    example = "[\"1.16.3\", \"1.16.4\"]"
  )
  public List<String> versions;
}
