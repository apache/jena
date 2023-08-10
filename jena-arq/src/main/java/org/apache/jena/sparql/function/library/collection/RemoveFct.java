package org.apache.jena.sparql.function.library.collection;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.cdt.LiteralLabelForMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class RemoveFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final Node n1 = nv1.asNode();

		if ( ! CompositeDatatypeMap.isMapLiteral(n1) )
			throw new ExprEvalException("Not a map literal: " + nv1);

		final Map<CDTKey,CDTValue> map = CompositeDatatypeMap.getValue( n1.getLiteral() );

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

		final LiteralLabel lit = new LiteralLabelForMap(newMap);
		return NodeValue.makeNode( NodeFactory.createLiteral(lit) );
	}

}
