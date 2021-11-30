/*
 * This file is part of bibliothek, licensed under the MIT License.
 *
 * Copyright (c) 2019-2021 PaperMC
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

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@CompoundIndex(def = "{'project': 1, 'group': 1}")
@CompoundIndex(def = "{'project': 1, 'name': 1}")
@Document(collection = "versions")
public record Version(
  @Id ObjectId _id,
  ObjectId project,
  ObjectId group,
  String name,
  @Nullable Instant time
) {
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static final Comparator<Version> COMPARATOR = (o1, o2) -> {
    final Comparable c1 = Objects.requireNonNullElseGet(o1.time(), o1::name);
    final Comparable c2 = Objects.requireNonNullElseGet(o2.time(), o2::name);
    return c1.compareTo(c2);
  };
}
