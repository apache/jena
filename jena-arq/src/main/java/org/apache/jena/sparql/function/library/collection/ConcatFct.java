package org.apache.jena.sparql.function.library.collection;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.LiteralLabelForList;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase;

public class ConcatFct extends FunctionBase
{
	@Override
	public void checkBuild( final String uri, final ExprList args ) {
		// nothing to check
	}

	@Override
	public NodeValue exec( final List<NodeValue> args ) {
		if ( args.isEmpty() ) {
			final List<CDTValue> result = new ArrayList<>();
			final LiteralLabel lit = new LiteralLabelForList(result);
			final Node n = NodeFactory.createLiteral(lit);
			return NodeValue.makeNode(n);
		}

		if ( args.size() == 1 ) {
			final NodeValue nv =  args.get(0);
			// make sure that the argument is a well-formed cdt:List literal
			getInputList(nv);

			return nv;
		}

		final List<CDTValue> result = new ArrayList<>();
		for ( final NodeValue nv : args ) {
			result.addAll( getInputList(nv) );
		}

		final LiteralLabel lit = new LiteralLabelForList(result);
		final Node n = NodeFactory.createLiteral(lit);
		return NodeValue.makeNode(n);
	}

	protected List<CDTValue> getInputList( final NodeValue nv ) {
		final Node n = nv.asNode();

		if ( ! CompositeDatatypeList.isListLiteral(n) )
			throw new ExprEvalException("Not a cdt:List literal: " + nv);

		try {
			return CompositeDatatypeList.getValue( n.getLiteral() );
		}
		catch ( final DatatypeFormatException ex ) {
			throw new ExprEvalException("Literal with incorrect lexical form: " + nv, ex);
		}
	}

}
