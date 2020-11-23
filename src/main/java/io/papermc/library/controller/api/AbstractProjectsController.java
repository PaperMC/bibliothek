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
package io.papermc.library.controller.api;

import io.papermc.library.configuration.AppConfiguration;
import io.papermc.library.database.collection.BuildCollection;
import io.papermc.library.database.collection.ProjectCollection;
import io.papermc.library.database.collection.VersionCollection;
import io.papermc.library.database.collection.VersionGroupCollection;
import io.papermc.library.database.document.Build;
import io.papermc.library.database.document.Download;
import io.papermc.library.database.document.Project;
import io.papermc.library.database.document.Version;
import io.papermc.library.database.document.VersionGroup;
import io.papermc.library.http.HTTP;
import io.papermc.library.http.error.UhOh;
import io.papermc.library.json.JsonFactory;
import io.papermc.library.http.JavaArchive;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

public abstract class AbstractProjectsController {
  protected static final CacheControl PROJECTS_CACHE = HTTP.publicCacheSMA(Duration.ofDays(7));
  protected static final CacheControl PROJECT_CACHE = HTTP.publicCacheSMA(Duration.ofMinutes(30));
  protected static final CacheControl VERSION_CACHE = HTTP.publicCacheSMA(Duration.ofMinutes(5));
  protected static final CacheControl BUILDS_CACHE = HTTP.publicCacheSMA(Duration.ofMillis(5));
  protected static final CacheControl BUILD_CACHE = HTTP.publicCacheSMA(Duration.ofDays(7));

  private final AppConfiguration downloadsConfig;

  protected final ProjectCollection projects;
  protected final VersionGroupCollection versionGroups;
  protected final VersionCollection versions;
  protected final BuildCollection builds;

  protected final JsonFactory json;

  protected AbstractProjectsController(final AppConfiguration downloadsConfig, final ProjectCollection projects, final VersionGroupCollection versionGroups, final VersionCollection versions, final BuildCollection builds, final JsonFactory json) {
    this.downloadsConfig = downloadsConfig;
    this.projects = projects;
    this.versionGroups = versionGroups;
    this.versions = versions;
    this.builds = builds;
    this.json = json;
  }

  protected ResponseEntity<?> download(final Project project, final Version version, final Build build, final String downloadName) throws UhOh {
    for(final Map.Entry<String, Download> download : build.downloads.entrySet()) {
      if(download.getValue().name.equals(downloadName)) {
        try {
          return new JavaArchive(
            this.downloadsConfig.getStoragePath()
              .resolve(project.name)
              .resolve(version.name)
              .resolve(String.valueOf(build.number))
              .resolve(downloadName)
          );
        } catch(final IOException e) {
          throw UhOh.internalServerError(error -> error.put("error", "an internal error occurred while serving your download"));
        }
      }
    }
    throw UhOh.notFound(error -> error.put("error", "no such download available"));
  }

  protected final Project getProject(final String name) throws UhOh {
    final Project project = this.projects.findByName(name);
    if(project == null) throw UhOh.notFound(error -> error.put("error", "no such project"));
    return project;
  }

  protected final VersionGroup getVersionGroup(final Project project, final String name) throws UhOh {
    final VersionGroup group = this.versionGroups.findByProjectAndName(project._id, name);
    if(group == null) throw UhOh.notFound(error -> error.put("error", "no such version group"));
    return group;
  }

  protected final Version getVersion(final Project project, final String name) throws UhOh {
    final Version version = this.versions.findByProjectAndName(project._id, name);
    if(version == null) throw UhOh.notFound(error -> error.put("error", "no such version"));
    return version;
  }

  protected final Build getBuild(final Project project, final Version version, final int number) throws UhOh {
    final Build build = this.builds.findByProjectAndVersionAndNumber(project._id, version._id, number);
    if(build == null) throw UhOh.notFound(error -> error.put("error", "no such build"));
    return build;
  }
}
