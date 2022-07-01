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

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.arq.ARQParser;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.lang.arq.TokenMgrError;

/**
 * A Select clause handler.
 *
 */
public class SelectHandler implements Handler {

    // the query to handle
    private final Query query;
    private final AggregationHandler aggHandler;

    /**
     * Constructor.
     * 
     * @param aggHandler The aggregate handler that wraps the query we want to
     * handle.
     */
    public SelectHandler(AggregationHandler aggHandler) {
        this.query = aggHandler.getQuery();
        this.aggHandler = aggHandler;
        setDistinct(query.isDistinct());
        setReduced(query.isReduced());
    }

    /**
     * Set the distinct flag. Set or unset the distinct flag. Will set the reduced
     * flag if it was previously set.
     * 
     * @param state the state to set the distinct flag to.
     */
    public void setDistinct(boolean state) {
        query.setDistinct(state);
        if (state) {
            query.setReduced(false);
        }
    }

    /**
     * Set the reduced flag. Set or unset the reduced flag. Will set the reduced
     * flag if it was previously set.
     * 
     * @param state the state to set the reduced flag to.
     */
    public void setReduced(boolean state) {
        query.setReduced(state);
        if (state) {
            query.setDistinct(false);
        }
    }

    /**
     * Add a variable to the select. If the variable is <code>null</code> the
     * variables are set to star.
     * 
     * @param var The variable to add.
     */
    public void addVar(Var var) {
        if (var == null) {
            query.setQueryResultStar(true);
        } else {
            query.setQueryResultStar(false);
            query.addResultVar(var);
        }
    }

    /**
     * Add an Expression as variable to the select. If the variable is the variables
     * are set to star.
     * 
     * @param expression The expression as a string.
     * @param var The variable to add.
     */
    public void addVar(String expression, Var var) {
        addVar(parseExpr(expression), var);
    }

    /**
     * Parse an expression string into an expression.
     * 
     * This must be able to be parsed as though it were written "SELECT "+s
     * 
     * @param s the select string to parse.
     * @return the expression
     * @throws QueryParseException on error
     */
    private Expr parseExpr(String s) throws QueryParseException {
        try {
            ARQParser parser = new ARQParser(new StringReader("SELECT " + s));
            parser.setQuery(new Query());
            parser.getQuery().setPrefixMapping(query.getPrefixMapping());
            parser.SelectClause();
            Query q = parser.getQuery();
            VarExprList vel = q.getProject();
            return vel.getExprs().values().iterator().next();
        } catch (ParseException ex) {
            throw new QueryParseException(ex.getMessage(), ex.currentToken.beginLine, ex.currentToken.beginLine);
        } catch (TokenMgrError tErr) {
            throw new QueryParseException(tErr.getMessage(), -1, -1);
        } catch (Error err) {
            // The token stream can throw java.lang.Error's
            String tmp = err.getMessage();
            if (tmp == null)
                throw new QueryParseException(err, -1, -1);
            throw new QueryParseException(tmp, -1, -1);
        }
    }

    /**
     * Add an Expression as variable to the select.
     * 
     * @param expr The expression to add.
     * @param var The variable to add.
     */
    public void addVar(Expr expr, Var var) {
        if (expr == null) {
            throw new IllegalArgumentException("expr may not be null");
        }
        if (var == null) {
            throw new IllegalArgumentException("var may not be null");
        }
        query.setQueryResultStar(false);
        query.addResultVar(var, expr);
        aggHandler.add(expr, var);
    }

    /**
     * Get the list of variables from the query.
     * 
     * @return The list of variables in the query.
     */
    public List<Var> getVars() {
        return query.getProjectVars();
    }

    /**
     * Return the projected var expression list.
     * 
     * @return The projected var expression list.
     */
    public VarExprList getProject() {
        return query.getProject();
    }

    /**
     * Add all the variables from the select handler variable.
     * 
     * @param selectHandler The select handler to copy the variables from.
     */
    public void addAll(SelectHandler selectHandler) {
        setReduced(selectHandler.query.isReduced());
        setDistinct(selectHandler.query.isDistinct());
        query.setQueryResultStar(selectHandler.query.isQueryResultStar());
        VarExprList shProjectVars = selectHandler.query.getProject();
        VarExprList qProjectVars = query.getProject();
        for (Var var : shProjectVars.getVars()) {
            // make sure there are no duplicates
            if (!qProjectVars.contains(var)) {
                qProjectVars.add(var, shProjectVars.getExpr(var));
            }
        }
        aggHandler.addAll(selectHandler.aggHandler);
    }

    @Override
    public void setVars(Map<Var, Node> values) {
        // nothing to do
    }

    @Override
    public void build() {
        if (query.getProject().getVars().isEmpty()) {
            query.setQueryResultStar(true);
        }

        aggHandler.build();

        // handle the SELECT * case
        query.getProjectVars();
    }
}
