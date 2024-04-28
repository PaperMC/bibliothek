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

import io.papermc.bibliothek.api.model.Download;
import io.papermc.bibliothek.configuration.AppConfiguration;
import io.papermc.bibliothek.database.model.BuildEntity;
import io.papermc.bibliothek.database.model.ProjectEntity;
import io.papermc.bibliothek.database.model.VersionEntity;
import io.papermc.bibliothek.database.repository.BuildRepository;
import io.papermc.bibliothek.database.repository.ProjectRepository;
import io.papermc.bibliothek.database.repository.VersionRepository;
import io.papermc.bibliothek.exception.BuildNotFound;
import io.papermc.bibliothek.exception.DownloadFailed;
import io.papermc.bibliothek.exception.DownloadNotFound;
import io.papermc.bibliothek.exception.ProjectNotFound;
import io.papermc.bibliothek.exception.VersionNotFound;
import io.papermc.bibliothek.util.HTTP;
import io.papermc.bibliothek.util.MediaTypes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@NullMarked
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class DownloadController {
  // NOTE: this pattern cannot contain any capturing groups
  @Language("RegExp")
  private static final String DOWNLOAD_NAME_PATTERN = "[a-zA-Z0-9._-]+";
  private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
  private final AppConfiguration configuration;
  private final ProjectRepository projects;
  private final VersionRepository versions;
  private final BuildRepository builds;

  @Autowired
  private DownloadController(
    final AppConfiguration configuration,
    final ProjectRepository projects,
    final VersionRepository versions,
    final BuildRepository builds
  ) {
    this.configuration = configuration;
    this.projects = projects;
    this.versions = versions;
    this.builds = builds;
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
    value = "/v2/projects/{project:[a-z]+}/versions/{version:" + VersionEntity.PATTERN + "}/builds/{build:\\d+}/downloads/{download:" + DOWNLOAD_NAME_PATTERN + "}",
    produces = {
      MediaType.APPLICATION_JSON_VALUE,
      MediaType.ALL_VALUE
    }
  )
  @Operation(summary = "Downloads the given file from a build's data.")
  public ResponseEntity<?> download(
    @Parameter(name = "project", description = "The project identifier.", example = "paper")
    @PathVariable("project")
    @Pattern(regexp = "[a-z]+") //
    final String projectName,
    @Parameter(description = "A version of the project.")
    @PathVariable("version")
    @Pattern(regexp = VersionEntity.PATTERN) //
    final String versionName,
    @Parameter(description = "A build of the version.")
    @PathVariable("build")
    @Positive //
    final int buildNumber,
    @Parameter(description = "A download of the build.")
    @PathVariable("download")
    @Pattern(regexp = DOWNLOAD_NAME_PATTERN) //
    final String downloadName
  ) {
    final ProjectEntity project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
    final VersionEntity version = this.versions.findByProjectAndName(project, versionName).orElseThrow(VersionNotFound::new);
    final BuildEntity build = this.builds.findByProjectAndVersionAndNumber(project, version, buildNumber).orElseThrow(BuildNotFound::new);

    for (final Map.Entry<String, Download> download : build.downloads().entrySet()) {
      if (download.getValue().name().equals(downloadName)) {
        try {
          return new JavaArchive(
            this.configuration.getStoragePath()
              .resolve(project.name())
              .resolve(version.name())
              .resolve(String.valueOf(build.number()))
              .resolve(download.getValue().name()),
            CACHE
          );
        } catch (final IOException e) {
          throw new DownloadFailed(e);
        }
      }
    }
    throw new DownloadNotFound();
  }

  private static class JavaArchive extends ResponseEntity<FileSystemResource> {
    JavaArchive(final Path path, final CacheControl cache) throws IOException {
      super(new FileSystemResource(path), headersFor(path, cache), HttpStatus.OK);
    }

    private static HttpHeaders headersFor(final Path path, final CacheControl cache) throws IOException {
      final HttpHeaders headers = new HttpHeaders();
      headers.setCacheControl(cache);
      headers.setContentDisposition(HTTP.attachmentDisposition(path.getFileName()));
      headers.setContentType(MediaTypes.fromFileName(path.getFileName().toString()));
      headers.setLastModified(Files.getLastModifiedTime(path).toInstant());
      return headers;
    }
  }
}
