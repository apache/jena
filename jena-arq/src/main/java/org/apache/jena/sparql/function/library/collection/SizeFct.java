package org.apache.jena.sparql.function.library.collection;

import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;
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

		final int size;
		try {
			final LiteralLabel lit = n.getLiteral();
			final String datatypeURI = n.getLiteralDatatypeURI();
			
			if ( datatypeURI.equals(CompositeDatatypeList.uri) ) {
				size = CompositeDatatypeList.getValue(lit).size();
			}
			else if ( datatypeURI.equals(CompositeDatatypeMap.uri) ) {
				size = CompositeDatatypeMap.getValue(lit).size();
			}
			else {
				throw new ExprEvalException("Literal with wrong datatype: " + datatypeURI);
			}
		}
		catch ( final DatatypeFormatException ex ) {
			throw new ExprEvalException("Literal with incorrect lexical form: " + nv, ex);
		}

		return NodeValue.makeInteger(size);
	}

}
