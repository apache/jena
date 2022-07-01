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

package org.apache.jena.sparql.modify.request;

import org.apache.jena.atlas.lib.Sink ;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple ;
import org.apache.jena.query.QueryParseException ;
import org.apache.jena.sparql.core.Quad ;

/** Accumulate quads (excluding allowing variables) during parsing. */
public class QuadDataAccSink extends QuadAccSink
{
    public QuadDataAccSink(Sink<Quad> sink) {
        super(sink);
    }

    @Override
    protected void check(Triple triple) {
        check(getGraph(), triple.getSubject(), triple.getPredicate(), triple.getObject());
    }

    @Override
    protected void check(Quad quad) {
        check(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
    }

    private void check(Node g, Node s, Node p, Node o) {
        if ( templateOnly(g) || templateOnly(s) || templateOnly(p) || templateOnly(o) )
            throw new QueryParseException("Variables not permitted in data", -1, -1);
        if ( s.isLiteral() )
            throw new QueryParseException("Literals not allowed as subjects in data", -1, -1);
    }

    private boolean templateOnly(Node n) {
        // Variables, and recursively for triple terms.
        return !n.isConcrete();
    }
}
