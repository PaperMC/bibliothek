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

import io.papermc.bibliothek.api.model.Change;
import io.papermc.bibliothek.api.model.Channel;
import io.papermc.bibliothek.api.model.Download;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

@CompoundIndex(def = "{'project': 1, 'version': 1}")
@CompoundIndex(def = "{'project': 1, 'version': 1, 'number': 1}")
@Document(collection = "builds")
@NullMarked
public class BuildEntity {
  @Field
  @Id
  private ObjectId _id;

  @DocumentReference
  @Field
  private ProjectEntity project;

  @DocumentReference
  @Field
  private VersionEntity version;

  @Field
  private int number;

  @Field
  private Instant time;

  @Field
  private List<Change> changes;

  @Field
  private Map<String, Download> downloads;

  @Field
  private @Nullable Channel channel;

  @Field
  private @Nullable Boolean promoted;

  public BuildEntity() {
  }

  @Id
  public ObjectId _id() {
    return this._id;
  }

  public ProjectEntity project() {
    return this.project;
  }

  public VersionEntity version() {
    return this.version;
  }

  public int number() {
    return this.number;
  }

  public Instant time() {
    return this.time;
  }

  public List<Change> changes() {
    return this.changes;
  }

  public Map<String, Download> downloads() {
    return this.downloads;
  }

  public @Nullable Channel channel() {
    return this.channel;
  }

  public Channel channelOrDefault() {
    return Objects.requireNonNullElse(this.channel(), Channel.DEFAULT);
  }

  public @Nullable Boolean promoted() {
    return this.promoted;
  }

  public boolean promotedOrDefault() {
    return Objects.requireNonNullElse(this.promoted(), false);
  }
}
