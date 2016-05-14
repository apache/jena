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

package org.apache.jena.hadoop.rdf.io.output;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.TripleWritable;

/**
 * Abstract tests for triple output formats
 * 
 *
 */
public abstract class AbstractTripleOutputFormatTests extends AbstractNodeTupleOutputFormatTests<Triple, TripleWritable> {

    @Override
    protected Iterator<TripleWritable> generateTuples(int num) {
        List<TripleWritable> ts = new ArrayList<TripleWritable>();
        for (int i = 0; i < num; i++) {
            Triple t = new Triple(NodeFactory.createURI("http://example.org/subjects/" + i), NodeFactory.createURI("http://example.org/predicate"), NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger));
            ts.add(new TripleWritable(t));
        }
        return ts.iterator();
    }
}
