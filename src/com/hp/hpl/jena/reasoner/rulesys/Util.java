/******************************************************************
 * File:        Util.java
 * Created by:  Dave Reynolds
 * Created on:  11-Apr-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Util.java,v 1.10 2003-08-26 15:16:14 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Finder;
import com.hp.hpl.jena.reasoner.IllegalParameterException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * A small random collection of utility functions used by the rule systems.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.10 $ on $Date: 2003-08-26 15:16:14 $
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
     * Compare two numeric nodes.
     * @param n1 the first numeric valued literal node
     * @param n2 the second numeric valued literal node
     * @return -1 if n1 is less than n2, 0 if n1 equals n2 and +1 if n1 greater than n2
     * @throws ClassCastException if either not is not numeric
     */
    public static int compareNumbers(Node n1, Node n2) {
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteral().getValue();
            Object v2 = n2.getLiteral().getValue();
            if (v1 instanceof Number && v2 instanceof Number) {
                if (v1 instanceof Float || v1 instanceof Double 
                        || v1 instanceof Float || v2 instanceof Double) {
                            return Double.compare(((Number)v1).doubleValue(), ((Number)v2).doubleValue());                            
                } else {
                    long l1 = ((Number)v1).longValue();
                    long l2 = ((Number)v2).longValue();
                    return (l1 < l2) ? -1 : ( (l1 == l2) ? 0 : +1 );
                }
            }
        }
        throw new ClassCastException("Non-numeric literal in compareNumbers");
    }
    
    /**
     * Helper - returns the (singleton) value for the given property on the given
     * root node in the data graph.
     */
    public static Node getPropValue(Node root, Node prop, Finder context) {
        return doGetPropValue(context.find(new TriplePattern(root, prop, null)));
    }
   
    /**
     * Helper - returns the (singleton) value for the given property on the given
     * root node in the data graph.
     */
    public static Node getPropValue(Node root, Node prop, Graph context) {
        return doGetPropValue(context.find(root, prop, null));
    }
    
    /**
     * Helper - returns the (singleton) value for the given property on the given
     * root node in the data graph.
     */
    public static Node getPropValue(Node root, Node prop, RuleContext context) {
        return doGetPropValue(context.find(root, prop, null));
    }
    
    /**
     * Internall implementation of all the getPropValue variants.
     */
    private static Node doGetPropValue(ClosableIterator it) {
        Node result = null;
        if (it.hasNext()) {
            result = ((Triple)it.next()).getObject();
        }
        it.close();
        return result;
    }
    
    /**
     * Convert an (assumed well formed) RDF list to a java list of Nodes
     * @param root the root node of the list
     * @param context the graph containing the list assertions
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
     * Construct a new long valued node
     */
    public static Node makeLongNode(long value) {
        if (value > Integer.MAX_VALUE) {
            return Node.createLiteral(new LiteralLabel(new Long(value)));
        } else {
            return Node.createLiteral(new LiteralLabel(new Integer((int)value)));
        }
    }
    
    /**
     * Construct a new double valued node
     */
    public static Node makeDoubleNode(double value) {
        return Node.createLiteral(new LiteralLabel(new Double(value)));
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
            if (line.startsWith("//")) continue;     // Skip comment lines
            result.append(line);
            result.append("\n");
        }
        return result.toString();
    }
    
    /**
     * Open a file defined by a URL and read it into a single string.
     * If the URL fails it will try a plain file name as well.
     */
    public static String loadURLFile(String urlStr) throws IOException {
        BufferedReader dataReader;
        try {
            URL url = new URL(urlStr);
            dataReader = new BufferedReader(new InputStreamReader(url.openStream())) ;
        } catch (java.net.MalformedURLException e) {
            // Try as a file.
            dataReader = new BufferedReader(new FileReader(urlStr));
        }
        StringWriter sw = new StringWriter(1024);
        char buff[] = new char[1024];
        while (dataReader.ready()) {
            int l = dataReader.read(buff);
            if (l <= 0)
                break;
            sw.write(buff, 0, l);
        }
        dataReader.close();
        sw.close();
        return sw.toString();
    }
    
    /**
     * Helper method - extracts the truth of a boolean configuration
     * predicate.
     * @param predicate the predicate to be tested
     * @param configuration the configuration node
     * @return null if there is no setting otherwise a Boolean giving the setting value
     */
    public static Boolean checkBinaryPredicate(Property predicate, Resource configuration) {
        StmtIterator i = configuration.listProperties(predicate);
        if (i.hasNext()) {
            return new Boolean(i.nextStatement().getObject().toString().equalsIgnoreCase("true"));
        } else {
            return null;
        }
    }
    
    /**
     * Helper method - extracts the value of an integer configuration
     * predicate.
     * @param predicate the predicate to be tested
     * @param configuration the configuration node
     * @return null if there is no such configuration parameter otherwise the value as an integer
     */
    public static Integer getIntegerPredicate(Property predicate, Resource configuration) {
        StmtIterator i = configuration.listProperties(predicate);
        if (i.hasNext()) {
            RDFNode lit = i.nextStatement().getObject();
            if (lit instanceof Literal) {
                return new Integer(((Literal)lit).getInt());
            }
        }
        return null;
    }

    /**
     * Convert the value of a boolean configuration parameter to a boolean value.
     * Allows the value to be specified using a String or Boolean.
     * @param parameter the configuration property being set (to help with error messages)
     * @param value the parameter value
     * @return the converted value
     * @throws IllegalParameterException if the value can't be converted
     */
    public static boolean convertBooleanPredicateArg(Property parameter, Object value) {
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        } else if (value instanceof String) {
            return ((String)value).equalsIgnoreCase("true");
        } else {
            throw new IllegalParameterException("Illegal type for " + parameter + " setting - use a Boolean");
        }
        
    }

    /**
     * Convert the value of an integer configuration parameter to an int value.
     * Allows the value to be specified using a String or Number.
     * @param parameter the configuration property being set (to help with error messages)
     * @param value the parameter value
     * @return the converted value
     * @throws IllegalParameterException if the value can't be converted
     */
    public static int convertIntegerPredicateArg(Property parameter, Object value) {
        if (value instanceof Number) {
            return ((Number)value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException e) {
                throw new IllegalParameterException("Illegal type for " + parameter + " setting - use an integer");
            }
        } else {
            throw new IllegalParameterException("Illegal type for " + parameter + " setting - use an integer");
        }            
    }
    
    /**
     * Replace the value for a given parameter on the resource by a new value.
     * @param config the resource whose values are to be updated
     * @param parameter a predicate defining the parameter to be set
     * @param value the new value
     */
    public static void updateParameter(Resource config, Property parameter, Object value) {
        for (StmtIterator i = config.listProperties(parameter); i.hasNext(); ) {
             i.next();
             i.remove();
        }
        config.addProperty(parameter, value);
    }
}
