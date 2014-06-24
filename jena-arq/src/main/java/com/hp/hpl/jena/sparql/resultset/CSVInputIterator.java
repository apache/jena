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

package com.hp.hpl.jena.sparql.resultset;

import java.io.BufferedReader ;
import java.io.IOException ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.query.QueryException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase ;
import com.hp.hpl.jena.sparql.graph.NodeConst ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;

/**
 * Class used to do streaming parsing of actual result rows from the CSV
 */
public class CSVInputIterator extends QueryIteratorBase
{
	private BufferedReader reader;
	private BindingMap binding;
	private int expectedItems;
	private List<Var> vars;
	private long lineNum = 1;
	
	/**
	 * Creates a new CSV Input Iterator
	 * <p>
	 * Assumes the Header Row has already been read and that the next row to be read from the reader will be a Result Row
	 * </p>
	 */
	public CSVInputIterator(BufferedReader reader, List<Var> vars)
	{
		this.reader = reader;
		this.expectedItems = vars.size();
		this.vars = vars;
	}
	
	@Override
	public void output(IndentedWriter out, SerializationContext sCxt) {
	    // Not needed - only called as part of printing/debugging query plans.
		out.println("CSVInputIterator") ;
	}

	@Override
	protected boolean hasNextBinding() {
		if (this.reader != null)
		{
			if (this.binding == null)
				return this.parseNextBinding();
			else
				return true;
		}
		else
		{
			return false;
		}
	}
	
	private boolean parseNextBinding()
	{
	    String line;
	    try 
	    {
	        line = this.reader.readLine();
	        //Once EOF has been reached we'll see null for this call so we can return false because there are no further bindings
	        if (line == null) return false;
	        this.lineNum++;
	    } 
	    catch (IOException e) 
	    { throw new QueryException("Error parsing CSV results - " + e.getMessage()); }

	    if ( line.isEmpty() )
	    {
	        // Empty input line - no bindings.
	    	// Only valid when we expect zero/one values as otherwise we should get a sequence of tab characters
	    	// which means a non-empty string which we handle normally
	    	if (expectedItems > 1) 
	    	    throw new QueryException(String.format("Error Parsing CSV results at Line %d - The result row had 0/1 values when %d were expected", this.lineNum, expectedItems));
	        binding = BindingFactory.create() ;
	        if ( expectedItems == 1 )
	            binding.add(vars.get(0), NodeConst.emptyString) ; 
	        return true ;
	    }
	    
	    binding = parseLine(vars, line) ;
	    return true ;
	}
	    
	    
    private BindingMap parseLine(List<Var> vars, String line)
    {
        BindingMap binding = BindingFactory.create() ;
        List<String> terms = new ArrayList<>() ;
        int idx = 0 ;
        
        while(idx < line.length())
        {
            char ch = line.charAt(idx) ;
            
            StringBuilder s = new StringBuilder() ;
            if ( ch == '\"' || ch == '\'' )
            {
                char qCh = ch ;
                idx++ ;
                while(idx < line.length() )
                {
                    ch = line.charAt(idx) ;
                    idx++ ;
                    if ( ch == qCh )
                        break ;
                    // escapes??
                    s.append(ch) ;
                }
                if ( ch != qCh )
                    throw new QueryException(String.format("Error Parsing CSV results at Line %d  - Unterminated quoted string", this.lineNum));
                if ( idx < line.length() )
                {
                    ch = line.charAt(idx) ;
                    if ( ch != ',' )
                        throw new QueryException(String.format("Error Parsing CSV results at Line %d - Expected comma after quote", this.lineNum)) ;
                }
            }
            else
            {
                while(idx < line.length() )
                {
                    ch = line.charAt(idx) ;
                    if ( ch == ',' )
                        break ;
                    idx++ ;
                    // escapes
                    s.append(ch) ;
                }
            }
            
            terms.add(s.toString()) ;
            // At end of per-term processing, we are looking at "," or EOL.  

            // Looking at , or EOL.
            if ( ch == ',' && idx==line.length()-1 )
            {
                //EOL
                terms.add("") ;
                break ;
            }
            // Skip ","
            idx++ ;
        }
        
        if ( terms.size() != vars.size() )
            throw new QueryException(String.format("Error Parsing CSV results at Line %d - The result row '%s' has %d items when %d was expected", this.lineNum, line, terms.size(), vars.size())) ;
        for ( int i = 0 ; i < vars.size() ; i++ )
            binding.add(vars.get(i), NodeFactory.createLiteral(terms.get(i))) ;
        return binding ;
    }

	@Override
	protected Binding moveToNextBinding() {
        if (!hasNext()) throw new NoSuchElementException() ;
        Binding b = this.binding;
        this.binding = null ;
        return b;
	}

	@Override
	protected void closeIterator() {
	    IO.close(reader) ;
	    reader = null;
	}

	@Override
	protected void requestCancel() {
		//Don't need to do anything special to cancel
		//Superclass should take care of that and call closeIterator() where we do our actual clean up
	}
}
