/******************************************************************
 * File:        Util.java
 * Created by:  Dave Reynolds
 * Created on:  11-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Util.java,v 1.2 2003-04-22 14:20:07 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.*;
import java.util.*;

/**
 * A small random collection of utility functions used by the rule systems.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-04-22 14:20:07 $
 */
public class Util {

    /**
     * Check whether a Node is a numeric (integer) value
     */
    public static boolean isNumeric(Node n) {
        if (n.isLiteral()) {
            Object o = n.getLiteral().getValue();
            return (o instanceof Number);
        } else {
            return false;
        }
    }
    
    /**
     * Return the integer value of a literal node
     */
    public static int getIntValue(Node n) {
        return ((Number)n.getLiteral().getValue()).intValue();
    }
   
    /**
     * Helper - returns the (singleton) value for the given property on the given
     * root node in the data graph.
     */
    public static Node getPropValue(Node root, Node prop, Graph context) {
        Iterator i = context.find(root, prop, null);
        if (i.hasNext()) {
            return ((Triple)i.next()).getObject();
        } else {
            return null;
        }
    }
    
    /**
     * Helper - returns the (singleton) value for the given property on the given
     * root node in the data graph.
     */
    public static Node getPropValue(Node root, Node prop, RuleContext context) {
        Iterator i = context.find(root, prop, null);
        if (i.hasNext()) {
            return ((Triple)i.next()).getObject();
        } else {
            return null;
        }
    }
    
    /**
     * Convert an (assumed well formed) RDF list to a java list of Nodes
     * @param root the root node of the list
     * @param graph the graph containing the list assertions
     */
    public static List convertList(Node root, RuleContext context) {
        return convertList(root, context, new LinkedList());
    }
    
    /**
     * Convert an (assumed well formed) RDF list to a java list of Nodes
     */
    private static List convertList(Node node, RuleContext context, List sofar) {
        if (node == null || node.equals(RDF.nil.asNode())) return sofar;
        sofar.add(getPropValue(node, RDF.first.asNode(), context));
        return convertList(getPropValue(node, RDF.rest.asNode(), context), context, sofar);
    }
    
    /**
     * Construct a new integer valued node
     */
    public static Node makeIntNode(int value) {
        return Node.createLiteral(new LiteralLabel(new Integer(value)));
    }
    
    /**
     * Open an resource file for reading.
     */
    public static BufferedReader openResourceFile(String filename) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(filename);
        if (is == null) {
            // Can't find it on classpath, so try relative to current directory
            is = new FileInputStream(filename);
        }
        return new BufferedReader(new InputStreamReader(is, "UTF-8"));
    }
    
    /**
     * Open a resource file and read it all into a single string.
     * Treats lines starting with # as comment lines
     */
    public static String loadResourceFile(String filename) throws IOException {
        BufferedReader src = openResourceFile(filename);
        StringBuffer result = new StringBuffer();
        String line;
        while ((line = src.readLine()) != null) {
            if (line.startsWith("#")) continue;     // Skip comment lines
            result.append(line);
            result.append("\n");
        }
        return result.toString();
    }
    
    /**
     * Helper method - extracts the truth of a boolean configuration
     * predicate.
     * @param uri the base URI of the reasoner being configured
     * @param predicate the predicate to be tested
     * @param configuration the configuration model
     * @return null if there is no setting otherwise a Boolean giving the setting value
     */
    public static Boolean checkBinaryPredicate(String uri, Property predicate, Model configuration) {
        Resource base = configuration.getResource(uri);
        StmtIterator i = base.listProperties(predicate);
        if (i.hasNext()) {
            return new Boolean(i.nextStatement().getObject().toString().equalsIgnoreCase("true"));
        } else {
            return null;
        }
    }
    
    /**
     * Helper method - extracts the value of an integer configuration
     * predicate.
     * @param uri the base URI of the reasoner being configured
     * @param predicate the predicate to be tested
     * @param configuration the configuration model
     * @return null if there is no such configuration parameter otherwise the value as an integer
     */
    public static Integer getIntegerPredicate(String uri, Property predicate, Model configuration) {
        Resource base = configuration.getResource(uri);
        StmtIterator i = base.listProperties(predicate);
        if (i.hasNext()) {
            RDFNode lit = i.nextStatement().getObject();
            if (lit instanceof Literal) {
                return new Integer(((Literal)lit).getInt());
            }
        }
        return null;
    }


}
