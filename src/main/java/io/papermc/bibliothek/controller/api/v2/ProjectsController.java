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

import com.vdurmont.semver4j.Semver;
import io.papermc.bibliothek.configuration.AppConfiguration;
import io.papermc.bibliothek.controller.api.AbstractProjectsController;
import io.papermc.bibliothek.controller.api.v2.response.BuildResponse;
import io.papermc.bibliothek.controller.api.v2.response.ProjectResponse;
import io.papermc.bibliothek.controller.api.v2.response.ProjectsResponse;
import io.papermc.bibliothek.controller.api.v2.response.VersionGroupBuildsResponse;
import io.papermc.bibliothek.controller.api.v2.response.VersionGroupResponse;
import io.papermc.bibliothek.controller.api.v2.response.VersionResponse;
import io.papermc.bibliothek.database.collection.BuildCollection;
import io.papermc.bibliothek.database.collection.ProjectCollection;
import io.papermc.bibliothek.database.collection.VersionCollection;
import io.papermc.bibliothek.database.collection.VersionGroupCollection;
import io.papermc.bibliothek.database.document.Build;
import io.papermc.bibliothek.database.document.Change;
import io.papermc.bibliothek.database.document.Download;
import io.papermc.bibliothek.database.document.Project;
import io.papermc.bibliothek.database.document.Version;
import io.papermc.bibliothek.database.document.VersionGroup;
import io.papermc.bibliothek.http.HTTP;
import io.papermc.bibliothek.http.error.UhOh;
import io.papermc.bibliothek.json.JsonFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.Pattern;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectsController extends AbstractProjectsController {
  private static final String PROJECTS = "projects";

  private static final String PROJECT_ID = "project_id";
  private static final String PROJECT_NAME = "project_name";

  private static final String VERSION_GROUPS = "version_groups";
  private static final String VERSION_GROUP = "version_group";

  private static final String VERSIONS = "versions";
  private static final String VERSION = "version";
  private static final String BUILDS = "builds";
  private static final String BUILD = "build";
  private static final String TIME = "time";

  private static final String CHANGES = "changes";
  private static final String COMMIT = "commit";
  private static final String SUMMARY = "summary";
  private static final String MESSAGE = "message";

  private static final String DOWNLOADS = "downloads";
  private static final String NAME = "name";
  private static final String SHA256 = "sha256";

  @Autowired
  private ProjectsController(final AppConfiguration downloadsConfig, final ProjectCollection projects, final VersionGroupCollection versionGroups, final VersionCollection versions, final BuildCollection builds, final JsonFactory json) {
    super(downloadsConfig, projects, versionGroups, versions, builds, json);
  }

  @ApiResponse(
    responseCode = "200",
    content = @Content(
      schema = @Schema(implementation = ProjectsResponse.class)
    )
  )
  @GetMapping("/v2/projects")
  @Operation(summary = "Get a list of all available projects.")
  public ResponseEntity<?> projects() {
    return HTTP.ok(
      this.json.object(json -> {
        json.set(PROJECTS, this.json.array(projectsJson -> {
          for(final Project project : this.projects.findAll()) {
            projectsJson.add(project.name);
          }
        }));
      }),
      CACHE_7_DAYS
    );
  }

  @ApiResponse(
    responseCode = "200",
    content = @Content(
      schema = @Schema(implementation = ProjectResponse.class)
    )
  )
  @GetMapping("/v2/projects/{project:[a-z]+}")
  @Operation(summary = "Get information about a project.")
  @SuppressWarnings("checkstyle:Indentation")
  public ResponseEntity<?> project(
    final @Parameter(description = "The ID of the project.", example = "paper") @PathVariable("project") @Pattern(regexp = "[a-z]+") String projectName
  ) throws UhOh {
    final Project project = this.getProject(projectName);
    final List<VersionGroup> versionGroups = this.versionGroups.findAllByProject(project._id);
    final List<Version> versions = this.versions.findAllByProject(project._id);
    return HTTP.ok(
      this.json.object(json -> {
        json.put(PROJECT_ID, project.name);
        json.put(PROJECT_NAME, project.friendlyName);
        json.set(VERSION_GROUPS, this.json.array(versionsGroupsJson -> {
          for(final VersionGroup version : sortVersionGroups(versionGroups)) {
            versionsGroupsJson.add(version.name);
          }
        }));
        json.set(VERSIONS, this.json.array(versionsJson -> {
          for(final Version version : sortVersions(versions)) {
            versionsJson.add(version.name);
          }
        }));
      }),
      CACHE_30_MINUTES
    );
  }

  @ApiResponse(
    responseCode = "200",
    content = @Content(
      schema = @Schema(implementation = VersionResponse.class)
    )
  )
  @GetMapping("/v2/projects/{project:[a-z]+}/versions/{version:[0-9pre.-]+}")
  @Operation(summary = "Get information about a version.")
  @SuppressWarnings("checkstyle:Indentation")
  public ResponseEntity<?> projectVersion(
    final @Parameter(description = "The ID of the project.", example = "paper") @PathVariable("project") @Pattern(regexp = "[a-z]+") String projectName,
    final @Parameter(description = "A version of the project.") @PathVariable("version") @Pattern(regexp = "[0-9pre.-]+") String versionName
  ) throws UhOh {
    final Project project = this.getProject(projectName);
    final Version version = this.getVersion(project, versionName);
    final List<Build> builds = this.builds.findAllByProjectAndVersion(project._id, version._id);
    return HTTP.ok(
      this.json.object(json -> {
        json.put(PROJECT_ID, project.name);
        json.put(PROJECT_NAME, project.friendlyName);
        json.put(VERSION, version.name);
        json.set(BUILDS, this.json.array(buildsJson -> {
          for(final Build build : builds) {
            buildsJson.add(build.number);
          }
        }));
      }),
      CACHE_5_MINUTES
    );
  }

  @ApiResponse(
    responseCode = "200",
    content = @Content(
      schema = @Schema(implementation = BuildResponse.class)
    )
  )
  @GetMapping("/v2/projects/{project:[a-z]+}/versions/{version:[0-9pre.-]+}/builds/{build:\\d+}")
  @Operation(summary = "Get all available builds for a version.")
  @SuppressWarnings("checkstyle:Indentation")
  public ResponseEntity<?> projectBuild(
    final @Parameter(description = "The ID of the project.", example = "paper") @PathVariable("project") @Pattern(regexp = "[a-z]+") String projectName,
    final @Parameter(description = "A version of the project.") @PathVariable("version") @Pattern(regexp = "[0-9pre.-]+") String versionName,
    final @Parameter(description = "A build of the version.") @PathVariable("build") @Pattern(regexp = "\\d+") int buildNumber
  ) throws UhOh {
    final Project project = this.getProject(projectName);
    final Version version = this.getVersion(project, versionName);
    final Build build = this.getBuild(project, version, buildNumber);
    return HTTP.ok(
      this.json.object(json -> {
        json.put(PROJECT_ID, project.name);
        json.put(PROJECT_NAME, project.friendlyName);
        json.put(VERSION, version.name);
        json.put(BUILD, build.number);
        json.put(TIME, build.time.toString());
        json.set(CHANGES, this.json.array(changesJson -> {
          for(final Change change : build.changes) {
            changesJson.add(this.json.object(changeJson -> {
              changeJson.put(COMMIT, change.commit);
              changeJson.put(SUMMARY, change.summary);
              changeJson.put(MESSAGE, change.message);
            }));
          }
        }));
        json.set(DOWNLOADS, this.json.object(downloadsJson -> {
          for(final Map.Entry<String, Download> download : build.downloads.entrySet()) {
            downloadsJson.set(download.getKey(), this.json.object(downloadJson -> {
              downloadJson.put(NAME, download.getValue().name);
              downloadJson.put(SHA256, download.getValue().sha256);
            }));
          }
        }));
      }),
      CACHE_7_DAYS
    );
  }

  @ApiResponse(
    responseCode = "200",
    headers = {
      @Header(
        name = "Content-Disposition",
        description = "A header indicating that the content is expected to be displayed as an attachment, that is downloaded and saved locally.",
        schema = @Schema(type = "string")
      ),
      @Header(
        name = "ETag",
        description = "An identifier for a specific version of a resource. It lets caches be more efficient and save bandwidth, as a web server does not need to resend a full response if the content has not changed.",
        schema = @Schema(type = "string")
      ),
      @Header(
        name = "Last-Modified",
        description = "The date and time at which the origin server believes the resource was last modified.",
        schema = @Schema(type = "string")
      )
    }
  )
  @GetMapping(
    value = "/v2/projects/{project:[a-z]+}/versions/{version:[0-9pre.-]+}/builds/{build:\\d+}/downloads/{download:[a-z0-9._-]+}",
    produces = {
      MediaType.APPLICATION_JSON_VALUE,
      HTTP.APPLICATION_JAVA_ARCHIVE_VALUE
    }
  )
  @Operation(summary = "Downloads the given file from a build's data.")
  @SuppressWarnings("checkstyle:Indentation")
  public ResponseEntity<?> projectBuildDownload(
    final @Parameter(description = "The ID of the project.", example = "paper") @PathVariable("project") @Pattern(regexp = "[a-z]+") String projectName,
    final @Parameter(description = "A version of the project.") @PathVariable("version") @Pattern(regexp = "[0-9pre.-]+") String versionName,
    final @Parameter(description = "A build of the version.") @PathVariable("build") @Pattern(regexp = "\\d+") int buildNumber,
    final @Parameter(description = "A download of the build.") @PathVariable("download") @Pattern(regexp = "[a-z0-9._-]+") String downloadName
  ) throws UhOh {
    final Project project = this.getProject(projectName);
    final Version version = this.getVersion(project, versionName);
    final Build build = this.getBuild(project, version, buildNumber);
    return this.download(project, version, build, downloadName, CACHE_7_DAYS);
  }

  @ApiResponse(
    responseCode = "200",
    content = @Content(
      schema = @Schema(implementation = VersionGroupResponse.class)
    )
  )
  @GetMapping("/v2/projects/{project:[a-z]+}/version_group/{versionGroup:[0-9pre.-]+}")
  @Operation(summary = "Get information about a version group.")
  @SuppressWarnings("checkstyle:Indentation")
  public ResponseEntity<?> versionGroup(
    final @Parameter(description = "The ID of the project.", example = "paper") @PathVariable("project") @Pattern(regexp = "[a-z]+") String projectName,
    final @Parameter(description = "A version group of the project.") @PathVariable("versionGroup") @Pattern(regexp = "[0-9pre.-]+") String versionGroupName
  ) throws UhOh {
    final Project project = this.getProject(projectName);
    final VersionGroup group = this.getVersionGroup(project, versionGroupName);
    final List<Version> versions = this.versions.findAllByProjectAndGroup(project._id, group._id);
    return HTTP.ok(
      this.json.object(json -> {
        json.put(PROJECT_ID, project.name);
        json.put(PROJECT_NAME, project.friendlyName);
        json.put(VERSION_GROUP, group.name);
        json.set(VERSIONS, this.json.array(versionsJson -> {
          for(final Version version : sortVersions(versions)) {
            versionsJson.add(version.name);
          }
        }));
      }),
      CACHE_5_MINUTES
    );
  }

  @ApiResponse(
    responseCode = "200",
    content = @Content(
      schema = @Schema(implementation = VersionGroupBuildsResponse.class)
    )
  )
  @GetMapping("/v2/projects/{project:[a-z]+}/version_group/{versionGroup:[0-9pre.-]+}/builds")
  @Operation(summary = "Get all available builds for a version group.")
  @SuppressWarnings("checkstyle:Indentation")
  public ResponseEntity<?> versionGroupBuilds(
    final @Parameter(description = "The ID of the project.", example = "paper") @PathVariable("project") @Pattern(regexp = "[a-z]+") String projectName,
    final @Parameter(description = "A version group of the project.") @PathVariable("versionGroup") @Pattern(regexp = "[0-9pre.-]+") String versionGroupName
  ) throws UhOh {
    final Project project = this.getProject(projectName);
    final VersionGroup group = this.getVersionGroup(project, versionGroupName);
    final Map<ObjectId, Version> versions = this.versions.findAllByProjectAndGroup(project._id, group._id).stream()
      .collect(Collectors.toMap(version -> version._id, Function.identity()));
    final List<Build> builds = this.builds.findAllByProjectAndVersionIn(project._id, versions.keySet());
    return HTTP.ok(
      this.json.object(json -> {
        json.put(PROJECT_ID, project.name);
        json.put(PROJECT_NAME, project.friendlyName);
        json.put(VERSION_GROUP, group.name);
        json.set(VERSIONS, this.json.array(versionsJson -> {
          for(final Version version : sortVersions(versions.values())) {
            versionsJson.add(version.name);
          }
        }));
        json.set(BUILDS, this.json.array(buildsJson -> {
          for(final Build build : builds) {
            buildsJson.add(this.json.object(buildJson -> {
                buildJson.put(VERSION, versions.get(build.version).name);
                buildJson.put(BUILD, build.number);
                buildJson.put(TIME, build.time.toString());
                buildJson.set(CHANGES, this.json.array(changesJson -> {
                  for(final Change change : build.changes) {
                    changesJson.add(this.json.object(changeJson -> {
                      changeJson.put(COMMIT, change.commit);
                      changeJson.put(SUMMARY, change.summary);
                      changeJson.put(MESSAGE, change.message);
                    }));
                  }
                }));
                buildJson.set(DOWNLOADS, this.json.object(downloadsJson -> {
                  for(final Map.Entry<String, Download> download : build.downloads.entrySet()) {
                    downloadsJson.set(download.getKey(), this.json.object(downloadJson -> {
                      downloadJson.put(NAME, download.getValue().name);
                      downloadJson.put(SHA256, download.getValue().sha256);
                    }));
                  }
                }));
              }
            ));
          }
        }));
      }),
      CACHE_5_MINUTES
    );
  }

  public static List<Version> sortVersions(final Collection<Version> versions) {
    return semanticSorted(versions, version -> new Semver(version.name, Semver.SemverType.LOOSE));
  }

  public static List<VersionGroup> sortVersionGroups(final Collection<VersionGroup> versions) {
    return semanticSorted(versions, version -> new Semver(version.name, Semver.SemverType.LOOSE));
  }

  private static <T> List<T> semanticSorted(final Collection<T> unsorted, final Function<T, Semver> fn) {
    final List<T> sorted = new ArrayList<>(unsorted);
    sorted.sort(Comparator.comparing(fn));
    return sorted;
  }
}
