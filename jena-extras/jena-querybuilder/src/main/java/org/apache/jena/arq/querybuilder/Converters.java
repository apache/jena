/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.arq.querybuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.graph.impl.LiteralLabelFactory;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.util.NodeFactoryExtra;

/**
 * A collection of static methods to convert from Objects to various types used
 * in Query and Update construction.
 */
public class Converters {

    private Converters() {
        // do not make instance
    }

    /**
     * Converts any Node_Variable nodes into Var nodes.
     * 
     * @param n the node to check
     * @return the node n or a new Var if n is an instance of Node_Variable
     */
    public static Node checkVar(Node n) {
        if (n.isVariable()) {
            return Var.alloc(n);
        }
        return n;
    }

    /**
     * Creates a literal from an object. If the object type is registered with the
     * TypeMapper the associated literal string is returned. If the object is not
     * registered an IllegalArgumentException is thrown.
     * 
     * @param o the object to convert.
     * @return the literal node.
     * @throws IllegalArgumentException if object type is not registered.
     */
    public static Node makeLiteral(Object o) {

        RDFDatatype dt = TypeMapper.getInstance().getTypeByValue(o);
        if (dt == null) {
            String msg = "No TypeDef defined for %s. Use TypeMapper.getInstance().register() to "
                    + "register one or use makeLiteral() method in query builder instance.";
            throw new IllegalArgumentException(String.format(msg, o.getClass()));
        }
        return NodeFactory.createLiteral(LiteralLabelFactory.createTypedLiteral(o));

    }

    /**
     * Creates a literal from the value and type URI. There are several possible
     * outcomes:
     * <ul>
     * <li>If the URI is registered with TypeMapper and the value is the proper
     * lexical form for the type, the registered TypeMapper is used and calling
     * {@code getLiteralValue()} on the returned node will return a proper object.
     * </li>
     * <li>If the URI is unregistered a Datatype is created but not registered with
     * the TypeMapper. The resulting node is properly constructed for used in output
     * serialization, queries, or updates. Calling {@code getLiteralValue()} on the
     * returned node will throw DatatypeFormatException. Note that if
     * {@code JenaParameters.enableEagerLiteralValidation} is true the
     * DatatypeFormatException will be thrown by this method.</li>
     * <li>If the URI is registered but the value is not a proper lexical form a
     * DatatypeFormatException will be thrown by this method.</li>
     * </ul>
     * 
     * @param value   the value for the literal
     * @param typeUri the type URI for the literal node.
     * @return the literal node.
     * @throws DatatypeFormatException on errors noted above
     */
    public static Node makeLiteral(String value, String typeUri) {
        Object oValue = value;
        RDFDatatype dt = TypeMapper.getInstance().getTypeByName(typeUri);
        if (dt == null) {
            dt = new BaseDatatype(typeUri) {

                @Override
                public boolean isValidValue(Object valueForm) {
                    return false;
                }

                @Override
                public Object parse(String lexicalForm) throws DatatypeFormatException {
                    RDFDatatype dt = TypeMapper.getInstance().getTypeByName(uri);
                    if (dt == null) {
                        throw new DatatypeFormatException("no registered Datatype for " + uri);
                    }
                    return dt.parse(lexicalForm);
                }

            };
        } else {
            oValue = dt.parse(value);
        }
        LiteralLabel ll = LiteralLabelFactory.createByValue(oValue, null, dt);
        return NodeFactory.createLiteral(ll);
    }

    /**
     * Makes a node from an object while using the associated prefix mapping.
     * <ul>
     * <li>Will return Node.ANY if object is null.</li>
     * <li>Will return the enclosed Node from a FrontsNode</li>
     * <li>Will return the object if it is a Node.</li>
     * <li>Will call NodeFactoryExtra.parseNode() using the currently defined
     * prefixes if the object is a String</li>
     * <li>Will call makeLiteral() to create a literal representation if the
     * parseNode() fails or for any other object type.</li>
     * </ul>
     * 
     * @param o        The object to convert (may be null).
     * @param pMapping The prefix mapping to use for prefix resolution.
     * @return The Node value.
     * @see #makeLiteral(Object)
     */
    public static Node makeNode(Object o, PrefixMapping pMapping) {
        if (o == null) {
            return Node.ANY;
        }
        if (o instanceof FrontsNode) {
            return checkVar(((FrontsNode) o).asNode());
        }

        if (o instanceof Node) {
            return checkVar((Node) o);
        }
        if (o instanceof String) {
            try {
                return checkVar(NodeFactoryExtra.parseNode((String) o, PrefixMapFactory.create(pMapping)));
            } catch (final RiotException e) {
                // expected in some cases -- do nothing
            }

        }
        return makeLiteral(o);
    }

