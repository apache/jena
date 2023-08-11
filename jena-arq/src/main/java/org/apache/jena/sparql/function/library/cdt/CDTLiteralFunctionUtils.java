package org.apache.jena.sparql.function.library.cdt;

import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.cdt.LiteralLabelForList;
import org.apache.jena.cdt.LiteralLabelForMap;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;

public class CDTLiteralFunctionUtils
{
	/**
	 * Uses {@link CompositeDatatypeList#isListLiteral(Node)} to check whether
	 * the given node is a cdt:List literal and throws an exception if not.
	 */
	public static final void ensureListLiteral( final Node n ) throws ExprEvalException {
		if ( ! CompositeDatatypeList.isListLiteral(n) )
			throw new ExprEvalException("Not a cdt:List literal: " + n);
	}

	/**
	 * Uses {@link CompositeDatatypeMap#isMapLiteral(Node)} to check whether
	 * the given node is a cdt:Map literal and throws an exception if not.
	 */
	public static final void ensureMapLiteral( final Node n ) throws ExprEvalException {
		if ( ! CompositeDatatypeMap.isMapLiteral(n) )
			throw new ExprEvalException("Not a cdt:Map literal: " + n);
	}

	/**
	 * Assumes that the given node is a cdt:List literal and uses
	 * {@link CompositeDatatypeList#getValue(LiteralLabel)} to get
	 * the list.
	 * 
	 * Throws an ExprEvalException if a DatatypeFormatException is
	 * thrown by {@link CompositeDatatypeList#getValue(LiteralLabel)}.
	 */
	public static final List<CDTValue> getList( final Node n ) throws ExprEvalException {
		try {
			return CompositeDatatypeList.getValue( n.getLiteral() );
		}
		catch ( final DatatypeFormatException ex ) {
			throw new ExprEvalException("Not a well-formed cdt:List literal: " + n, ex);
		}
	}

	/**
	 * Assumes that the given node is a cdt:Map literal and uses
	 * {@link CompositeDatatypeMap#getValue(LiteralLabel)} to get
	 * the map.
	 * 
	 * Throws an ExprEvalException if a DatatypeFormatException is
	 * thrown by {@link CompositeDatatypeMap#getValue(LiteralLabel)}.
	 */
	public static final Map<CDTKey,CDTValue> getMap( final Node n ) throws ExprEvalException {
		try {
			return CompositeDatatypeMap.getValue( n.getLiteral() );
		}
		catch ( final DatatypeFormatException ex ) {
			throw new ExprEvalException("Not a well-formed cdt:Map literal: " + n, ex);
		}
	}

	/**
	 * Calls {@link #ensureListLiteral(Node)} first, and {@link #getList(Node)}
	 * afterwards.
	 */
	public static final List<CDTValue> checkAndGetList( final Node n ) throws ExprEvalException {
		ensureListLiteral(n);
		return getList(n);
	}

	/**
	 * Calls {@link #ensureMapLiteral(Node)} first, and {@link #getMap(Node)}
	 * afterwards.
	 */
	public static final Map<CDTKey,CDTValue> checkAndGetMap( final Node n ) throws ExprEvalException {
		ensureMapLiteral(n);
		return getMap(n);
	}

	public static final List<CDTValue> checkAndGetList( final NodeValue nv ) throws ExprEvalException {
		return checkAndGetList( nv.asNode() );
	}

	public static final Map<CDTKey,CDTValue> checkAndGetMap( final NodeValue nv ) throws ExprEvalException {
		return checkAndGetMap( nv.asNode() );
	}

	/**
	 * Creates a {@link NodeValue} with a cdt:List literal that represents the
	 * given list.
	 */
	public static final NodeValue createNodeValue( final List<CDTValue> list ) {
		final LiteralLabel lit = new LiteralLabelForList(list);
		final Node n = NodeFactory.createLiteral(lit);
		return NodeValue.makeNode(n);
	}

	/**
	 * Creates a {@link NodeValue} with a cdt:Map literal that represents the
	 * given map.
	 */
	public static final NodeValue createNodeValue( final Map<CDTKey,CDTValue> map ) {
		final LiteralLabel lit = new LiteralLabelForMap(map);
		final Node n = NodeFactory.createLiteral(lit);
		return NodeValue.makeNode(n);
	}

}
