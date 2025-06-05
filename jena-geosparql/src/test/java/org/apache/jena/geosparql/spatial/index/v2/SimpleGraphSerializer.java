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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/** Only used for testing Node_Graph serialization. */
public class SimpleGraphSerializer
    extends Serializer<Graph>
{
    @Override
    public void write(Kryo kryo, Output output, Graph graph) {
        ExtendedIterator<Triple> it = graph.find();
        try {
            it.forEach(t -> kryo.writeObjectOrNull(output, t, Triple.class));
        } finally {
            it.close();
        }
        kryo.writeObjectOrNull(output, null, Triple.class);
    }

    @Override
    public Graph read(Kryo kryo, Input input, Class<Graph> type) {
        Graph result = GraphFactory.createDefaultGraph();
        for (;;) {
            Triple t = kryo.readObjectOrNull(input, Triple.class);
            if (t == null) {
                break;
            }
            result.add(t);
        }
        return result;
    }
}
