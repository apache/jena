package org.apache.jena.cdt;

import java.util.Iterator;
import java.util.Map;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;

public class CompositeDatatypeMap extends CompositeDatatypeBase<Map<CDTKey,CDTValue>>
{
	public final static String uri = "http://example.org/cdt/Map";
	public final static CompositeDatatypeMap type = new CompositeDatatypeMap();

	protected CompositeDatatypeMap() {}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public boolean isValidValue( final Object value ) {
		if ( !(value instanceof Map<?,?>) ) {
			return false;
		}

		final Map<?,?> m = (Map<?,?>) value;
		for ( final Map.Entry<?,?> e : m.entrySet() ) {
			if ( !(e.getKey() instanceof CDTKey) ) {
				return false;
			}
			if ( !(e.getValue() instanceof CDTValue) ) {
				return false;
			}
		}

		return true; 
	}

	@Override
	public boolean isValidLiteral( final LiteralLabel lit ) {
		// LiteralLabelForMap objects are supposed to be used for this
		// datatype and the implementation of LiteralLabelForMap makes
		// sure that these are valid.
		if ( lit instanceof LiteralLabelForMap ) {
			return lit.isWellFormed();
		}

		// However, the given LiteralLabel may come from somewhere else,
		// in which case we have to check its validity as follows.

		final String dtURI = lit.getDatatypeURI();
		if ( dtURI == null || ! dtURI.equals(uri) ) {
			return false;
		}

		final String lang = lit.language();
		if ( lang != null && ! lang.isEmpty() ) {
			return false;
		}

		final String lex = lit.getLexicalForm();
		return isValid(lex);
	}

	@Override
	public boolean isValid( final String lexicalForm ) {
		try {
			// 'recursive ' must be false here because the validity check
			// is only for the literal with the given lexical form and not
			// for any possible CDT literals inside it
			ParserForCDTLiterals.parseMapLiteral(lexicalForm, false);
			return true;
		}
		catch ( final Exception ex ) {
			return false;
		}
	}

	@Override
	public Map<CDTKey,CDTValue> parse( final String lexicalForm ) throws DatatypeFormatException {
		final boolean recursive = false;
		try {
			return ParserForCDTLiterals.parseMapLiteral(lexicalForm, recursive);
		}
		catch ( final Exception ex ) {
			throw new DatatypeFormatException(lexicalForm, type, ex);
		}
	}

	@Override
	public String unparse( final Object value ) {
		if ( !(value instanceof Map<?,?>) ) {
			throw new IllegalArgumentException();
		}

		@SuppressWarnings("unchecked")
		final Map<CDTKey,CDTValue> map = (Map<CDTKey,CDTValue>) value;

		return unparseValue(map);
	}

	@Override
	public String unparseValue( final Map<CDTKey,CDTValue> map ) {
		final StringBuilder sb = new StringBuilder();
		sb.append("{");
		if ( ! map.isEmpty() ) {
			final Iterator<Map.Entry<CDTKey,CDTValue>> it = map.entrySet().iterator();

			final Map.Entry<CDTKey,CDTValue> firstEntry = it.next();
			unparseMapEntry(firstEntry, sb);

			while ( it.hasNext() ) {
				sb.append(", ");

				final Map.Entry<CDTKey,CDTValue> nextEntry = it.next();
				unparseMapEntry(nextEntry, sb);
			}
		}

		sb.append("}");
		return sb.toString();
	}

	protected void unparseMapEntry( final Map.Entry<CDTKey,CDTValue> entry, final StringBuilder sb ) {
		sb.append( entry.getKey().asLexicalForm() );
		sb.append(" : ");
		sb.append( entry.getValue().asLexicalForm() );
	}

	@Override
	public int getHashCode( final LiteralLabel lit ) {
		return lit.getDefaultHashcode();
	}

	@Override
	public boolean isEqual( final LiteralLabel value1, final LiteralLabel value2 ) {
		if ( ! isMapLiteral(value1) || ! isMapLiteral(value2) ) {
			return false;
		}

		final Map<CDTKey,CDTValue> map1 = getValue(value1);
		final Map<CDTKey,CDTValue> map2 = getValue(value2);

		return map1.equals(map2);
	}

	/**
	 * Returns true if the given node is a literal with {@link #uri}
	 * as its datatype URI. Notice that this does not mean that this
	 * literal is actually valid; for checking validity, use
	 * {@link #isValidLiteral(LiteralLabel)}.
	 */
	public static boolean isMapLiteral( final Node n ) {
		return n.isLiteral() && n.getLiteralDatatypeURI().equals(uri);
	}

	/**
	 * Returns true if the datatype URI of the given {@link LiteralLabel} is
	 * {@link #uri}. Notice that this does not mean that this LiteralLabel is
	 * actually valid; for checking validity, use {@link #isValidLiteral(LiteralLabel)}.
	 */
	public static boolean isMapLiteral( final LiteralLabel lit ) {
		return lit.getDatatypeURI().equals(uri);
	}

	/**
	 * Assumes that the datatype of the given literal is cdt:Map.
	 */
	public static Map<CDTKey,CDTValue> getValue( final LiteralLabel lit ) throws DatatypeFormatException {
		if ( lit instanceof LiteralLabelForMap ) {
			return ( (LiteralLabelForMap) lit ).getValue();
		}

		final Object value = lit.getValue();
		if ( value == null || ! (value instanceof Map<?,?>) ) {
			throw new IllegalArgumentException( lit.toString() + " - " + value );
		}

		@SuppressWarnings("unchecked")
		final Map<CDTKey,CDTValue> map = (Map<CDTKey,CDTValue>) value;
		return map;
	}

}
