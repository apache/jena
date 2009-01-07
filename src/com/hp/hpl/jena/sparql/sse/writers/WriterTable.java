/*
 * (c) Copyright 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.sse.writers;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;

import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.util.FmtUtils;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

public class WriterTable
{
    public static void output(IndentedWriter out, Table table, SerializationContext sCxt)
    {
        WriterLib.start(out, Tags.tagTable, WriterLib.NL) ;
        outputPlain(out, table, sCxt) ;
        WriterLib.finish(out, Tags.tagTable) ;
    }
    
    public static void outputPlain(IndentedWriter out, Table table, SerializationContext sCxt)
    {
        QueryIterator qIter = table.iterator(null) ; 
        for ( ; qIter.hasNext(); )
        {
            Binding b = qIter.nextBinding() ;
            output(out, b, sCxt) ;
            out.println() ;
        }
        qIter.close() ;
    }
    
    public static void output(IndentedWriter out, Binding binding, SerializationContext sCxt )
    {
        WriterLib.start(out, Tags.tagRow, WriterLib.NoSP) ;
        for ( Iterator<Var> iter = binding.vars() ; iter.hasNext() ; )
        {
            Var v = iter.next() ;
            Node n = binding.get(v) ;
            out.print(" ") ;
            WriterLib.start2(out) ;
            out.print(FmtUtils.stringForNode(v, sCxt)) ;
            out.print(" ") ;
            out.print(FmtUtils.stringForNode(n, sCxt)) ;
            WriterLib.finish2(out) ;
        }
        WriterLib.finish(out, Tags.tagRow) ;
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