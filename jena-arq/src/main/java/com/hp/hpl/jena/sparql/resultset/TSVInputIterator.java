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

import static java.lang.String.format ;

import java.io.BufferedReader ;
import java.io.IOException ;
import java.util.List ;
import java.util.NoSuchElementException ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.riot.RiotException ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIteratorBase ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

/**
 * Class used to do streaming parsing of actual result rows from the TSV
 */
public class TSVInputIterator extends QueryIteratorBase
{
	private BufferedReader reader;
	private BindingMap binding;
	private int expectedItems;
	private List<Var> vars;
	private long lineNum = 1;
	
	/**
	 * Creates a new TSV Input Iterator
	 * <p>
	 * Assumes the Header Row has already been read and that the next row to be read from the reader will be a Result Row
	 * </p>
	 */
	public TSVInputIterator(BufferedReader reader, List<Var> vars)
	{
		this.reader = reader;
		this.expectedItems = vars.size();
		this.vars = vars;
	}
	
	@Override
	public void output(IndentedWriter out, SerializationContext sCxt) {
	    // Not needed - only called as part of printing/debugging query plans.
		out.println("TSVInputIterator") ;
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
	    { throw new ResultSetException("Error parsing TSV results - " + e.getMessage()); }

	    if ( line.isEmpty() )
	    {
	        // Empty input line - no bindings.
	    	// Only valid when we expect zero/one values as otherwise we should get a sequence of tab characters
	    	// which means a non-empty string which we handle normally
	    	if (expectedItems > 1) throw new ResultSetException(format("Error Parsing TSV results at Line %d - The result row had 0/1 values when %d were expected", this.lineNum, expectedItems));
	        this.binding = BindingFactory.create() ;
	        return true ;
	    }
	    
        String[] tokens = TSVInput.pattern.split(line, -1);
	    
        if (tokens.length != expectedItems)
        	 throw new ResultSetException(format("Error Parsing TSV results at Line %d - The result row '%s' has %d values instead of the expected %d.", this.lineNum, line, tokens.length, expectedItems));
        this.binding = BindingFactory.create();

        for ( int i = 0; i < tokens.length; i++ ) 
        {
            String token = tokens[i];

            //If we see an empty string this denotes an unbound value
            if (token.equals("")) continue; 

            //Bound value so parse it and add to the binding
            try {
                Node node = NodeFactoryExtra.parseNode(token) ;
                if ( !node.isConcrete() )
                    throw new ResultSetException(format("Line %d: Not a concrete RDF term: %s",lineNum, token)) ;
                this.binding.add(this.vars.get(i), node);
            } catch (RiotException ex)
            {
                throw new ResultSetException(format("Line %d: Data %s contains error: %s", lineNum, token, ex.getMessage()));
            }
        }

        return true;
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
