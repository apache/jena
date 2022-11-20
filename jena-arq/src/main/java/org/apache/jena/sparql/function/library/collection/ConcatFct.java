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
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class ConcatFct extends FunctionBase2
{
	@Override
	public NodeValue exec( final NodeValue v1, final NodeValue v2 ) {
		final List<CDTValue> list1 = getInputList(v1);
		final List<CDTValue> list2 = getInputList(v2);

		if ( list1.isEmpty() ) {
			return v2;
		}

		if ( list2.isEmpty() ) {
			return v1;
		}

		final List<CDTValue> result = new ArrayList<>();
		result.addAll(list1);
		result.addAll(list2);

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
