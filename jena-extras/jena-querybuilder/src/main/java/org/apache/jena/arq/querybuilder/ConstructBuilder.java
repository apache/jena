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
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.clauses.ConstructClause;
import org.apache.jena.arq.querybuilder.clauses.DatasetClause;
import org.apache.jena.arq.querybuilder.clauses.SolutionModifierClause;
import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.arq.querybuilder.handlers.ConstructHandler;
import org.apache.jena.arq.querybuilder.handlers.DatasetHandler;
import org.apache.jena.arq.querybuilder.handlers.HandlerBlock;
import org.apache.jena.arq.querybuilder.handlers.SolutionModifierHandler;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

/**
 * Builder for SPARQL Construct Queries.
 * <p>
 * The ConstructBuilder provides chainable methods to programmatically generate
 * SPARQL Construct Queries. The application order of the methods is not
 * relevant for the resulting query. An {@link ExprFactory} is intended for use
 * along with the ConstructBuilder to generate needed {@link Expr} parameter
 * values. An {@link ExprFactory} that works with the same prefixes can be
 * obtained with {@link ConstructBuilder#getExprFactory()}.
 * <p>
 * The ConstructBuilder can be used as<b>prepared query</b>. Values for
 * variables in the created query can be set with
 * {@link ConstructBuilder#setVar(Object, Object)} and
 * {@link ConstructBuilder#setVar(Var, Node)}. The method
 * {@link ConstructBuilder#clearWhereValues()} allows to clear the set values.
 *
 * @see AskBuilder
 * @see DescribeBuilder
 * @see SelectBuilder
 * @see UpdateBuilder
 */
