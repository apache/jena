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

package org.apache.jena.tdb2.store.tupletable;

import org.apache.jena.atlas.lib.BitsLong;
import org.apache.jena.tdb2.store.NodeId;
import org.apache.jena.tdb2.store.NodeIdFactory;
import org.apache.jena.tdb2.store.NodeIdType;

public class NData {
    public static NodeId n1 = create(1);
    public static NodeId n2 = create(2);
    public static NodeId n3 = create(3);
    public static NodeId n4 = NodeIdFactory.createValue(NodeIdType.XSD_INTEGER, 1);
    public static NodeId n5 = NodeIdFactory.createValue(NodeIdType.XSD_INTEGER, 2);
    // -2 as inlined.
    public static NodeId n6 = NodeIdFactory.createValue(NodeIdType.XSD_INTEGER, BitsLong.clear(-2L, 56, 64));

    private static NodeId create(long v) {
        return NodeIdFactory.createPtr(v);
    }
}
