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
		if ( !(other instanceof CDTValue) ) {
			if ( other instanceof CDTKey && isNode() ) {
				final CDTKey otherKey = (CDTKey) other;
				return asNode().equals( otherKey.asNode() );
			}
			else {
				return false;
			}
		}

		if ( isNull() ) {
			return false;
		}

		final CDTValue otherValue = (CDTValue) other;

		if ( otherValue.isNull() ) {
			return false;
		}

		if ( isNode() && otherValue.isNode() ) {
			return asNode().equals( otherValue.asNode() );
		}
		else if ( isList() && otherValue.isList() ) {
			return asList().equals( otherValue.asList() );
		}
		else if ( isMap() && otherValue.isMap() ) {
			return asMap().equals( otherValue.asMap() );
		}
		else {
			return false;
		}
	}
}
