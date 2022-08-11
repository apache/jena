package org.apache.jena.sparql.function.library.collection;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.iterator.QueryIterUnfold;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class SizeFct extends FunctionBase1
{
	@Override
	public NodeValue exec( final NodeValue nv ) {
		final Node n = nv.asNode();

		if ( ! n.isLiteral() )
			throw new ExprEvalException("Not a literal: " + nv);

		final String datatypeURI = n.getLiteralDatatypeURI();
		final int size;
		if ( QueryIterUnfold.datatypeUriUntypedList.equals(datatypeURI) ) {
			size = determineSizeOfUntypedList( n.getLiteral().getLexicalForm() );
		}
		else if ( QueryIterUnfold.datatypeUriTypedList.equals(datatypeURI) ) {
			size = determineSizeOfTypedList( n.getLiteral().getLexicalForm() );
		}
		else if ( QueryIterUnfold.datatypeUriUntypedMap.equals(datatypeURI) ) {
			size = determineSizeOfUntypedMap( n.getLiteral().getLexicalForm() );
		}
		else if ( QueryIterUnfold.datatypeUriTypedMap.equals(datatypeURI) ) {
			size = determineSizeOfTypedMap( n.getLiteral().getLexicalForm() );
		}
		else {
			throw new ExprEvalException("Literal with wrong datatype: " + nv);
		}

		return NodeValue.makeInteger(size);
	}

	protected int determineSizeOfUntypedList( final String listAsString ) {
		final Iterator<?> it = QueryIterUnfold.parseUntypedList(listAsString, null);
		return determineNumberOfElements(it);
	}

	protected int determineSizeOfTypedList( final String listAsString ) {
		final Iterator<?> it = QueryIterUnfold.parseTypedList(listAsString, null);
		return determineNumberOfElements(it);
	}

	protected int determineSizeOfUntypedMap( final String mapAsString ) {
		final Iterator<?> it = QueryIterUnfold.parseUntypedMap(mapAsString, null);
		return determineNumberOfElements(it);
	}

	protected int determineSizeOfTypedMap( final String mapAsString ) {
		final Iterator<?> it = QueryIterUnfold.parseTypedMap(mapAsString, null);
		return determineNumberOfElements(it);
	}

	protected int determineNumberOfElements( final Iterator<?> it ) {
		int i = 0;
		while ( it.hasNext() ) {
			i++;
			it.next();
		}
		return i;
	}

}
