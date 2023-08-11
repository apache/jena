package org.apache.jena.sparql.function.library.collection;

import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;

public class TailFct extends FunctionBase1List
{
	@Override
	protected NodeValue _exec( final List<CDTValue> list, final NodeValue nvList ) {
		if ( list.size() == 0 )
			throw new ExprEvalException("Empty list");

		final List<CDTValue> sublist = list.subList( 1, list.size() );
		return CDTLiteralFunctionUtils.createNodeValue(sublist);
	}

}
