/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVisitor;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.Tags;
import com.hp.hpl.jena.sparql.sse.writers.WriterLib;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

/** Marker for extension points
 *  Execution will be per-engine specific
 * @author Andy Seaborne
 */
public abstract class OpExt extends OpBase
{ 
    protected final String tag ;
    
    public OpExt(String name) { tag = name ; }
    
    /** Return an op that will used by query processing algorithms such as 
     *  optimization.  This method returns a non-extension Op expression that
     *  is the equivalent SPARQL expression.  For example, this is the Op replaced
     *  by this extension node.   
     */ 
    public abstract Op effectiveOp() ;
    
    /** Evaluate the op, given a stream of bindings as input 
     *  Throw UnsupportedOperationException if this OpExt is not executeable. 
     */
    public abstract QueryIterator eval(QueryIterator input, ExecutionContext execCxt) ;
    
    public final String getName() { return tag ; }
    
    public final void visit(OpVisitor opVisitor)
    { opVisitor.visit(this) ; }

    @Override
    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        int line = out.getRow() ;
        
        if ( false )
        {
            // Write in (ext NAME ...) form.
            WriterLib.start(out, Tags.tagExt, WriterLib.NoNL) ;
            out.print(getName()) ;
            out.print(" ") ;
            outputArgs(out, sCxt) ;
            WriterLib.finish(out, Tags.tagExt) ;
        }
        else
        {
         // Write in (NAME ...) form.
            WriterLib.start(out, tag, WriterLib.NoNL) ;
            outputArgs(out, sCxt) ;
            WriterLib.finish(out, tag) ;
        }
        
        if ( line != out.getRow() )
            out.ensureStartOfLine() ;
    }
    
//    /** Return the sub tag - must match the builder */ 
//    public abstract String getSubTag() ;

    /** Output the arguments in legal SSE format. Multiple items, whitespace separated */ 
    public abstract void outputArgs(IndentedWriter out, SerializationContext sCxt) ;
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