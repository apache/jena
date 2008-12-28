/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.io.OutputStream;
import java.io.PrintWriter;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.util.FileUtils;

public class PlainFormat implements ResultSetProcessor
{
    PrintWriter out ;
    int count = 0 ;
    boolean lineNumbers = true ;
    boolean first = true ;
    SerializationContext context ;

    public PlainFormat(OutputStream outStream, SerializationContext context)
    {
        this.out = FileUtils.asPrintWriterUTF8(outStream) ;
        this.context = context ;
    }
    
    public PlainFormat(OutputStream outStream, Prologue prologue)
    {
        this(outStream, new SerializationContext(prologue)) ;
    }
    
    public void start(ResultSet rs) {}
    public void finish(ResultSet rs) { out.flush() ; } 
    public void start(QuerySolution qs)
    {
        count++ ;
        //insertLineNumber() ;
        first = true ;
    }
    
    public void finish(QuerySolution qs) { out.println() ; }
    public void binding(String varName, RDFNode value)
    {
        if ( value == null )
            return ; // Unbound
        if ( ! first )
            out.print(" ") ;
        // Would like to share code Binding here.
        String s = FmtUtils.stringForRDFNode(value, context) ;
        out.print("( ?"+varName+" = "+s+" )") ;
        first = false ;
    }
    
    void insertLineNumber()
    {
        if ( ! lineNumbers )
            return ;
        String s = Integer.toString(count) ;
        for ( int i = 0 ; i < 3-s.length() ; i++ )
            out.print(' ') ;
        out.print(s) ;
        out.print(' ') ;
    }

}
/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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