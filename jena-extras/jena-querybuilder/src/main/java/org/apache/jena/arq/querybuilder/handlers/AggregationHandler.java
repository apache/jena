package org.apache.jena.arq.querybuilder.handlers;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.aggregate.Aggregator;

public class AggregationHandler implements Handler {
	private final Query query;
	private final Map<Var,ExprAggregator> aggMap;

	public AggregationHandler( Query query )
	{
		this.query = query;
		aggMap = new HashMap<Var,ExprAggregator>();
	}
	
	public AggregationHandler addAll(AggregationHandler handler)
	{
		for (ExprAggregator agg : handler.query.getAggregators())
		{
			query.allocAggregate(agg.getAggregator());
		}
		for (Map.Entry<Var, ExprAggregator> entry : handler.aggMap.entrySet())
		{
			aggMap.put( entry.getKey(), entry.getValue());
		}
		return this;
	}
		
	public Query getQuery()
	{
		return query;
	}
	
	public Map<Var, ExprAggregator> getVarMap() {
		Map<Var,ExprAggregator> retval = new HashMap<Var,ExprAggregator>();
		for (ExprAggregator agg : query.getAggregators())
		{
			retval.put( agg.getVar(), agg);
		}
		return retval;
	}
	
	@Override
	public void setVars(Map<Var, Node> values) {
		// nothing to do

	}

	@Override
	public void build() {
		for (Map.Entry<Var,Expr> entry : query.getProject().getExprs().entrySet())
		{
			if (aggMap.containsKey(entry.getKey()))
			{
				entry.setValue( aggMap.get(entry.getKey()));
			}
		}
	}
	
	public void add(Expr expr, Var var) {
		if (expr instanceof ExprAggregator)
		{
			ExprAggregator eAgg = (ExprAggregator)expr;
			Expr expr2 = query.allocAggregate( eAgg.getAggregator() );	
			aggMap.put(var, (ExprAggregator)expr2);
		}
	}

}
