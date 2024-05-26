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
package org.apache.jena.arq.querybuilder.handlers;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.rewriters.ExprRewriter;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.lang.arq.ARQParser;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.lang.arq.TokenMgrError;

/**
 * The Solution Modifier handler.
 *
 */
public class SolutionModifierHandler implements Handler {
    // the query to modify
    private final Query query;

    /**
     * Constructor
     *
     * @param query The query to modify.
     */
    public SolutionModifierHandler(Query query) {
        this.query = query;
    }

    /**
     * Copy all the modifications from the Solution Modifier argument
     *
     * @param solutionModifier The solution modifier to copy from.
     */
    public void addAll(SolutionModifierHandler solutionModifier) {
        List<SortCondition> lst = solutionModifier.query.getOrderBy();
        if (lst != null) {
            for (SortCondition sc : lst) {
                query.addOrderBy(sc);
            }
        }
        query.getGroupBy().addAll(solutionModifier.query.getGroupBy());
        query.getHavingExprs().addAll(solutionModifier.query.getHavingExprs());
        query.setLimit(solutionModifier.query.getLimit());
        query.setOffset(solutionModifier.query.getOffset());
    }

    /**
     * Add an order by clause
     *
     * @param condition The SortCondition to add to the order by.
     */
    public void addOrderBy(SortCondition condition) {
        query.addOrderBy(condition);
    }

    /**
     * Add an expression to the order by clause. Sorts in Default order.
     *
     * @param expr The expression to add.
     */
    public void addOrderBy(Expr expr) {
        query.addOrderBy(expr, Query.ORDER_DEFAULT);
    }

    /**
     * Add an expression to the order by clause.
     *
     * @param expr The expression to add.
     * @param order The direction of the ordering.
     */
    public void addOrderBy(Expr expr, Order order) {
        query.addOrderBy(expr, order == Order.ASCENDING ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING);
    }

    /**
     * Add a var to the order by clause. Sorts in default order
     *
     * @param var The var to use for sorting
     */
    public void addOrderBy(Var var) {
        query.addOrderBy(var, Query.ORDER_DEFAULT);
    }

    /**
     * Add a var to the order by clause.
     *
     * @param var The var to sort by.
     * @param order The direction of the ordering.
     */
    public void addOrderBy(Var var, Order order) {
        query.addOrderBy(var, order == Order.ASCENDING ? Query.ORDER_ASCENDING : Query.ORDER_DESCENDING);
    }

    /**
     * Add an expression to the group by clause.
     *
     * @param expr The expression to add.
     */
    public void addGroupBy(Expr expr) {
        query.addGroupBy(expr);
    }

    /**
     * Add a node to the group by clause.
     *
     * @param var The variable to add.
     */
    public void addGroupBy(Var var) {
        query.addGroupBy(var);
    }

    /**
     * Add var and expression to the group by clause.
     *
     * @param var The variable to add.
     * @param expr The expression to add.
     */
    public void addGroupBy(Var var, Expr expr) {
        query.addGroupBy(var, expr);
    }

    /**
     * Add a having expression.
     *
     * @param expression The expression to add
     */
    public void addHaving(String expression) {
        // Must allow the parser to set "allowAggregatesInExpressions"
        String havingClause = "HAVING (" + expression + " )";
        try {
            ARQParser parser = new ARQParser(new ByteArrayInputStream(StrUtils.asUTF8bytes(havingClause)));
            parser.setQuery(query);
            parser.HavingClause();
        } catch (ParseException ex) {
            throw new QueryParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginLine);
        } catch (TokenMgrError tErr) {
            throw new QueryParseException(tErr.getMessage(), -1, -1);
        } catch (Error err) {
            // The token stream can throw java.lang.Error's
            String tmp = err.getMessage();
            if ( tmp == null )
                throw new QueryParseException(err, -1, -1);
            throw new QueryParseException(tmp, -1, -1);
        }
    }

    /**
     * Add a variable to the having clause.
     *
     * @param var The variable to add.
     */
    public void addHaving(Var var) {
        query.addHavingCondition(new ExprVar(var));
    }

    /**
     * Add an expression to the having clause.
     *
     * @param expr The expression to add.
     */
    public void addHaving(Expr expr) {
        query.addHavingCondition(expr);
    }

    /**
     * Set the limit for the number of results to return. Setting the limit to zero
     * (0) or removes the limit.
     *
     * @param limit The limit to set.
     */
    public void setLimit(int limit) {
        query.setLimit(limit < 1 ? Query.NOLIMIT : limit);
    }

    /**
     * Set the offset for the results to return. Setting the offset to zero (0) or
     * removes the offset.
     *
     * @param offset The offset to set.
     */
    public void setOffset(int offset) {
        query.setOffset(offset < 1 ? Query.NOLIMIT : offset);
    }

    @Override
    public void setVars(Map<Var, Node> values) {
        if (values.isEmpty()) {
            return;
        }

        ExprRewriter exprRewriter = new ExprRewriter(values);

        ExprList having = exprRewriter.rewrite(new ExprList(query.getHavingExprs()));
        List<SortCondition> orderBy = exprRewriter.rewriteSortConditionList(query.getOrderBy());

        VarExprList groupBy = exprRewriter.rewrite(query.getGroupBy());

        query.getHavingExprs().clear();
        query.getHavingExprs().addAll(having.getList());
        if (orderBy != null) {
            if (query.getOrderBy() == null) {
                for (SortCondition sc : orderBy) {
                    query.addOrderBy(sc);
                }
            } else {
                query.getOrderBy().clear();
                query.getOrderBy().addAll(orderBy);
            }
        }

        query.getGroupBy().clear();
        query.getGroupBy().addAll(groupBy);
    }

    @Override
    public void build() {
        // no special commands.
    }
}
