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

package com.hp.hpl.jena.sparql.resultset;

import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.Iterator ;
import java.util.List ;

import org.apache.jena.atlas.csv.CSVParser ;
import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;
import org.apache.jena.atlas.logging.FmtLog ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;

/** Convenient comma separated values - see also TSV (tab separated values)
 *  which outputs full RDF terms (in Turtle-style).
 *  
 *  The CSV format supported is:
 *  <ul>
 *  <li>First row is variable names without '?'</li>
 *  <li>Strings, quoted if necessary and numbers output only.
 *  No language tags, or datatypes.
 *  URIs are send without $lt;&gt;  
 *  </li>
 *  CSV is RFC 4180, but there are many variations. 
 *  </ul>
 *  This code reads the file and treats everything as strings.
 *  <p>
 *  The code also allows for parsing boolean results where we expect the header to be a single string
 *  from the set: true yes false no
 *  </p>
 *  <p>
 *  Any other value is considered an error for parsing a boolean results and anything past the first line is ignored
 *  </p>
 */
public class CSVInput
{
    // This code exists to support the SPARQL WG tests. 
    private static Logger log = LoggerFactory.getLogger(CSVInput.class) ;
    public static ResultSet fromCSV(InputStream in)
    {
        CSVParser parser = CSVParser.create(in) ;
        final List<Var> vars = vars(parser) ;
        List<String> varNames = Var.varNames(vars) ;
        Transform<List<String>, Binding> transform = new Transform<List<String>, Binding>(){
            private int count = 1 ;
            @Override
            public Binding convert(List<String> row) {
                if ( row.size() != vars.size() )
                    FmtLog.warn(log, "Row %d: Length=%d: expected=%d", count, row.size(), vars.size()) ;
                
                BindingMap binding = BindingFactory.create() ;
                // Check.
                for (int i = 0 ; i < vars.size() ; i++ ) {
                    Var v = vars.get(i) ;
                    String field = (i<row.size()) ? row.get(i) : "" ;
                    Node n = NodeFactory.createLiteral(field) ;
                    binding.add(v, n);
                }
                count++ ;
                return binding ;
            }} ;
        Iterator<Binding> bindings = Iter.map(parser, transform) ;
        
        //Generate an instance of ResultSetStream using TSVInputIterator
        //This will parse actual result rows as needed thus minimising memory usage
        return new ResultSetStream(varNames, null, bindings);
    }
    
    private static List<Var> vars(CSVParser parser) {
        final List<Var> vars = new ArrayList<>();
        List<String> varNames = parser.parse1() ;
        if ( varNames == null )
            throw new ARQException("SPARQL CSV Results malformed, input is empty");
        for ( String vn : varNames ) {
            vars.add(Var.alloc(vn)) ;
        }
        return vars ;
    }
    
    public static boolean booleanFromCSV(InputStream in)
    {
        CSVParser parser = CSVParser.create(in) ;
        final List<Var> vars = vars(parser) ;
        if ( vars.size() != 1 ) {
            throw new ARQException("CSV Boolean Results malformed: variables line='"+vars+"'") ;
        }
        if ( ! vars.get(0).getName().equals("_askResult")) {
            FmtLog.warn(log, "Boolean result variable is '%s', not '_askResult'", vars.get(0).getName()) ; 
        }
        
        
        List<String> line = parser.parse1() ;
        if ( line.size() != 1 ) {
            throw new ARQException("CSV Boolean Results malformed: data line='"+line+"'") ;
        }
        String str = line.get(0) ;
        boolean b ;
        if ( str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes") )
            b = true ;
        else if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no"))
            b = false;
        else {
            throw new ARQException("CSV Boolean Results malformed, expected one of - true yes false no - but got " + str);
            }
        
        List<String> line2 = parser.parse1() ;
        if ( line2 != null ) {
            FmtLog.warn(log, "Extra rows: first is "+line2) ;
        }
        return b ;
    }
}
