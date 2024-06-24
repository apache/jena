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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.arq.querybuilder.rewriters.AbstractRewriter;
import org.apache.jena.arq.querybuilder.rewriters.PathRewriter;
import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.FrontsNode;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.lang.arq.ARQParser;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sparql.syntax.TripleCollectorMark;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.vocabulary.RDF;

/**
 * A collection of static methods to convert from Objects to various types used
 * in Query and Update construction.
 */
public class Converters {

    private Converters() {
        // do not make instance
    }

    /**
     * @param o the object to test.
     * @return true if the object is a Java collection or a string starting with '(' and ending with ')'
     */
    private static boolean isCollection(Object o) {
        Predicate<String> isLiteralCollection = (s) -> s.charAt(0) == '(' && s.charAt(s.length()-1) == ')';
        return o != null && (o instanceof Collection ||
               (o instanceof String && isLiteralCollection.test(((String) o).trim())));
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
        return NodeFactory.createLiteralByValue(o, dt);
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
     * @param value the value for the literal
     * @param typeUri the type URI for the literal node.
     * @return the literal node.
     * @throws DatatypeFormatException on errors noted above
     */
    public static Node makeLiteral(String value, String typeUri) {
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
            dt.parse(value);
        }
        return NodeFactory.createLiteralDT(value, dt);
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
     * @param o The object to convert (may be null).
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
     * @param o the object that should be interpreted as a path or a node.
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
     * Will use single quotes if there are no single quotes in the string or if the
     * double quote is before the single quote in the string.
     *
     * Will use double quote otherwise.
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
     * @param iter the iterator of objects, may be null or empty.
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

    /**
     * Gathers the triples and adds them to the collector.
     * @param collector the collector for the triples.
     * @param s Subject object may be expanded to an RDF collection.
     * @param p Subject object may be expanded to an RDF collection.
     * @param o Subject object may be expanded to an RDF collection.
     * @param prefixMapping the prefix mapping to use for URI resolution.
     */
    private static void gatherTriples(ReadableTripleCollectorMark collector, Object s, Object p, Object o,
            PrefixMapping prefixMapping) {
        Node sNode = null;
        Object pNode = null;
        Node oNode = null;

        Function<Object, Node> processNode = (n) -> {
            if (isCollection(n)) {
                int mark = collector.mark();
                gatherTriples(collector, n, prefixMapping);
                return collector.getSubject(mark);
            }
            return makeNode(n, prefixMapping);
        };

        sNode = processNode.apply(s);

        if (isCollection(p)) {
            int mark = collector.mark();
            gatherTriples(collector, p, prefixMapping);
            pNode = collector.getSubject(mark);
        } else {
            pNode = makeNodeOrPath(p, prefixMapping);
        }

        oNode = processNode.apply(o);

        if (pNode instanceof Path) {
            collector.addTriplePath(new TriplePath(sNode, (Path) pNode, oNode));
        } else {
            collector.addTriple(Triple.create(sNode, (Node) pNode, oNode));
        }

    }

    /**
     * Creates a collection of {@code TriplePath}s from the {@code s, p, o}. If
     * {@code s}, {@code p}, or {@code o} is a collection or a String representation of a
     * collection like {@code "(a, b, c)"} the
     * {@code makeCollectionTriplePaths()} conversions are applied. If {@code s} and/or
     * {@code o} is not a collection the {@code makeNode()} conversions are applied.
     * if {@code p} is not a collection the @{code makeNodeOrPath()} conversion is
     * applied.
     * <p><em>Note: Path objects are not supported in RDF collections.  A custom Datatype would need
     * to be registered to place them in collections</em></p>
     *
     * @param s the object for the subject.
     * @param p the object for the predicate.
     * @param o the object for the object.
     * @param prefixMapping the PrefixMapping to resolve nodes.
     * @return A list of {@code TriplePath} objects.
     * @see #makeNodeOrPath(Object, PrefixMapping)
     * @see #makeCollectionTriplePaths(Object, PrefixMapping)
     * @see #makeNode(Object, PrefixMapping)
     */
    public static List<TriplePath> makeTriplePaths(Object s, Object p, Object o, PrefixMapping prefixMapping) {
        ConvertersTriplePathCollector result = new ConvertersTriplePathCollector();
        gatherTriples(result, s, p, o, prefixMapping);
        return result.result;
    }

    /**
     * Creates a collection of {@code Triple}s from the {@code s, p, o}. If
     * {@code s}, {@code p}, or {@code o} is a collection or a String representation of a
     * collection like {@code "(a, b, c)"} the
     * {@code makeCollectionTriples()} conversions are applied. If {@code s}, {@code p}, or
     * {@code o} is not a collection the {@code makeNode()} conversions are applied.
     * This differs from
     * {@link #makeTriplePaths(Object, Object, Object, PrefixMapping)} in that the
     * {@code p} may not be a path.
     *
     * <p><em>Note: Path objects are not supported in RDF collections.  A custom Datatype would need
     * to be registered to place them in collections</em></p>
     *
     * @param s the object for the subject.
     * @param p the object for the predicate.
     * @param o the object for the object.
     * @param prefixMapping the PrefixMapping to resolve nodes.
     * @return A list of {@code Triple} objects.
     * @see #makeNodeOrPath(Object, PrefixMapping)
     * @see #makeCollectionTriples(Object, PrefixMapping)
     * @see #makeNode(Object, PrefixMapping)
     */
    public static List<Triple> makeTriples(Object s, Object p, Object o, PrefixMapping prefixMapping) {
        ConvertersTripleCollector result = new ConvertersTripleCollector();
        gatherTriples(result, s, p, o, prefixMapping);
        return result.result;
    }

    /**
     * Creates an RDF collection from a collection object. The collection object may be either
     * a Java collection or an ARQParser collection literal like {@code "(a, b, c)"}.
     * @param collector the TripleCollector to add the triples to.
     * @param collection the collection of objects or string representation of collection to convert.
     * @param prefixMapping the prefix mapping to use.
     */
    @SuppressWarnings("unchecked")
    private static void gatherTriples(ReadableTripleCollectorMark collector, Object collection,
            PrefixMapping prefixMapping) {
        if (collection instanceof Collection) {
            Node previous = null;
            for (Object obj : (Collection<Object>) collection) {
                Node current = NodeFactory.createBlankNode();
                if (previous != null) {
                    collector.addTriple(Triple.create(previous, RDF.rest.asNode(), current));
                }
                if (isCollection(obj)) {
                    int mark = collector.mark();
                    gatherTriples(collector, obj, prefixMapping);
                    collector.addTriple(Triple.create(current, RDF.first.asNode(), collector.getSubject(mark)));
                } else {
                    collector.addTriple(Triple.create(current, RDF.first.asNode(), makeNode(obj, prefixMapping)));
                }
                previous = current;
            }
            collector.addTriple(Triple.create(previous, RDF.rest.asNode(), RDF.nil.asNode()));
        } else {
            String parserInput = collection.toString().trim();
            ARQParser parser = new ARQParser(new StringReader(parserInput));
            int mark = collector.mark();
            try {
                parser.CollectionPath(collector);
            } catch (ParseException e) {
                throw new IllegalArgumentException(String.format("Unable to parse: %s", collection), e);
            }
            collector.rewriteFrom(mark);
        }
    }

    /**
     * Create an RDF collection from a collection of objects as per
     * <a href='http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#collections'>
     * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#collections</a>
     * <p>
     * Embedded collections are recursively expanded and added to the resulting
     * list. Other object types in the collections are converted using
     * {@code makeNode} PrefixMapping)}.
     * <p>
     * <p><em>Note: Path objects are not supported in RDF collections.  A custom Datatype would need
     * to be registered to place them in collections</em></p>
     * Usage:
     * <ul>
     * <li>In most cases direct calls to makeCollection are unnecessary as passing
     * the collection to methods like {@code addWhere(Object, Object, Object)} will
     * correctly create and add the list.</li>
     * <li>In cases where makeCollectionTriples is called the Subject of the first
     * {@code Triple} is the RDF Collection node.</li>
     * </ul>
     * </p>
     *
     * @param collection the collections of objects for the list.
     * @return A list of {@code Triple} objects.
     * @see #makeNode(Object, PrefixMapping)
     */
    public static List<Triple> makeCollectionTriples(Object collection, PrefixMapping prefixMapping) {
        ConvertersTripleCollector collector = new ConvertersTripleCollector();
        gatherTriples(collector, collection, prefixMapping);
        return collector.result;
    }

    /**
     * Create an RDF collection from a collection of objects as per
     * <a href='http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#collections'>
     * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#collections</a>.
     * <p>
     * Embedded collections are recursively expanded and added to the resulting
     * list. Other object types in the collections are converted using
     * {@code makeNode} PrefixMapping)}.
     * <p>
     * <p><em>Note: Path objects are not supported in RDF collections.  A custom Datatype would need
     * to be registered to place them in collections</em></p>
     * Usage:
     * <ul>
     * <li>In most cases direct calls to makeCollectionTriplePaths are unnecessary
     * as passing the collection to methods like
     * {@code addWhere(Object, Object, Object)} will correctly create and add the
     * list.</li>
     * <li>In cases where makeCollectionTriplePath is called the Subject of the
     * first {@code TriplePath} is the RDF Collection node.</li>
     * </ul>
     * </p>
     *
     * @param n the collections of objects for the list.
     * @return A list of {@code TriplePath} objects.
     * @see #makeNode(Object, PrefixMapping)
     */
    public static List<TriplePath> makeCollectionTriplePaths(Object n, PrefixMapping prefixMapping) {
        ConvertersTriplePathCollector collector = new ConvertersTriplePathCollector();
        gatherTriples(collector, n, prefixMapping);
        return collector.result;
    }

    /**
     * defines methods needed by converters to convert collections into into RDF collections.
     */
    interface ReadableTripleCollectorMark extends TripleCollectorMark {
        /**
         * Get the subject from the entry in the collection at the {@code mark} location.
         * @param mark the location to retrieve from.
         * @return the Subject node
         */
        Node getSubject(int mark);

        /**
         * Rewrite the variable nodes in the triples from {@code mark} to the end.
         * This is required because the parser that converts from literal {@code "(a, b, c)"}
         * to the RDF list will number the variables from 0.
         * @param mark the position to start the rewrite from.
         */
        void rewriteFrom(int mark);
    }

    /**
     * Collects triples only.
     */
    private static class ConvertersTripleCollector implements ReadableTripleCollectorMark {
        List<Triple> result = new ArrayList<>();

        @Override
        public void addTriple(Triple t) {
            result.add(t);
        }

        @Override
        public void addTriplePath(TriplePath tPath) {
            throw new IllegalArgumentException("Path is not allowed in a Triple");
        }

        @Override
        public int mark() {
            return result.size();
        }

        @Override
        public void addTriple(int index, Triple t) {
            result.add(index, t);
        }

        @Override
        public void addTriplePath(int index, TriplePath tPath) {
            throw new IllegalArgumentException("Path is not allowed in a Triple");
        }

        @Override
        public Node getSubject(int mark) {
            return result.get(mark).getSubject();
        }

        @Override
        public void rewriteFrom(int mark) {
            Map<Var, Node> values = new HashMap<>();
            TripleRewriter rewriter = new TripleRewriter(values);
            for (int i = mark; i < mark(); i++) {
                result.set(i, rewriter.rewrite(result.get(i)));
            }
        }
    }

    /**
     * Collects triple paths.
     */
    private static class ConvertersTriplePathCollector implements ReadableTripleCollectorMark {
        List<TriplePath> result = new ArrayList<>();

        @Override
        public void addTriple(Triple t) {
            result.add(new TriplePath(t));
        }

        @Override
        public void addTriplePath(TriplePath tPath) {
            result.add(tPath);
        }

        @Override
        public int mark() {
            return result.size();
        }

        @Override
        public void addTriple(int index, Triple t) {
            result.add(index, new TriplePath(t));
        }

        @Override
        public void addTriplePath(int index, TriplePath tPath) {
            result.add(index, tPath);
        };

        @Override
        public Node getSubject(int mark) {
            return result.get(mark).getSubject();
        }

        @Override
        public void rewriteFrom(int mark) {
            Map<Var, Node> values = new HashMap<>();
            TripleRewriter rewriter = new TripleRewriter(values);
            for (int i = mark; i < mark(); i++) {
                result.set(i, rewriter.rewrite(result.get(i)));
            }
        }
    }

    /**
     * Rewriter implementation to convert the numbered variables to blank nodes.
     */
    private static class TripleRewriter extends AbstractRewriter<Node> {
        private PathRewriter pathRewriter;

        protected TripleRewriter(Map<Var, Node> values) {
            super(values);
            pathRewriter = new PathRewriter(values) {
                @Override
                protected Node changeNode(Node n) {
                    return TripleRewriter.this.changeNode(n);
                }
            };
        }

        @Override
        protected Node changeNode(Node n) {
            if (n == null) {
                return n;
            }
            if (n.isVariable() && n.toString().startsWith("??")) {
                Var key = Var.alloc(n);
                Node result = values.get(key);
                if (result == null) {
                    result = NodeFactory.createBlankNode();
                    values.put(key, result);
                }
                return result;
            }
            return n;
        }

        /**
         * Rewrite a triple path.
         *
         * @param t The triple path to rewrite.
         * @return the triple path after rewriting.
         */
        @Override
        public TriplePath rewrite(TriplePath t) {
            if (t.getPath() == null) {
                return new TriplePath(Triple.create(changeNode(t.getSubject()), changeNode(t.getPredicate()),
                        changeNode(t.getObject())));
            }
            t.getPath().visit(pathRewriter);
            return new TriplePath(changeNode(t.getSubject()), pathRewriter.getResult(), changeNode(t.getObject()));
        }
    }
}
