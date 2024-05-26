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

import java.util.Map;

import org.apache.jena.graph.impl.LiteralLabel;

public class LiteralLabelForMap extends LiteralLabelForCDTs<Map<CDTKey,CDTValue>>
{
	public LiteralLabelForMap( final Map<CDTKey,CDTValue> valueForm ) {
		super(valueForm);
	}

	public LiteralLabelForMap( final String lexicalForm ) {
		super(lexicalForm);
	}

	public LiteralLabelForMap( final String lexicalForm, final boolean checkedAndIdentifiedAsIllformed ) {
		super(lexicalForm, checkedAndIdentifiedAsIllformed );
	}

	/**
	 * Use this constructor only if you have made sure that the given lexical
	 * form parses indeed into the given value form, which implicitly also
	 * means that the given lexical form is well formed.
	 */
	public LiteralLabelForMap( final String lexicalForm, final Map<CDTKey,CDTValue> valueForm ) {
		super(lexicalForm, valueForm);
	}

	@Override
	public CompositeDatatypeMap getDatatype() {
		return CompositeDatatypeMap.type;
	}

	@Override
	public boolean sameValueAs( final LiteralLabel other ) {
		if ( other instanceof LiteralLabelForMap ) {
			final LiteralLabelForMap otherMapLiteral = (LiteralLabelForMap) other;
			// If the lexical forms of both are equivalent (and we
			// actually have the lexical forms of both; i.e., we do
			// not need to materialize them just for this check), then
			// the values are trivially equivalent.
			if (    isLexicalFormSet()
			     && otherMapLiteral.isLexicalFormSet()
			     && getLexicalForm().equals(otherMapLiteral.getLexicalForm()) ) {
				return true;
			}

			// Otherwise, we compare the actual lists that they represent.
			return getValue().equals( otherMapLiteral.getValue() );
		}

		return super.sameValueAs(other);
	}

}
