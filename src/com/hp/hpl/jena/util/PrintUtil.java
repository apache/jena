/******************************************************************
 * File:        PrintUtil.java
 * Created by:  Dave Reynolds
 * Created on:  29-Mar-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: PrintUtil.java,v 1.4 2003-06-13 16:31:47 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.util;

import java.util.*;
import java.io.*;
import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.TriplePattern;

/**
 * A collection of small utilites for pretty printing nodes, triples
 * and associated things. The core functionality here is a static
 * prefix map which is preloaded with known prefixes.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-06-13 16:31:47 $
 */
public class PrintUtil {

    /** The global, static, prefix map */
    protected static Map prefixToNS = new HashMap();
    
    /** The inverse prefix map */
    protected static Map nsToPrefix = new HashMap();
    
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
        registerPrefix("owl", OWL.NAMESPACE);
        registerPrefix("daml", DAML_OIL.NAMESPACE_DAML.getURI());
        registerPrefix("jr", ReasonerVocabulary.getJenaReasonerNS());
        registerPrefix("rb", ReasonerVocabulary.getRBNamespace());
        registerPrefix("eg", egNS);
    }
    
    /**
     * Register a new prefix/namespace mapping which will be used to shorten
     * the print strings for resources in known namespaces.
     */
    public static void registerPrefix(String prefix, String namespace) {
        prefixToNS.put(prefix, namespace);
        nsToPrefix.put(namespace, prefix);
    }
    
    /**
     * Return a simplified print string for a Node. 
     */
    public static String print(Node node) {
        if (node instanceof Node_URI) {
            String uri = node.getURI();
            int split = uri.lastIndexOf('#');
            if (split == -1) {
                split = uri.lastIndexOf('/');
                if (split == -1) split = -1;
            }
            String ns = uri.substring(0, split+1);
            String prefix = (String)nsToPrefix.get(ns);
            if (prefix == null) {
                return node.toString();
            } else {
                return prefix + ":" + uri.substring(split+1);
            }
        } else if (node instanceof Node_Literal) {
            LiteralLabel ll = node.getLiteral();
            if (ll.getDatatype() == null) {
                return "'" + ll + "'";
            } else {
                return ll.toString();
            }
        } else if (node instanceof Node_ANY) {
            return "*";
        }
        if (node == null) {
            return "Null";
        }
        return node.toString();
    }
    
    /**
     * Return a simplified print string for an RDFNode. 
     */
    public static String print(RDFNode node) {
        return print(node.asNode());
    }
    
    /**
     * Return a simplified print string for a Triple
     */
    public static String print(Triple triple) {
        return "(" + print(triple.getSubject()) + " " +
                      print(triple.getPredicate()) + " " +
                      print(triple.getObject()) + ")";
    }
    
    /**
     * Return a simplified print string for a TriplePattern
     */
    public static String print(TriplePattern triple) {
        return "(" + print(triple.getSubject()) + " " +
                      print(triple.getPredicate()) + " " +
                      print(triple.getObject()) + ")";
    }
    
    /**
     * Return a simplified print string for a statment
     */
    public static String print(Statement stmt) {
        return print(stmt.asTriple());
    }
    
    /**
     * Default print which just uses tostring
     */
    public static String print(Object obj) {
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
        int split = uri.indexOf(':');
        String nsPrefix = uri.substring(0, split);
        String localname = uri.substring(split+1);
        String ns = (String)prefixToNS.get(nsPrefix);
        if (ns != null) {
            return ns + localname;
        } else {
            return uri;
        }
    }
    
    /**
     * Print an n-space indent to the given output stream
     */
    public static void printIndent(PrintWriter out, int indent) {
        StringBuffer spaces = new StringBuffer();
        for (int i = 0; i < indent; i++) spaces.append(" ");
        out.print(spaces.toString());
    }
}

/*
    (c) Copyright Hewlett-Packard Company 2003
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