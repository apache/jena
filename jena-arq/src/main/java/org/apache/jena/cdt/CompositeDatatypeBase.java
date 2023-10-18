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

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.Expr;

public abstract class CompositeDatatypeBase<T> implements RDFDatatype
{
	@Override
	public Class<?> getJavaClass() {
		return null;
	}

	@Override
	public Object cannonicalise( final Object value ) {
		return value;
	}

	@Override
	public Object extendedTypeDefinition() {
		return null;
	}

	@Override
	public RDFDatatype normalizeSubType( final Object value, final RDFDatatype dt ) {
		return this;
	}

	@Override
	public abstract T parse( final String lexicalForm ) throws DatatypeFormatException;

	public abstract String unparseValue( final T value );

	// helper for the compare function in each of the subclasses
	protected static int compareByLexicalForms( final LiteralLabel value1, final LiteralLabel value2 ) {
		final int lexCmp = value1.getLexicalForm().compareTo( value2.getLexicalForm() );
		if ( lexCmp < 0 ) return Expr.CMP_LESS;
		if ( lexCmp > 0 ) return Expr.CMP_GREATER;
		return Expr.CMP_EQUAL;
	}
}
