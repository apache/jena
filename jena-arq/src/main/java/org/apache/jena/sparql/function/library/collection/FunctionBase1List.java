package org.apache.jena.sparql.function.library.collection;

import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public abstract class FunctionBase1List extends FunctionBase1
{
	@Override
	public NodeValue exec( final NodeValue nv ) {
		final List<CDTValue> list = CDTLiteralFunctionUtils.checkAndGetList(nv);
		return _exec(list, nv);
	}

	protected abstract NodeValue _exec( List<CDTValue> list, NodeValue nvList );
}
