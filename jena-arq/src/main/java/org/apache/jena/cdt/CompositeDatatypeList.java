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
		return unparseList(list);
	}

	public static String unparseList( final List<CDTValue> list ) {
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

	public static String unparseListElement( final CDTValue elmt ) {
		return elmt.asLexicalForm();
	}

	@Override
	public List<CDTValue> parse( final String lexicalForm ) throws DatatypeFormatException {
		return parseList(lexicalForm);
	}

	public static List<CDTValue> parseList( final String lexicalForm ) throws DatatypeFormatException {
		final boolean recursive = false;
		try {
			return ParserForCDTLiterals.parseListLiteral(lexicalForm, recursive);
		}
		catch ( final Exception ex ) {
			throw new DatatypeFormatException(lexicalForm, type, ex);
		}
	}

	@Override
	public boolean isValid( final String lexicalForm ) {
		final boolean recursive = false;
		try {
			ParserForCDTLiterals.parseListLiteral(lexicalForm, recursive);
		}
		catch ( final Exception ex ) {
			return false;
		}
		return true;
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
			return true;
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
	 * Returns true if the datatype URI of the given {@link LiteralLabel} is
	 * {@link #uri}. Notice that this does not mean that this LiteralLabel is
	 * actually valid; for checking validity, use {@link #isValidLiteral(LiteralLabel)}.
	 */
	public static boolean isListLiteral( final LiteralLabel lit ) {
		return lit.getDatatypeURI().equals(uri);
	}

	@Override
	public boolean isEqual( final LiteralLabel value1, final LiteralLabel value2 ) {
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

	public int compare( final LiteralLabel value1, final LiteralLabel value2 ) throws ExprNotComparableException {
		final List<CDTValue> list1;
		final List<CDTValue> list2;
		try {
			list1 = getValue(value1);
			list2 = getValue(value2);
		}
		catch ( final DatatypeFormatException e ) {
			throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
		}

		if ( list1.isEmpty() && list2.isEmpty() ) return 0;
		if ( list1.isEmpty() && ! list2.isEmpty() ) return -1;
		if ( list2.isEmpty() && ! list1.isEmpty() ) return 1;

		final int n = Math.min( list1.size(), list2.size() );
		for ( int i = 0; i < n; i++ ) {
			final CDTValue elmt1 = list1.get(i);
			final CDTValue elmt2 = list2.get(i);

			if ( elmt1.isNull() || elmt2.isNull() ) {
				throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
			}

			final Node n1 = elmt1.asNode();
			final Node n2 = elmt2.asNode();

			if ( n1.isBlank() && n2.isBlank() ) {
				throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
			}

			if ( ! elmt1.sameAs(elmt2) ) {
				final NodeValue nv1 = NodeValue.makeNode(n1);
				final NodeValue nv2 = NodeValue.makeNode(n2);

				final int c;
				try {
					c = NodeValue.compare(nv1, nv2);
				}
				catch ( final Exception e ) {
					throw new ExprNotComparableException("Can't compare "+value1+" and "+value2);
				}

				if ( c != 0 ) return c;
			}
		}

		return ( list1.size() - list2.size() );
	}

	@SuppressWarnings("unchecked")
	public static List<CDTValue> getValue( final LiteralLabel lit ) throws DatatypeFormatException {
		if ( lit instanceof LiteralLabelForList ) {
			return ( (LiteralLabelForList) lit ).getValue();
		}
		else {
			final Object value = lit.getValue();
			//if ( value != null && value instanceof List<?> )
				//return (List<CDTValue>) value;

			final String lex = lit.getLexicalForm();
			return parseList(lex);
		}
	}

	@Override
	public int getHashCode( final LiteralLabel lit ) {
		if ( lit instanceof LiteralLabelForList ) {
			return lit.hashCode();
		}
		else {
			return lit.getLexicalForm().hashCode();
		}
	}

}
