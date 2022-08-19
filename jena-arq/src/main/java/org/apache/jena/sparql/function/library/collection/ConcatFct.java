package org.apache.jena.sparql.function.library.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Node_LiteralWithList;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.engine.iterator.QueryIterUnfold;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class ConcatFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue v1, final NodeValue v2 ) {
		final String list1AsString = getInputListAsString(v1);
		final String list2AsString = getInputListAsString(v2);
System.out.println("list1AsString " + list1AsString);
System.out.println("list2AsString " + list2AsString);
		return createResultList(list1AsString, list2AsString);
	}

	protected String getInputListAsString( final NodeValue v ) {
		final Node n = v.asNode();

		if ( ! n.isLiteral() )
			throw new ExprEvalException("Not a literal: " + v);

		final String datatypeURI = n.getLiteralDatatypeURI();
		if ( ! Node_LiteralWithList.datatypeUriUntypedList.equals(datatypeURI) ) {
			throw new ExprEvalException("Literal with wrong datatype: " + v);
		}

		return n.getLiteralLexicalForm();
	}

	protected NodeValue createResultList( final String list1AsString, final String list2AsString ) {
		final String resultListAsString = createResultListAsString(list1AsString, list2AsString);
		final RDFDatatype datatype = Node_LiteralWithList.datatypeUntypedList;

System.out.println("resultListAsString " + resultListAsString);
		final Node n = NodeFactory.createLiteral(resultListAsString, datatype);
		return NodeValue.makeNode(n);
	}

	protected String createResultListAsString( final String list1AsString, final String list2AsString ) {
		final Iterator<Node> it1 = QueryIterUnfold.parseUntypedList(list1AsString, null);
		final Iterator<Node> it2 = QueryIterUnfold.parseUntypedList(list2AsString, null);
		final Iterator<Node> it = new ConcatenatingIterator<Node>(it1, it2);

		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		if ( it.hasNext() ) {
			final Node firstNode = it.next();
			final String firstNodeAsString = NodeFmtLib.strTTL(firstNode);
			sb.append(firstNodeAsString);
			while ( it.hasNext() ) {
				final Node nextNode = it.next();
				final String nextNodeAsString = NodeFmtLib.strTTL(nextNode);
				sb.append(", ");
				sb.append(nextNodeAsString);
			}
		}
		sb.append("]");

		return sb.toString();
	}

	protected static class ConcatenatingIterator<T> implements Iterator<T> {
		protected final Iterator<T> it1;
		protected final Iterator<T> it2;
		public ConcatenatingIterator( final Iterator<T> it1, final Iterator<T> it2 ) { this.it1 = it1; this.it2 = it2; }
		@Override public boolean hasNext() { return it1.hasNext() || it2.hasNext(); }
		@Override public T next() {
			if ( it1.hasNext() )
				return it1.next();
			else if ( it2.hasNext() )
				return it2.next();
			else
				throw new NoSuchElementException();
		}
	}

}
