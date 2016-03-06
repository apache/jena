package org.apache.jena.arq.querybuilder.handlers;

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
