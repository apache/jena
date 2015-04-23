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

package org.apache.jena.propertytable;

import java.util.Collection;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;

public abstract class BaseTest {
	protected PropertyTable table;
	protected PropertyTable table2;
	protected Row row;
	private static final String ns = "eh:foo/bar#";
	protected static final Node rowSubject = URI("rowSubject");
	protected static final String csvFilePath = "src/test/resources/test.csv";
	
	
	protected static Node URI(String localName) {
		return NodeFactory.createURI(ns + localName);
	}
	
	protected static boolean collectionContains(
			final Collection<Column> columns, final Node columnkey) {
		for (final Column column : columns) {
			if (column.getColumnKey().equals(columnkey))
				return true;
		}
		return false;
	}
}
