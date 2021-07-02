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

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;

/**
 * Class to handle manipulation the aggregation variables in the query.
 *
 */
public class AggregationHandler implements Handler {
    // the query
    private final Query query;

    // a map of variables to aggregators
    private final Map<Var, ExprAggregator> aggMap;

    /**
     * Constructor.
     * 
     * @param query the query to handle.
     */
    public AggregationHandler(Query query) {
        this.query = query;
        aggMap = new HashMap<Var, ExprAggregator>();
    }

    /**
     * Add all the aggregations from the other handler.
     * 
     * @param handler The other handler.
     * @return This handler for chaining.
     */
    public AggregationHandler addAll(AggregationHandler handler) {
        for (ExprAggregator agg : handler.query.getAggregators()) {
            query.allocAggregate(agg.getAggregator());
        }
        for (Map.Entry<Var, ExprAggregator> entry : handler.aggMap.entrySet()) {
            aggMap.put(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Get the query we are executing against.
     * 
     * @return the query.
     */
    public Query getQuery() {
        return query;
    }

    @Override
    public void setVars(Map<Var, Node> values) {
        // nothing to do

    }

    @Override
    public void build() {
        for (Map.Entry<Var, Expr> entry : query.getProject().getExprs().entrySet()) {
            if (aggMap.containsKey(entry.getKey())) {
                entry.setValue(aggMap.get(entry.getKey()));
            }
        }
    }

    /**
     * Add and expression aggregator and variable to the mapping.
     * 
     * if the expr parameter is not an instance of ExprAggregator then no action is
     * taken.
     * 
     * @param expr The expression to add.
     * @param var  The variable that it is bound to.
     */
    public void add(Expr expr, Var var) {
        if (expr instanceof ExprAggregator) {
            ExprAggregator eAgg = (ExprAggregator) expr;
            Expr expr2 = query.allocAggregate(eAgg.getAggregator());
            aggMap.put(var, (ExprAggregator) expr2);
        }
    }

}
