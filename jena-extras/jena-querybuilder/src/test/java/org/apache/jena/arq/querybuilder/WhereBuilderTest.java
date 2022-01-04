/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

public class WhereBuilderTest {

    @Test
    public void verifyToStringShowsValues() {
        // Node creation
        Node svarNode = Converters.checkVar(NodeFactory.createVariable("s"));
        Node pvarNode = Converters.checkVar(NodeFactory.createVariable("p"));
        Node ovarNode = Converters.checkVar(NodeFactory.createVariable("o"));

        // Values map creation
        ArrayList<Node> subjColl = new ArrayList<>();
        subjColl.add(NodeFactory.createURI("http://example.org#subject"));

        HashMap<Node, ArrayList<Node>> valMap = new HashMap<>();
        valMap.put(svarNode, subjColl);

        // WhereBuilder creation
        WhereBuilder wb = new WhereBuilder();
        wb.addWhere(svarNode, pvarNode, ovarNode);
        wb.addWhereValueVars(valMap);

        assertTrue(wb.toString().contains("VALUES ?s { <http://example.org#subject> }"));

    }

}
