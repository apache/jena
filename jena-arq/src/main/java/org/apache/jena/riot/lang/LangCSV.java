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
import org.apache.jena.atlas.csv.CSVTokenIterator;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.tokens.Token;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

public class LangCSV implements LangRIOT {
	
	public static final String CSV_PREFIX = "http://w3c/future-csv-vocab/";
	public static final String CSV_ROW = CSV_PREFIX + "row";
	
    private InputStream input = null ;
    private Reader reader = null ;
    private String xmlBase ;
    private String filename ;
    private StreamRDF sink ;
    private ParserProfile profile ;             // Warning - we don't use all of this.

	@Override
	public Lang getLang() {
		return RDFLanguages.CSV;

	}
	
    @Override
    public ParserProfile getProfile()
    {
        return profile ;
    }

    @Override
    public void setProfile(ParserProfile profile)
    { this.profile = profile ; }
        
    public LangCSV(Reader reader, String xmlBase, String filename, ErrorHandler errorHandler, StreamRDF sink)
    {
        this.reader = reader ;
        this.xmlBase = xmlBase ;
        this.filename = filename ;
        this.sink = sink ;
        this.profile = RiotLib.profile(getLang(), xmlBase, errorHandler) ;
    }
    
    public LangCSV(InputStream in, String xmlBase, String filename, ErrorHandler errorHandler, StreamRDF sink)
    {
        this.input = in ;
        this.xmlBase = xmlBase ;
        this.filename = filename ;
        this.sink = sink ;
        this.profile = RiotLib.profile(getLang(), xmlBase, errorHandler) ;
    }

	@Override
	public void parse() {
		 sink.start() ;
		 CSVTokenIterator iter;
		 if ( input != null ){
			 iter = new CSVTokenIterator(input) ;
		 } else {
			 iter = new CSVTokenIterator(reader) ;
		 }
		 
		 CSVParser parser = new CSVParser(iter) ;
		 List<String> row = null ;
		 ArrayList<Node> predicates = new ArrayList<Node>();
		 int rowNum = 0;
		 while ( (row=parser.parse1())!=null) {
			 rowNum++;
			 if (rowNum==1){
				 for (String column: row){
					 Node predicate = this.profile.createURI(filename + "#" + column.trim(), -1, -1);
					 predicates.add(predicate);
				 }
			 }else {
				 Node subject = this.profile.createBlankNode(null, -1, -1);
				 Node predicateRow = this.profile.createURI(CSV_ROW, -1, -1);
				 Node objectRow = this.profile.createTypedLiteral( (rowNum+"").trim(), XSDDatatype.XSDinteger, -1, -1);
				 sink.triple(this.profile.createTriple(subject, predicateRow, objectRow, -1, -1)   );
				 for (int i=0;i<row.size();i++){
					 Node predicate = predicates.get(i);
					 
				     String columnValue = row.get(i).trim();
					 try{
						 Double.parseDouble(columnValue);
					 }catch(Exception e){
						 columnValue = "\""+columnValue +"\"";
					 }
					 sink.triple(this.profile.createTriple(subject, predicate, parse(columnValue), -1, -1)   );
				 }
				 
			 }
         }
		 sink.finish() ;
		
	}
	
    private Node parse(String string)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        if ( ! tokenizer.hasNext() )
            return null ;
        Token t = tokenizer.next();
        Node n = profile.create(null, t) ;
        if ( tokenizer.hasNext() )
            Log.warn(RiotLib.class, "String has more than one token in it: "+string) ;
        return n ;
    }

}
