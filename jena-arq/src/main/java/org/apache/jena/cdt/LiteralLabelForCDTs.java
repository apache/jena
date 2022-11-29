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
			lexicalForm = unparseValueForm(valueForm);
		}

		return lexicalForm;
	}

	protected abstract String unparseValueForm( T valueForm );

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

			valueForm = parseLexicalForm(lexicalForm);
		}

		return valueForm;
	}

	protected abstract T parseLexicalForm( String lex ) throws DatatypeFormatException;

	@Override
	public String getDatatypeURI() {
		return getDatatype().getURI();
	}

	@Override
	public boolean sameValueAs( final LiteralLabel other ) {
		if ( ! other.getDatatypeURI().equals(getDatatypeURI()) ) {
			return false;
		}

		if ( lexicalForm != null && other.getLexicalForm().equals(lexicalForm) ) {
			return true;
		}

		return getValue().equals( other.getValue() );
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
