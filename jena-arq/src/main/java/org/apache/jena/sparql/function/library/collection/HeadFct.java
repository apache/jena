package org.apache.jena.sparql.function.library.collection;

import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;

public class HeadFct extends FunctionBase1List
{
	@Override
	protected NodeValue _exec( final List<CDTValue> list ) {
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
