/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import org.openjena.atlas.io.IndentedLineBuffer ;
import org.openjena.atlas.io.IndentedWriter ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.Syntax ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.engine.Plan ;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory ;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry ;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot ;
import com.hp.hpl.jena.sparql.serializer.SerializationContext ;
import com.hp.hpl.jena.sparql.sse.WriterSSE ;

public class QueryOutputUtils
{
    // ---- PrintSerializable
    public static String toString(PrintSerializable item, PrefixMapping pmap)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        SerializationContext sCxt = new SerializationContext(pmap) ;
        item.output(buff, sCxt) ;
        return buff.toString() ;
    }

    public static String toString(PrintSerializable item)
    { return toString(item, null) ; }
    
    // ModQueryOut
        
    public static void printPlan(Query query, QueryExecution qe)
    {
        QueryEngineFactory f = QueryEngineRegistry.findFactory(query, qe.getDataset().asDatasetGraph(), ARQ.getContext()) ;
        if ( f == null )
            System.err.println("printPlan: Unknown engine type: "+Utils.className(qe)) ;
        
        Plan plan = f.create(query, qe.getDataset().asDatasetGraph(), BindingRoot.create(), ARQ.getContext()) ;
        SerializationContext sCxt = new SerializationContext(query) ;
        IndentedWriter out = IndentedWriter.stdout ;
    
        plan.output(out, sCxt) ;
        out.flush();
    }

    public static void printQuery(Query query)
    {
        IndentedWriter out = IndentedWriter.stdout ;
        printQuery(out, query) ;
    }

    public static void printQuery(IndentedWriter out, Query query)
    {
        printQuery(out, query, Syntax.defaultQuerySyntax) ;
    }

    public static void printQuery(IndentedWriter out, Query query, Syntax syntax)
    {
        query.serialize(out, syntax) ;
        out.flush() ;
    }

    public static void printOp(Query query, boolean optimize)
    {
        IndentedWriter out = IndentedWriter.stdout ;
        printOp(out, query, optimize) ; // Flush done
    }

    public static void printOp(IndentedWriter out, Query query, boolean printOptimized)
    {
        Op op = Algebra.compile(query) ;
        if ( printOptimized )
            op =  Algebra.optimize(op) ;
        WriterSSE.out(out, op, query) ;
        out.flush();
    }

    public static void printQuad(Query query, boolean printOptimized)
    {
        IndentedWriter out = IndentedWriter.stdout ;
        printQuad(out, query, printOptimized) ; // Flush done
    }

    public static void printQuad(IndentedWriter out, Query query, boolean printOptimized)
    {
        Op op = Algebra.compile(query) ;
        if ( printOptimized )
            op =  Algebra.optimize(op) ;
        op = Algebra.toQuadForm(op) ;
        WriterSSE.out(out, op, query) ;
//        SerializationContext sCxt = new SerializationContext(query) ;
//        op.output(out, sCxt) ;
        out.flush() ;
    }
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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