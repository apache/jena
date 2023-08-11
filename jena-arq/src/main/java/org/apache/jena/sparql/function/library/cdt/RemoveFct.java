package org.apache.jena.sparql.function.library.cdt;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class RemoveFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final Map<CDTKey,CDTValue> map = CDTLiteralFunctionUtils.checkAndGetMap(nv1);

		if ( map.isEmpty() )
			return nv1;

		final Node n2 = nv2.asNode();

		// If the second term is not a map key (and the first term is a well-
		// formed cdt:Map literal), the first term needs to be returned as is.
		if ( ! n2.isURI() && ! n2.isLiteral() )
			return nv1;

		final CDTKey key = CDTFactory.createKey(n2);

		if ( ! map.containsKey(key) )
			return nv1;

		final Map<CDTKey,CDTValue> newMap = new HashMap<>(map);
		newMap.remove(key);

		return CDTLiteralFunctionUtils.createNodeValue(newMap);
	}

}
