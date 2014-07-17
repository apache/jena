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

package com.hp.hpl.jena.propertytable.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.csv.CSVParser;
import org.apache.jena.riot.lang.LangCSV;
import org.apache.jena.riot.system.IRIResolver;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.propertytable.PropertyTable;
import com.hp.hpl.jena.propertytable.Row;

public class PropertyTableBuilder {

	public static Node CSV_ROW_NODE = NodeFactory.createURI(LangCSV.CSV_ROW);
	
	public static PropertyTable buildPropetyTableHashMapImplFromCsv(String csvFilePath) {		
		PropertyTable table = new PropertyTableHashMapImpl();
		return fillPropertyTable(table, csvFilePath);
	}
	
	public static PropertyTable buildPropetyTableArrayImplFromCsv(String csvFilePath) {
		PropertyTable table = createEmptyPropertyTableArrayImpl(csvFilePath);
		return fillPropertyTable(table, csvFilePath);
	}
	
	private static PropertyTable createEmptyPropertyTableArrayImpl (String csvFilePath) {
		CSVParser parser = CSVParser.create(csvFilePath);
		List<String> rowLine = null;
		int rowNum = 0;
		int columnNum = 0;
		
		while ((rowLine = parser.parse1()) != null) {
			if (rowNum == 0) {
				columnNum = rowLine.size();
			}
			rowNum++;
		}
		if (rowNum!=0 && columnNum!=0){
			return new PropertyTableArrayImpl(rowNum, columnNum+1);
		} else {
			return null;
		}
	}

	
	private static PropertyTable fillPropertyTable(PropertyTable table, String csvFilePath ){
		if (table == null){
			return null;
		}
		CSVParser parser = CSVParser.create(csvFilePath);
		List<String> rowLine = null;
		ArrayList<Node> predicates = new ArrayList<Node>();
		int rowNum = 0;

		while ((rowLine = parser.parse1()) != null) {
			if (rowNum == 0) {
				table.createColumn(CSV_ROW_NODE);
				for (String column : rowLine) {
					String uri = IRIResolver.resolveString(csvFilePath) + "#"
							+ LangCSV.toSafeLocalname(column);
					Node p = NodeFactory.createURI(uri);
					predicates.add(p);
					table.createColumn(p);
				}
			} else {
				Node subject = LangCSV.caculateSubject(rowNum, csvFilePath);
				Row row = table.createRow(subject);
				
				row.setValue(table.getColumn(CSV_ROW_NODE), NodeFactory.createLiteral(
						(rowNum + ""), XSDDatatype.XSDinteger));

				for (int col = 0; col < rowLine.size(); col++) {

					String columnValue = rowLine.get(col).trim();
					Node o;
					try {
						// Try for a double.
						double d = Double.parseDouble(columnValue);
						o = NodeFactory.createLiteral(columnValue,
								XSDDatatype.XSDdouble);
					} catch (Exception e) {
						o = NodeFactory.createLiteral(columnValue);
					}
					row.setValue(table.getColumn(predicates.get(col)), o);
				}
			}
			rowNum++;
		}
		return table;
	}
}
