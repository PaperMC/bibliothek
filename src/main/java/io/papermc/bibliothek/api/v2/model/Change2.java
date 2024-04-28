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
import io.papermc.bibliothek.api.model.Change;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.jspecify.annotations.NullMarked;

import static io.papermc.bibliothek.api.v2.Constants2.FIELD_COMMIT;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_MESSAGE;
import static io.papermc.bibliothek.api.v2.Constants2.FIELD_SUMMARY;

@NullMarked
@Schema
public record Change2(
  @JsonProperty(FIELD_COMMIT)
  @Schema(name = FIELD_COMMIT)
  String commit,

  @JsonProperty(FIELD_SUMMARY)
  @Schema(name = FIELD_SUMMARY)
  String summary,

  @JsonProperty(FIELD_MESSAGE)
  @Schema(name = FIELD_MESSAGE)
  String message
) {
  private Change2(final Change that) {
    this(that.commit(), that.summary(), that.message());
  }

  public static List<Change2> map(final List<Change> list) {
    return list
      .stream()
      .map(Change2::new)
      .toList();
  }
}
