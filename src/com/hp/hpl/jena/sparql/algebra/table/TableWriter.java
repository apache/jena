/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.table;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class TableWriter
{
    public static String asSSE(Table table)
    {
        IndentedLineBuffer out = new IndentedLineBuffer() ;
        TableWriter.output(table, out) ;
        return out.asString() ;
    }
    
    public static void output(Table table, IndentedWriter out)
    {
        output(table, out, null) ;
    }
    
    public static void output(Table table, IndentedWriter out, SerializationContext sCxt)
    {
        if ( sCxt != null )
        {}  // Prefix.  But then qnames are wrong.
        out.print("(table") ;
        out.incIndent() ;
        QueryIterator qIter = table.iterator(null) ;
        for ( ; qIter.hasNext() ; )
        {
            out.println() ;
            Binding binding = qIter.nextBinding() ;
            output(binding, out, sCxt) ;
        }
        out.decIndent() ;
        
        out.print(")") ;
    }

    private static void output(Binding binding, IndentedWriter out, SerializationContext sCxt)
    {
        out.print("(row") ;
        for ( Iterator<Var> iter = binding.vars() ; iter.hasNext() ; )
        {
            Var v = iter.next();
            Node n = binding.get(v) ;
            out.print(" ") ;
            out.print(Plan.startMarker2) ;
            out.print(FmtUtils.stringForNode(v)) ; 
            out.print(" ") ;
            out.print(FmtUtils.stringForNode(n)) ;
            out.print(Plan.finishMarker2) ;
        }
        out.print(")") ;
    }
}

/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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