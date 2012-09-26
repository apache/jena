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
import org.openjena.riot.RiotException ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

/**
 * Input reader associated to {@link TSVOutput}.
 */
public class TSVInput {

	static Pattern pattern = Pattern.compile("\t");
	
	/**
	 * Reads SPARQL Results from TSV format into a {@link ResultSet} instance
	 * @param in Input Stream
	 */
    public static ResultSet fromTSV(InputStream in)
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
        	    throw new ARQException("TSV Results malformed, input is empty (no header row)") ;
        	if ( ! str.isEmpty() )
        	{
        	    String[] tokens = pattern.split(str,-1);
        	    for ( String token : tokens ) 
        	    {
        	        Node v ;
        	        try {
        	            v = NodeFactory.parseNode(token) ;
        	            if ( v == null || ! v.isVariable())
        	                throw new ResultSetException("TSV Results malformed, not a variable: "+token);
        	        } catch (RiotException ex)
        	        { throw new ResultSetException("TSV Results malformed, variable names must begin with a ? in the header: "+token); }

        	        Var var = Var.alloc(v);
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
    
    /**
     * Reads SPARQL Boolean result from TSV
     * @param in Input Stream
     * @return boolean
     */
    public static boolean booleanFromTSV(InputStream in)
    {
    	BufferedReader reader = IO.asBufferedUTF8(in);
    	String str = null;
    	try
    	{
    		str = reader.readLine();
    		if (str == null) throw new ARQException("TSV Boolean Results malformed - input is empty");
    		str = str.trim(); //Remove extraneous white space
    		if (str.toLowerCase().equals("true") || str.toLowerCase().equals("yes")) {
    			return true;
    		} else if (str.toLowerCase().equals("false") || str.toLowerCase().equals("no")) {
    			return false;
    		} else if (str.startsWith("?") || str.contains("\t")) {
    			throw new ARQException("TSV Boolean Results malformed, appears to be a normal result set header, use TSVInput.fromTSV() to parse a ResultSet");
    		} else {
    			throw new ARQException("TSV Boolean Results malformed, expected one of - true yes false no - but got " + str);
    		}
    	}
    	catch (IOException ex)
    	{
    		throw new ARQException(ex);
    	}
    }
 }
