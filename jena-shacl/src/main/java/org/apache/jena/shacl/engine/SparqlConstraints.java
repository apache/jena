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

package org.apache.jena.shacl.engine;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.engine.constraint.SparqlConstraint;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.util.iterator.ExtendedIterator;

public class SparqlConstraints {
    //    5. SPARQL-based Constraints
    //
    //    5.1 An Example SPARQL-based Constraint
    //    5.2 Syntax of SPARQL-based Constraints
    //        5.2.1 Prefix Declarations for SPARQL Queries
    //    5.3 Validation with SPARQL-based Constraints
    //        5.3.1 Pre-bound Variables in SPARQL Constraints ($this, $shapesGraph, $currentShape)
    //        5.3.2 Mapping of Solution Bindings to Result Properties

    public static Constraint parseSparqlConstraint(Graph shapesGraph, Node shape, Node p, Node sparqlConstraintNode) {
        /*
           sh:sparql [
               a sh:SPARQLConstraint ;   # This triple is optional
               sh:message "Values are literals with German language tag." ;
               sh:prefixes ex: ;
               sh:select """
                   SELECT $this (ex:germanLabel AS ?path) ?value
                   WHERE {
                       $this ex:germanLabel ?value .
                       FILTER (!isLiteral(?value) || !langMatches(lang(?value), "de"))
                   }
                   """ ;
           ] .
         */
        Node message = G.getZeroOrOneSP(shapesGraph, sparqlConstraintNode, SHACL.message);
        boolean deactivated = absentOrOne(shapesGraph, sparqlConstraintNode, SHACL.deactivated, NodeConst.nodeTrue);

        // XXX Optimize prefixes acquisition in case of use from more than one place.
        Query query = ShLib.extractSPARQLQuery(shapesGraph, sparqlConstraintNode);
        String msg = (message != null && message.isLiteral() ? message.getLiteralLexicalForm() : null );
        return new SparqlConstraint(query, msg);
    }

    /**
     * Test for zero or one occurrences of a triple pattern that is expected to be
     * Returns false for zero, true for one.
     * Throws an exception on two or more.
     */
    private static boolean absentOrOne(Graph g, Node s, Node p, Node o) {
        ExtendedIterator<Triple> iter = G.find(g, s, p, null);
        try {
            if ( ! iter.hasNext() )
                return false;
            iter.next();
            if ( ! iter.hasNext() )
                return true;
            long x = Iter.count(G.find(g, s, p, null));
            throw new ShaclParseException("More than one (" + x + ") of " + String.format("(%s %s %s)", s, p, o));
        }
        finally { iter.close(); }
    }
}