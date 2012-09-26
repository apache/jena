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

import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.io.IO ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;

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
    
    public static ResultSet fromCSV(InputStream in)
    {
        BufferedReader reader = IO.asBufferedUTF8(in);
        List<Var> vars = new ArrayList<Var>();
        List<String> varNames = new ArrayList<String>();

        String str = null;
        try 
        {
            //Here we try to parse only the Header Row
            str = reader.readLine();
            if (str == null ) 
                throw new ARQException("CSV Results malformed, input is empty (no header row)") ;
            
            if ( ! str.isEmpty() )
            {
                String[] tokens = str.split(",") ;
                for ( String token : tokens ) 
                {
                    if (token.startsWith("?")) token = token.substring(1);
                    Var var = Var.alloc(token);
                    vars.add(var);
                    varNames.add(var.getName());
                }
            }
        } 
        catch ( IOException ex )
        {
            throw new ARQException(ex) ;
        }

        //Generate an instance of ResultSetStream using TSVInputIterator
        //This will parse actual result rows as needed thus minimising memory usage
        return new ResultSetStream(varNames, null, new CSVInputIterator(reader, vars));
    }
    
    public static boolean booleanFromCSV(InputStream in)
    {
    	BufferedReader reader = IO.asBufferedUTF8(in);
    	String str = null;
    	try
    	{
    		str = reader.readLine();
    		if (str == null) throw new ARQException("CSV Results malformed, input is empty");
    		str = str.trim(); //Remove extraneous white space
    		if (str.toLowerCase().equals("true") || str.toLowerCase().equals("yes")) {
    			return true;
    		} else if (str.toLowerCase().equals("false") || str.toLowerCase().equals("no")) {
    			return false;
    		} else if (str.startsWith("?") || str.contains(",")) {
    			throw new ARQException("CSV Boolean Results malformed, appears to be a normal result set header, use CSVInput.fromCSV() to parse a ResultSet");
    		} else {
    			throw new ARQException("CSV Boolean Results malformed, expected one of - true yes false no - but got " + str);
    		}
    	}
    	catch (IOException ex)
    	{
    		throw new ARQException(ex);
    	}
    }
}
