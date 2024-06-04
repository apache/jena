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

import java.util.Objects;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.rdf.model.impl.Util;

public abstract class LiteralLabelForCDTs<T> implements LiteralLabel
{
	protected final int hash;

	private T valueForm;
	private String lexicalForm;
	private boolean lexicalFormTested;

	public LiteralLabelForCDTs( final T valueForm ) {
		this.valueForm = valueForm;
		this.lexicalForm = null;

		this.lexicalFormTested = true;
		this.hash = valueForm.hashCode();
	}

	public LiteralLabelForCDTs( final String lexicalForm ) {
		this.valueForm = null;
		this.lexicalForm = lexicalForm;

		this.lexicalFormTested = false;
		this.hash = lexicalForm.hashCode();
	}

	@Override
	public abstract CompositeDatatypeBase<T> getDatatype();

	@Override
	public String getDatatypeURI() {
		return getDatatype().getURI();
	}

	@Override
	public boolean isXML() {
		return false;
	}

	@Override
	public boolean isWellFormed() {
		if ( lexicalFormTested ) {
			return valueForm != null;
		}

		try {
			getValue();
		}
		catch ( final DatatypeFormatException ex ) {
			return false;
		}

		return true;
	}

	@Override
	public boolean isWellFormedRaw() {
		return isWellFormed();
	}

	@Override
	public String toString( final boolean quoting ) {
		final StringBuilder b = new StringBuilder();

		if ( quoting ) b.append('"');

		String lex = getLexicalForm();
		lex = Util.replace(lex, "\"", "\\\"");
		b.append(lex);

		if ( quoting ) b.append('"');

		b.append("^^").append( getDatatypeURI() );

		return b.toString();
	}

	@Override
	public String toString() {
		return toString(false);
	}

	@Override
	public String getLexicalForm() {
		if ( lexicalForm == null ) {
			lexicalForm = getDatatype().unparseValue(valueForm);
		}

		return lexicalForm;
	}

	protected boolean isLexicalFormSet() {
		return lexicalForm != null;
	}

	protected boolean isValueFormSet() {
		return valueForm != null;
	}

	@Override
	public Object getIndexingValue() {
		return getLexicalForm();
	}

	@Override
	public String language() {
		return "";
	}

	@Override
	public T getValue() throws DatatypeFormatException {
		if ( valueForm == null && ! lexicalFormTested ) {
			lexicalFormTested = true;

			// The following function may fail with a DatatypeFormatException,
			// in which case 'valueForm' will remain being 'null' but we won't
			// try again at the next call of 'getValue()' because now we have
			// set 'lexicalFormTested'.

			valueForm = getDatatype().parse(lexicalForm);
		}

		return valueForm;
	}

	@Override
	public boolean sameValueAs( final LiteralLabel other ) {
		return getDatatype().isEqual(this, other);
	}

	@Override
	public boolean equals( final Object other ) {
		if ( this == other ) return true;

		if ( other == null || !(other instanceof LiteralLabel) ) return false;

		final LiteralLabel otherLiteral = (LiteralLabel) other;

		final String otherLang = otherLiteral.language();
		if ( otherLang != null && ! otherLang.isEmpty() ) return false;

		final String otherDTypeURI = otherLiteral.getDatatypeURI();
		if ( ! Objects.equals(getDatatypeURI(), otherDTypeURI) ) return false;

		return Objects.equals( getLexicalForm(), otherLiteral.getLexicalForm() );
	}

	@Override
	public int getDefaultHashcode() {
		return hash;
	}

	@Override
	public int hashCode() {
		return hash;
	}

}
