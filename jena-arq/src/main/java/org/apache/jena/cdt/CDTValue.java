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

import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.sparql.expr.NodeValue;

public abstract class CDTValue extends CDTKey
{
	/**
	 * Returns true if this object is a list. In that case, {@link #asList()}
	 * can be used to get it as an actual {@link List} object.
	 */
	public boolean isList() {
		return false;
	}

	/**
	 * Returns true if this object is a map. In that case, {@link #asMap()}
	 * can be used to get it as an actual {@link Map} object.
	 */
	public boolean isMap() {
		return false;
	}

	/**
	 * Returns true if this is a null value.
	 */
	public boolean isNull() {
		return false;
	}

	/**
	 * Returns this object as a list, assuming it is one. If it is not,
	 * then an {@link UnsupportedOperationException} is thrown.
	 */
	public List<CDTValue> asList() {
		throw new UnsupportedOperationException( this + " is not a list" );
	}

	/**
	 * Returns this object as a map, assuming it is one. If it is not,
	 * then an {@link UnsupportedOperationException} is thrown.
	 */
	public Map<CDTKey,CDTValue> asMap() {
		throw new UnsupportedOperationException( this + " is not a map" );
	}

	@Override
	public final boolean equals( final Object other ) {
		if ( isNull() ) {
			return false;
		}

		if ( !(other instanceof CDTValue) ) {
			if ( other instanceof CDTKey && isNode() ) {
				final CDTKey otherKey = (CDTKey) other;
				return asNode().equals( otherKey.asNode() );
			}
			else {
				return false;
			}
		}

		final CDTValue otherValue = (CDTValue) other;

		if ( otherValue.isNull() ) return false;

		if ( isNode() ) return isEqual( asNode(), otherValue );

		if ( isList() ) return isEqual( asList(), otherValue );

		if ( isMap() ) return isEqual( asMap(), otherValue );

		throw new IllegalStateException( "unexpected type of CDTValue: " + this.getClass().getName() );
	}

	public static boolean isEqual( final Node n1, final CDTValue v2 ) {
		if ( CompositeDatatypeList.isListLiteral(n1) ) {
			final LiteralLabel lit1 = n1.getLiteral();

			if ( v2.isNode() && CompositeDatatypeList.isListLiteral(v2.asNode()) ) {
				final LiteralLabel lit2 = v2.asNode().getLiteral();
				return CompositeDatatypeList.type.isEqual(lit1, lit2);
			}

			if ( v2.isList() ) {
				return CompositeDatatypeList.getValue(lit1).equals( v2.asList() );
			}

			return false;
		}

		if ( CompositeDatatypeMap.isMapLiteral(n1) ) {
			final LiteralLabel lit1 = n1.getLiteral();

			if ( v2.isNode() && CompositeDatatypeMap.isMapLiteral(v2.asNode()) ) {
				final LiteralLabel lit2 = v2.asNode().getLiteral();
				return CompositeDatatypeList.type.isEqual(lit1, lit2);
			}

			if ( v2.isMap() ) {
				return CompositeDatatypeMap.getValue(lit1).equals( v2.asMap() );
			}

			return false;
		}

		if ( v2.isNode() ) {
			final NodeValue nv1 = NodeValue.makeNode(n1);
			final NodeValue nv2 = NodeValue.makeNode( v2.asNode() );
			return NodeValue.sameAs(nv1, nv2);
		}

		return false;
	}

	public static boolean isEqual( final List<CDTValue> list1, final CDTValue v2 ) {
		if ( v2.isNode() && CompositeDatatypeList.isListLiteral(v2.asNode()) ) {
			final LiteralLabel lit2 = v2.asNode().getLiteral();
			return CompositeDatatypeList.getValue(lit2).equals(list1);
		}

		if ( v2.isList() ) {
			return v2.asList().equals(list1);
		}

		return false;
	}

	public static boolean isEqual( final Map<CDTKey,CDTValue> map1, final CDTValue v2 ) {
		if ( v2.isNode() && CompositeDatatypeMap.isMapLiteral(v2.asNode()) ) {
			final LiteralLabel lit2 = v2.asNode().getLiteral();
			return CompositeDatatypeMap.getValue(lit2).equals(map1);
		}

		if ( v2.isMap() ) {
			return v2.asMap().equals(map1);
		}

		return false;
	}

}
