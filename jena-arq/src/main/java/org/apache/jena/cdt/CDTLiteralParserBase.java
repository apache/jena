package org.apache.jena.cdt;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.riot.lang.extra.LangParserBase;

public class CDTLiteralParserBase extends LangParserBase
{
	@Override
	protected Node createLiteral( final String lex, final String langTag, final String datatypeURI, final int line, final int column ) {
		if ( CompositeDatatypeList.uri.equals(datatypeURI) ) {
			final LiteralLabel lit = new LiteralLabelForList(lex);
			return NodeFactory.createLiteral(lit);
		}

		if ( CompositeDatatypeMap.uri.equals(datatypeURI) ) {
			final LiteralLabel lit = new LiteralLabelForMap(lex);
			return NodeFactory.createLiteral(lit);
		}

		return super.createLiteral(lex, langTag, datatypeURI, line, column);
	}

}
