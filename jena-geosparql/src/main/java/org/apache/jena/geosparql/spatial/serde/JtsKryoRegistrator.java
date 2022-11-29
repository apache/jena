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
package org.apache.jena.geosparql.spatial.serde;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_Blank;
import org.apache.jena.graph.Node_URI;
import org.apache.sedona.common.geometrySerde.GeometrySerde;
import org.apache.sedona.common.geometrySerde.SpatialIndexSerde;
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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;

public class JtsKryoRegistrator {

    final static Logger log = LoggerFactory.getLogger(JtsKryoRegistrator.class);

    public static void registerClasses(Kryo kryo) {
        GeometrySerde serializer = new CustomGeometrySerde();
        SpatialIndexSerde indexSerializer = new CustomSpatialIndexSerde(serializer);

        log.debug("Registering custom serializers for geometry types");

        kryo.register(Point.class, serializer);
        kryo.register(LineString.class, serializer);
        kryo.register(Polygon.class, serializer);
        kryo.register(MultiPoint.class, serializer);
        kryo.register(MultiLineString.class, serializer);
        kryo.register(MultiPolygon.class, serializer);
        kryo.register(GeometryCollection.class, serializer);
        kryo.register(Envelope.class, serializer);

        kryo.register(Quadtree.class, indexSerializer);
        kryo.register(STRtree.class, indexSerializer);
        kryo.register(Node.class, new NodeSerializer());
        kryo.register(Node_URI.class, new NodeSerializer());
        kryo.register(Node_Blank.class, new NodeSerializer());

        kryo.register(HashMap.class, new MapSerializer());
        kryo.register(Map.class, new MapSerializer());
    }

    static class NodeSerializer extends Serializer<Node> {

        @Override
        public void write(Kryo kryo, Output output, Node node) {
            output.writeString(node.getURI());
        }

        @Override
        public Node read(Kryo kryo, Input input, Class<Node> aClass) {
            return NodeFactory.createURI(input.readString());
        }
    }
}
