package org.apache.jena.sparql.function.library.collection;

import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTFactory;
import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class GetFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final Node n1 = nv1.asNode();

		if ( CompositeDatatypeList.isListLiteral(n1) )
			return getFromList(n1, nv2);

		if ( CompositeDatatypeMap.isMapLiteral(n1) )
			return getFromMap(n1, nv2);

		throw new ExprEvalException("Neither a list nor a map literal: " + nv1);
	}

	protected NodeValue getFromList( final Node n1, final NodeValue nv2 ) {
		if ( ! nv2.isInteger() )
			throw new ExprEvalException("Not an integer literal: " + nv2);

		final int index = nv2.getInteger().intValue();

		if ( index < 1 )
			throw new ExprEvalException("Out of bounds index value: " + nv2);

		final List<CDTValue> list = CDTLiteralFunctionUtils.getList(n1);

		if ( index > list.size() )
			throw new ExprEvalException("Out of bounds index value: " + nv2);

		final CDTValue value = list.get( index-1 );
		if ( value.isNull() ) {
			throw new ExprEvalException("accessing null value from list" );
		}
		else if ( value.isNode() ) {
			return NodeValue.makeNode( value.asNode() );
		}
		else {
			throw new ExprEvalException("Unexpected type of CDTValue: " + value.getClass().getName() );
		}
	}

	protected NodeValue getFromMap( final Node n1, final NodeValue nv2 ) {
		final Node n2 = nv2.asNode();
		if ( ! n2.isURI() && ! n2.isLiteral() )
			throw new ExprEvalException("Not a valid map key: " + nv2);

		final Map<CDTKey,CDTValue> map = CDTLiteralFunctionUtils.getMap(n1);

		if ( map.isEmpty() )
			throw new ExprEvalException("empty map");

		final CDTKey key = CDTFactory.createKey(n2);
		final CDTValue value = map.get(key);

		if ( value == null ) {
			throw new ExprEvalException("key is not in the map");
		}
		else if ( value.isNull() ) {
			throw new ExprEvalException("value for key is in null");
		}
		else if ( value.isNode() ) {
			return NodeValue.makeNode( value.asNode() );
		}
		else {
			throw new ExprEvalException("Unexpected type of CDTValue: " + value.getClass().getName() );
		}
	}

}
