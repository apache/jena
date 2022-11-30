package org.apache.jena.sparql.function.library.collection;

import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class HeadFct extends FunctionBase1
{
	@Override
	public NodeValue exec( final NodeValue nv ) {
		final Node n = nv.asNode();

		if ( ! CompositeDatatypeList.isListLiteral(n) )
			throw new ExprEvalException("Not a list literal: " + nv);

		final List<CDTValue> list = CompositeDatatypeList.getValue( n.getLiteral() );

		if ( list.size() == 0 )
			throw new ExprEvalException("Empty list");

		final CDTValue value = list.get(0);
		if ( value.isNull() ) {
			throw new ExprEvalException("accessing null value from list" );
		}
		else if ( value.isNode() ) {
			return NodeValue.makeNode( value.asNode() );
		}
		else {
			throw new ExprEvalException("Unexpected type of CDTValue: " + value.getClass().getName() );
		}
	}

}
