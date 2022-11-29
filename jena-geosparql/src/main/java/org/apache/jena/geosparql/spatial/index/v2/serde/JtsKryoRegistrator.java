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
package org.apache.jena.geosparql.spatial.index.v2.serde;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.geosparql.spatial.index.v2.serde.sedona.CustomGeometrySerde;
import org.apache.jena.geosparql.spatial.index.v2.serde.sedona.CustomSpatialIndexSerde;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_Literal;
import org.apache.jena.graph.Node_Triple;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.graph.Triple;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.index.strtree.STRtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class JtsKryoRegistrator {

    final static Logger log = LoggerFactory.getLogger(JtsKryoRegistrator.class);

    public static void registerClasses(Kryo kryo, GeometrySerdeAdapter geometrySerdeAdapter) {
        CustomGeometrySerde geometrySerde = new CustomGeometrySerde(geometrySerdeAdapter);
        CustomSpatialIndexSerde indexSerializer = new CustomSpatialIndexSerde(geometrySerde);
        Serializer<Node> nodeSerializer = new NodeSerializer();

        log.debug("Registering custom serializers for geometry types");

        kryo.register(HashMap.class, new MapSerializer());
        kryo.register(Map.class, new MapSerializer());

        registerGeometrySerializers(kryo, geometrySerde);

        // Envelope is not a subclass of JTS geometry but the serializer handles it anyway.
        kryo.register(Envelope.class, geometrySerde);

        kryo.register(Quadtree.class, indexSerializer);
        kryo.register(STRtree.class, indexSerializer);

        registerNodeSerializers(kryo, nodeSerializer);

        kryo.register(Triple.class, new TripleSerializer());
        // kryo.register(Triple[].class);

        // Omitted: NODE_{ANY, Blank, Ext, Graph, Variable}
        kryo.register(Triple.class, nodeSerializer);
    }

    public static void registerGeometrySerializers(Kryo kryo, Serializer<?> geometrySerializer) {
        kryo.register(Point.class, geometrySerializer);
        kryo.register(LineString.class, geometrySerializer);
        kryo.register(Polygon.class, geometrySerializer);
        kryo.register(MultiPoint.class, geometrySerializer);
        kryo.register(MultiLineString.class, geometrySerializer);
        kryo.register(MultiPolygon.class, geometrySerializer);
        kryo.register(GeometryCollection.class, geometrySerializer);
    }

    public static void registerNodeSerializers(Kryo kryo, Serializer<Node> nodeSerializer) {
        kryo.register(Node.class, nodeSerializer);
        kryo.register(Node_Blank.class, nodeSerializer);
        kryo.register(Node_URI.class, nodeSerializer);
        kryo.register(Node_Literal.class, nodeSerializer);
        kryo.register(Node_Triple.class, nodeSerializer);
    }
}
