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

package org.apache.jena.propertytable.graph;

import org.apache.jena.propertytable.PropertyTable;
import org.apache.jena.propertytable.impl.PropertyTableBuilder;


/**
 * GraphCSV is a sub class of GraphPropertyTable aiming at CSV data.
 * Its constructor takes a CSV file path as the parameter, parse the file using a CSV Parser,
 * and makes a PropertyTable through PropertyTableBuilder.
 *
 */
@Deprecated
public class GraphCSV extends GraphPropertyTable {
	
	public static GraphCSV createHashMapImpl( String csvFilePath ){
		return new GraphCSVHashMapImpl(csvFilePath);
	}
	
	public static GraphCSV createArrayImpl( String csvFilePath ){
		return new GraphCSVArrayImpl(csvFilePath);
	}
	
	protected GraphCSV (PropertyTable table) {
		super(table);
	}
	
	// use the Java array implementation of PropertyTable for default
	public GraphCSV ( String csvFilePath ){
		super(PropertyTableBuilder.buildPropetyTableArrayImplFromCsv(csvFilePath));
	}
}


class GraphCSVHashMapImpl extends GraphCSV{
	protected GraphCSVHashMapImpl(String csvFilePath){
		super(PropertyTableBuilder.buildPropetyTableHashMapImplFromCsv(csvFilePath));
	}
}

class GraphCSVArrayImpl extends GraphCSV{
	protected GraphCSVArrayImpl(String csvFilePath){
		super(PropertyTableBuilder.buildPropetyTableArrayImplFromCsv(csvFilePath));
	}
}
