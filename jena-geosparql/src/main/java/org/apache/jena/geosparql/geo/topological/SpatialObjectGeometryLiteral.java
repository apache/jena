/*
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.geosparql.geo.topological;

import org.apache.jena.geosparql.implementation.vocabulary.Geo;
import org.apache.jena.geosparql.implementation.vocabulary.SpatialExtension;
import org.apache.jena.geosparql.spatial.ConvertLatLon;
import java.util.Objects;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

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
     * @param targetSpatialObject
     * @return SpatialObject/GeometryLiteral pair.
     */
    protected static final SpatialObjectGeometryLiteral retrieve(Graph graph, Node targetSpatialObject) {

        Node geometry = null;
        if (graph.contains(targetSpatialObject, RDF.type.asNode(), Geo.FEATURE_NODE)) {
            //Target is Feature - find the default Geometry.
            ExtendedIterator<Triple> geomIter = graph.find(targetSpatialObject, Geo.HAS_DEFAULT_GEOMETRY_NODE, null);
            geometry = extractObject(geomIter);
        } else if (graph.contains(targetSpatialObject, RDF.type.asNode(), Geo.GEOMETRY_NODE)) {
            //Target is a Geometry.
            geometry = targetSpatialObject;
        }

        if (geometry != null) {
            //Find the Geometry Literal of the Geometry.
            ExtendedIterator<Triple> iter = graph.find(geometry, Geo.HAS_SERIALIZATION_NODE, null);
            Node literalNode = extractObject(iter);
            if (literalNode != null) {
                return new SpatialObjectGeometryLiteral(targetSpatialObject, literalNode);
            }
        } else {
            //Target is not a Feature or Geometry but could have Geo Predicates.
            if (graph.contains(targetSpatialObject, SpatialExtension.GEO_LAT_NODE, null) && graph.contains(targetSpatialObject, SpatialExtension.GEO_LON_NODE, null)) {
                Node lat = graph.find(targetSpatialObject, SpatialExtension.GEO_LAT_NODE, null).next().getObject();
                Node lon = graph.find(targetSpatialObject, SpatialExtension.GEO_LON_NODE, null).next().getObject();
                Node latLonGeometryLiteral = ConvertLatLon.toNode(lat, lon);
                return new SpatialObjectGeometryLiteral(targetSpatialObject, latLonGeometryLiteral);
            }
        }

        return new SpatialObjectGeometryLiteral(null, null);
    }

    private static Node extractObject(ExtendedIterator<Triple> iter) {

        if (iter.hasNext()) {
            Triple triple = iter.next();
            return triple.getObject();
        } else {
            return null;
        }
    }

}
