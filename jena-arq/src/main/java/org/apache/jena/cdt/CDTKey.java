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

public abstract class CDTKey
{
	/**
	 * Returns this object as an RDF term (i.e., an IRI, a literal,
	 * or a blank node), assuming it is one. If it is not, then an
	 * {@link UnsupportedOperationException} is thrown.
	 */
	public Node asNode() {
		throw new UnsupportedOperationException( this + " is not an RDF term" );
	}

	@Override
	public boolean equals( final Object other ) {
		if ( other instanceof CDTKey ) {
			final CDTKey otherKey = (CDTKey) other;

			final Node n1 = asNode();
			final Node n2 = otherKey.asNode();
			return n1.equals(n2);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return asNode().hashCode();
	}

	@Override
	public String toString() {
		return asNode().toString();
	}

	/**
	 * Returns a string representation of this element
	 * to be included in the lexical form of literals.
	 */
	public String asLexicalForm() {
		return NodeFmtLib.strTTL( asNode() );
	}

}