public class ConstructBuilder extends AbstractQueryBuilder<ConstructBuilder> implements DatasetClause<ConstructBuilder>,
        WhereClause<ConstructBuilder>, SolutionModifierClause<ConstructBuilder>, ConstructClause<ConstructBuilder> {

    private final HandlerBlock handlerBlock;

    public ConstructBuilder() {
        super();
        query.setQueryConstructType();
        handlerBlock = new HandlerBlock(query);
    }

    @Override
    public DatasetHandler getDatasetHandler() {
        return handlerBlock.getDatasetHandler();
    }

    @Override
    public ConstructHandler getConstructHandler() {
        return handlerBlock.getConstructHandler();
    }

    @Override
    public SolutionModifierHandler getSolutionModifierHandler() {
        return handlerBlock.getModifierHandler();
    }

    @Override
    public HandlerBlock getHandlerBlock() {
        return handlerBlock;
    }

    @Override
    public ConstructBuilder clone() {
        ConstructBuilder qb = new ConstructBuilder();
        qb.handlerBlock.addAll(handlerBlock);
        return qb;
    }

    @Override
    public ConstructBuilder fromNamed(Object graphName) {
        getDatasetHandler().fromNamed(graphName);
        return this;
    }

    @Override
    public ConstructBuilder from(Object graphName) {
        getDatasetHandler().from(graphName);
        return this;
    }

    @Override
    public ConstructBuilder addOrderBy(Expr orderBy) {
        getSolutionModifierHandler().addOrderBy(orderBy);
        return this;
    }

    @Override
    public ConstructBuilder addOrderBy(Object orderBy) {
        getSolutionModifierHandler().addOrderBy(Converters.makeVar(orderBy));
        return this;
    }

    @Override
    public ConstructBuilder addOrderBy(SortCondition orderBy) {
        getSolutionModifierHandler().addOrderBy(orderBy);
        return this;
    }

    @Override
    public ConstructBuilder addOrderBy(Expr orderBy, Order order) {
        getSolutionModifierHandler().addOrderBy(orderBy, order);
        return this;
    }

    @Override
    public ConstructBuilder addOrderBy(Object orderBy, Order order) {
        getSolutionModifierHandler().addOrderBy(Converters.makeVar(orderBy), order);
        return this;
    }

    @Override
    public ConstructBuilder addGroupBy(Object groupBy) {
        getSolutionModifierHandler().addGroupBy(Converters.makeVar(groupBy));
        return this;
    }

    @Override
    public ConstructBuilder addGroupBy(Expr groupBy) {
        getSolutionModifierHandler().addGroupBy(groupBy);
        return this;
    }

    @Override
    public ConstructBuilder addGroupBy(Object var, Expr expr) {
        getSolutionModifierHandler().addGroupBy(Converters.makeVar(var), expr);
        return this;
    }

    @Override
    public ConstructBuilder addGroupBy(Object var, String expr) {
        getSolutionModifierHandler().addGroupBy(Converters.makeVar(var), makeExpr(expr));
        return this;
    }

    @Override
    public ConstructBuilder addHaving(String having) {
        getSolutionModifierHandler().addHaving(having);
        return this;
    }

    @Override
    public ConstructBuilder addHaving(Expr expression) {
        getSolutionModifierHandler().addHaving(expression);
        return this;
    }

    @Override
    public ConstructBuilder addHaving(Object var) {
        getSolutionModifierHandler().addHaving(Converters.makeVar(var));
        return this;
    }

    @Override
    public ConstructBuilder setLimit(int limit) {
        getSolutionModifierHandler().setLimit(limit);
        return this;
    }

    @Override
    public ConstructBuilder setOffset(int offset) {
        getSolutionModifierHandler().setOffset(offset);
        return this;
    }

    @Override
    public ConstructBuilder addWhere(TriplePath t) {
        getWhereHandler().addWhere(t);
        return this;
    }

    @Override
    public ConstructBuilder addWhere(Collection<TriplePath> collection) {
        getWhereHandler().addWhere(collection);
        return this;
    }
    
    @Override
    public ConstructBuilder addWhere(Triple t) {
        getWhereHandler().addWhere(new TriplePath(t));
        return this;
    }

    @Override
    public ConstructBuilder addWhere(FrontsTriple t) {
        getWhereHandler().addWhere(new TriplePath(t.asTriple()));
        return this;
    }

    @Override
    public ConstructBuilder addWhere(Object s, Object p, Object o) {
        getWhereHandler().addWhere(makeTriplePaths(s, p, o));
        return this;
    }

    @Override
    public ConstructBuilder addWhereValueVar(Object var) {
        getWhereHandler().addValueVar(getPrologHandler().getPrefixes(), var);
        return this;
    }

    @Override
    public ConstructBuilder addWhereValueVar(Object var, Object... values) {
        getWhereHandler().addValueVar(getPrologHandler().getPrefixes(), var, values);
        return this;
    }

    @Override
    public <K extends Collection<?>> ConstructBuilder addWhereValueVars(Map<?, K> dataTable) {
        getWhereHandler().addValueVars(getPrologHandler().getPrefixes(), dataTable);
        return this;
    }

    @Override
    public ConstructBuilder addWhereValueRow(Object... values) {
        getWhereHandler().addValueRow(getPrologHandler().getPrefixes(), values);
        return this;
    }

    @Override
    public ConstructBuilder addWhereValueRow(Collection<?> values) {
        getWhereHandler().addValueRow(getPrologHandler().getPrefixes(), values);
        return this;
    }

    @Override
    public List<Var> getWhereValuesVars() {
        return getWhereHandler().getValuesVars();
    }

    @Override
    public Map<Var, List<Node>> getWhereValuesMap() {
        return getWhereHandler().getValuesMap();
    }

    @Override
    public ConstructBuilder clearWhereValues() {
        getWhereHandler().clearValues();
        return this;
    }

    @Override
    public ConstructBuilder addOptional(TriplePath t) {
        getWhereHandler().addOptional(Arrays.asList(t));
        return this;
    }

    @Override
    public ConstructBuilder addOptional(Collection<TriplePath> collection) {
        getWhereHandler().addOptional(collection);
        return this;
    }
    
    @Override
    public ConstructBuilder addOptional(Triple t) {
        return addOptional(new TriplePath(t));
    }

    @Override
    public ConstructBuilder addOptional(AbstractQueryBuilder<?> t) {
        getWhereHandler().addOptional(t.getWhereHandler());
        return this;
    }

    @Override
    public ConstructBuilder addOptional(FrontsTriple t) {
        return addOptional(new TriplePath(t.asTriple()));
    }

    @Override
    public ConstructBuilder addOptional(Object s, Object p, Object o) {
        getWhereHandler().addOptional(makeTriplePaths(s, p, o));
        return this;
    }

    @Override
    public ConstructBuilder addFilter(Expr expr) {
        getWhereHandler().addFilter(expr);
        return this;
    }

    @Override
    public ConstructBuilder addFilter(String s) {
        getWhereHandler().addFilter(s);
        return this;
    }

    @Override
    public ConstructBuilder addSubQuery(AbstractQueryBuilder<?> subQuery) {
        getWhereHandler().addSubQuery(subQuery);
        return this;
    }

    @Override
    public ConstructBuilder addUnion(AbstractQueryBuilder<?> subQuery) {
        getWhereHandler().addUnion(subQuery);
        return this;
    }

    @Override
    public ConstructBuilder addGraph(Object graph, AbstractQueryBuilder<?> subQuery) {
        getPrologHandler().addAll(subQuery.getPrologHandler());
        getWhereHandler().addGraph(makeNode(graph), subQuery.getWhereHandler());
        return this;
    }

    @Override
    public ConstructBuilder addGraph(Object graph, FrontsTriple triple) {
        return addGraph(graph, new TriplePath(triple.asTriple()));
    }

    @Override
    public ConstructBuilder addGraph(Object graph, Object subject, Object predicate, Object object) {
        getWhereHandler().addGraph(makeNode(graph), makeTriplePaths(subject, predicate, object));
        return this;
    }

    @Override
    public ConstructBuilder addGraph(Object graph, Triple triple) {
        return addGraph(graph, new TriplePath(triple));
    }

    @Override
    public ConstructBuilder addGraph(Object graph, TriplePath triplePath) {
        getWhereHandler().addGraph(makeNode(graph), Arrays.asList(triplePath));
        return this;
    }
    
    @Override
    public ConstructBuilder addGraph(Object graph, Collection<TriplePath> collection) {
        getWhereHandler().addGraph(makeNode(graph), collection);
        return this;
    }

    @Override
    public ConstructBuilder addBind(Expr expression, Object var) {
        getWhereHandler().addBind(expression, Converters.makeVar(var));
        return this;
    }

    @Override
    public ConstructBuilder addBind(String expression, Object var) {
        getWhereHandler().addBind(expression, Converters.makeVar(var));
        return this;
    }

    @Override
    public ConstructBuilder addConstruct(Triple t) {
        getConstructHandler().addConstruct(t);
        return this;
    }

    @Override
    public ConstructBuilder addConstruct(FrontsTriple t) {
        return addConstruct(t.asTriple());
    }

    @Override
    public ConstructBuilder addConstruct(Object s, Object p, Object o) {
        return addConstruct(Triple.create(makeNode(s), makeNode(p), makeNode(o)));
    }

    /*
     * @deprecated use {@code addWhere(Converters.makeCollection(List.of(Object...)))}, or simply call {@link #addWhere(Object, Object, Object)} passing the collection for one of the objects.
     */
    @Deprecated(since="5.0.0")
    @Override
    public Node list(Object... objs) {
        return getWhereHandler().list(objs);
    }

    @Override
    public ConstructBuilder addMinus(AbstractQueryBuilder<?> t) {
        getWhereHandler().addMinus(t);
        return this;
    }
}