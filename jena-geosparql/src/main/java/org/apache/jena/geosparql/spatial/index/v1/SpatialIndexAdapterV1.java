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
package org.apache.jena.geosparql.spatial.index.v1;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.jena.geosparql.implementation.SRSInfo;
import org.apache.jena.geosparql.spatial.SpatialIndex;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.locationtech.jts.geom.Envelope;

/** Adapter class for spatial index v1. */
@Deprecated(forRemoval=true)
@SuppressWarnings("removal")
public class SpatialIndexAdapterV1
    implements SpatialIndex
{
    protected org.apache.jena.geosparql.spatial.index.v1.SpatialIndexV1 v1;

    public SpatialIndexAdapterV1(org.apache.jena.geosparql.spatial.index.v1.SpatialIndexV1 v1) {
        super();
        this.v1 = v1;
    }

    @Override
    public SRSInfo getSrsInfo() {
        return v1.getSrsInfo();
    }

    @Override
    public boolean isEmpty() {
        return v1.isEmpty();
    }

    @Override
    public long getSize() {
        return -1;
    }

    protected Collection<Node> adapt(Collection<? extends RDFNode> resources) {
        Collection<Node> result = resources.stream()
            .map(RDFNode::asNode)
            .collect(Collectors.toCollection(HashSet::new));
        return result;
    }

    @Override
    public Collection<Node> query(Envelope searchEnvelope, Node graphName) {
        HashSet<Resource> resources = v1.query(searchEnvelope);
        return adapt(resources);
    }

    @Override
    public Path getLocation() {
        Path result = Optional.ofNullable(v1.getLocation()).map(File::toPath).orElse(null);
        return result;
    }

    @Override
    public void setLocation(Path location) {
        v1.setLocation(location == null ? null : location.toFile());
    }
}
