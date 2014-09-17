/**
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

package org.apache.jena.riot.lang;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.csv.CSVParser;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.IRILib;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

public class LangCSV implements LangRIOT {

	public static final String CSV_PREFIX = "http://w3c/future-csv-vocab/";
	public static final String CSV_ROW = CSV_PREFIX + "row";

	private InputStream input = null;
	private Reader reader = null;
	private String base;
	private String filename;
	private StreamRDF sink;
	private ParserProfile profile; // Warning - we don't use all of this.

	@Override
	public Lang getLang() {
		return RDFLanguages.CSV;

	}

	@Override
	public ParserProfile getProfile() {
		return profile;
	}

	@Override
	public void setProfile(ParserProfile profile) {
		this.profile = profile;
	}

	public LangCSV(Reader reader, String base, String filename,
			ErrorHandler errorHandler, StreamRDF sink) {
		this.reader = reader;
		this.base = base;
		this.filename = filename;
		this.sink = sink;
		this.profile = RiotLib.profile(getLang(), base, errorHandler);
	}

	public LangCSV(InputStream in, String base, String filename,
			ErrorHandler errorHandler, StreamRDF sink) {
		this.input = in;
		this.base = base;
		this.filename = filename;
		this.sink = sink;
		this.profile = RiotLib.profile(getLang(), base, errorHandler);
	}

	@Override
	public void parse() {
		sink.start();
		CSVParser parser = (input != null) ? CSVParser.create(input)
				: CSVParser.create(reader);
		ArrayList<Node> predicates = new ArrayList<Node>();
		int rowNum = 0;
		for (List<String> row : parser) {
			
			if (rowNum == 0) {
				for (String column : row) {
					String uri = IRIResolver.resolveString(filename) + "#"
							+ toSafeLocalname(column);
					Node predicate = this.profile.createURI(uri, rowNum, 0);
					predicates.add(predicate);
				}
			} else {
				//Node subject = this.profile.createBlankNode(null, -1, -1);
				Node subject = caculateSubject(rowNum, filename);
				Node predicateRow = this.profile.createURI(CSV_ROW, -1, -1);
				Node objectRow = this.profile
						.createTypedLiteral((rowNum + ""),
								XSDDatatype.XSDinteger, rowNum, 0);
				sink.triple(this.profile.createTriple(subject, predicateRow,
						objectRow, rowNum, 0));
				for (int col = 0; col < row.size() && col<predicates.size(); col++) {
					Node predicate = predicates.get(col);
					String columnValue = row.get(col).trim();
					if("".equals(columnValue)){
						continue;
					}					
					Node o;
					try {
						// Try for a double.
						Double.parseDouble(columnValue);
						o = NodeFactory.createLiteral(columnValue,
								XSDDatatype.XSDdouble);
					} catch (Exception e) {
						o = NodeFactory.createLiteral(columnValue);
					}
					sink.triple(this.profile.createTriple(subject, predicate,
							o, rowNum, col));
				}

			}
			rowNum++;
		}
		sink.finish();

	}

	public static String toSafeLocalname(String raw) {
		String ret = raw.trim();
		return encodeURIComponent(ret);
		
	}
	
	public static String encodeURIComponent(String s) {
	    return IRILib.encodeUriComponent(s);
	}
	
	public static Node caculateSubject(int rowNum, String filename){
		Node subject = NodeFactory.createAnon();
//		String uri = IRIResolver.resolveString(filename) + "#Row_" + rowNum; 
//		Node subject =  NodeFactory.createURI(uri);
		return subject;
	}
}
