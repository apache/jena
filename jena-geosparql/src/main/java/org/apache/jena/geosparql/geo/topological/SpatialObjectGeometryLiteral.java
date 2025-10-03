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
package org.apache.jena.geosparql.geo.topological;

import java.util.Objects;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.geosparql.implementation.access.AccessGeoSPARQL;
import org.apache.jena.geosparql.implementation.access.AccessWGS84;
import org.apache.jena.geosparql.implementation.datatype.GeometryDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.system.G;

/**
 *
 *
 */
public class SpatialObjectGeometryLiteral {

    private final Node spatialObject;
    private final Node geometryLiteral;
    private final boolean valid;

    public SpatialObjectGeometryLiteral(Node spatialObject, Node geometryLiteral) {
        this.spatialObject = spatialObject;
        this.geometryLiteral = geometryLiteral;
        this.valid = !(geometryLiteral == null || spatialObject == null);
    }

    public Node getSpatialObject() {
        return spatialObject;
    }

    public Node getGeometryLiteral() {
        return geometryLiteral;
    }

    public boolean isValid() {
        return valid;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.spatialObject);
        hash = 47 * hash + Objects.hashCode(this.geometryLiteral);
        hash = 47 * hash + (this.valid ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SpatialObjectGeometryLiteral other = (SpatialObjectGeometryLiteral) obj;
        if (this.valid != other.valid) {
            return false;
        }
        if (!Objects.equals(this.spatialObject, other.spatialObject)) {
            return false;
        }
        return Objects.equals(this.geometryLiteral, other.geometryLiteral);
    }

    @Override
    public String toString() {
        return "SpatialObjectGeometryLiteral{" + "spatialObject=" + spatialObject + ", geometryLiteral=" + geometryLiteral + ", valid=" + valid + '}';
    }

    /**
     * Retrieve the default Geometry Literal for Feature or Geometry (Spatial
     * Objects).
     *
     * @param graph
     * @param targetSpatialObject The spatial object.
     * @return SpatialObject/GeometryLiteral pair.
     */
    // XXX This should return an iterator over all geometry literals rather than just picking an arbitrary one.
    protected static final SpatialObjectGeometryLiteral retrieve(Graph graph, Node targetSpatialObject) {
        if (targetSpatialObject == null) {
            return new SpatialObjectGeometryLiteral(null, null);
        }

        // Special case: Directly supplied literal - must be a geometry.
        if (targetSpatialObject.isLiteral()) {
            if (targetSpatialObject.getLiteralDatatype() instanceof GeometryDatatype) {
                return new SpatialObjectGeometryLiteral(NodeFactory.createBlankNode(), targetSpatialObject);
            } else {
                throw new DatatypeFormatException(targetSpatialObject.getLiteralLexicalForm() + " is no Geometry literal");
            }
        }

        // If target has a default geometry then it is implicitly a feature.
        // Use the feature's default geometry if present ...
        // XXX The original code did not consider geo:hasGeometry here - does the spec really only mandate handling of default geometry?
        Node geometry = G.getSP(graph, targetSpatialObject, Geo.HAS_DEFAULT_GEOMETRY_NODE);

        // ... otherwise try to treat the target itself as the geometry resource.
        if (geometry == null) {
            geometry = targetSpatialObject;
        }

        Node literalNode = AccessGeoSPARQL.getGeoLiteral(graph, geometry);

        // Last resort: Try the legacy WGS84 Geo Positioning vocabulary on the targetSpatialObject.
        if (literalNode == null) {
            literalNode = AccessWGS84.getGeoLiteral(graph, targetSpatialObject);
        }

        if (literalNode != null) {
            return new SpatialObjectGeometryLiteral(targetSpatialObject, literalNode);
        }

        // No geometry literal found.
        return new SpatialObjectGeometryLiteral(null, null);
    }
}
