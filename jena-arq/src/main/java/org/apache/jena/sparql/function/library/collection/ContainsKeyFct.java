package org.apache.jena.sparql.function.library.collection;

import java.util.Map;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class ContainsKeyFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final Map<CDTKey,CDTValue> map = CDTLiteralFunctionUtils.checkAndGetMap(nv1);

		final Node n2 = nv2.asNode();

		// If the second argument is not an IRI or a literal, then it cannot be
		// a map key in the given map and, by definition of cdt:containsKey, we
		// have to return false.
		if ( ! n2.isURI() && ! n2.isLiteral() )
			return NodeValue.booleanReturn(false);

		if ( map.isEmpty() )
			return NodeValue.booleanReturn(false);

		final CDTKey key = CDTFactory.createKey(n2);
		final CDTValue value = map.get(key);

		return NodeValue.booleanReturn( value != null );
	}

}
