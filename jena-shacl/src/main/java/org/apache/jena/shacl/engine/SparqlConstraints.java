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

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.riot.other.G;
import org.apache.jena.shacl.engine.constraint.SparqlConstraint;
import org.apache.jena.shacl.lib.ShLib;
import org.apache.jena.shacl.parser.Constraint;
import org.apache.jena.shacl.parser.ShaclParseException;
import org.apache.jena.shacl.vocabulary.SHACL;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
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
        //G.contains(shapesGraph, sparqlConstraintNode, C.rdfType, SHACL.SPARQLConstraint);
        Node message = G.getZeroOrOneSP(shapesGraph, sparqlConstraintNode, SHACL.message);
        boolean deactivated = absentOrOne(shapesGraph, sparqlConstraintNode, SHACL.deactivated, NodeConst.nodeTrue);

        // XXX Optimize prefixes acquisition in case of use from more than one place.
        String prefixes = prefixes(shapesGraph, sparqlConstraintNode);

        Node selectNode = G.getOneSP(shapesGraph, sparqlConstraintNode, SHACL.select);
        if ( ! Util.isSimpleString(selectNode) )
            throw new ShaclParseException("Not a string for sh:select: "+ShLib.displayStr(selectNode));
        String selectQuery = selectNode.getLiteralLexicalForm();
        // If parse error - constraint failure.
        String qs = prefixes+"\n"+selectQuery;
        String msg = (message != null && message.isLiteral() ? message.getLiteralLexicalForm() : null );
        try {
            Query query = parseQueryString(qs);
            return new SparqlConstraint(query, msg);
        } catch (QueryParseException ex) {
//            Log.warn("SHACL", "SPARQL parse error: "+ex.getMessage()+"\n"+qs);
//            return new SparqlConstraint(new Query(), msg);
            throw new ShaclParseException("SPARQL parse error: "+ex.getMessage()+"\n"+qs);
        }
    }

    /** 
     * Parse a string to produce a {@link Query}.
     * All {@link Query} should go through this function to allow of inserting default prefixes. 
     */
    public static Query parseQueryString(String queryString) {
        Query query = new Query();
        // The SHACL spec does not define any default prefixes.
        // But for identified practical reasons some may be added such as: 
//        query.getPrefixMapping().setNsPrefix("owl",  OWL.getURI());
//        query.getPrefixMapping().setNsPrefix("rdf",  RDF.getURI());
//        query.getPrefixMapping().setNsPrefix("rdfs", RDFS.getURI());
//        query.getPrefixMapping().setNsPrefix("sh",   SHACL.getURI());
//        query.getPrefixMapping().setNsPrefix("xsd",  XSD.getURI());
        QueryFactory.parse(query, queryString, null,  Syntax.defaultQuerySyntax);
        return query;
    }

    /**
     * Test for zero or one occurrences of a tripel pattern that is expected to be   
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

    private static String prefixesQueryString = StrUtils.strjoinNL
        ("PREFIX owl:     <http://www.w3.org/2002/07/owl#>"
        ,"PREFIX sh:      <http://www.w3.org/ns/shacl#>"
        ,"SELECT * { ?x sh:prefixes/owl:imports*/sh:declare [ sh:prefix ?prefix ; sh:namespace ?namespace ] }"
        );
    private static Query prefixesQuery = QueryFactory.create(prefixesQueryString);
    private static Var varPrefix = Var.alloc("prefix");
    private static Var varNamespace = Var.alloc("namespace");

    public static String prefixes(Graph shapesGraph, Node sparqlNode) {
        // XXX Ignores sparqlNode ATM
        StringJoiner prefixesSJ = new StringJoiner("\n");
        QueryExecution qExec = QueryExecutionFactory.create(prefixesQuery, DatasetGraphFactory.wrap(shapesGraph));
        ResultSet rs = qExec.execSelect();
        Map<String, String> seen = new HashMap<>();

        while(rs.hasNext()) {
            Binding binding = rs.nextBinding();
            Node nPrefix = binding.get(varPrefix);
            Node nNamespace = binding.get(varNamespace);
            String prefix = nPrefix.getLiteralLexicalForm();
            String ns = nNamespace.getLiteralLexicalForm();
            if ( seen.containsKey(prefix) ) {
                if ( seen.get(prefix).equals(ns) )
                    continue;
            }
            prefixesSJ.add("PREFIX "+prefix+": <"+ns+">");
            seen.put(prefix, ns);
        }
        return prefixesSJ.toString();
    }
}
