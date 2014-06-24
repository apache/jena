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

package com.hp.hpl.jena.util;

import java.util.*;
import java.io.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * A collection of small utilites for pretty printing nodes, triples
 * and associated things. The core functionality here is a static
 * prefix map which is preloaded with known prefixes.
 */
public class PrintUtil {
    
    protected static PrefixMapping prefixMapping = PrefixMapping.Factory.create();
    
    /** Default built in eg namespace used in testing */
    public static final String egNS = "urn:x-hp:eg/";
        
    static {
        init();
    }
    
    /**
     * Load built in prefixes.
     */
    public static void init() {
        registerPrefix("rdf", RDF.getURI());
        registerPrefix("rdfs", RDFS.getURI());
        registerPrefix("drdfs", "urn:x-hp-direct-predicate:http_//www.w3.org/2000/01/rdf-schema#");
        registerPrefix("owl", OWL.getURI());
        registerPrefix("jr", ReasonerVocabulary.getJenaReasonerNS());
        registerPrefix("rb", ReasonerVocabulary.getRBNamespace());
        registerPrefix("eg", egNS);
        registerPrefix("xsd", XSDDatatype.XSD + "#");
    }
    
    /**
     * Register a new prefix/namespace mapping which will be used to shorten
     * the print strings for resources in known namespaces.
     */
    public static void registerPrefix(String prefix, String namespace) {
        prefixMapping.setNsPrefix( prefix, namespace );
    }
    
    /**
     * Register a set of new prefix/namespace mapping which will be used to shorten
     * the print strings for resources in known namespaces.
     */
    public static void registerPrefixMap(Map<String, String> map) {
        prefixMapping.setNsPrefixes( map );
    }
    
    /**
     * Remove a registered prefix from the table of known short forms
     */
    public static void removePrefix(String prefix) {
        prefixMapping.removeNsPrefix(prefix);
    }
    
    /**
     * Remove a set of prefix mappings from the table of known short forms
     */
    public static void removePrefixMap(Map<String, String> map) {
        for ( String s : map.keySet() )
        {
            prefixMapping.removeNsPrefix( s );
        }
    }
    
    /**
     * Return a simplified print string for a Node. 
     */
    public static String print(Node node) {
        if (node instanceof Node_URI) {
            return node.toString( prefixMapping );
        } else if (node instanceof Node_Literal) {
            String lf = node.getLiteralLexicalForm();
            return "'" + lf + "'" + (node.getLiteralDatatype() == null ? "" : "^^" + node.getLiteralDatatypeURI());
        } else if (node instanceof Node_ANY) {
            return "*";
        }
        if (node == null) {
            return "null";
        }
        return node.toString();
    }
    
    /**
     * Return a simplified print string for an RDFNode. 
     */
    public static String print(RDFNode node) {
        if (node == null) return "null";
        return print(node.asNode());
    }
    
    /**
     * Return a simplified print string for a Triple
     */
    public static String print(Triple triple) {
        if (triple == null) return "(null)";
        return "(" + print(triple.getSubject()) + " " +
                      print(triple.getPredicate()) + " " +
                      print(triple.getObject()) + ")";
    }
    
    /**
     * Return a simplified print string for a TriplePattern
     */
    public static String print(TriplePattern triple) {
        if (triple == null) return "(null)";
        return "(" + print(triple.getSubject()) + " " +
                      print(triple.getPredicate()) + " " +
                      print(triple.getObject()) + ")";
    }
    
    /**
     * Return a simplified print string for a statment
     */
    public static String print(Statement stmt) {
        if (stmt == null) return "(null)";
        return print(stmt.asTriple());
    }
    
    /**
     * Default print which just uses tostring
     */
    public static String print(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Triple) {
            return print((Triple)obj);
        } else if (obj instanceof TriplePattern) {
            return print((TriplePattern)obj);
        } else if (obj instanceof Node) {
            return print((Node)obj);
        } else if (obj instanceof RDFNode) {
            return print((RDFNode)obj);
        } else if (obj instanceof Statement) {
            return print((Statement)obj);
        } else {
            return obj.toString();
        }
    }
    
    /**
     * Expand qnames to URIs. If the given URI appears
     * to start with one of the registered prefixes then
     * expand the prefix, otherwise return the original URI
     */
    public static String expandQname(String uri) {
        return prefixMapping.expandPrefix( uri );
    }
    
    /**
     * Print an n-space indent to the given output stream
     */
    public static void printIndent(PrintWriter out, int indent) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < indent; i++) spaces.append(" ");
        out.print(spaces.toString());
    }
    
    /**
     * Print all the Triple values from a find iterator.
     */
    public static void printOut( Iterator<?> it ) {
        while (it.hasNext()) {
            System.out.println("   " + print(it.next()));
        }
    }
}
