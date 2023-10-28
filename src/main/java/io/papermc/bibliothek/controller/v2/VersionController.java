/*
 * This file is part of bibliothek, licensed under the MIT License.
 *
 * Copyright (c) 2019-2023 PaperMC
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
import io.papermc.bibliothek.database.repository.BuildCollection;
import io.papermc.bibliothek.database.repository.ProjectCollection;
import io.papermc.bibliothek.database.repository.VersionCollection;
import io.papermc.bibliothek.exception.ChannelNotFound;
import io.papermc.bibliothek.exception.ProjectNotFound;
import io.papermc.bibliothek.exception.VersionNotFound;
import io.papermc.bibliothek.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import java.time.Duration;
import java.util.List;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("checkstyle:FinalClass")
public class VersionController {
  private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofMinutes(5));
  private final ProjectCollection projects;
  private final VersionCollection versions;
  private final BuildCollection builds;

  @Autowired
  private VersionController(
    final ProjectCollection projects,
    final VersionCollection versions,
    final BuildCollection builds
  ) {
    this.projects = projects;
    this.versions = versions;
    this.builds = builds;
  }

  @ApiResponse(
    content = @Content(
      schema = @Schema(implementation = VersionResponse.class)
    ),
    responseCode = "200"
  )
  @GetMapping("/v2/projects/{project:[a-z]+}/versions/{version:" + Version.PATTERN + "}")
  @Operation(summary = "Gets information about a version.")
  public ResponseEntity<?> version(
    @Parameter(name = "project", description = "The project identifier.", example = "paper")
    @PathVariable("project")
    @Pattern(regexp = "[a-z]+") //
    final String projectName,
    @Parameter(description = "A version of the project.")
    @PathVariable("version")
    @Pattern(regexp = Version.PATTERN) //
    final String versionName,
    @Parameter(description = "The channel to filter builds.")
    @RequestParam(required = false, defaultValue = "all") //
    final String channel
  ) {
    final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
    final Version version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
    final boolean hasChannel = !channel.isBlank() && !channel.equalsIgnoreCase("all");
    if (hasChannel && !EnumUtils.isValidEnumIgnoreCase(Build.Channel.class, channel)) {
      throw new ChannelNotFound(channel);
    }
    final List<Build> builds = (!hasChannel) ? this.builds.findAllByProjectAndVersion(project._id(), version._id()) : this.builds.findAllByProjectAndVersionAndChannel(project._id(), version._id(), channel.toUpperCase());
    return HTTP.cachedOk(VersionResponse.from(project, version, channel, builds), CACHE);
  }

  @Schema
  private record VersionResponse(
    @Schema(name = "project_id", pattern = "[a-z]+", example = "paper")
    String project_id,
    @Schema(name = "project_name", example = "Paper")
    String project_name,
    @Schema(name = "version", pattern = Version.PATTERN, example = "1.18")
    String channel,
    @Schema(name = "channel", examples = {"stable", "all"})
    String version,
    @Schema(name = "builds")
    List<Integer> builds
  ) {
    static VersionResponse from(final Project project, final Version version, final String channel, final List<Build> builds) {
      return new VersionResponse(
        project.name(),
        project.friendlyName(),
        channel,
        version.name(),
        builds.stream().map(Build::number).toList()
      );
    }
  }
}
