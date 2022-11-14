package org.apache.jena.sparql.function.library.collection;

import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class ContainsFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue nv1, final NodeValue nv2 ) {
		final Node n1 = nv1.asNode();
		final Node n2 = nv2.asNode();

		if ( ! CompositeDatatypeList.isListLiteral(n1) )
			throw new ExprEvalException("Not a list literal: " + nv1);

		final List<CDTValue> list = CompositeDatatypeList.getValue( n1.getLiteral() );

		final boolean result;
		if ( CompositeDatatypeList.isListLiteral(n2) ) {
			result = containsList( list, n2.getLiteral() );
		}
		else if ( CompositeDatatypeMap.isMapLiteral(n2) ) {
			result = containsMap( list, n2.getLiteral() );
		}
		else {
			result = containsNode( list, nv2 );
		}

		return NodeValue.booleanReturn(result);
	}

	/**
	 * Returns true if the given list contains the given RDF term, assuming
	 * that this RDF terms is neither a cdt:List literal nor a cdt:Map literal.
	 */
	protected boolean containsNode( final List<CDTValue> list, final NodeValue n ) {
		for ( final CDTValue v : list ) {
			if ( v.isNode() ) {
				final NodeValue vv = NodeValue.makeNode( v.asNode() );
				if ( NodeValue.sameAs(vv,n) ) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns true if the given list contains the list represented by
	 * the given cdt:List literal (i.e., assumes that the given literal
	 * is a cdt:List literal).
	 */
	protected boolean containsList( final List<CDTValue> list, final LiteralLabel lit ) {
		// get the list for the literal only if needed
		List<CDTValue> otherList = null;

		for ( final CDTValue v : list ) {
			final List<CDTValue> vList; 
			if ( v.isList() ) {
				vList = v.asList();
			}
			else if ( v.isNode() && CompositeDatatypeList.isListLiteral(v.asNode()) ) {
				vList = CompositeDatatypeList.getValue( v.asNode().getLiteral() );
			}
			else {
				vList = null;
			}

			if ( vList != null ) {
				// now we need to get the list
				if ( otherList == null ) {
					otherList = CompositeDatatypeList.getValue(lit);
				}

				if ( vList.equals(otherList) ) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns true if the given list contains the map represented by
	 * the given cdt:Map literal (i.e., assumes that the given literal
	 * is a cdt:Map literal).
	 */
	protected boolean containsMap( final List<CDTValue> list, final LiteralLabel lit) {
		// get the map for the literal only if needed
		Map<CDTKey,CDTValue> otherMap = null;

		for ( final CDTValue v : list ) {
			final Map<CDTKey,CDTValue> vMap;
			if ( v.isMap() ) {
				vMap = v.asMap();
			}
			else if ( v.isNode() && CompositeDatatypeMap.isMapLiteral(v.asNode()) ) {
				vMap = CompositeDatatypeMap.getValue( v.asNode().getLiteral() );
			}
			else {
				vMap = null;
			}

			if ( vMap != null ) {
				// now we need to get the map
				if ( otherMap == null ) {
					otherMap = CompositeDatatypeMap.getValue(lit);
				}

				if ( vMap.equals(otherMap) ) {
					return true;
				}
			}
		}

		return false;
	}
}
