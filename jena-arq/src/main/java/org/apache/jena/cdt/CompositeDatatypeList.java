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
	public final static String uri = "http://w3id.org/awslabs/neptune/SPARQL-CDTs/List";
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
			ParserForCDTLiterals.parseListLiteral(lexicalForm);
			return true;
		}
		catch ( final Exception ex ) {
			return false;
		}
	}

	@Override
	public List<CDTValue> parse( final String lexicalForm ) throws DatatypeFormatException {
		try {
			return ParserForCDTLiterals.parseListLiteral(lexicalForm);
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
		return lit.hashCode();
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
				if ( ! v1.isNull() || ! v2.isNull() ) {
					return false;
				}
			}
			else {
				final Node n1 = v1.asNode();
				final Node n2 = v2.asNode();

				if ( n1.isBlank() || n2.isBlank() ) {
					// If at least one of the two elements is a blank node,
					// throw an error unless both elements are *the same*
					// blank node.
					if ( ! n1.equals(n2) ) {
						throw new ExprEvalException("blank nodes in lists cannot be compared");
					}
				}

				if ( ! n1.sameValueAs(n2) ) {
					return false;
				}
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
		// Verify first that both literals are well-formed. If at least one of
		// them is not well-formed, then their relative order is undefined.
		if ( ! value1.isWellFormed() || ! value2.isWellFormed() )
			throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);

		final List<CDTValue> list1;
		final List<CDTValue> list2;
		try {
			list1 = getValue(value1);
			list2 = getValue(value2);
		}
		catch ( final Exception e ) {
			throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
		}

		// If at least one of the two lists is empty, then we can decide their
		// relative order without a pair-wise comparison of the list elements.
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
				final NodeValue nv1 = NodeValue.makeNode( elmt1.asNode() );
				final NodeValue nv2 = NodeValue.makeNode( elmt2.asNode() );

				if ( ! sortOrderingCompare && nv1.isBlank() && nv2.isBlank() ) {
					throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
				}

				// Compare the values represented by the two list elements
				// in terms of which of them is greater than the other one.
				try {
					final int c;
					if ( sortOrderingCompare )
						c = NodeValue.compareAlways(nv1, nv2);
					else
						c = NodeValue.compare(nv1, nv2);

					// If one of the two values is greater than the other one,
					// return this result as the result for comparing the two
					// lists.
					if ( c < 0 ) return Expr.CMP_LESS;
					if ( c > 0 ) return Expr.CMP_GREATER;
				}
				catch ( final Exception e ) {
					// If the comparison failed, simply move on to the
					// next code block (i.e., nothing to do here).
				}

				// If the comparison of nv1 and nv2 failed with an exception
				// or none of them is greater than the other, we test whether
				// they are the same value (i.e., comparing them using =
				// results in true).
				boolean sameValueTestResult = false;
				boolean sameValueTestFailed = false;
				try {
					sameValueTestResult = NodeValue.sameValueAs(nv1, nv2);
				}
				catch ( final Exception e ) {
					sameValueTestFailed = true;
				}

				// If nv1 and nv2 are not the same value or testing whether
				// they are the same value failed, we can terminate the whole
				// comparison (as being done in the following if block).
				// If, however, nv1 and nv2 are indeed the same value (in which
				// case the following if condition is not true), we continue to
				// the next pair of list elements.
				if ( sameValueTestFailed == true || sameValueTestResult == false ) {
					if ( sortOrderingCompare )
						return Expr.CMP_INDETERMINATE;
					else
						throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
				}
			}
			else {
				// This else-branch covers cases in which at least one of the
				// two list elements is null.
				if ( ! sortOrderingCompare ) {
					// When comparing as per the list-less-than semantics,
					// null cannot be compared to anything other than null.
					if ( ! elmt1.isNull() || ! elmt2.isNull() )
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
		final Object value = lit.getValue();
		if ( value == null || ! (value instanceof List<?>) ) {
			throw new IllegalArgumentException( lit.toString() + " - " + value );
		}

		@SuppressWarnings("unchecked")
		final List<CDTValue> list = (List<CDTValue>) value;
		return list;
	}

}
