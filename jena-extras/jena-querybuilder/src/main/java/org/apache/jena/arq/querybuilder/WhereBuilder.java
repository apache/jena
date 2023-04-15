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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.clauses.WhereClause;
import org.apache.jena.arq.querybuilder.handlers.HandlerBlock;
import org.apache.jena.arq.querybuilder.handlers.WhereHandler;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

/**
 * A simple implementation of WhereClause for use in building complex sub
 * queries where a SelectBuilder or similar implementation is more than is
 * needed.
 *
 */
public class WhereBuilder extends AbstractQueryBuilder<WhereBuilder> implements WhereClause<WhereBuilder> {
    private HandlerBlock block;
    private WhereHandler handler;

    /**
     * Constructor.
     */
    public WhereBuilder() {
        query = new Query();
        block = new HandlerBlock(query);
        handler = block.getWhereHandler();
    }

    @Override
    public WhereBuilder addWhere(Triple t) {
        return addWhere(new TriplePath(t));
    }

    @Override
    public WhereBuilder addWhere(TriplePath t) {
        handler.addWhere(t);
        return this;
    }

    @Override
    public WhereBuilder addWhere(FrontsTriple t) {
        return addWhere(t.asTriple());
    }

    @Override
    public WhereBuilder addWhere(Object s, Object p, Object o) {
        return addWhere(makeTriplePath(s, p, o));
    }

    @Override
    public WhereBuilder addWhereValueVar(Object var) {
        handler.addValueVar(getPrologHandler().getPrefixes(), var);
        return this;
    }

    @Override
    public WhereBuilder addWhereValueVar(Object var, Object... values) {
        getWhereHandler().addValueVar(getPrologHandler().getPrefixes(), var, values);
        return this;
    }

    @Override
    public <K extends Collection<?>> WhereBuilder addWhereValueVars(Map<?, K> dataTable) {
        getWhereHandler().addValueVars(getPrologHandler().getPrefixes(), dataTable);
        return this;
    }

    @Override
    public WhereBuilder addWhereValueRow(Object... values) {
        getWhereHandler().addValueRow(getPrologHandler().getPrefixes(), values);
        return this;
    }

    @Override
    public WhereBuilder addWhereValueRow(Collection<?> values) {
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
    public WhereBuilder clearWhereValues() {
        getWhereHandler().clearValues();
        return this;
    }

    @Override
    public WhereBuilder addOptional(TriplePath t) {
        getWhereHandler().addOptional(t);
        return this;
    }

    @Override
    public WhereBuilder addOptional(Triple t) {
        getWhereHandler().addOptional(new TriplePath(t));
        return this;
    }

    @Override
    public WhereBuilder addOptional(FrontsTriple t) {
        getWhereHandler().addOptional(new TriplePath(t.asTriple()));
        return this;
    }

    @Override
    public WhereBuilder addOptional(Object s, Object p, Object o) {
        getWhereHandler().addOptional(makeTriplePath(s, p, o));
        return this;
    }

    @Override
    public WhereBuilder addOptional(AbstractQueryBuilder<?> t) {
        getWhereHandler().addOptional(t.getWhereHandler());
        return this;
    }

    @Override
    public WhereBuilder addFilter(Expr expr) {
        getWhereHandler().addFilter(expr);
        return this;
    }

    @Override
    public WhereBuilder addFilter(String s) {
        getWhereHandler().addFilter(s);
        return this;
    }

    @Override
    public WhereBuilder addSubQuery(AbstractQueryBuilder<?> subQuery) {
        getWhereHandler().addSubQuery(subQuery);
        return this;
    }

    @Override
    public WhereBuilder addUnion(AbstractQueryBuilder<?> subQuery) {
        getWhereHandler().addUnion(subQuery);
        return this;
    }

    @Override
    public WhereBuilder addGraph(Object graph, AbstractQueryBuilder<?> subQuery) {
        getPrologHandler().addAll(subQuery.getPrologHandler());
        getWhereHandler().addGraph(makeNode(graph), subQuery.getWhereHandler());
        return this;
    }

    @Override
    public WhereBuilder addGraph(Object graph, FrontsTriple triple) {
        getWhereHandler().addGraph(makeNode(graph), new TriplePath(triple.asTriple()));
        return this;
    }

    @Override
    public WhereBuilder addGraph(Object graph, Object subject, Object predicate, Object object) {
        getWhereHandler().addGraph(makeNode(graph), makeTriplePath(subject, predicate, object));
        return this;
    }

    @Override
    public WhereBuilder addGraph(Object graph, Triple triple) {
        getWhereHandler().addGraph(makeNode(graph), new TriplePath(triple));
        return this;
    }

    @Override
    public WhereBuilder addGraph(Object graph, TriplePath triplePath) {
        getWhereHandler().addGraph(makeNode(graph), triplePath);
        return this;
    }

    @Override
    public WhereBuilder addBind(Expr expression, Object var) {
        getWhereHandler().addBind(expression, Converters.makeVar(var));
        return this;
    }

    @Override
    public WhereBuilder addBind(String expression, Object var) {
        getWhereHandler().addBind(expression, Converters.makeVar(var));
        return this;
    }

    @Override
    public Node list(Object... objs) {
        return getWhereHandler().list(objs);
    }

    @Override
    public WhereBuilder addMinus(AbstractQueryBuilder<?> t) {
        getWhereHandler().addMinus(t);
        return this;
    }

    @Override
    public HandlerBlock getHandlerBlock() {
        return block;
    }

}
