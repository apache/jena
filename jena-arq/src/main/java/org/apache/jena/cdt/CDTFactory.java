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

public class CDTFactory
{
	public static CDTKey createKey( final Node n ) {
		return new CDTKey() {
			@Override public boolean isNode() { return true; }
			@Override public Node asNode() { return n; }
		};
	}

	public static CDTValue createValue( final Node n ) {
		return new CDTValue() {
			@Override public boolean isNode() { return true; }
			@Override public Node asNode() { return n; }
		};
	}

	public static CDTValue createValue( final List<CDTValue> l ) {
		return new CDTValue() {
			@Override public boolean isList() { return true; }
			@Override public List<CDTValue> asList() { return l; }
		};
	}

	public static CDTValue createValue( final Map<CDTKey,CDTValue> m ) {
		return new CDTValue() {
			@Override public boolean isMap() { return true; }
			@Override public Map<CDTKey,CDTValue> asMap() { return m; }
		};
	}

	public static CDTValue getNullValue() {
		if ( nullValue == null ) {
			return new CDTValue() {
				@Override public boolean isNull() { return true; }
			};
		}

		return nullValue;
	}

	private static CDTValue nullValue = null;

}
