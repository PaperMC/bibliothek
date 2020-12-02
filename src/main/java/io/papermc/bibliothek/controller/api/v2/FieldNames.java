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
package io.papermc.bibliothek.controller.api.v2;

public final class FieldNames {
  public static final String PROJECTS = "projects";

  public static final String PROJECT_ID = "project_id";
  public static final String PROJECT_NAME = "project_name";

  public static final String VERSION_GROUPS = "version_groups";
  public static final String VERSION_GROUP = "version_group";

  public static final String VERSIONS = "versions";
  public static final String VERSION = "version";
  public static final String BUILDS = "builds";
  public static final String BUILD = "build";
  public static final String TIME = "time";

  public static final String CHANGES = "changes";
  static final String COMMIT = "commit";
  static final String SUMMARY = "summary";
  static final String MESSAGE = "message";

  public static final String DOWNLOADS = "downloads";
  static final String NAME = "name";
  static final String SHA256 = "sha256";

  private FieldNames() {
  }
}
