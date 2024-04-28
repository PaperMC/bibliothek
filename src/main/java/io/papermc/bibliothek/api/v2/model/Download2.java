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
package io.papermc.bibliothek.api.v2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.papermc.bibliothek.api.model.Download;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

import static io.papermc.bibliothek.api.v2.Constants2.EXAMPLE_DOWNLOAD;
import static io.papermc.bibliothek.api.v2.Constants2.EXAMPLE_SHA256;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_NAME;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_SHA256;
import static io.papermc.bibliothek.api.v2.Constants2.PATTERN_DOWNLOAD;
import static io.papermc.bibliothek.api.v2.Constants2.PATTERN_SHA256;

@NullMarked
@Schema
public record Download2(
  @JsonProperty(FIELD_NAME)
  @Schema(name = FIELD_NAME, pattern = PATTERN_DOWNLOAD, example = EXAMPLE_DOWNLOAD)
  String name,

  @JsonProperty(FIELD_SHA256)
  @Schema(name = FIELD_SHA256, pattern = PATTERN_SHA256, example = EXAMPLE_SHA256)
  String sha256
) {
  private Download2(final Download that) {
    this(that.name(), that.sha256());
  }

  public static Map<String, Download2> map(final Map<String, Download> map) {
    return map.entrySet()
      .stream()
      .map(e -> Map.entry(e.getKey(), new Download2(e.getValue())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
