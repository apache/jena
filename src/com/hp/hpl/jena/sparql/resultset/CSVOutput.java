/*
 * (c) Copyright 2009 Talis Systems Ltd
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.io.BufferedWriter ;
import java.io.IOException ;
import java.io.OutputStream ;
import java.io.Writer ;
import java.util.ArrayList ;
import java.util.List ;

import org.openjena.atlas.lib.StrUtils ;

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
    
    public void format(OutputStream out, ResultSet resultSet)
    {
        try {
            Writer w = FileUtils.asUTF8(out) ;
            NodeToLabelMap bnodes = new NodeToLabelMap() ;
            w = new BufferedWriter(w) ;
            
            String sep = null ;
            List<String> varNames = resultSet.getResultVars() ;
            List<Var> vars = new ArrayList<Var>(varNames.size()) ;
            
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
        return str;
    }

    static final byte[] yesBytes = StrUtils.asUTF8bytes("yes") ;
    static final byte[] noBytes = StrUtils.asUTF8bytes("no") ;
    static final byte[] NLBytes = StrUtils.asUTF8bytes(NL) ;
    
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

/*
 * (c) Copyright 2009 Talis Systems Ltd
 * (c) Copyright 2011 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */