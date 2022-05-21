/*
 * This file is part of bibliothek, licensed under the MIT License.
 *
 * Copyright (c) 2019-2022 PaperMC
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
package io.papermc.bibliothek.configuration;

import io.papermc.bibliothek.database.model.Build;
import io.papermc.bibliothek.database.model.Project;
import io.papermc.bibliothek.database.model.Version;
import java.net.URL;
import java.nio.file.Path;
import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public class AppConfiguration {
  private URL apiBaseUrl;
  private String apiTitle;
  private @NotNull Path storagePath;
  private String publishSecret;

  @SuppressWarnings("checkstyle:MethodName")
  public URL getApiBaseUrl() {
    return this.apiBaseUrl;
  }

  @SuppressWarnings("checkstyle:MethodName")
  public void setApiBaseUrl(final URL apiBaseUrl) {
    this.apiBaseUrl = apiBaseUrl;
  }

  @SuppressWarnings("checkstyle:MethodName")
  public String getApiTitle() {
    return this.apiTitle;
  }

  @SuppressWarnings("checkstyle:MethodName")
  public void setApiTitle(final String apiTitle) {
    this.apiTitle = apiTitle;
  }

  @SuppressWarnings("checkstyle:MethodName")
  public String getPublishSecret() {
    return this.publishSecret;
  }

  @SuppressWarnings("checkstyle:MethodName")
  public void setPublishSecret(final String publishSecret) {
    this.publishSecret = publishSecret;
  }

  @SuppressWarnings("checkstyle:MethodName")
  public Path getStoragePath() {
    return this.storagePath;
  }

  public Path storagePathFor(final Project project, final Version version, final Build build, final Build.Download download) {
    return this.storagePathFor(project, version, build.number(), download.name());
  }

  public Path storagePathFor(final Project project, final Version version, final int build, final String file) {
    return this.getStoragePath()
      .resolve(project.name())
      .resolve(version.name())
      .resolve(String.valueOf(build))
      .resolve(file);
  }

  @SuppressWarnings("checkstyle:MethodName")
  public void setStoragePath(final Path storagePath) {
    this.storagePath = storagePath;
  }
}
