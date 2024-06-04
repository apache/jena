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

import org.apache.jena.graph.Node;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.expr.ExprEvalException;

public abstract class CDTValue
{
	/**
	 * Returns true if this is a null value (in which
	 * case {@link #isNode()} must return false).
	 */
	public boolean isNull() {
		return false;
	}

	/**
	 * Returns true if this object is an RDF term (i.e., an IRI, a literal,
	 * or a blank node). In that case, {@link #asNode()} can be used to get
	 * a corresponding {@link Node} representation of this RDF term.
	 */
	public boolean isNode() {
		return false;
	}

	/**
	 * Returns this object as an RDF term (i.e., an IRI, a literal,
	 * or a blank node), assuming it is one. If it is not, then an
	 * {@link UnsupportedOperationException} is thrown.
	 */
	public Node asNode() {
		throw new UnsupportedOperationException( this + " is not an RDF term" );
	}

	@Override
	public String toString() {
		if ( isNode() )
			return asNode().toString();

		if ( isNull() )
			return "null";

		throw new IllegalArgumentException( "not null and not a Node (" + this.getClass().getName() + ")" );
	}

	@Override
	public int hashCode() {
		if ( isNode() )
			return asNode().hashCode();;

		if ( isNull() )
			return 1;

		throw new IllegalArgumentException( "not null and not a Node (" + this.getClass().getName() + ")" );
	}

	@Override
	public boolean equals( final Object other ) {
		try {
			return sameAs(other);
		}
		catch ( final ExprEvalException ex ) {
			return false;
		}
	}

	public boolean sameAs( final Object other ) throws ExprEvalException {
		if ( isNull() ) {
			throw new ExprEvalException("nulls cannot be compared");
		}

		if ( other instanceof CDTValue ) {
			final CDTValue otherValue = (CDTValue) other;

			if ( otherValue.isNull() ) {
				throw new ExprEvalException("nulls cannot be compared");
			}

			if ( isNode() ) {
				return otherValue.isNode() && asNode().sameValueAs( otherValue.asNode() );
			}

			throw new IllegalStateException( "unexpected type of CDTValue: " + this.getClass().getName() );
		}

		return false;
	}

	public final boolean sameAs( final CDTValue otherValue ) throws ExprEvalException {
		if ( isNull() ) {
			throw new ExprEvalException("nulls cannot be compared");
		}

		if ( otherValue.isNull() ) {
			throw new ExprEvalException("nulls cannot be compared");
		}

		if ( isNode() ) {
			return otherValue.isNode() && asNode().sameValueAs( otherValue.asNode() );
		}

		throw new IllegalStateException( "unexpected type of CDTValue: " + this.getClass().getName() );
	}

	public String asLexicalForm() {
		if ( isNull() ) {
			return "null";
		}

		if ( isNode() ) {
			final Node n = asNode();
			if ( CompositeDatatypeList.isListLiteral(n) ) {
				return n.getLiteralLexicalForm();
			}
			else if ( CompositeDatatypeMap.isMapLiteral(n) ) {
				return n.getLiteralLexicalForm();
			}
			else {
				return NodeFmtLib.strTTL(n);
			}
		}

		throw new IllegalStateException( "unexpected type of CDTValue: " + this.getClass().getName() );
	}

}
