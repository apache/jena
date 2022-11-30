package org.apache.jena.sparql.function.library.collection;

import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public abstract class FunctionBase1List extends FunctionBase1
{
	@Override
	public NodeValue exec( final NodeValue nv ) {
		final Node n = nv.asNode();

		if ( ! CompositeDatatypeList.isListLiteral(n) )
			throw new ExprEvalException("Not a list literal: " + nv);

		final List<CDTValue> list = CompositeDatatypeList.getValue( n.getLiteral() );

		return _exec(list);
	}

	protected abstract NodeValue _exec( List<CDTValue> list );
}
