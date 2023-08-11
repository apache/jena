package org.apache.jena.sparql.function.library.collection;

import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.function.FunctionBase2;

public class ContainsTermFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final List<CDTValue> list = CDTLiteralFunctionUtils.checkAndGetList(nv1);
		final boolean result = containsTerm( list, nv2 );
		return NodeValue.booleanReturn(result);
	}

	/**
	 * Returns true if the given list contains the given RDF term.
	 */
	protected boolean containsTerm( final List<CDTValue> list, final NodeValue nv ) {
		for ( final CDTValue v : list ) {
			if ( v.isNode() ) {
				if ( NodeFunctions.sameTerm(v.asNode(), nv.asNode()) ) {
					return true;
				}
			}
		}

		return false;
	}

}
