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
package org.apache.jena.arq.querybuilder.clauses;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.Converters;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

/**
 * Interface that defines the WhereClause as per
 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rWhereClause
 * 
 * @param <T> The Builder type that the clause is part of.
 */
public interface WhereClause<T extends AbstractQueryBuilder<T>> {

    /**
     * Adds a triple to the where clause.
     * 
     * @param t The triple path to add
     * @return This Builder for chaining.
     */
    public T addWhere(Triple t);

    /**
     * Adds a triple path to the where clause.
     * 
     * @param t The triple path to add
     * @return This Builder for chaining.
     */
    public T addWhere(TriplePath t);

    /**
     * Adds a triple to the where clause.
     * 
     * @param t The triple to add
     * @return This Builder for chaining.
     */
    public T addWhere(FrontsTriple t);

    /**
     * Adds a triple or triple path to the where clause.
     * 
     * See {@link AbstractQueryBuilder#makeTriplePath} for conversion of the param
     * values.
     * 
     * @param s The subject.
     * @param p The predicate.
     * @param o The object.
     * @return This Builder for chaining.
     */
    public T addWhere(Object s, Object p, Object o);

    /**
     * Adds the elements from the whereClause to this where Clause.
     * 
     * @param whereClause The whereClause to add to this statement.
     * @return This Builder for chaining.
     */
    public T addWhere(AbstractQueryBuilder<?> whereClause);

    /**
     * Add a variable or variable and values to the value statement.
     * 
     * The first var (or first item in a collection) is converted to a variable
     * using the makeVar strategy. A variable may be added multiple times, doing so
     * will append values to the list of variable values. The order in which
     * variables are added to the values table is preserved.
     * 
     * Adding a collection as the var will use the first object in the collection as
     * the var and the remaining objects as values.
     * 
     * Values are created using makeNode() strategy except that null values are
     * converted to UNDEF.
     * 
     * @param var The variable or collection to add.
     * @return The builder for chaining.
     * @see AbstractQueryBuilder#makeNode(Object)
     * @see Converters#makeVar(Object)
     */
    public T addWhereValueVar(Object var);

    /**
     * Add a variable and values to the value statement.
     * 
     * The var is converted to a variable using the makeVar strategy. A variable may
     * be added multiple times, doing so will append values to the list of variable
     * values. The order in which variables are added to the values table is
     * preserved.
     * 
     * Values are created using makeNode() strategy except that null values are
     * converted to UNDEF.
     * 
     * @param var    The variable to add.
     * @param values The values for the variable
     * @return The builder for chaining.
     * @see AbstractQueryBuilder#makeNode(Object)
     * @see Converters#makeVar(Object)
     */
    public T addWhereValueVar(Object var, Object... values);

    /**
     * Add a data table to the value statement.
     * 
     * Each key in the map is used converted into a variable using the makeVar
     * strategy. The order in which variables are added to the values table is
     * preserved.
     * 
     * Variables are added in the iteration order for the map. It may be advisable
     * to use a LinkedHashMap to preserver the insert order.
     * 
     * @see java.util.LinkedHashMap
     * 
     *      Each item in the value collection is converted into a node using
     *      makeNode() strategy except that null values are converted to UNDEF.
     * 
     *      If there are already values in the value statement the data table is
     *      adds as follows:
     *      <ul>
     *      <li>If the variable already exists in the table the map values are
     *      appended to the list of values</li>
     *      <li>If the variable does not exist in the table and there are other
     *      variables defined, an appropriate number of nulls is added to the front
     *      of the map values to create UNDEF entries for the existing rows</li>
     *      <li>If there are variables in the value statement that are not specified
     *      in the map additional UNDEF entries are appended to them to account for
     *      new rows that are added.</li>
     *      </ul>
     * 
     * @param dataTable The data table to add.
     * @return The builder for chaining.
     * @see AbstractQueryBuilder#makeNode(Object)
     * @see Converters#makeVar(Object)
     */
    public <K extends Collection<?>> T addWhereValueVars(Map<?, K> dataTable);

