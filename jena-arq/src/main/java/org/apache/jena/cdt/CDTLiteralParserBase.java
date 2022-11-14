package org.apache.jena.cdt;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.ttl.turtle.TurtleParserBase;

public class CDTLiteralParserBase extends TurtleParserBase
{
	@Override
	protected Node createLiteral( final String lex, final String langTag, final String datatypeURI ) {
		if ( datatypeURI.equals(CompositeDatatypeList.uri) ) {
			final LiteralLabel lit = new LiteralLabelForList(lex);
			return NodeFactory.createLiteral(lit);
		}

		if ( datatypeURI.equals(CompositeDatatypeMap.uri) ) {
			final LiteralLabel lit = new LiteralLabelForMap(lex);
			return NodeFactory.createLiteral(lit);
		}

		return super.createLiteral(lex, langTag, datatypeURI);
	}

}
