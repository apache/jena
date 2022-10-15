/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.shex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

/**
 * <a href="https://shexspec.github.io/shape-map/">ShEx shape map</a> used for both
 * targeting validation and reporting violations.
 */
public class ShapeMap {

    protected final List<ShexRecord> associations;

    public static ShapeMap create(List<ShexRecord> associations) {
        associations = new ArrayList<>(associations);
        return new ShapeMap(associations);
    }

    private ShapeMap(List<ShexRecord> associations) {
        this.associations = associations;
    }

    public List<ShexRecord> entries() {
        return Collections.unmodifiableList(associations);
    }

    public static ShapeMap.Builder newBuilder() {
        return new Builder();
    }

    public static ShapeMap record(Node focus, Node shapeRef) {
        return new Builder().add(focus, shapeRef).build();
    }

    public static ShapeMap record(Triple pattern, Node shapeRef) {
        return new Builder().add(pattern, shapeRef).build();
    }

    public static class Builder {

        private List<ShexRecord> records = new ArrayList<>();

        Builder() {}

        Builder(ShapeMap base) {
            base.entries().forEach(records::add);
        }

        public Builder add(Node focus, Node shapeRef) {
            records.add(new ShexRecord(focus, shapeRef));
            return this;
        }

        public Builder add(Triple pattern, Node shapeRef) {
            records.add(new ShexRecord(pattern, shapeRef));
            return this;
        }

        public ShapeMap build() {
            // Copies argument.
            ShapeMap map = ShapeMap.create(records);
            records.clear();
            return map;
        }
    }

}
