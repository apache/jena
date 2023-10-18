/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.cdt;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.NodeValue;

public class CompositeDatatypeList extends CompositeDatatypeBase<List<CDTValue>>
{
	public final static String uri = "http://example.org/cdt/List";
	public final static CompositeDatatypeList type = new CompositeDatatypeList();

	protected CompositeDatatypeList() {}

	@Override
	public String getURI() {
		return uri;
	}

	@Override
	public boolean isValidValue( final Object value ) {
		if ( !(value instanceof List<?>) ) {
			return false;
		}

		final List<?> l = (List<?>) value;
		for ( final Object e : l ) {
			if ( !(e instanceof CDTValue) ) {
				return false;
			}
		}

		return true; 
	}

	@Override
	public boolean isValidLiteral( final LiteralLabel lit ) {
		// LiteralLabelForList objects are supposed to be used for this
		// datatype and the implementation of LiteralLabelForList makes
		// sure that these are valid.
		if ( lit instanceof LiteralLabelForList ) {
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
			// 'recursive' must be false here because the validity check
			// is only for the literal with the given lexical form and not
			// for any possible CDT literals inside it
			ParserForCDTLiterals.parseListLiteral(lexicalForm, false);
			return true;
		}
		catch ( final Exception ex ) {
			return false;
		}
	}

	@Override
	public List<CDTValue> parse( final String lexicalForm ) throws DatatypeFormatException {
		final boolean recursive = false;
		try {
			return ParserForCDTLiterals.parseListLiteral(lexicalForm, recursive);
		}
		catch ( final Exception ex ) {
			throw new DatatypeFormatException(lexicalForm, type, ex);
		}
	}

	@Override
	public String unparse( final Object value ) {
		if ( !(value instanceof List<?>) ) {
			throw new IllegalArgumentException();
		}

		@SuppressWarnings("unchecked")
		final List<CDTValue> list = (List<CDTValue>) value;

		return unparseValue(list);
	}

	@Override
	public String unparseValue( final List<CDTValue> list ) {
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		if ( ! list.isEmpty() ) {
			final Iterator<CDTValue> it = list.iterator();
			final CDTValue firstElmt = it.next();
			final String firstElmtAsString = unparseListElement(firstElmt);
			sb.append(firstElmtAsString);
			while ( it.hasNext() ) {
				final CDTValue nextElmt = it.next();
				final String nextElmtAsString = unparseListElement(nextElmt);
				sb.append(", ");
				sb.append(nextElmtAsString);
			}
		}
		sb.append("]");
		return sb.toString();
	}

	protected String unparseListElement( final CDTValue elmt ) {
		return elmt.asLexicalForm();
	}

	@Override
	public int getHashCode( final LiteralLabel lit ) {
		return lit.getDefaultHashcode();
	}

