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
package io.papermc.bibliothek.controller.v2;

import io.papermc.bibliothek.database.model.Build;
import io.papermc.bibliothek.database.model.Project;
import io.papermc.bibliothek.database.model.Version;
import io.papermc.bibliothek.database.model.VersionFamily;
import io.papermc.bibliothek.database.repository.BuildCollection;
import io.papermc.bibliothek.database.repository.ProjectCollection;
import io.papermc.bibliothek.database.repository.VersionCollection;
import io.papermc.bibliothek.database.repository.VersionFamilyCollection;
import io.papermc.bibliothek.exception.ProjectNotFound;
import io.swagger.v3.oas.annotations.Hidden;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("checkstyle:FinalClass")
public class PublishController {
  private final ProjectCollection projects;
  private final VersionFamilyCollection families;
  private final VersionCollection versions;
  private final BuildCollection builds;

  @Autowired
  private PublishController(
    final ProjectCollection projects,
    final VersionFamilyCollection families,
    final VersionCollection versions,
    final BuildCollection builds
  ) {
    this.projects = projects;
    this.families = families;
    this.versions = versions;
    this.builds = builds;
  }

  @Hidden
  @PostMapping(
    path = "/v2/projects/{project:[a-z]+}/versions/{version:" + Version.PATTERN + "}/builds",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<?> createBuild(@RequestBody final PublishRequest request) {
    final Project project = this.projects.findByName(request.project()).orElseThrow(ProjectNotFound::new);
    final VersionFamily family = this.families.findByProjectAndName(project._id(), request.family())
      .orElseGet(() -> this.families.save(new VersionFamily(null, project._id(), request.family(), request.familyTime())));
    final Version version = this.versions.findByProjectAndName(project._id(), request.version())
      .orElseGet(() -> this.versions.save(new Version(null, project._id(), family._id(), request.version(), request.versionTime())));
    final Build build = this.builds.insert(new Build(
      null,
      project._id(),
      version._id(),
      request.build(),
      request.time(),
      request.changes(),
      request.downloads(),
      request.channel(),
      false
    ));
    return ResponseEntity.created(URI.create(
      MvcUriComponentsBuilder
        .fromMappingName("project.version.build")
        .arg(0, project.name())
        .arg(1, version.name())
        .arg(2, build.number())
        .build()
    )).body(new PublishResponse(true, build));
  }

  private record PublishRequest(
    String project,
    String family,
    @Nullable Instant familyTime,
    String version,
    @Nullable Instant versionTime,
    int build,
    Instant time,
    Build.Channel channel,
    List<Build.Change> changes,
    Map<String, Build.Download> downloads
  ) {
  }

  private record PublishResponse(
    boolean success,
    Build build
  ) {
  }
}