    /**
     * Creates a Path or Node as appropriate.
     * <ul>
     * <li>Will return Node.ANY if object is null.</li>
     * <li>Will return the object if it is a Path
     * <li>Will return the enclosed Node from a FrontsNode</li>
     * <li>Will return the object if it is a Node.</li>
     * <li>Will call PathParser.parse() using the prefix mapping if the object is a
     * String</li>
     * <li>Will call NodeFactoryExtra.parseNode() using the currently defined
     * prefixes if the object is a String and the PathParser.parse() fails.</li>
     * <li>Will call makeLiteral() to create a literal representation if the
     * parseNode() fails or for any other object type.</li>
     * </ul>
     * 
     * @param o        the object that should be interpreted as a path or a node.
     * @param pMapping the prefix mapping to resolve path or node with
     * @return the Path or Node
     * @see #makeLiteral(Object)
     */
    public static Object makeNodeOrPath(Object o, PrefixMapping pMapping) {
        if (o == null) {
            return Node.ANY;
        }
        if (o instanceof Path) {
            return o;
        }
        if (o instanceof FrontsNode) {
            return checkVar(((FrontsNode) o).asNode());
        }

        if (o instanceof Node) {
            return checkVar((Node) o);
        }
        if (o instanceof String) {
            try {
                final Path p = PathParser.parse((String) o, pMapping);
                if (p instanceof P_Link) {
                    return ((P_Link) p).getNode();
                }
                return p;
            }

            catch (final Exception e) {
                // expected in some cases -- do nothing
            }

        }
        return makeNode(o, pMapping);
    }

    /**
     * Makes a Var from an object.
     * <ul>
     * <li>Will return Var.ANON if object is null.</li>
     * <li>Will return null if the object is "*" or Node_RuleVariable.WILD</li>
     * <li>Will return the object if it is a Var</li>
     * <li>Will return resolve FrontsNode to Node and then resolve to Var</li>
     * <li>Will return resolve Node if the Node implements Node_Variable, otherwise
     * throws an NotAVariableException (instance of ARQInternalErrorException)</li>
     * <li>Will return ?x if object is "?x"</li>
     * <li>Will return ?x if object is "x"</li>
     * <li>Will return the enclosed Var of a ExprVar</li>
     * <li>For all other objects will return the "?" prefixed to the toString()
     * value.</li>
     * </ul>
     *
     * @param o The object to convert.
     * @return the Var value.
     * @throws ARQInternalErrorException
     */
    public static Var makeVar(Object o) throws ARQInternalErrorException {
        if (o == null) {
            return Var.ANON;
        }
        if (o instanceof Var) {
            return (Var) o;
        }
        Var retval = null;
        if (o instanceof FrontsNode) {
            retval = Var.alloc(((FrontsNode) o).asNode());
        } else if (o instanceof Node) {
            retval = Var.alloc((Node) o);
        } else if (o instanceof ExprVar) {
            retval = Var.alloc((ExprVar) o);
        } else {
            retval = Var.alloc(Var.canonical(o.toString()));
        }
        if ("*".equals(Var.canonical(retval.toString()))) {
            return null;
        }
        return retval;
    }

    /**
     * A convenience method to quote a string.
     * 
     * @param q the string to quote.
     *
     *          Will use single quotes if there are no single quotes in the string
     *          or if the double quote is before the single quote in the string.
     *
     *          Will use double quote otherwise.
     *
     * @return the quoted string.
     */
    public static String quoted(String q) {
        int qt = q.indexOf('"');
        int sqt = q.indexOf("'");
        if (qt == -1) {
            qt = Integer.MAX_VALUE;
        }
        if (sqt == -1) {
            sqt = Integer.MAX_VALUE;
        }

        if (qt <= sqt) {
            return String.format("'%s'", q);
        }
        return String.format("\"%s\"", q);
    }

    /**
     * Creates a collection of nodes from an iterator of Objects.
     * 
     * @param iter          the iterator of objects, may be null or empty.
     * @param prefixMapping the PrefixMapping to use when nodes are created.
     * @return a Collection of nodes or null if iter is null or empty.
     */
    public static Collection<Node> makeValueNodes(Iterator<?> iter, PrefixMapping prefixMapping) {
        if (iter == null || !iter.hasNext()) {
            return null;
        }
        final List<Node> values = new ArrayList<Node>();
        while (iter.hasNext()) {
            final Object o = iter.next();
            // handle null as UNDEF
            if (o == null) {
                values.add(null);
            } else {
                values.add(makeNode(o, prefixMapping));
            }
        }
        return values;
    }

}
