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
package io.papermc.bibliothek.database.model;

import io.papermc.bibliothek.util.BringOrderToChaos;
import io.papermc.bibliothek.util.NameSource;
import io.papermc.bibliothek.util.TimeSource;
import java.time.Instant;
import java.util.Comparator;
import org.bson.types.ObjectId;
import org.intellij.lang.annotations.Language;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@CompoundIndex(def = "{'project': 1, 'group': 1}")
@CompoundIndex(def = "{'project': 1, 'name': 1}")
@Document(collection = "versions")
@NullMarked
public class VersionEntity implements NameSource, TimeSource {
  // NOTE: this pattern cannot contain any capturing groups
  @Language("RegExp")
  public static final String PATTERN = "[0-9.]+-?(?:pre|SNAPSHOT)?(?:[0-9.]+)?";
  public static final Comparator<VersionEntity> COMPARATOR = BringOrderToChaos.timeOrNameComparator();

  @Field
  @Id
  private ObjectId _id;

  @DocumentReference
  @Field
  private ProjectEntity project;

  @DocumentReference
  @Field
  private VersionFamilyEntity group;

  @Field
  private String name;

  @Field
  private @Nullable Instant time;

  public VersionEntity() {
  }

  public ObjectId _id() {
    return this._id;
  }

  public ProjectEntity project() {
    return this.project;
  }

  public VersionFamilyEntity group() {
    return this.group;
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public @Nullable Instant time() {
    return this.time;
  }
}
