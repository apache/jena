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

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Geometry de-/serialization via ShapeSerde of Apache sedona-spark.
 * This is a stub, because ShapeSerde currently draws in many dependencies; some which cause
 * issues with the maven enforcer plugin.
 */
public class GeometrySerdeAdapterShapeSerde
    implements GeometrySerdeAdapter
{
    protected GeometryFactory geometryFactory;

    public GeometrySerdeAdapterShapeSerde() {
        this(new GeometryFactory());
    }

    public GeometrySerdeAdapterShapeSerde(GeometryFactory geometryFactory) {
        super();
        this.geometryFactory = geometryFactory;
    }

    @Override
    public void write(Kryo kryo, Output output, Geometry geometry) {
        // byte[] data = ShapeSerde.serialize(geometry);
        // output.write(data, 0, data.length);
        throw new UnsupportedOperationException();
    }

    @Override
    public Geometry read(Kryo kryo, Input input) throws Exception {
        // Geometry geometry = ShapeSerde.deserialize(input, geometryFactory);
        // return geometry
        throw new UnsupportedOperationException();
    }
}
