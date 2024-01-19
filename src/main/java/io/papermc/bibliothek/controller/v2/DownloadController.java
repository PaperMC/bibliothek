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

import io.papermc.bibliothek.configuration.AppConfiguration;
import io.papermc.bibliothek.database.model.Build;
import io.papermc.bibliothek.database.model.Project;
import io.papermc.bibliothek.database.model.Version;
import io.papermc.bibliothek.database.repository.BuildCollection;
import io.papermc.bibliothek.database.repository.ProjectCollection;
import io.papermc.bibliothek.database.repository.VersionCollection;
import io.papermc.bibliothek.exception.BuildNotFound;
import io.papermc.bibliothek.exception.DownloadFailed;
import io.papermc.bibliothek.exception.DownloadNotFound;
import io.papermc.bibliothek.exception.ProjectNotFound;
import io.papermc.bibliothek.exception.VersionNotFound;
import io.papermc.bibliothek.util.HTTP;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.function.BiFunction;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SuppressWarnings("checkstyle:FinalClass")
public class DownloadController {
  private static final CacheControl CACHE = HTTP.sMaxAgePublicCache(Duration.ofDays(7));
  private final AppConfiguration configuration;
  private final ProjectCollection projects;
  private final VersionCollection versions;
  private final BuildCollection builds;

  @Autowired
  private DownloadController(
    final AppConfiguration configuration,
    final ProjectCollection projects,
    final VersionCollection versions,
    final BuildCollection builds
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
    value = "/v2/projects/{project:[a-z]+}/versions/{version:" + Version.PATTERN + "}/builds/{build:\\d+}/downloads/{download:" + Build.Download.PATTERN + "}",
    produces = {
      MediaType.APPLICATION_JSON_VALUE,
      HTTP.APPLICATION_JAVA_ARCHIVE_VALUE
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
    @Pattern(regexp = Version.PATTERN) //
    final String versionName,
    @Parameter(description = "A build of the version.")
    @PathVariable("build")
    @Pattern(regexp = "\\d+") //
    final int buildNumber,
    @Parameter(description = "A download of the build.")
    @PathVariable("download")
    @Pattern(regexp = Build.Download.PATTERN) //
    final String downloadName
  ) {
    final Project project = this.projects.findByName(projectName).orElseThrow(ProjectNotFound::new);
    final Version version = this.versions.findByProjectAndName(project._id(), versionName).orElseThrow(VersionNotFound::new);
    final Build build = this.builds.findByProjectAndVersionAndNumber(project._id(), version._id(), buildNumber).orElseThrow(BuildNotFound::new);

    for (final Map.Entry<String, Build.Download> download : build.downloads().entrySet()) {
      if (download.getValue().name().equals(downloadName)) {
        try {
          return JavaArchive.resolve(
            this.configuration,
            download.getValue(),
            (cdn, file) -> URI.create(String.format("%s/%s/%s/%d/%s", cdn, project.name(), version.name(), build.number(), file.name())),
            (path, file) -> path
              .resolve(project.name())
              .resolve(version.name())
              .resolve(String.valueOf(build.number()))
              .resolve(file.name()),
            CACHE
          );
        } catch (final IOException e) {
          throw new DownloadFailed(e);
        }
      }
    }
    throw new DownloadNotFound();
  }

  private static class JavaArchive extends ResponseEntity<AbstractResource> {
    public static JavaArchive resolve(
      final AppConfiguration config,
      final Build.Download download,
      final BiFunction<String, Build.Download, URI> cdnGetter,
      final BiFunction<Path, Build.Download, Path> localGetter,
      final CacheControl cache
    ) throws IOException {
      @Nullable IOException cdnException = null;
      final @Nullable String cdnUrl = config.getCdnUrl();
      if (cdnUrl != null) {
        final @Nullable URI cdn = cdnGetter.apply(cdnUrl, download);
        if (cdn != null) {
          try {
            return forUrl(download, cdn, cache);
          } catch (final IOException e) {
            cdnException = e;
          }
        }
      }
      @Nullable IOException localException = null;
      final @Nullable Path local = localGetter.apply(config.getStoragePath(), download);
      if (local != null) {
        try {
          return forPath(download, local, cache);
        } catch (final IOException e) {
          localException = e;
        }
      }
      final IOException exception = new IOException("Could not resolve download via CDN or Local Storage");
      if (cdnException != null) {
        exception.addSuppressed(cdnException);
      }
      if (localException != null) {
        exception.addSuppressed(localException);
      }
      throw exception;
    }

    private static JavaArchive forUrl(final Build.Download download, final URI uri, final CacheControl cache) throws IOException {
      final UrlResource resource = new UrlResource(uri);
      final HttpHeaders headers = headersFor(download, cache);
      headers.setLastModified(resource.lastModified());
      return new JavaArchive(resource, headers);
    }

    private static JavaArchive forPath(final Build.Download download, final Path path, final CacheControl cache) throws IOException {
      final FileSystemResource resource = new FileSystemResource(path);
      final HttpHeaders headers = headersFor(download, cache);
      headers.setLastModified(Files.getLastModifiedTime(path).toInstant());
      return new JavaArchive(resource, headers);
    }

    private JavaArchive(final AbstractResource resource, final HttpHeaders headers) {
      super(resource, headers, HttpStatus.OK);
    }

    private static HttpHeaders headersFor(final Build.Download download, final CacheControl cache) {
      final HttpHeaders headers = new HttpHeaders();
      headers.setCacheControl(cache);
      headers.setContentDisposition(HTTP.attachmentDisposition(download.name()));
      headers.setContentType(HTTP.APPLICATION_JAVA_ARCHIVE);
      return headers;
    }
  }
}
