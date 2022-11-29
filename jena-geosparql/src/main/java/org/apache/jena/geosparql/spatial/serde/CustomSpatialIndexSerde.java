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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.sedona.common.geometrySerde.GeometrySerde;
import org.apache.sedona.common.geometrySerde.SpatialIndexSerde;
import org.locationtech.jts.index.strtree.STRtree;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CustomSpatialIndexSerde extends SpatialIndexSerde {
    public CustomSpatialIndexSerde(GeometrySerde geometrySerde) {
        super(geometrySerde);
    }

    @Override
    public void write(Kryo kryo, Output output, Object o) {
        if (o instanceof STRtree) {
            //serialize rtree index
            output.writeByte((byte) 1);
            STRtree tree = (STRtree) o;
            org.locationtech.jts.index.strtree.IndexSerde indexSerde
                    = new org.locationtech.jts.index.strtree.IndexSerde();
            try {
                FieldUtils.writeField(indexSerde, "geometrySerde", new CustomGeometrySerde(), true);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            indexSerde.write(kryo, output, tree);
        } else {
            super.write(kryo, output, o);
        }
    }

    @Override
    public Object read(Kryo kryo, Input input, Class aClass) {
        byte typeID = input.readByte();
        if (typeID == 1) {
            org.locationtech.jts.index.strtree.IndexSerde indexSerde =
                    new org.locationtech.jts.index.strtree.IndexSerde();
            try {
                FieldUtils.writeField(indexSerde, "geometrySerde", new CustomGeometrySerde(), true);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return indexSerde.read(kryo, input);
        } else {
            return super.read(kryo, input, aClass);
        }

    }
}