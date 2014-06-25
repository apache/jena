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

package com.hp.hpl.jena.reasoner.rulesys;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Finder;
import com.hp.hpl.jena.reasoner.IllegalParameterException;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

import java.io.*;
import java.util.*;

/**
 * A small random collection of utility functions used by the rule systems.
 */
public class Util {

    /**
     * Check whether a Node is a numeric (integer) value
     */
    public static boolean isNumeric(Node n) {
        return n.isLiteral() && n.getLiteralValue() instanceof Number;
    }

    /**
     * Return the integer value of a literal node
     */
    public static int getIntValue(Node n) {
        return ((Number)n.getLiteralValue()).intValue();
    }

    /**
     * Compare two numeric nodes.
     * @param n1 the first numeric valued literal node
     * @param n2 the second numeric valued literal node
     * @return -1 if n1 is less than n2, 0 if n1 equals n2 and +1 if n1 greater than n2
     * @throws ClassCastException if either node is not numeric
     */
    public static int compareNumbers(Node n1, Node n2) {
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteralValue();
            Object v2 = n2.getLiteralValue();
            if (v1 instanceof Number && v2 instanceof Number) {
                if (v1 instanceof Float || v1 instanceof Double
                        || v2 instanceof Float || v2 instanceof Double) {
                            double d1 = ((Number)v1).doubleValue();
                            double d2 = ((Number)v2).doubleValue();
                            return (d1 < d2) ? -1 : ( (d1 == d2) ? 0 : +1 );
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
     * Check whether a Node is an Instant (DateTime) value
     */
    public static boolean isInstant(Node n) {
        if (n.isLiteral()) {
            Object o = n.getLiteralValue();
            return (o instanceof XSDDateTime);
        } else {
            return false;
        }
    }

    /**
     * Compare two time Instant nodes.
     * @param n1 the first time instant (XSDDateTime) valued literal node
     * @param n2 the second time instant (XSDDateTime) valued literal node
     * @return -1 if n1 is less than n2, 0 if n1 equals n2 and +1 if n1 greater than n2
     * @throws ClassCastException if either not is not numeric
     */
    public static int compareInstants(Node n1, Node n2) {
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteralValue();
            Object v2 = n2.getLiteralValue();
            if (v1 instanceof XSDDateTime && v2 instanceof XSDDateTime) {
                XSDDateTime a = (XSDDateTime) v1;
                XSDDateTime b = (XSDDateTime) v2;
                return a.compare(b);
            }
        }
        throw new ClassCastException("Non-numeric literal in compareNumbers");
    }

    /**
     * General order comparator for typed literal nodes, works for all numbers and
     * for date times.
     *
     */
    // Thanks to Bradley Schatz (Bradley@greystate.com) for the original suggestions
    // for datetime comparison. This code is a rewrite based on those suggestions.
    public static int compareTypedLiterals(Node n1, Node n2) {
        if (n1.isLiteral() && n2.isLiteral()) {
            Object v1 = n1.getLiteralValue();
            Object v2 = n2.getLiteralValue();
            if (v1 instanceof XSDDateTime && v2 instanceof XSDDateTime) {
                XSDDateTime a = (XSDDateTime) v1;
                XSDDateTime b = (XSDDateTime) v2;
                return a.compare(b);
            } else {
                if (v1 instanceof Number && v2 instanceof Number) {
                    if (v1 instanceof Float || v1 instanceof Double
                            || v2 instanceof Float || v2 instanceof Double) {
                                double d1 = ((Number)v1).doubleValue();
                                double d2 = ((Number)v2).doubleValue();
                                return (d1 < d2) ? -1 : ( (d1 == d2) ? 0 : +1 );
                    } else {
                        long l1 = ((Number)v1).longValue();
                        long l2 = ((Number)v2).longValue();
                        return (l1 < l2) ? -1 : ( (l1 == l2) ? 0 : +1 );
                    }
                }
            }
        }
        throw new ClassCastException("Compare typed literals can only compare numbers and datetimes");
    }

    /**
     * Test if two literals are comparable by an order operator (both numbers or both times)
     */
    public static boolean comparable(Node n1, Node n2) {
       return (isNumeric(n1) && isNumeric(n2)) || (isInstant(n1) && isInstant(n2));
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
    private static Node doGetPropValue(ClosableIterator<Triple> it) {
        Node result = null;
        if (it.hasNext()) {
            result = it.next().getObject();
        }
        it.close();
        return result;
    }

    /**
     * Convert an (assumed well formed) RDF list to a java list of Nodes
     * @param root the root node of the list
     * @param context the graph containing the list assertions
     */
    public static List<Node> convertList(Node root, RuleContext context) {
        return convertList(root, context, new LinkedList<Node>());
    }

    /**
     * Convert an (assumed well formed) RDF list to a java list of Nodes
     */
    private static List<Node> convertList( Node node, RuleContext context, List<Node> sofar ) {
        if (node == null || node.equals(RDF.nil.asNode())) return sofar;
        Node next = getPropValue(node, RDF.first.asNode(), context);
        if (next != null) {
            sofar.add(next);
            return convertList(getPropValue(node, RDF.rest.asNode(), context), context, sofar);
        } else {
            return sofar;
        }
    }

    /**
     * Construct a new integer valued node
     */
    public static Node makeIntNode(int value) {
        return NodeFactory.createLiteral(LiteralLabelFactory.create( value ));
    }

    /**
     * Construct a new long valued node
     */
    public static Node makeLongNode(long value) {
        if (value > Integer.MAX_VALUE) {
            return NodeFactory.createLiteral(LiteralLabelFactory.create( value ));
        } else {
            return NodeFactory.createLiteral(LiteralLabelFactory.create( (int) value ));
        }
    }

    /**
     * Construct a new double valued node
     */
    public static Node makeDoubleNode(double value) {
        return NodeFactory.createLiteral(LiteralLabelFactory.create( value ));
    }

    /**
     * Construct an RDF list from the given array of nodes and assert it
     * in the graph returning the head of the list.
     */
    public static Node makeList(Node[] nodes, Graph graph) {
        return doMakeList(nodes, 0, graph);
    }

    /**
     * Internals of makeList.
     */
    private static Node doMakeList(Node[] nodes, int next, Graph graph) {
        if (next < nodes.length) {
            Node listNode = NodeFactory.createAnon();
            graph.add(new Triple(listNode, RDF.Nodes.first, nodes[next]));
            graph.add(new Triple(listNode, RDF.Nodes.rest, doMakeList(nodes, next+1, graph)));
            return listNode;
        } else {
            return RDF.Nodes.nil;
        }
    }

    /**
     * Open a resource file and read it all into a single string.
     * Treats lines starting with # as comment lines, as per stringFromReader
     */
    public static Rule.Parser loadRuleParserFromResourceFile( String filename ) {
        return Rule.rulesParserFromReader( FileUtils.openResourceFile( filename ) );
    }

    /**
     * Open a file defined by a URL and read all of it into a single string.
     * If the URL fails it will try a plain file name as well.
     */
    public static String loadURLFile(String urlStr) throws IOException {
        try ( BufferedReader dataReader = FileUtils.readerFromURL( urlStr );
              StringWriter sw = new StringWriter(1024); ) {
            char buff[] = new char[1024];
            while (dataReader.ready()) {
                int l = dataReader.read(buff);
                if (l <= 0)
                    break;
                sw.write(buff, 0, l);
            }
            return sw.toString();
        }
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
            return i.nextStatement().getObject().toString().equalsIgnoreCase( "true" );
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
                return ( (Literal) lit ).getInt();
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
        config.addProperty( parameter, value.toString() );
    }
}