    /**
     * Add objects as a row of values. This method is different from the other
     * methods in that the values are appended to each of the variables in the
     * clause. There must be sufficient entries in the list to provide data for each
     * variable in the table. Values objects are converted to nodes using the
     * makeNode strategy. Variables will always be in the order added to the values
     * table.
     * 
     * @param values the collection of values to add.
     * @return The builder for chaining.
     * @see AbstractQueryBuilder#makeNode(Object)
     */
    public T addWhereValueRow(Object... values);

    /**
     * Add a collection of objects as row of values. This method is different from
     * the other methods in that the values are appended to each of the variables in
     * the clause. There must be sufficient entries in the list to provide data for
     * each variable in the table. Values objects are converted to nodes using the
     * makeNode strategy. Variables will always be in the order added to the values
     * table.
     * 
     * @param values the collection of values to add.
     * @return The builder for chaining.
     * @see AbstractQueryBuilder#makeNode(Object)
     */
    public T addWhereValueRow(Collection<?> values);

    /**
     * Get an unmodifiable list of vars from the where clause values in the order
     * that they appear in the values table.
     * 
     * @return an unmodifiable list of vars.
     */
    public List<Var> getWhereValuesVars();

    /**
     * Get an unmodifiable map of vars from the where clause values and their
     * values.
     * 
     * Null values are considered as UNDEF values.
     * 
     * @return an unmodifiable map of vars and their values.
     */
    public Map<Var, List<Node>> getWhereValuesMap();

    /**
     * Reset the values table in the where clause to the initial undefined state.
     * Used primarily to reset the builder values table to a known state.
     */
    public T clearWhereValues();

    /**
     * Adds an optional triple to the where clause.
     * 
     * @param t The triple to add
     * @return This Builder for chaining.
     */
    public T addOptional(Triple t);

    /**
     * Adds an optional triple path to the where clause.
     * 
     * @param t The triple path to add
     * @return This Builder for chaining.
     */
    public T addOptional(TriplePath t);

    /**
     * Adds an optional triple as to the where clause.
     * 
     * @param t The triple to add
     * @return This Builder for chaining.
     */
    public T addOptional(FrontsTriple t);

    /**
     * Adds an optional triple or triple path to the where clause.
     * 
     * See {@link AbstractQueryBuilder#makeTriplePath} for conversion of the param
     * values.
     * 
     * @param s The subject.
     * @param p The predicate.
     * @param o The object.
     * @return This Builder for chaining.
     */
    public T addOptional(Object s, Object p, Object o);

    /**
     * Adds an optional group pattern to the where clause.
     * 
     * @param t The select builder to add as an optional pattern
     * @return This Builder for chaining.
     */
    public T addOptional(AbstractQueryBuilder<?> t);

    /**
     * Adds a filter to the where clause
     * 
     * @param expression the expression to evaluate for the filter.
     * @return @return This Builder for chaining.
     * @throws ParseException If the expression can not be parsed.
     */
    public T addFilter(String expression) throws ParseException;

    /**
     * Adds a filter to the where clause
     * 
     * Use ExprFactory or NodeValue static or the AbstractQueryBuilder.makeExpr
     * methods to create the expression.
     * 
     * @see ExprFactory
     * @see org.apache.jena.sparql.expr.NodeValue
     * @see AbstractQueryBuilder#makeExpr(String)
     * 
     * @param expression the expression to evaluate for the filter.
     * @return @return This Builder for chaining.
     */
    public T addFilter(Expr expression);

    /**
     * Add a sub query.
     * 
     * @param subQuery The subquery as defined by a SelectBuilder.
     * @return This builder for chaining.
     */
    public T addSubQuery(AbstractQueryBuilder<?> subQuery);

    /**
     * Add a union.
     * 
     * @param union The union as defined by a SelectBuilder.
     * @return This builder for chaining.
     */
    public T addUnion(AbstractQueryBuilder<?> union);

