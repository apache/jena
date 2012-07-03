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

import java.io.BufferedWriter ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.io.Writer ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.out.NodeFormatterTTL;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.util.FileUtils ;

/**
 * Tab Separated Values.
 * 
 * First row is variable names (with ?).
 * Subsequent rows are RDF terms, written Turtle style.
 */
public class TSVOutput extends OutputBase
{
    // Tab Separated Values
    // http://www.iana.org/assignments/media-types/text/tab-separated-values 
    
    static String NL   = "\n" ;
    static String SEP  = "\t" ;
    
    @Override
    public void format(OutputStream out, ResultSet resultSet)
    {
        try {
        	//Use a Turtle formatter to format terms
        	NodeFormatterTTL formatter = new NodeFormatterTTL(null, null);
        	
            Writer w = FileUtils.asUTF8(out) ;
            w = new BufferedWriter(w) ;
            
            String sep = null ;
            List<String> varNames = resultSet.getResultVars() ;
            List<Var> vars = new ArrayList<Var>(varNames.size()) ;
            
            // writes the variables on the first line
            for( String v : varNames )
            {
                if ( sep != null )
                    w.write(sep) ;
                else
                    sep = SEP ;
                Var var = Var.alloc(v) ;
                w.write(var.toString()) ; 
                vars.add(var) ;
            }
            w.write(NL) ;
            
            // writes one binding by line
            for ( ; resultSet.hasNext() ; )
            {
                sep = null ;
                Binding b = resultSet.nextBinding() ;
                
                for( Var v : vars )
                {
                    if ( sep != null )
                        w.write(sep) ;
                    sep = SEP ;
                    
                    Node n = b.get(v) ;
                    if ( n != null )
                    {
                        // This will not include a raw tab.
                        formatter.format(w, n);
                    }
                }
                w.write(NL) ;
            }
            
            w.flush() ;
        } catch (IOException ex)
        {
            throw new ARQException(ex) ;
        }
    }

    static final byte[] yesBytes = StrUtils.asUTF8bytes("yes") ;
    static final byte[] noBytes = StrUtils.asUTF8bytes("no") ;
    static final byte[] NLBytes = StrUtils.asUTF8bytes(NL) ;
    
    @Override
    public void format(OutputStream out, boolean booleanResult)
    {
        try
        {
            if (booleanResult) 
                out.write(yesBytes) ;
            else
                out.write(noBytes) ;
            out.write(NLBytes) ;
        } catch (IOException ex)
        {
            throw new ARQException(ex) ;
        }
    }

}
