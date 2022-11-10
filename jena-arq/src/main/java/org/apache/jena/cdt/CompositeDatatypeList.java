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
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.riot.out.NodeFmtLib;

public class CompositeDatatypeList extends CompositeDatatypeBase
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
		if ( elmt.isNode() )
			return NodeFmtLib.strTTL( elmt.asNode() );
		else if ( elmt.isList() )
			return unparseList( elmt.asList() );
		else if ( elmt.isMap() )
			return CompositeDatatypeMap.unparseMap( elmt.asMap() );
		else if ( elmt.isNull() )
			return "null";
		else
			throw new UnsupportedOperationException( "unexpected list element: " + elmt.getClass().getName() );
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

	@Override
	public boolean isEqual( final LiteralLabel value1, final LiteralLabel value2 ) {
		final List<CDTValue> list1 = getValue(value1);
		final List<CDTValue> list2 = getValue(value2);

		return list1.equals(list2);
	}

	public static List<CDTValue> getValue( final LiteralLabel lit ) {
		if ( lit instanceof LiteralLabelForList ) {
			return ( (LiteralLabelForList) lit ).getValue();
		}
		else {
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
