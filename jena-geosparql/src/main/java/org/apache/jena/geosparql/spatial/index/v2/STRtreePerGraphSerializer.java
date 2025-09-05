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

import java.util.Map;

import org.apache.jena.graph.Node;
import org.locationtech.jts.index.strtree.STRtree;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

public class STRtreePerGraphSerializer
    extends Serializer<STRtreePerGraph>
{
    @Override
    public void write(Kryo kryo, Output output, STRtreePerGraph index) {
        output.writeBoolean(index.isBuilt());
        kryo.writeClassAndObject(output, index.getInternalTreeMap());
    }

    @Override
    public STRtreePerGraph read(Kryo kryo, Input input, Class<? extends STRtreePerGraph> type) {
        boolean isBuilt = input.readBoolean();
        @SuppressWarnings("unchecked")
        Map<Node, STRtree> treeMap = (Map<Node, STRtree>)kryo.readClassAndObject(input);
        STRtreePerGraph result = new STRtreePerGraph(treeMap);
        if (isBuilt) {
            result.build();
        }
        return result;
    }
}
