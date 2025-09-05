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
package org.apache.jena.geosparql.kryo;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

/** Kryo serializer for {@link Triple}. Depends on registered {@link Node} serializers. */
public class TripleSerializer extends Serializer<Triple> {
    @Override
    public void write(Kryo kryo, Output output, Triple obj) {
        kryo.writeClassAndObject(output, obj.getSubject());
        kryo.writeClassAndObject(output, obj.getPredicate());
        kryo.writeClassAndObject(output, obj.getObject());
    }

    @Override
    public Triple read(Kryo kryo, Input input, Class<? extends Triple> objClass) {
        Node s = (Node)kryo.readClassAndObject(input);
        Node p = (Node)kryo.readClassAndObject(input);
        Node o = (Node)kryo.readClassAndObject(input);
        Triple result = Triple.create(s, p, o);
        return result;
    }
}
