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
package io.papermc.bibliothek.controller.api.v1;

import io.papermc.bibliothek.configuration.AppConfiguration;
import io.papermc.bibliothek.controller.api.AbstractProjectsController;
import io.papermc.bibliothek.controller.api.v2.ProjectsController;
import io.papermc.bibliothek.database.collection.BuildCollection;
import io.papermc.bibliothek.database.collection.ProjectCollection;
import io.papermc.bibliothek.database.collection.VersionCollection;
import io.papermc.bibliothek.database.collection.VersionGroupCollection;
import io.papermc.bibliothek.database.document.Build;
import io.papermc.bibliothek.database.document.Project;
import io.papermc.bibliothek.database.document.Version;
import io.papermc.bibliothek.http.HTTP;
import io.papermc.bibliothek.http.error.UhOh;
import io.papermc.bibliothek.json.JsonFactory;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Deprecated(forRemoval = true)
@Hidden
@RestController("projectsControllerV1")
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectsControllerV1 extends AbstractProjectsController {
  @Autowired
  private ProjectsControllerV1(final AppConfiguration downloadsConfig, final ProjectCollection projects, final VersionGroupCollection versionGroups, final VersionCollection versions, final BuildCollection builds, final JsonFactory json) {
    super(downloadsConfig, projects, versionGroups, versions, builds, json);
  }

  @GetMapping("/v1/{projectName:[a-z]+}")
  public ResponseEntity<?> project(final @PathVariable String projectName) throws UhOh {
    final Project project = this.getProject(projectName);
    final List<Version> versions = this.versions.findAllByProject(project._id);
    return HTTP.ok(
      this.json.object(json -> {
        json.put("project", project.name);
        json.set("versions", this.json.array(versionsJson -> {
          final List<Version> reverse = ProjectsController.sortVersions(versions);
          Collections.reverse(reverse);
          for(final Version version : reverse) {
            versionsJson.add(version.name);
          }
        }));
      }),
      CACHE_30_MINUTES
    );
  }

  @GetMapping("/v1/{project:[a-z]+}/{version:[0-9pre.-]+}")
  public ResponseEntity<?> version(final @PathVariable("project") String projectName, final @PathVariable("version") String versionName) throws UhOh {
    final Project project = this.getProject(projectName);
    final Version version = this.getVersion(project, versionName);
    final List<Build> builds = this.builds.findAllByProjectAndVersion(project._id, version._id);
    return HTTP.ok(
      this.json.object(json -> {
        json.put("project", project.name);
        json.put("version", version.name);
        json.set("builds", this.json.object(buildsJson -> {
          buildsJson.put("latest", builds.get(builds.size() - 1).number);
          buildsJson.set("all", this.json.array(allJson -> {
            final List<Build> reverse = new ArrayList<>(builds);
            Collections.reverse(reverse);
            for(final Build build : reverse) {
              allJson.add(build.number);
            }
          }));
        }));
      }),
      CACHE_5_MINUTES
    );
  }

  @GetMapping("/v1/{project:[a-z]+}/{version:[0-9pre.-]+}/{build:\\d+}")
  public ResponseEntity<?> build(final @PathVariable("project") String projectName, final @PathVariable("version") String versionName, final @PathVariable("build") int buildNumber) throws UhOh {
    final Project project = this.getProject(projectName);
    final Version version = this.getVersion(project, versionName);
    final Build build = this.getBuild(project, version, buildNumber);
    return this.build(project, version, build, CACHE_7_DAYS);
  }

  @GetMapping("/v1/{project:[a-z]+}/{version:[0-9pre.-]+}/{build:\\d+}/download")
  public ResponseEntity<?> buildDownload(final @PathVariable("project") String projectName, final @PathVariable("version") String versionName, final @PathVariable("build") int buildNumber) throws UhOh {
    final Project project = this.getProject(projectName);
    final Version version = this.getVersion(project, versionName);
    final Build build = this.getBuild(project, version, buildNumber);
    final String downloadName = this.downloadName(project, version, build);
    return this.download(project, version, build, downloadName, CACHE_7_DAYS);
  }

  @GetMapping("/v1/{project:[a-z]+}/{version:[0-9pre.-]+}/latest")
  public ResponseEntity<?> latestBuild(final @PathVariable("project") String projectName, final @PathVariable("version") String versionName) throws UhOh {
    final Project project = this.getProject(projectName);
    final Version version = this.getVersion(project, versionName);
    final Build build = this.getLatestBuild(project, version);
    return this.build(project, version, build, CACHE_5_MINUTES);
  }

  @GetMapping("/v1/{project:[a-z]+}/{version:[0-9pre.-]+}/latest/download")
  public ResponseEntity<?> latestBuildDownload(final @PathVariable("project") String projectName, final @PathVariable("version") String versionName) throws UhOh {
    final Project project = this.getProject(projectName);
    final Version version = this.getVersion(project, versionName);
    final Build build = this.getLatestBuild(project, version);
    final String downloadName = this.downloadName(project, version, build);
    return this.download(project, version, build, downloadName, CACHE_5_MINUTES);
  }

  private ResponseEntity<?> build(final Project project, final Version version, final Build build, final CacheControl cache) throws UhOh {
    return HTTP.ok(
      this.json.object(json -> {
        json.put("project", project.name);
        json.put("version", version.name);
        json.put("build", build.number);
      }),
      cache
    );
  }

  private String downloadName(final Project project, final Version version, final Build build) {
    return project.name + "-" + version.name + "-" + build.number + ".jar";
  }

  private Build getLatestBuild(final Project project, final Version version) {
    final List<Build> builds = this.builds.findAllByProjectAndVersion(project._id, version._id);
    return builds.get(builds.size() - 1); // we can safely assume that a version has at least one build
  }
}
