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

package org.apache.jena.riot.system;

import org.apache.jena.atlas.lib.BitsLong ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.datatypes.RDFDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.sparql.core.Quad ;

public class FactoryRDFStd implements FactoryRDF {
    // Needs reset?
    private final LabelToNode labelMapping ;

    public FactoryRDFStd() {
        this(SyntaxLabels.createLabelToNode()) ;
    }

    public FactoryRDFStd(LabelToNode labelMapping) {
        this.labelMapping = labelMapping ;
    }

    @Override
    public Triple createTriple(Node subject, Node predicate, Node object) {
        return Triple.create(subject, predicate, object);
    }

    @Override
    public Quad createQuad(Node graph, Node subject, Node predicate, Node object) {
        return Quad.create(graph, subject, predicate, object) ;
    }

    @Override
    public Node createURI(String uriStr) {
        return RiotLib.createIRIorBNode(uriStr) ;
        //return NodeFactory.createURI(uriStr) ;
    }

    @Override
    public Node createTypedLiteral(String lexical, RDFDatatype datatype) {
        return NodeFactory.createLiteral(lexical, datatype) ;
    }

    @Override
    public Node createLangLiteral(String lexical, String langTag) {
        return NodeFactory.createLiteral(lexical, langTag) ;
    }

    @Override
    public Node createStringLiteral(String lexical) {
        return NodeFactory.createLiteral(lexical) ;
    }

    @Override
    public Node createBlankNode(long mostSigBits, long leastSigBits) {
        if ( false ) {
            //int version = (int)BitsLong.unpack(mostSigBits, 12,16) ;
            int variant = (int)BitsLong.unpack(leastSigBits, 62, 64) ;
            if ( variant != 2 )
                Log.warn(this, "Bad variant "+variant+" for blank node") ;
        }

        // XXX Style: Do this fast.  Guava? Apache commons? Special case for char[32]
        // (Eventually, blank node Nodes will have two longs normally.)
        return createBlankNode(String.format("%08X%08X", mostSigBits, leastSigBits)) ;
    }

    // Fixed scope.
    private static Node scope = null ;

    @Override
    public Node createBlankNode(String label) {
        return labelMapping.get(scope, label) ;
    }

    @Override
    public Node createBlankNode() {
        return labelMapping.create() ;
    }

    @Override
    public void reset() {
        labelMapping.clear();
    }
}
