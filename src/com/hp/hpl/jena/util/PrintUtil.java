/******************************************************************
 * File:        PrintUtil.java
 * Created by:  Dave Reynolds
 * Created on:  29-Mar-03
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: PrintUtil.java,v 1.15 2005-02-21 12:18:57 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.util;

import java.util.*;
import java.io.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * A collection of small utilites for pretty printing nodes, triples
 * and associated things. The core functionality here is a static
 * prefix map which is preloaded with known prefixes.
 * 
 * <p>updated by Chris March 2004 to use a PrefixMapping rather than the
 * specialised tables.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.15 $ on $Date: 2005-02-21 12:18:57 $
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
        registerPrefix("daml", DAML_OIL.NAMESPACE_DAML.getURI());
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
     * Return a simplified print string for a Node. 
     */
    public static String print(Node node) {
        if (node instanceof Node_URI) {
            return node.toString( prefixMapping );
        } else if (node instanceof Node_Literal) {
            LiteralLabel ll = node.getLiteral();
            String lf = ll.getLexicalForm();
            return ll.getDatatype() == null ? "'" + lf + "'" : lf + "^^" + ll.getDatatypeURI();
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
        StringBuffer spaces = new StringBuffer();
        for (int i = 0; i < indent; i++) spaces.append(" ");
        out.print(spaces.toString());
    }
    
    /**
     * Print all the Triple values from a find iterator.
     */
    public static void printOut(Iterator it) {
        while (it.hasNext()) {
            System.out.println("   " + print(it.next()));
        }
    }
}

/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/