	@Override
	public boolean isEqual( final LiteralLabel value1, final LiteralLabel value2 ) {
		if ( ! isListLiteral(value1) || ! isListLiteral(value2) ) {
			return false;
		}

		final List<CDTValue> list1 = getValue(value1);
		final List<CDTValue> list2 = getValue(value2);

		if ( list1.size() != list2.size() ) return false;
		if ( list1.isEmpty() ) return true;

		final Iterator<CDTValue> it1 = list1.iterator();
		final Iterator<CDTValue> it2 = list2.iterator();
		while ( it1.hasNext() ) {
			final CDTValue v1 = it1.next();
			final CDTValue v2 = it2.next();

			if ( v1.isNull() || v2.isNull() ) {
				throw new ExprEvalException("nulls in lists cannot be compared");
			}

			final Node n1 = v1.asNode();
			final Node n2 = v2.asNode();

			if ( n1.isBlank() || n2.isBlank() ) {
				throw new ExprEvalException("blank nodes in lists cannot be compared");
			}

			if ( ! n1.sameValueAs(n2) ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Assumes that the datatype of both of the given literals is cdt:List.
	 * If 'sortOrderingCompare' is true, the two lists are compared as per the
	 * semantics for ORDER BY. If 'sortOrderingCompare' is false, the comparison
	 * applies the list-less-than semantics.
	 */
	public static int compare( final LiteralLabel value1, final LiteralLabel value2, final boolean sortOrderingCompare ) throws ExprNotComparableException {
		List<CDTValue> list1 = null;
		try {
			list1 = getValue(value1);
		}
		catch ( final Exception e ) {
			// do nothing at this point, we will check for errors next
		}

		List<CDTValue> list2 = null;
		try {
			list2 = getValue(value2);
		}
		catch ( final Exception e ) {
			// do nothing at this point, we will check for errors next
		}

		// handling errors / ill-formed literals now
		if ( list1 == null || list2 == null ) {
			if ( ! sortOrderingCompare ) {
				// If comparing as per the list-less-than semantics,
				// both literals must be well-formed.
				throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
			}
			else {
				// If comparing as per the ORDER BY semantics, the
				// ill-formed one of the two literals is order higher.
				if ( list1 != null ) return Expr.CMP_LESS;
				if ( list2 != null ) return Expr.CMP_GREATER;

				// If both literals are ill-formed, the order
				// is determined based on the lexical forms.
				return compareByLexicalForms(value1, value2);
			}
		}

		// If at least one of the two lists is empty, we can decide
		// without a pair-wise comparison of the list elements.
		if ( list1.isEmpty() || list2.isEmpty() ) {
			// The literal with the non-empty list is greater
			// than the literal with the empty list.
			if ( ! list1.isEmpty() ) return Expr.CMP_GREATER;
			if ( ! list2.isEmpty() ) return Expr.CMP_LESS;

			// If both lists are empty, under the ORDER BY semantics we then
			// decide the order based on the lexical forms, and under the
			// list-less-than semantics, both literals are considered equal.
			if ( sortOrderingCompare )
				return compareByLexicalForms(value1, value2);
			else
				return Expr.CMP_EQUAL;
		}

		// Now we go into the pair-wise comparison of the list elements.
		final int n = Math.min( list1.size(), list2.size() );
		for ( int i = 0; i < n; i++ ) {
			final CDTValue elmt1 = list1.get(i);
			final CDTValue elmt2 = list2.get(i);

			if ( ! elmt1.isNull() && ! elmt2.isNull() ) {
				final Node n1 = elmt1.asNode();
				final Node n2 = elmt2.asNode();

				if ( ! sortOrderingCompare && n1.isBlank() && n2.isBlank() ) {
					throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
				}

				// Test whether the two RDF terms are the same value (i.e.,
				// comparing them using = results in true). If they are, we
				// can skip to the next pair of list elements. If they are
				// not, we compare them.
				if ( ! elmt1.sameAs(elmt2) ) {
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
				// two list elements is null.
				if ( ! sortOrderingCompare ) {
					// When comparing as per the list-less-than semantics,
					// null values cannot be compared to one another.
					throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
				}
				else {
					// When comparing as per the ORDER BY semantics, nulls are
					// ordered the same. Hence, if both elements are null, we
					// don't do anything here and simply advance to the next
					// pair of elements.
					// However, if only one of the two elements is null, then
					// the literal with this element is ordered lower than the
					// other literal.
					if ( elmt1.isNull() && ! elmt2.isNull() )
						return Expr.CMP_LESS;

					if ( elmt2.isNull() && ! elmt1.isNull() )
						return Expr.CMP_GREATER;
				}
			}
		}

		// At this point, the pair-wise comparison of list elements has not
		// led to any decision. Now, we decide based on the size of the lists.
		final int sizeDiff = list1.size() - list2.size();
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
	public static boolean isListLiteral( final Node n ) {
		return n.isLiteral() && n.getLiteralDatatypeURI().equals(uri);
	}

	/**
	 * Returns true if the datatype URI of the given literal is {@link #uri}.
	 * Notice that this does not mean that this literal is actually valid;
	 * for checking validity, use {@link #isValidLiteral(LiteralLabel)}.
	 */
	public static boolean isListLiteral( final LiteralLabel lit ) {
		return lit.getDatatypeURI().equals(uri);
	}

	/**
	 * Assumes that the datatype of the given literal is cdt:List.
	 */
	public static List<CDTValue> getValue( final LiteralLabel lit ) throws DatatypeFormatException {
		if ( lit instanceof LiteralLabelForList ) {
			return ( (LiteralLabelForList) lit ).getValue();
		}

		final Object value = lit.getValue();
		if ( value == null || ! (value instanceof List<?>) ) {
			throw new IllegalArgumentException( lit.toString() + " - " + value );
		}

		@SuppressWarnings("unchecked")
		final List<CDTValue> list = (List<CDTValue>) value;
		return list;
	}

}
