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
package io.papermc.bibliothek.api.v2.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.papermc.bibliothek.api.model.Channel;
import io.papermc.bibliothek.api.serialization.LegacyChannelSerializer;
import io.papermc.bibliothek.api.v2.model.Change2;
import io.papermc.bibliothek.api.v2.model.Download2;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NullMarked;

import static io.papermc.bibliothek.api.v2.Constants2.EXAMPLE_PROJECT_ID;
import static io.papermc.bibliothek.api.v2.Constants2.EXAMPLE_PROJECT_NAME;
import static io.papermc.bibliothek.api.v2.Constants2.EXAMPLE_VERSION;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_BUILD;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_BUILDS;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_CHANGES;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_CHANNEL;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_DOWNLOADS;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_PROJECT_ID;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_PROJECT_NAME;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_PROMOTED;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_TIME;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_VERSION;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_VERSIONS;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_VERSION_GROUP;
import static io.papermc.bibliothek.api.v2.Constants2.PATTERN_PROJECT_ID;
import static io.papermc.bibliothek.api.v2.Constants2.PATTERN_VERSION;

@NullMarked
public record VersionFamilyBuildsResponse(
  @JsonProperty(FIELD_PROJECT_ID)
  @Schema(name = FIELD_PROJECT_ID, pattern = PATTERN_PROJECT_ID, example = EXAMPLE_PROJECT_ID)
  String projectId,

  @JsonProperty(FIELD_PROJECT_NAME)
  @Schema(name = FIELD_PROJECT_NAME, example = EXAMPLE_PROJECT_NAME)
  String projectName,

  @JsonProperty(FIELD_VERSION_GROUP)
  @Schema(name = FIELD_VERSION_GROUP, pattern = PATTERN_VERSION, example = EXAMPLE_VERSION)
  String versionGroup,

  @JsonProperty(FIELD_VERSIONS)
  @Schema(name = FIELD_VERSIONS)
  List<String> versions,

  @JsonProperty(FIELD_BUILDS)
  @Schema(name = FIELD_BUILDS)
  List<VersionFamilyBuild> builds
) {

  @NullMarked
  public record VersionFamilyBuild(
    @JsonProperty(FIELD_VERSION)
    @Schema(name = FIELD_VERSION, pattern = PATTERN_VERSION, example = EXAMPLE_VERSION)
    String version,

    @JsonProperty(FIELD_BUILD)
    @Schema(name = FIELD_BUILD, pattern = "\\d+", example = "10")
    int build,

    @JsonProperty(FIELD_TIME)
    @Schema(name = FIELD_TIME)
    Instant time,

    @JsonProperty(FIELD_CHANNEL)
    @JsonSerialize(using = LegacyChannelSerializer.class)
    @Schema(name = FIELD_CHANNEL)
    Channel channel,

    @JsonProperty(FIELD_PROMOTED)
    @Schema(name = FIELD_PROMOTED)
    boolean promoted,

    @JsonProperty(FIELD_CHANGES)
    @Schema(name = FIELD_CHANGES)
    List<Change2> changes,

    @JsonProperty(FIELD_DOWNLOADS)
    @Schema(name = FIELD_DOWNLOADS)
    Map<String, Download2> downloads
  ) {
  }
}
