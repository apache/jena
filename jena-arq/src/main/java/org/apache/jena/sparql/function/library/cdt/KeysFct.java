package org.apache.jena.sparql.function.library.cdt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class KeysFct extends FunctionBase1
{
	@Override
	public NodeValue exec( final NodeValue nv ) {
		final Map<CDTKey,CDTValue> map = CDTLiteralFunctionUtils.checkAndGetMap(nv);

		final List<CDTValue> list = new ArrayList<>( map.size() );
		for ( final CDTKey key : map.keySet() ) {
			final CDTValue value = CDTFactory.createValue( key.asNode() );
			list.add(value);
		}

		return CDTLiteralFunctionUtils.createNodeValue(list);
	}

}
