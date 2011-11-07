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
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.ResultSetStream ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper ;

/**
 * Input reader associated to {@link TSVOutput}.
 * 
 * @author Laurent Pellegrino
 */
public class TSVInput {

	static Pattern pattern = Pattern.compile("\t");
	
    public static ResultSet fromTSV(InputStream in) {
    	BufferedReader reader = IO.asBufferedUTF8(in);
        List<Var> vars = new ArrayList<Var>();
        List<String> varNames = new ArrayList<String>();
        List<Binding> bindings = new ArrayList<Binding>();

        boolean first = true;
    	String str = null;
    	int line = 0;
        try {
        	while ( ( str = reader.readLine() ) != null ) {
        		line++;
        		String[] tokens = pattern.split(str,-1);
        		if ( first ) {
        			for ( String token : tokens ) {
                		if ( token.startsWith("?") ) 
                			token = token.substring(1);
                		Var var = Var.alloc(token);
                		vars.add(var);
                		varNames.add(var.getName());
                	}
                	first = false;
        		} else {
        			int num_tokens = tokens.length;
        	        if ( num_tokens != vars.size() ) {
        	        	 throw new ARQException(String.format("Line %d has %d values instead of %d.", line, num_tokens, vars.size()));
        	        }
        	        BindingMap binding = BindingFactory.create();
        	        for ( int i = 0; i < tokens.length; i++ ) {
        	        	String token = tokens[i];
                		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(token);
                		if ( tokenizer.hasNext() && token.length() > 0 ) {
                			Node node = tokenizer.next().asNode();
                			binding.add(vars.get(i), node);
                		}
                	}
                	bindings.add(binding);
        		}
        	}
        } catch ( IOException ex ) {
        	throw new ARQException(ex) ;
        }

        return new ResultSetStream(varNames, null, new QueryIterPlainWrapper(bindings.iterator()));
    }

}
