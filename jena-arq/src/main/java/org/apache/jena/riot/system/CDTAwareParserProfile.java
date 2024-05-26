package org.apache.jena.riot.system;

import java.util.List;
import java.util.Map;

import org.apache.jena.cdt.CDTKey;
import org.apache.jena.cdt.CDTValue;
import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.cdt.LiteralLabelForList;
import org.apache.jena.cdt.LiteralLabelForMap;
import org.apache.jena.cdt.ParserForCDTLiterals;
import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.irix.IRIxResolver;
import org.apache.jena.sparql.util.Context;

/**
 * This is a {@link ParserProfile} that supports parsing of CDT literals
 * that occur within the parsed file. The main point is to share the
 * {@link FactoryRDF} object of this parser profile with the parser of
 * these literals in order to get the same blank nodes for the same
 * blank node identifiers both within and outside of the literals, as
 * well as across multiple CDT literals that occur in the parsed file.
 */
public class CDTAwareParserProfile extends ParserProfileStd {

	public CDTAwareParserProfile( final FactoryRDF factory,
	                              final ErrorHandler errorHandler,
	                              final IRIxResolver resolver,
	                              final PrefixMap prefixMap,
	                              final Context context,
	                              final boolean checking,
	                              final boolean strictMode ) {
		super(factory, errorHandler, resolver, prefixMap, context, checking, strictMode);
	}

	@Override
	public Node createTypedLiteral( final String lex, final RDFDatatype datatype, final long line, final long col ) {
		if ( datatype.equals(CompositeDatatypeList.type) ) {
			return createListLiteral(lex);
		}

		if ( datatype.equals(CompositeDatatypeMap.type) ) {
			return createMapLiteral(lex);
		}

		return super.createTypedLiteral(lex, datatype, line, col);
	}

	protected Node createListLiteral( final String lex ) {
		// Attention: In contrast to the overridden createTypedLiteral function
		// in the superclass, for literals of the CDT datatypes we do not perform
		// a checkLiteral check because that would parse the lexical form of the
		// literal already once before doing the other parse to obtain the value.

		final boolean recursive = false;
		final List<CDTValue> value;
		try {
			value = ParserForCDTLiterals.parseListLiteral(this, lex, recursive);
		}
		catch ( final Exception ex ) {
			throw new DatatypeFormatException(lex, CompositeDatatypeList.type, ex);
		}

		final LiteralLabel lit = new LiteralLabelForList(lex, value);
		return NodeFactory.createLiteral(lit);
	}

	protected Node createMapLiteral( final String lex ) {
		// Attention: In contrast to the overridden createTypedLiteral function
		// in the superclass, for literals of the CDT datatypes we do not perform
		// a checkLiteral check because that would parse the lexical form of the
		// literal already once before doing the other parse to obtain the value.

		final boolean recursive = false;
		final Map<CDTKey,CDTValue> value;
		try {
			value = ParserForCDTLiterals.parseMapLiteral(this, lex, recursive);
		}
		catch ( final Exception ex ) {
			throw new DatatypeFormatException(lex, CompositeDatatypeMap.type, ex);
		}

		final LiteralLabel lit = new LiteralLabelForMap(lex, value);
		return NodeFactory.createLiteral(lit);
	}

}
