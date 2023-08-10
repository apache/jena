package org.apache.jena.sparql.function.library.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.cdt.LiteralLabelForList;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class KeysFct extends FunctionBase1
{
	@Override
	public NodeValue exec( final NodeValue nv ) {
		final Node n = nv.asNode();

		if ( ! CompositeDatatypeMap.isMapLiteral(n) )
			throw new ExprEvalException("Not a map literal: " + nv);

		final Map<CDTKey,CDTValue> map = CompositeDatatypeMap.getValue( n.getLiteral() );

		final List<CDTValue> list = new ArrayList<>( map.size() );
		for ( final CDTKey key : map.keySet() ) {
			final CDTValue value = CDTFactory.createValue( key.asNode() );
			list.add(value);
		}

		final LiteralLabel lit = new LiteralLabelForList(list);
		return NodeValue.makeNode( NodeFactory.createLiteral(lit) );
	}

}
