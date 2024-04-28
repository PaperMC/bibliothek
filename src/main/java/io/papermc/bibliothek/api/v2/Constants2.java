/*
 * This file is part of bibliothek, licensed under the MIT License.
 *
 * Copyright (c) 2019-2024 PaperMC
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
package io.papermc.bibliothek.api.v2;

import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Constants2 {
  public static final String FIELD_BUILD = "build";
  public static final String FIELD_BUILDS = "builds";
  public static final String FIELD_CHANGES = "changes";
  public static final String FIELD_CHANNEL = "channel";
  public static final String FIELD_COMMIT = "commit";
  public static final String FIELD_DOWNLOADS = "downloads";
  public static final String FIELD_MESSAGE = "message";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_PROJECTS = "projects";
  public static final String FIELD_PROJECT_ID = "project_id";
  public static final String FIELD_PROJECT_NAME = "project_name";
  public static final String FIELD_PROMOTED = "promoted";
  public static final String FIELD_SHA256 = "sha256";
  public static final String FIELD_SUMMARY = "summary";
  public static final String FIELD_TIME = "time";
  public static final String FIELD_VERSION = "version";
  public static final String FIELD_VERSIONS = "versions";
  public static final String FIELD_VERSION_GROUP = "version_group";
  public static final String FIELD_VERSION_GROUPS = "version_groups";

  @Language("RegExp")
  public static final String PATTERN_DOWNLOAD = "[a-z0-9._-]+";
  @Language("RegExp")
  public static final String PATTERN_PROJECT_ID = "[a-z]+";
  @Language("RegExp")
  public static final String PATTERN_SHA256 = "[a-f0-9]{64}";
  @Language("RegExp")
  public static final String PATTERN_VERSION = "[0-9.]+-?(?:pre|SNAPSHOT)?(?:[0-9.]+)?";

  public static final String EXAMPLE_DOWNLOAD = "paper-1.18-10.jar";
  public static final String EXAMPLE_PROJECT_ID = "paper";
  public static final String EXAMPLE_PROJECT_NAME = "Paper";
  public static final String EXAMPLE_SHA256 = "f065e2d345d9d772d5cf2a1ce5c495c4cc56eb2fcd6820e82856485fa19414c8";
  public static final String EXAMPLE_VERSION = "1.18";

  private Constants2() {
  }
}
