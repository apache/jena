package org.apache.jena.sparql.function.library.collection;

import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class GetFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final Node n1 = nv1.asNode();

		if ( ! CompositeDatatypeList.isListLiteral(n1) )
			throw new ExprEvalException("Not a list literal: " + nv1);

		if ( ! nv2.isInteger() )
			throw new ExprEvalException("Not an integer literal: " + nv2);

		final int index = nv2.getInteger().intValue();

		if ( index < 1 )
			throw new ExprEvalException("Out of bounds index value: " + nv2);

		final List<CDTValue> list = CompositeDatatypeList.getValue( n1.getLiteral() );

		if ( index > list.size() )
			throw new ExprEvalException("Out of bounds index value: " + nv2);

		final CDTValue value = list.get( index-1 );
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
