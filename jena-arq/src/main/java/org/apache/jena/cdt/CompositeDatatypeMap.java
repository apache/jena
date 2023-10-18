package org.apache.jena.cdt;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.NodeValue;

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
	public boolean isEqual( final LiteralLabel lit1, final LiteralLabel lit2 ) {
		if ( ! isMapLiteral(lit1) || ! isMapLiteral(lit2) ) {
			return false;
		}

		final Map<CDTKey,CDTValue> map1 = getValue(lit1);
		final Map<CDTKey,CDTValue> map2 = getValue(lit2);

		if ( map1.size() != map2.size() ) return false;

		for ( final Map.Entry<CDTKey,CDTValue> entry1 : map1.entrySet() ) {
			final CDTValue v1 = entry1.getValue();
			final CDTValue v2 = map2.get( entry1.getKey() );
			if ( v2 == null ) return false;

			if ( v1.isNull() || v2.isNull() ) {
				throw new ExprEvalException("nulls in maps cannot be compared");
			}

			final Node n1 = v1.asNode();
			final Node n2 = v2.asNode();

			if ( n1.isBlank() || n2.isBlank() ) {
				throw new ExprEvalException("blank nodes in maps cannot be compared");
			}

			if ( ! n1.sameValueAs(n2) ) return false;
		}

		return true;
	}

	/**
	 * Assumes that the datatype of both of the given literals is cdt:Map.
	 *
	 * If 'sortOrderingCompare' is true, the two maps are compared as per the
	 * semantics for ORDER BY. If 'sortOrderingCompare' is false, the comparison
	 * applies the map-less-than semantics.
	 */
	public static int compare( final LiteralLabel value1, final LiteralLabel value2, final boolean sortOrderingCompare ) throws ExprNotComparableException {
		Map<CDTKey,CDTValue> map1 = null;
		try {
			map1 = getValue(value1);
		}
		catch ( final Exception e ) {
			// do nothing at this point, we will check for errors next
		}

		Map<CDTKey,CDTValue> map2 = null;
		try {
			map2 = getValue(value2);
		}
		catch ( final Exception e ) {
			// do nothing at this point, we will check for errors next
		}

		// handling errors / ill-formed literals now
		if ( map1 == null || map2 == null ) {
			if ( ! sortOrderingCompare ) {
				// If comparing as per the map-less-than semantics,
				// both literals must be well-formed.
				throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
			}
			else {
				// If comparing as per the ORDER BY semantics, the
				// ill-formed one of the two literals is order higher.
				if ( map1 != null ) return Expr.CMP_LESS;
				if ( map2 != null ) return Expr.CMP_GREATER;

				// If both literals are ill-formed, the order
				// is determined based on the lexical forms.
				return compareByLexicalForms(value1, value2);
			}
		}

		// If at least one of the two maps is empty, we can decide
		// without a pair-wise comparison of the map entries.
		if ( map1.isEmpty() || map2.isEmpty() ) {
			// The literal with the non-empty map is greater
			// than the literal with the empty map.
			if ( ! map1.isEmpty() ) return Expr.CMP_GREATER;
			if ( ! map2.isEmpty() ) return Expr.CMP_LESS;

			// If both maps are empty, under the ORDER BY semantics we then
			// decide the order based on the lexical forms, and under the
			// map-less-than semantics, both literals are considered equal.
			if ( sortOrderingCompare )
				return compareByLexicalForms(value1, value2);
			else
				return Expr.CMP_EQUAL;
		}

		// Now we go into the pair-wise comparison of the map entries.
		// To this end, we first need to sort the map entries.
		final CDTKeySorter keySorter = new CDTKeySorter();
		final TreeSet<CDTKey> sortedKeys1 = new TreeSet<>(keySorter);
		final TreeSet<CDTKey> sortedKeys2 = new TreeSet<>(keySorter);
		sortedKeys1.addAll( map1.keySet() );
		sortedKeys2.addAll( map2.keySet() );

		// Now we can perform the pair-wise comparison of the map entries.
		final Iterator<CDTKey> it1 = sortedKeys1.iterator();
		final Iterator<CDTKey> it2 = sortedKeys2.iterator();
		final int n = Math.min( sortedKeys1.size(), sortedKeys2.size() );
		for ( int i = 0; i < n; i++ ) {
			final CDTKey k1 = it1.next();
			final CDTKey k2 = it2.next();

			final int kCmp = keySorter.compare(k1, k2);
			if ( kCmp < 0 ) return Expr.CMP_LESS;
			if ( kCmp > 0 ) return Expr.CMP_GREATER;

			final CDTValue v1 = map1.get(k1);
			final CDTValue v2 = map2.get(k2);
			if ( ! v1.isNull() && ! v2.isNull() ) {
				final Node n1 = v1.asNode();
				final Node n2 = v2.asNode();

				// Test whether the two RDF terms are the same value (i.e.,
				// comparing them using = results in true). If they are, we
				// can skip to the next pair of list elements. If they are
				// not, we compare them.
				if ( ! n1.sameValueAs(n2) ) {
					final NodeValue nv1 = NodeValue.makeNode(n1);
					final NodeValue nv2 = NodeValue.makeNode(n2);

					final int c;
					try {
						if ( sortOrderingCompare )
							c = NodeValue.compareAlways(nv1, nv2);
						else
							c = NodeValue.compare(nv1, nv2);
					}
					catch ( final Exception e ) {
						throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
					}

					if ( c < 0 ) return Expr.CMP_LESS;
					if ( c > 0 ) return Expr.CMP_GREATER;

					if ( c == 0 && sortOrderingCompare ) return Expr.CMP_INDETERMINATE;
				}
			}
			else {
				// This else-branch covers cases in which at least one of the
				// two map entries has null as its value.
				if ( ! sortOrderingCompare ) {
					// When comparing as per the map-less-than semantics,
					// null values cannot be compared to one another.
					throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
				}
				else {
					// When comparing as per the ORDER BY semantics, nulls are
					// ordered the same. Hence, if both map entries have null
					// as their value, we don't do anything here and simply
					// advance to the next pair of map entries.
					// However, if the map value of only one of the two map
					// entries is null, then the literal with this map entry
					// is ordered lower than the other literal.
					if ( v1.isNull() && ! v2.isNull() )
						return Expr.CMP_LESS;

					if ( v2.isNull() && ! v1.isNull() )
						return Expr.CMP_GREATER;
				}
			}
		}

		// At this point, the pair-wise comparison of map entries has not led
		// to any decision. Now, we decide based on the size of the maps.
		final int sizeDiff = map1.size() - map2.size();
		if ( sizeDiff < 0 ) return Expr.CMP_LESS;
		if ( sizeDiff > 0 ) return Expr.CMP_GREATER;

		if ( sortOrderingCompare )
			return compareByLexicalForms(value1, value2);
		else
			return Expr.CMP_EQUAL;
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

	protected static class CDTKeySorter implements Comparator<CDTKey> {
		@Override
		public int compare( final CDTKey k1, final CDTKey k2 ) {
			final Node n1 = k1.asNode();
			final Node n2 = k2.asNode();

			if ( n1.isURI() ) {
				if ( ! n2.isURI() ) {
					return Expr.CMP_LESS;
				}

				final String uri1 = n1.getURI();
				final String uri2 = n2.getURI();
				return uri1.compareTo(uri2);
			}
			else if ( n2.isURI() ) {
				return Expr.CMP_GREATER;
			}

			// at this point, both RDF terms (n1 and n2) should be literals
			final String dt1 = n1.getLiteralDatatypeURI();
			final String dt2 = n2.getLiteralDatatypeURI();
			final int dtCmp = dt1.compareTo(dt2);
			if ( dtCmp != 0 ) {
				return dtCmp;
			}

			final String lex1 = n1.getLiteralLexicalForm();
			final String lex2 = n2.getLiteralLexicalForm();
			final int lexCmp = lex1.compareTo(lex2);
			if ( lexCmp != 0 || ! RDFLangString.rdfLangString.getURI().equals(dt1) ) {
				return lexCmp;
			}

			// at this point, both literals are rdf:LangString literals with the same value
			final String lang1 = n1.getLiteralLanguage();
			final String lang2 = n2.getLiteralLanguage();
			return lang1.compareTo(lang2);
		}
	}

}
