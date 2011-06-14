/*
 * (c) Copyright 2009 Talis Systems Ltd
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
import com.hp.hpl.jena.sparql.util.FmtUtils ;
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
    
    public void format(OutputStream out, ResultSet resultSet)
    {
        try {
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
                        String str = FmtUtils.stringForNode(n) ;
                        w.write(str) ;
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