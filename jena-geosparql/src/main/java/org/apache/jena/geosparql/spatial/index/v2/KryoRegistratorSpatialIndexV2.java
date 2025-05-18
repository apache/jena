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
package org.apache.jena.geosparql.spatial.index.v2;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.geosparql.kryo.EnvelopeSerializer;
import org.apache.jena.geosparql.kryo.NodeSerializer;
import org.apache.jena.geosparql.kryo.TripleSerializer;
import org.apache.jena.graph.Triple;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.index.strtree.STRtree;
import org.locationtech.jts.index.strtree.STRtreeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serializers.MapSerializer;

/**
 * The class is used to configure the kryo serialization
 * of the spatial index. Changes to the kryo configuration may
 * break loading of existing index files - so changes must be made
 * with care.
 */
public class KryoRegistratorSpatialIndexV2 {

    private final static Logger LOGGER = LoggerFactory.getLogger(KryoRegistratorSpatialIndexV2.class);

    public static void registerClasses(Kryo kryo, Serializer<Geometry> geometrySerializer) {
        LOGGER.debug("Registering kryo serializers for spatial index v2.");

        // Java
        Serializer<?> mapSerializer = new MapSerializer();
        kryo.register(Map.class, mapSerializer);
        kryo.register(HashMap.class, mapSerializer);
        kryo.register(LinkedHashMap.class, mapSerializer);

        // Jena
        NodeSerializer.register(kryo);
        registerTripleSerializer(kryo); // Needed for RDFstar Nodes.

        // JTS
        kryo.register(STRtree.class, new STRtreeSerializer());
        kryo.register(Envelope.class, new EnvelopeSerializer());

        // Jena + JTS
        kryo.register(STRtreePerGraph.class, new STRtreePerGraphSerializer());

        // The index only stores envelopes and jena nodes.
        // Therefore, geometry serializers should not be needed.
        if (geometrySerializer != null) {
            registerGeometrySerializers(kryo, geometrySerializer);
        }
    }

    /**
     * The default serializer for {@link Triple}.
     * Must be registered in addition to node serializers for RDF-Star (triples-in-nodes).
     */
    public static void registerTripleSerializer(Kryo kryo) {
        kryo.register(Triple.class, new TripleSerializer());

        // Array-of-triples serializer variant would have to be registered separately - but does not seem to be needed.
        // kryo.register(Triple[].class);
    }

    public static void registerGeometrySerializers(Kryo kryo, Serializer<Geometry> geometrySerializer) {
        kryo.register(Point.class, geometrySerializer);
        kryo.register(LinearRing.class, geometrySerializer);
        kryo.register(LineString.class, geometrySerializer);
        kryo.register(Polygon.class, geometrySerializer);
        kryo.register(MultiPoint.class, geometrySerializer);
        kryo.register(MultiLineString.class, geometrySerializer);
        kryo.register(MultiPolygon.class, geometrySerializer);
        kryo.register(GeometryCollection.class, geometrySerializer);
    }
}