    /**
     * Add a graph statement to the query as per
     * http://www.w3.org/TR/2013/REC-sparql11 -query-20130321/#rGraphGraphPattern.
     * 
     * See {@link AbstractQueryBuilder#makeNode} for conversion of the graph param.
     * 
     * @param graph    The iri or variable identifying the graph.
     * @param subQuery The graph to add.
     * @return This builder for chaining.
     */
    public T addGraph(Object graph, AbstractQueryBuilder<?> subQuery);

    /**
     * Add a graph statement to the query as per
     * http://www.w3.org/TR/2013/REC-sparql11 -query-20130321/#rGraphGraphPattern.
     * 
     * See {@link AbstractQueryBuilder#makeNode} for conversion of the graph param.
     * 
     * @param graph  The iri or variable identifying the graph.
     * @param triple a single s, p, o triple for the query.
     * @return This builder for chaining.
     */
    public T addGraph(Object graph, FrontsTriple triple);

    /**
     * Add a graph statement to the query as per
     * http://www.w3.org/TR/2013/REC-sparql11 -query-20130321/#rGraphGraphPattern.
     * 
     * See {@link AbstractQueryBuilder#makeNode} for conversion of the graph param.
     * 
     * @param graph     The iri or variable identifying the graph.
     * @param subject   The subject for the graph query
     * @param predicate The predicate for the graph query.
     * @param object    The object for the graph query.
     * @return This builder for chaining.
     */
    public T addGraph(Object graph, Object subject, Object predicate, Object object);

    /**
     * Add a graph statement to the query as per
     * http://www.w3.org/TR/2013/REC-sparql11 -query-20130321/#rGraphGraphPattern.
     * 
     * See {@link AbstractQueryBuilder#makeNode} for conversion of the graph param.
     * 
     * @param graph  The iri or variable identifying the graph.
     * @param triple a single triple for the query.
     * @return This builder for chaining.
     */
    public T addGraph(Object graph, Triple triple);

    /**
     * Add a graph statement to the query as per
     * http://www.w3.org/TR/2013/REC-sparql11 -query-20130321/#rGraphGraphPattern.
     * 
     * See {@link AbstractQueryBuilder#makeNode} for conversion of the graph param.
     * 
     * @param graph      The iri or variable identifying the graph.
     * @param triplePath a single triple path for the query.
     * @return This builder for chaining.
     */
    public T addGraph(Object graph, TriplePath triplePath);

    /**
     * Add a bind statement to the query *
     * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rGraphGraphPattern.
     * 
     * @param expression The expression to bind to the var.
     * @param var        The variable to bind to.
     * @return This builder for chaining.
     */
    public T addBind(Expr expression, Object var);

    /**
     * Add a bind statement to the query
     * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rGraphGraphPattern.
     * 
     * @param expression The expression to bind to the var.
     * @param var        The variable to bind to.
     * @return This builder for chaining.
     * @throws ParseException
     */
    public T addBind(String expression, Object var) throws ParseException;

    /**
     * Get the Where handler for this clause.
     * 
     * @return The WhereHandler used by this clause.
     */
    public WhereHandler getWhereHandler();

    /**
     * Create a list node from a list of objects as per RDF Collections.
     * 
     * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#collections
     * 
     * See {@link AbstractQueryBuilder#makeNode} for conversion of the param values.
     * <p>
     * usage:
     * <ul>
     * <li>list( param1, param2, param3, ... )</li>
     * <li>addWhere( list( param1, param2, param3, ... ), p, o )</li>
     * <li>addOptional( list( param1, param2, param3, ... ), p, o )</li>
     * </ul>
     * </p>
     * 
     * @param objs the list of objects for the list.
     * @return the first blank node in the list.
     */
    public Node list(Object... objs);

    /**
     * Add a minus clause to the query.
     * 
     * https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rMinusGraphPattern
     * 
     * @param t The select builder to add as a minus pattern
     * @return this builder for chaining
     */
    public T addMinus(AbstractQueryBuilder<?> t);

}
