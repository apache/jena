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

package org.apache.jena.lang.csv;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.csv.CSVParser;
import org.apache.jena.atlas.lib.IRILib ;
import org.apache.jena.atlas.web.ContentType;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.riot.ReaderRIOT;
import org.apache.jena.riot.system.*;
import org.apache.jena.sparql.util.Context;

public class ReaderRIOTCSV implements ReaderRIOT {
    
    public static final String CSV_PREFIX = "http://w3c/future-csv-vocab/";
	public static final String CSV_ROW = CSV_PREFIX + "row";

	private InputStream input = null;
	private Reader reader = null;
	private String base;
	private String filename;
	private StreamRDF sink;
	private ParserProfile maker;

	public ReaderRIOTCSV(ErrorHandler errorHandler) {
		this.maker = RiotLib.createParserProfile(errorHandler);
	}

	@Override
    public void read(InputStream in, String baseURI, ContentType ct, StreamRDF output, Context context) {
	    this.input = in;
        this.reader = null;
        this.base = baseURI;
        this.filename = baseURI;
        this.sink = output;
	    parse();
	}

    @Override
    public void read(Reader reader, String baseURI, ContentType ct, StreamRDF output, Context context) {
        this.input = null;
        this.reader = reader;
        this.base = baseURI;
        this.filename = baseURI;
        this.sink = output;
        parse();
    }

	public void parse() {
		sink.start();
		CSVParser parser = (input != null) ? CSVParser.create(input) : CSVParser.create(reader);
		ArrayList<Node> predicates = new ArrayList<>();
		int rowNum = 0;
		for (List<String> row : parser) {
			
			if (rowNum == 0) {
				for (String column : row) {
					String uri = IRIResolver.resolveString(filename) + "#"
							+ toSafeLocalname(column);
					Node predicate = this.maker.createURI(uri, rowNum, 0);
					predicates.add(predicate);
				}
			} else {
				//Node subject = this.profile.createBlankNode(null, -1, -1);
				Node subject = calculateSubject(rowNum, filename);
				Node predicateRow = this.maker.createURI(CSV_ROW, -1, -1);
				Node objectRow = this.maker.createTypedLiteral((rowNum + ""), XSDDatatype.XSDinteger, rowNum, 0);
				sink.triple(this.maker.createTriple(subject, predicateRow, objectRow, rowNum, 0));
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
					sink.triple(this.maker.createTriple(subject, predicate,
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
	
	private static String encodeURIComponent(String s) {
	    return IRILib.encodeUriComponent(s);
	}
	
	public static Node calculateSubject(int rowNum, String filename){
		Node subject = NodeFactory.createBlankNode();
//		String uri = IRIResolver.resolveString(filename) + "#Row_" + rowNum; 
//		Node subject =  NodeFactory.createURI(uri);
		return subject;
	}
}
