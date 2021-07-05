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

import org.apache.jena.arq.querybuilder.AbstractQueryBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.handlers.SolutionModifierHandler;
import org.apache.jena.query.SortCondition;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;

/**
 * Interface that defines the SolutionClause as per
 * http://www.w3.org/TR/2013/REC-sparql11-query-20130321/#rSolutionModifier
 * 
 * @param <T> The Builder type that the clause is part of.
 */
public interface SolutionModifierClause<T extends AbstractQueryBuilder<T>> {

    /**
     * Add an ascending order by.
     * 
     * Use ExprFactory or NodeValue static or the AbstractQueryBuilder.makeExpr
     * methods to create the expression.
     * 
     * @see ExprFactory
     * @see org.apache.jena.sparql.expr.NodeValue
     * @see AbstractQueryBuilder#makeExpr(String)
     * 
     * @param orderBy The expression to order by.
     * @return This builder for chaining.
     */
    public T addOrderBy(Expr orderBy);

    /**
     * Add an ascending order by.
     * 
     * @param orderBy The object to order by.
     * @return This builder for chaining.
     */
    public T addOrderBy(Object orderBy);

    /**
     * Add an ascending order by.
     * 
     * @param orderBy The SortCondition to order by.
     * @return This builder for chaining.
     */
    public T addOrderBy(SortCondition orderBy);

    /**
     * Add an order by with direction specified.
     * 
     * @param orderBy The expression to order by.
     * @param order The direction to order.
     * @return This builder for chaining.
     */
    public T addOrderBy(Expr orderBy, Order order);

    /**
     * Add an order by with direction specified.
     * 
     * @param orderBy The object to order by.
     * @param order The direction to order.
     * @return This builder for chaining.
     */
    public T addOrderBy(Object orderBy, Order order);

    /**
     * Add a variable to the group by clause.
     * 
     * @param groupBy The object to group by.
     * @return This builder for chaining.
     */
    public T addGroupBy(Object groupBy);

    /**
     * Add an expression to the group by clause. The expression may be created from
     * a string using the makeExpr() method.
     * 
     * @param groupBy The expression to add.
     */
    public T addGroupBy(Expr groupBy);

    /**
     * Add var and expression to the group by clause.
     * 
     * @param var The variable to add.
     * @param expr The expression to add.
     */
    public T addGroupBy(Object var, Expr expr);

    /**
     * Add var and expression to the group by clause.
     * 
     * @param var The variable to add.
     * @param expr The expression to add.
     */
    public T addGroupBy(Object var, String expr);

    /**
     * Add a having expression.
     * 
     * @param expression Expression to evaluate for the having.
     * @return This builder for chaining.
     */
    public T addHaving(String expression) throws ParseException;

    /**
     * Add a having expression.
     * 
     * Use ExprFactory or NodeValue static or the AbstractQueryBuilder.makeExpr
     * methods to create the expression.
     * 
     * @see ExprFactory
     * @see org.apache.jena.sparql.expr.NodeValue
     * @see AbstractQueryBuilder#makeExpr(String)
     * 
     * @param expression Expression to evaluate for the having.
     * @return This builder for chaining.
     */
    public T addHaving(Expr expression) throws ParseException;

    /**
     * Add a having expression.
     * 
     * @param var the variable to have.
     * @return This builder for chaining.
     */
    public T addHaving(Object var) throws ParseException;

    /**
     * Set the limit.
     * 
     * Setting the limit to 0 (zero) sets no limit.
     * 
     * @param limit the maximum number of results to return.
     * @return This builder for chaining.
     */
    public T setLimit(int limit);

    /**
     * Set the offset.
     * 
     * Setting the offset to 0 (zero) sets no offset.
     * 
     * @param offset the number of results to skip before returning results..
     * @return This builder for chaining.
     */
    public T setOffset(int offset);

    /**
     * Get the Solution modifier for this clause.
     * 
     * @return The SolutionModifierHandler the clause is using.
     */
    public SolutionModifierHandler getSolutionModifierHandler();

}
