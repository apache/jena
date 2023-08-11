package org.apache.jena.sparql.function.library.collection;

import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class SizeFct extends FunctionBase1
{
	@Override
	public NodeValue exec( final NodeValue nv ) {
		final Node n = nv.asNode();

		final int size;
		if ( CompositeDatatypeList.isListLiteral(n) ) {
			size = CDTLiteralFunctionUtils.getList(n).size();
		}
		else if ( CompositeDatatypeMap.isMapLiteral(n) ) {
			size = CDTLiteralFunctionUtils.getMap(n).size();
		}
		else {
			throw new ExprEvalException("Neither a list nor a map literal: " + nv);
		}

		return NodeValue.makeInteger(size);
	}

}
