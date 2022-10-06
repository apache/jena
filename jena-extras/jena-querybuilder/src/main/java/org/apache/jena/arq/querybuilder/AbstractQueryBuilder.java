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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.clauses.PrologClause;
import org.apache.jena.arq.querybuilder.clauses.ValuesClause;
import org.apache.jena.arq.querybuilder.handlers.HandlerBlock;
import org.apache.jena.arq.querybuilder.handlers.PrologHandler;
import org.apache.jena.arq.querybuilder.handlers.ValuesHandler;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.util.ExprUtils;

/**
 * Base class for all QueryBuilders.
 *
 * @param <T> The derived class type. Used for return types.
 */
public abstract class AbstractQueryBuilder<T extends AbstractQueryBuilder<T>>
        implements Cloneable, PrologClause<T>, ValuesClause<T> {

    /**
     * the query this builder is building
     */
    protected Query query;
    /**
     * a map of vars to nodes for replacement during build.
     */
    private final Map<Var, Node> values;

    /**
     * Make a Node from an object.
     * <ul>
     * <li>Will return Node.ANY if object is null.</li>
     * <li>Will return the enclosed Node from a FrontsNode</li>
     * <li>Will return the object if it is a Node.</li>
     * <li>Will call NodeFactoryExtra.parseNode() using the prefix mapping if the
     * object is a String</li>
     * <li>Will create a literal representation if the parseNode() fails or for any
     * other object type.</li>
     * </ul>
     *
     * Uses the internal query prefix mapping to resolve prefixes.
     *
     * @param o The object to convert. (may be null)
     * @return The Node value.
     */
    public Node makeNode(Object o) {
        return Converters.makeNode(o, query.getPrefixMapping());
    }

    /**
     * Make a node or path from the object using the query prefix mapping.
     * 
     * @param o the object to make the node or path from.
     * @return A node or path.
     * @see Converters#makeNodeOrPath(Object, PrefixMapping)
     */
    private Object makeNodeOrPath(Object o) {
        return Converters.makeNodeOrPath(o, query.getPrefixMapping());
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
     * <li>Will create a literal representation if the parseNode() fails or for any
     * other object type.</li>
     * </ul>
     * 
     * @param o the object that should be interpreted as a path or a node.
     * @param pMapping the prefix mapping to resolve path or node with
     * @return the Path or Node
     * @deprecated use {@link Converters#makeNodeOrPath(Object, PrefixMapping)}
     */
    @Deprecated
    public static Object makeNodeOrPath(Object o, PrefixMapping pMapping) {
        return Converters.makeNodeOrPath(o, pMapping);
    }

    public ElementSubQuery asSubQuery() {
        return getWhereHandler().makeSubQuery(this);
    }

    /**
     * Make a triple path from the objects.
     *
     * For subject, predicate and objects nodes
     * <ul>
     * <li>Will return Node.ANY if object is null.</li>
     * <li>Will return the enclosed Node from a FrontsNode</li>
     * <li>Will return the object if it is a Node.</li>
     * <li>If the object is a String
     * <ul>
     * <li>For <code>predicate</code> only will attempt to parse as a path</li>
     * <li>for subject, predicate and object will call NodeFactoryExtra.parseNode()
     * using the currently defined prefixes if the object is a String</li>
     * </ul>
     * </li>
     * <li>Will create a literal representation if the parseNode() fails or for any
     * other object type.</li>
     * </ul>
     *
     * @param s The subject object
     * @param p the predicate object
     * @param o the object object.
     * @return a TriplePath
     */
    public TriplePath makeTriplePath(Object s, Object p, Object o) {
        final Object po = makeNodeOrPath(p);
        if (po instanceof Path) {
            return new TriplePath(makeNode(s), (Path) po, makeNode(o));
        }
        return new TriplePath(Triple.create(makeNode(s), (Node) po, makeNode(o)));
    }

    /**
     * A convenience method to make an expression from a string. Evaluates the
     * expression with respect to the current query.
     *
     * @param expression The expression to parse.
     * @return the Expr object.
     * @throws QueryParseException on error.
     */
    public Expr makeExpr(String expression) throws QueryParseException {
        return ExprUtils.parse(query, expression, true);
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
     * @deprecated {@link Converters#quoted(String)}
     */
    @Deprecated
    public static String quote(String q) {
        return Converters.quoted(q);
    }

    /**
     * Verify that any Node_Variable nodes are returned as Var nodes.
     * 
     * @param n the node to check
     * @return the node n or a new Var if n is an instance of Node_Variable
     * @deprecated use {@link Converters#checkVar(Node)}
     */
    @Deprecated
    public static Node checkVar(Node n) {
        return Converters.checkVar(n);
    }

    /**
     * Make a node from an object while using the associated prefix mapping.
     * <ul>
     * <li>Will return Node.ANY if object is null.</li>
     * <li>Will return the enclosed Node from a FrontsNode</li>
     * <li>Will return the object if it is a Node.</li>
     * <li>Will call NodeFactoryExtra.parseNode() using the currently defined
     * prefixes if the object is a String</li>
     * <li>Will create a literal representation if the parseNode() fails or for any
     * other object type.</li>
     * </ul>
     * 
     * @param o The object to convert (may be null).
     * @param pMapping The prefix mapping to use for prefix resolution.
     * @return The Node value.
     * @deprecated use {@link Converters#makeNode(Object, PrefixMapping)}
     */
    @Deprecated
    public static Node makeNode(Object o, PrefixMapping pMapping) {
        return Converters.makeNode(o, pMapping);
    }

    /**
     * Make a Var from an object.
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
     * @deprecated use {@link Converters#makeVar(Object)}
     */
    @Deprecated
    public static Var makeVar(Object o) throws ARQInternalErrorException {
        return Converters.makeVar(o);
    }

    /**
     * Create a new query builder.
     */
    protected AbstractQueryBuilder() {
        query = new Query();
        values = new HashMap<Var, Node>();
    }

    /**
     * Get the HandlerBlock for this query builder.
     * 
     * @return The associated handler block.
     */
    public abstract HandlerBlock getHandlerBlock();

    @Override
    public final PrologHandler getPrologHandler() {
        return getHandlerBlock().getPrologHandler();
    }

    @Override
    public ValuesHandler getValuesHandler() {
        return getHandlerBlock().getValueHandler();
    }

    /**
     * Gets the where handler used by this QueryBuilder.
     * 
     * @return the where handler used by this QueryBuilder.
     */
    public final WhereHandler getWhereHandler() {
        return getHandlerBlock().getWhereHandler();
    }

    /**
     * Adds the contents of the whereClause to the where clause of this builder.
     * 
     * @param whereClause the where clause to add.
     * @return this builder for chaining.
     */
    @SuppressWarnings("unchecked")
    public final T addWhere(AbstractQueryBuilder<?> whereClause) {
        getWhereHandler().addAll(whereClause.getWhereHandler());
        return (T) this;
    }

    @Override
    public final ExprFactory getExprFactory() {
        return getHandlerBlock().getPrologHandler().getExprFactory();
    }

    /**
     * Set a variable replacement. During build all instances of var in the query
     * will be replaced with value. If value is null the replacement is cleared.
     *
     * @param var The variable to replace
     * @param value The value to replace it with or null to remove the replacement.
     */
    public void setVar(Var var, Node value) {
        if (value == null) {
            values.remove(var);
        } else {
            values.put(var, value);
        }
    }

    /**
     * Set a variable replacement. During build all instances of var in the query
     * will be replaced with value. If value is null the replacement is cleared.
     *
     * See {@link #makeVar} for conversion of the var param. See {@link #makeNode}
     * for conversion of the value param.
     *
     * @param var The variable to replace.
     * @param value The value to replace it with or null to remove the replacement.
     */
    public void setVar(Object var, Object value) {
        if (value == null) {
            setVar(Converters.makeVar(var), null);
        } else {
            setVar(Converters.makeVar(var), makeNode(value));
        }
    }

    @Override
    public T addPrefix(String pfx, Resource uri) {
        return addPrefix(pfx, uri.getURI());
    }

    @Override
    public T addPrefix(String pfx, Node uri) {
        return addPrefix(pfx, uri.getURI());
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addPrefix(String pfx, String uri) {
        getPrologHandler().addPrefix(pfx, uri);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addPrefixes(Map<String, String> prefixes) {
        getPrologHandler().addPrefixes(prefixes);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addPrefixes(PrefixMapping prefixMapping) {
        getPrologHandler().addPrefixes(prefixMapping);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T setBase(String base) {
        getPrologHandler().setBase(base);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T setBase(Object base) {
        setBase(makeNode(base).getURI());
        return (T) this;
    }

    // --- VALUES

    /**
     * Creates a collection of nodes from an iterator of Objects.
     * 
     * @param iter the iterator of objects, may be null or empty.
     * @param prefixMapping the PrefixMapping to use when nodes are created.
     * @return a Collection of nodes or null if iter is null or empty.
     * @deprecated use {@link Converters#makeValueNodes(Iterator, PrefixMapping)}
     */
    @Deprecated
    public static Collection<Node> makeValueNodes(Iterator<?> iter, PrefixMapping prefixMapping) {
        return Converters.makeValueNodes(iter, prefixMapping);
    }

    /**
     * Creates a collection of nodes from an iterator of Objects. Uses the prefix
     * mapping from the PrologHandler.
     * 
     * @param iter the iterator of objects, may be null or empty.
     * @return a Collection of nodes or null if iter is null or empty.
     */
    public Collection<Node> makeValueNodes(Iterator<?> iter) {
        return Converters.makeValueNodes(iter, getPrologHandler().getPrefixes());
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addValueVar(Object var) {
        if (var == null) {
            throw new IllegalArgumentException("var must not be null.");
        }
        if (var instanceof Collection<?>) {
            final Collection<?> column = (Collection<?>) var;
            if (column.size() == 0) {
                throw new IllegalArgumentException("column must have at least one entry.");
            }
            final Iterator<?> iter = column.iterator();
            final Var v = Converters.makeVar(iter.next());
            getValuesHandler().addValueVar(v, makeValueNodes(iter));
        } else {
            getValuesHandler().addValueVar(Converters.makeVar(var), null);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addValueVar(Object var, Object... objects) {

        Collection<Node> values = null;
        if (objects != null) {
            values = makeValueNodes(Arrays.asList(objects).iterator());
        }

        getValuesHandler().addValueVar(Converters.makeVar(var), values);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K extends Collection<?>> T addValueVars(Map<?, K> dataTable) {
        final ValuesHandler hdlr = new ValuesHandler(null);
        for (final Map.Entry<?, K> entry : dataTable.entrySet()) {
            Collection<Node> values = null;
            if (entry.getValue() != null) {
                values = makeValueNodes(entry.getValue().iterator());
            }
            hdlr.addValueVar(Converters.makeVar(entry.getKey()), values);
        }
        getValuesHandler().addAll(hdlr);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addValueRow(Object... values) {
        getValuesHandler().addValueRow(makeValueNodes(Arrays.asList(values).iterator()));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T addValueRow(Collection<?> values) {
        getValuesHandler().addValueRow(makeValueNodes(values.iterator()));
        return (T) this;
    }

    @Override
    public List<Var> getValuesVars() {
        return getValuesHandler().getValuesVars();
    }

    @Override
    public Map<Var, List<Node>> getValuesMap() {
        return getValuesHandler().getValuesMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T clearValues() {
        getValuesHandler().clear();
        return (T) this;
    }

    @Override
    public String toString() {
        return buildString();
    }

    /**
     * Build the query as a string.
     *
     * @return the string representation of the query.
     */
    public final String buildString() {
        return build().toString();
    }

    /**
     * Build the query. Performs the var replacements as specified by
     * setVar(var,node) calls.
     *
     * @return The query.
     */
    public final Query build() {
        final Query q = new Query();

        // set the query type
        switch (query.queryType()) {
        case ASK:
            q.setQueryAskType();
            break;
        case CONSTRUCT:
            q.setQueryConstructType();
            break;
        case DESCRIBE:
            q.setQueryDescribeType();
            break;
        case SELECT:
            q.setQuerySelectType();
            break;
        case UNKNOWN:
            // do nothing
            break;
        default:
            throw new IllegalStateException("Internal query is not a known type: " + q.queryType());
        }

        // use the HandlerBlock implementation to copy the data.
        final HandlerBlock handlerBlock = new HandlerBlock(q);
        handlerBlock.addAll(getHandlerBlock());

        // set the vars
        handlerBlock.setVars(values);

        // make sure we have a query pattern before we start building.
        if (q.getQueryPattern() == null) {
            q.setQueryPattern(new ElementGroup());
        }

        handlerBlock.build();

        q.resetResultVars();
        return q;
    }

    /**
     * Close the query.
     *
     * This can be used when the query would not normally parse as is required by
     * the Query.clone() method.
     *
     * @param q2 The query to clone
     * @return A clone of the q2 param.
     */
    public static Query clone(Query q2) {
        final Query retval = new Query();

        // set the query type
        if (q2.isSelectType()) {
            retval.setQuerySelectType();
        } else if (q2.isAskType()) {
            retval.setQueryAskType();
        } else if (q2.isDescribeType()) {
            retval.setQueryDescribeType();
        } else if (q2.isConstructType()) {
            retval.setQueryConstructType();
        }

        // use the handler block to clone the data
        final HandlerBlock hb = new HandlerBlock(retval);
        final HandlerBlock hb2 = new HandlerBlock(q2);
        hb.addAll(hb2);
        q2.resetResultVars();
        return retval;
    }

    /**
     * Rewrite a query replacing variables as specified in the values map.
     *
     * @param q2 The query to rewrite
     * @param values a Mapping of var to node for replacement.
     * @return The new query with the specified vars replaced.
     */
    public static Query rewrite(Query q2, Map<Var, Node> values) {
        final HandlerBlock hb = new HandlerBlock(q2);
        hb.setVars(values);
        return q2;
    }
}
