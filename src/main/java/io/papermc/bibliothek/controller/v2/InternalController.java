/*
 * This file is part of bibliothek, licensed under the MIT License.
 *
 * Copyright (c) 2019-2021 PaperMC
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
package io.papermc.bibliothek.controller.v2;

import io.papermc.bibliothek.database.model.Build;
import io.papermc.bibliothek.database.model.Project;
import io.papermc.bibliothek.database.model.Version;
import io.papermc.bibliothek.database.model.VersionFamily;
import io.papermc.bibliothek.database.repository.BuildCollection;
import io.papermc.bibliothek.database.repository.ProjectCollection;
import io.papermc.bibliothek.database.repository.VersionCollection;
import io.papermc.bibliothek.database.repository.VersionFamilyCollection;
import io.papermc.bibliothek.exception.BuildPublicationFailed;
import io.papermc.bibliothek.exception.ManifestUnavailable;
import io.papermc.bibliothek.exception.ManifestVersionNotFound;
import io.papermc.bibliothek.mojang.VersionManifest2;
import io.papermc.bibliothek.util.Changes;
import io.swagger.v3.oas.annotations.Hidden;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.Pattern;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("checkstyle:FinalClass")
public class InternalController {
  private final ProjectCollection projects;
  private final VersionFamilyCollection families;
  private final VersionCollection versions;
  private final BuildCollection builds;
  private final RestTemplate rest;
  private final GitHub gh;

  @Autowired
  InternalController(
    final ProjectCollection projects,
    final VersionFamilyCollection families,
    final VersionCollection versions,
    final BuildCollection builds,
    final RestTemplate rest,
    final GitHub gh
  ) {
    this.projects = projects;
    this.families = families;
    this.versions = versions;
    this.builds = builds;
    this.rest = rest;
    this.gh = gh;
  }

  @Hidden
  @PostMapping("/v2/projects/{project:[a-z]+}/actions/publish")
  public ResponseEntity<?> internalPublish(
    @PathVariable("project")
    final String projectName,
    @RequestParam("projectName")
    final String projectFriendlyName,
    @RequestParam("family")
    final String familyName,
    @RequestParam("version")
    final String versionName,
    @RequestParam("build")
    final int buildNumber,
    @RequestParam("gitHash")
    final String gitHash,
    @Pattern(regexp = "DEFAULT|EXPERIMENTAL")
    @RequestParam("channel")
    final Build.@Nullable Channel channel,
    @RequestBody
    final List<DownloadData> downloadData
  ) {
    final ProjectFamilyVersion pfv = this.getOrCreateProjectFamilyAndVersion(projectName, projectFriendlyName, familyName, versionName);
    final Project project = pfv.project();
    final Version version = pfv.version();

    @Nullable Build build = this.builds.findByProjectAndVersionAndNumber(project._id(), version._id(), buildNumber).orElse(null);
    if (build == null) {
      final List<Build.Change> changes = new ArrayList<>();

      final @Nullable String gitRepositoryName = project.gitRepository();
      if (gitRepositoryName != null) {
        final Optional<Build> previousBuild = this.builds.findTopByProjectAndVersion(project._id(), version._id());
        if (previousBuild.isPresent()) {
          final List<Build.Change> previousBuildChanges = previousBuild.get().changes();
          if (!previousBuildChanges.isEmpty()) {
            try {
              final GHRepository repository = this.gh.getRepository(gitRepositoryName);
              final GHCompare compare = repository.getCompare(previousBuildChanges.get(0).commit(), gitHash);
              for (final GHCompare.Commit commit : compare.getCommits()) {
                changes.add(Changes.toChange(commit));
              }
            } catch (final IOException e) {
              throw new BuildPublicationFailed(e);
            }
          }
        }
      }

      final Map<String, Build.Download> downloads = downloadData.stream().collect(Collectors.toMap(DownloadData::id, data -> new Build.Download(data.name(), data.sha256())));

      build = this.builds.save(new Build(
        new ObjectId(),
        project._id(),
        version._id(),
        buildNumber,
        Instant.now(),
        changes,
        downloads,
        Build.channelOrDefault(channel),
        null
      ));
    }

    return ResponseEntity.ok(build);
  }

  private ProjectFamilyVersion getOrCreateProjectFamilyAndVersion(
    final String projectName,
    final String projectFriendlyName,
    final String familyName,
    final String versionName
  ) {
    final Project project = this.projects.findByName(projectName).orElseGet(() -> this.projects.save(new Project(null, projectName, projectFriendlyName, null)));

    @Nullable VersionFamily family = this.families.findByProjectAndName(project._id(), familyName).orElse(null);
    @Nullable Version version = this.versions.findByProjectAndName(project._id(), versionName).orElse(null);

    if (family == null || version == null) {
      final @Nullable VersionManifest2 manifest = this.rest.getForObject(VersionManifest2.URL, VersionManifest2.class);
      if (manifest == null) {
        throw new ManifestUnavailable();
      }

      if (family == null) {
        final VersionManifest2.@Nullable Version release = manifest.versionById(familyName);
        if (release == null) {
          throw new ManifestVersionNotFound(familyName);
        }
        family = this.families.save(new VersionFamily(
          null,
          project._id(),
          familyName,
          release.releaseTime
        ));
      }

      if (version == null) {
        final VersionManifest2.@Nullable Version release = manifest.versionById(versionName);
        if (release == null) {
          throw new ManifestVersionNotFound(versionName);
        }
        version = this.versions.save(new Version(
          null,
          project._id(),
          family._id(),
          versionName,
          release.releaseTime
        ));
      }
    }

    return new ProjectFamilyVersion(project, family, version);
  }

  private record ProjectFamilyVersion(
    Project project,
    VersionFamily family,
    Version version
  ) {
  }

  private record DownloadData(
    String id,
    String name,
    String sha256
  ) {
  }
}
