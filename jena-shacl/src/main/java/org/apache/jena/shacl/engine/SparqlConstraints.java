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

import java.util.StringJoiner;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.shacl.engine.constraint.SparqlConstraint;
import org.apache.jena.shacl.lib.G;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.NodeConst;

public class SparqlConstraints {
    //    5. SPARQL-based Constraints
    //
    //    5.1 An Example SPARQL-based Constraint
    //    5.2 Syntax of SPARQL-based Constraints
    //        5.2.1 Prefix Declarations for SPARQL Queries
    //    5.3 Validation with SPARQL-based Constraints
    //        5.3.1 Pre-bound Variables in SPARQL Constraints ($this, $shapesGraph, $currentShape)
    //        5.3.2 Mapping of Solution Bindings to Result Properties

    private static String prefixesQueryString = StrUtils.strjoinNL
        ("PREFIX owl:     <http://www.w3.org/2002/07/owl#>"
        ,"PREFIX sh:      <http://www.w3.org/ns/shacl#>"
        ,"SELECT * { ?x sh:prefixes/owl:imports*/sh:declare [ sh:prefix ?prefix ; sh:namespace ?namespace ] }"
        );
    private static Query prefixesQuery = QueryFactory.create(prefixesQueryString);
    private static Var varPrefix = Var.alloc("prefix");
    private static Var varNamespace = Var.alloc("namespace");

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
        //G.contains(shapesGraph, sparqlConstraintNode, C.rdfType, SHACL.SPARQLConstraint);
        Node message = G.getZeroOrOneSP(shapesGraph, sparqlConstraintNode, SHACL.message);
        boolean deactivated = G.absentOrOne(shapesGraph, sparqlConstraintNode, SHACL.deactivated, NodeConst.nodeTrue);

        // XXX Optimize prefixes acquisition in case of use from more than one place.
        String prefixes = prefixes(shapesGraph, sparqlConstraintNode);

        Node selectNode = G.getOneSP(shapesGraph, sparqlConstraintNode, SHACL.select);
        if ( ! Util.isSimpleString(selectNode) )
            throw new ShaclParseException("Not a string for sh:select: "+ShLib.displayStr(selectNode));
        String selectQuery = selectNode.getLiteralLexicalForm();
        // If parse error - constraint failure.
        String qs = prefixes+"\n"+selectQuery;
        try { 
            Query query = QueryFactory.create(qs);
            return new SparqlConstraint(query);
        } catch (QueryParseException ex) {
            throw new ShaclParseException("SPARQL parse error: "+ex.getMessage()+"\n"+qs);
        }
    }

    public static String prefixes(Graph shapesGraph, Node sparqlNode) {
        // XXX Ignores sparqlNode ATM
        StringJoiner prefixesSJ = new StringJoiner("\n");
        QueryExecution qExec = QueryExecutionFactory.create(prefixesQuery, DatasetGraphFactory.wrap(shapesGraph));
        ResultSet rs = qExec.execSelect();

        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node nPrefix = binding.get(varPrefix);
            Node nNamespace = binding.get(varNamespace);
            String prefix = nPrefix.getLiteralLexicalForm();
            String ns = nNamespace.getLiteralLexicalForm();
            prefixesSJ.add("PREFIX "+prefix+": <"+ns+">");
        }
        return prefixesSJ.toString();
    }
}
