package org.apache.jena.sparql.function.library.cdt;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class MergeFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final Map<CDTKey,CDTValue> map1 = CDTLiteralFunctionUtils.checkAndGetMap(nv1);
		final Map<CDTKey,CDTValue> map2 = CDTLiteralFunctionUtils.checkAndGetMap(nv2);

		if ( map1.isEmpty() )
			return nv2;

		if ( map2.isEmpty() )
			return nv1;

		final Map<CDTKey,CDTValue> newMap = new HashMap<>(map2);
		newMap.putAll(map1);

		return CDTLiteralFunctionUtils.createNodeValue(newMap);
	}

}
