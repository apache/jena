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
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

public class ShexMapBuilder {

    private List<ShexRecord> records = new ArrayList<>();

    public static ShexMap record(Node focus, Node shapeRef) {
        return new ShexMapBuilder().add(focus, shapeRef).build();
    }

    public static ShexMap record(Triple pattern, Node shapeRef) {
        return new ShexMapBuilder().add(pattern, shapeRef).build();
    }

    public ShexMapBuilder() {}

    public ShexMapBuilder(ShexMap base) {
        base.entries().forEach(records::add);
    }

    public ShexMapBuilder add(Node focus, Node shapeRef) {
        records.add(new ShexRecord(focus, shapeRef));
        return this;
    }

    public ShexMapBuilder add(Triple pattern, Node shapeRef) {
        records.add(new ShexRecord(pattern, shapeRef));
        return this;
    }

    public ShexMap build() {
        // Copies argument.
        ShexMap map = ShexMap.create(records);
        records.clear();
        return map;
    }
}
