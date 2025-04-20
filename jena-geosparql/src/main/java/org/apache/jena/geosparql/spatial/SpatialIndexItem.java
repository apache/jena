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
package org.apache.jena.geosparql.spatial;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.locationtech.jts.geom.Envelope;

/**
 *
 *
 */
public class SpatialIndexItem {

    private final Envelope envelope;
    private final Node item;

    @Deprecated // (forRemoval = true)
    public static SpatialIndexItem of(Envelope envelope, RDFNode item) {
        return new SpatialIndexItem(envelope, item);
    }

    public static SpatialIndexItem of(Envelope envelope, Node node) {
        return new SpatialIndexItem(envelope, node);
    }

    @Deprecated // (forRemoval = true)
    public SpatialIndexItem(Envelope envelope, RDFNode item) {
        this(envelope, item.asNode());
    }

    public SpatialIndexItem(Envelope envelope, Node item) {
        this.envelope = envelope;
        this.item = item;
    }

    public Envelope getEnvelope() {
        return envelope;
    }

    public Node getItem() {
        return item;
    }

    @Override
    public String toString() {
        return "SpatialIndexItem{" + "envelope=" + envelope + ", item=" + item + '}';
    }
}
