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
import java.util.regex.Pattern ;

import org.openjena.atlas.io.IO ;

import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;

/**
 * Input reader associated to {@link TSVOutput}.
 */
public class TSVInput {

	static Pattern pattern = Pattern.compile("\t");
	
	/**
	 * Reads SPARQL Results from TSV format into a {@link ResultSet} instance
	 */
    public static ResultSet fromTSV(InputStream in)
    {
    	BufferedReader reader = IO.asBufferedUTF8(in);
        List<Var> vars = new ArrayList<Var>();
        List<String> varNames = new ArrayList<String>();

        boolean first = true;
    	String str = null;
        try 
        {
        	//Here we try to parse only the Header Row
        	str = reader.readLine();
        	if (str == null ) 
        	    throw new ARQException("TSV Results malformed - input is empty (no header row)") ;
        	if ( ! str.isEmpty() )
        	{
            	String[] tokens = pattern.split(str,-1);
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
        return new ResultSetStream(varNames, null, new TSVInputIterator(reader, vars));
    }
 

}
