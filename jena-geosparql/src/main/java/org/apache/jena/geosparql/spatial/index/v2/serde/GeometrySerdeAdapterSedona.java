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

/** Geometry de-/serialization via the facilities of Apache sedona-common. */
//public class GeometrySerdeAdapterSedona
//    implements GeometrySerdeAdapter, Serializable
//{
//    private static final long serialVersionUID = 1L;
//
//    @Override
//    public void write(Kryo kryo, Output output, Geometry geometry) {
//        byte[] data = GeometrySerializer.serialize(geometry);
//        output.writeInt(data.length);
//        output.write(data, 0, data.length);
//    }
//
//    @Override
//    public Geometry read(Kryo kryo, Input input) throws Exception {
//        int length = input.readInt();
//        byte[] bytes = new byte[length];
//        input.readBytes(bytes);
//        Geometry geometry = GeometrySerializer.deserialize(bytes);
//        return geometry;
//    }
//}
