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

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.util.NodeToLabelMap ;
import com.hp.hpl.jena.util.FileUtils ;

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
 */
public class CSVOutput extends OutputBase
{
    // RFC for CSV : http://www.ietf.org/rfc/rfc4180.txt
    
    static String NL = "\r\n" ;
    
    @Override
    public void format(OutputStream out, ResultSet resultSet)
    {
        try {
            Writer w = FileUtils.asUTF8(out) ;
            NodeToLabelMap bnodes = new NodeToLabelMap() ;
            w = new BufferedWriter(w) ;
            
            String sep = null ;
            List<String> varNames = resultSet.getResultVars() ;
            List<Var> vars = new ArrayList<>(varNames.size()) ;
            
            // Convert to Vars and output the header line.
            for( String v : varNames )
            {
                if ( sep != null )
                    w.write(sep) ;
                else
                    sep = "," ;
                w.write(csvSafe(v)) ; 
                vars.add(Var.alloc(v)) ;
            }
            w.write(NL) ;
            
            // Data output
            for ( ; resultSet.hasNext() ; )
            {
                sep = null ;
                Binding b = resultSet.nextBinding() ;
                
                for( Var v : vars )
                {
                    if ( sep != null )
                        w.write(sep) ;
                    sep = "," ;
                    
                    Node n = b.get(v) ;
                    if ( n != null )
                        output(w, n, bnodes) ;
                }
                w.write(NL) ;
            }
            w.flush() ;
        } catch (IOException ex)
        {
            throw new ARQException(ex) ;
        }
    }

    private void output(Writer w, Node n, NodeToLabelMap bnodes) throws IOException 
    {
        //String str = FmtUtils.stringForNode(n) ;
        String str = "?" ;
        if ( n.isLiteral() ) str = n.getLiteralLexicalForm() ;
        else if ( n.isURI() ) str = n.getURI() ;
        else if ( n.isBlank() )
            str = bnodes.asString(n) ;
        
        str = csvSafe(str) ;
        w.write(str) ;
    }

    private String csvSafe(String str)
    {
        // Apparently, there are CSV parsers that only accept "" as an escaped quote if inside a "..."  
        if (str.contains("\"")
            || str.contains(",")
            || str.contains("\r")
            || str.contains("\n") )
            str = "\"" + str.replaceAll("\"", "\"\"") + "\"";
        else if ( str.isEmpty() )
            // Return the quoted empty string. 
            str = "\"\"" ;
        return str;
    }

    static final byte[] headerBytes = StrUtils.asUTF8bytes("_askResult" + NL);
    static final byte[] yesBytes = StrUtils.asUTF8bytes("true") ;
    static final byte[] noBytes = StrUtils.asUTF8bytes("false") ;
    static final byte[] NLBytes = StrUtils.asUTF8bytes(NL) ;
    
    @Override
    public void format(OutputStream out, boolean booleanResult)
    {
        try
        {
        	out.write(headerBytes);
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
