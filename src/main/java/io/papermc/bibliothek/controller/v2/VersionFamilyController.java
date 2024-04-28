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

import io.papermc.bibliothek.api.v2.response.VersionFamilyResponse;
import io.papermc.bibliothek.database.model.ProjectEntity;
import io.papermc.bibliothek.database.model.VersionEntity;
import io.papermc.bibliothek.database.model.VersionFamilyEntity;
import io.papermc.bibliothek.database.repository.ProjectRepository;
import io.papermc.bibliothek.database.repository.VersionFamilyRepository;
import io.papermc.bibliothek.database.repository.VersionRepository;
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
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@NullMarked
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class VersionFamilyController {
  private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofMinutes(5));
  private final ProjectRepository projects;
  private final VersionFamilyRepository families;
  private final VersionRepository versions;

  @Autowired
  private VersionFamilyController(
    final ProjectRepository projects,
    final VersionFamilyRepository families,
    final VersionRepository versions
  ) {
    this.projects = projects;
    this.families = families;
    this.versions = versions;
  }

  @ApiResponse(
    content = @Content(
      schema = @Schema(implementation = VersionFamilyResponse.class)
    ),
    responseCode = "200"
  )
  @GetMapping("/v2/projects/{project:[a-z]+}/version_group/{family:" + VersionEntity.PATTERN + "}")
  @Operation(summary = "Gets information about a project's version group.")
  public ResponseEntity<?> family(
    @Parameter(name = "project", description = "The project identifier.", example = "paper")
    @PathVariable("project")
    @Pattern(regexp = "[a-z]+") //
    final String projectName,
    @Parameter(description = "The version group name.")
    @PathVariable("family")
    @Pattern(regexp = VersionEntity.PATTERN) //
    final String familyName
  ) {
    final ProjectEntity project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
    final VersionFamilyEntity family = this.families.findByProjectAndName(project, familyName).orElseThrow(VersionNotFound::new);
    final List<VersionEntity> versions = this.versions.findAllByProjectAndGroup(project, family);
    return HTTP.cachedOk(new VersionFamilyResponse(
      project.name(),
      project.friendlyName(),
      family.name(),
      versions.stream().sorted(VersionEntity.COMPARATOR).map(VersionEntity::name).toList()
    ), CACHE);
  }
}
