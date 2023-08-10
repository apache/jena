package org.apache.jena.sparql.function.library.collection;

import java.util.Map;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class ContainsKeyFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final Node n1 = nv1.asNode();

		if ( ! CompositeDatatypeMap.isMapLiteral(n1) )
			throw new ExprEvalException("Not a map literal: " + nv1);

		final Map<CDTKey,CDTValue> map = CompositeDatatypeMap.getValue( n1.getLiteral() );

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
