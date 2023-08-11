package org.apache.jena.sparql.function.library.cdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

public class ConcatFct extends FunctionBase
{
	@Override
	public void checkBuild( final String uri, final ExprList args ) {
		// nothing to check
	}

	@Override
	public NodeValue exec( final List<NodeValue> args ) {
		if ( args.isEmpty() ) {
			final List<CDTValue> result = Collections.emptyList();
			return CDTLiteralFunctionUtils.createNodeValue(result);
		}

		if ( args.size() == 1 ) {
			final NodeValue nv =  args.get(0);
			// make sure that the argument is a well-formed cdt:List literal
			CDTLiteralFunctionUtils.checkAndGetList(nv);

			return nv;
		}

		final List<CDTValue> result = new ArrayList<>();
		for ( final NodeValue nv : args ) {
			final List<CDTValue> l = CDTLiteralFunctionUtils.checkAndGetList(nv);
			result.addAll(l);
		}

		return CDTLiteralFunctionUtils.createNodeValue(result);
	}

}
