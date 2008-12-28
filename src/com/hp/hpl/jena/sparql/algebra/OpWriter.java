/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra;

import java.io.OutputStream;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp;
import com.hp.hpl.jena.sparql.util.IndentedWriter;


 class OpWriter
{
    private static final int NL = 1 ;
    private static final int NoNL = -1 ;
    
    public static void out(Op op)
    { out(System.out, op) ; }
    
    public static void out(Op op, PrefixMapping pMap)
    { out(System.out, op, pMap) ; }
    
    public static void out(Op op, Prologue prologue)
    { out(System.out, op, prologue) ; }
    
    public static void out(OutputStream out, Op op)
    { out(out, op, (PrefixMapping)null) ; }

    public static void out(OutputStream out, Op op, PrefixMapping pMap)
    { out(new IndentedWriter(out), op, pMap) ; }

    public static void out(OutputStream out, Op op, Prologue prologue)
    { out(new IndentedWriter(out), op, prologue) ; }

    public static void out(IndentedWriter iWriter, Op op)
    { out(iWriter, op, (PrefixMapping)null) ; }

    public static void out(IndentedWriter iWriter, Op op, PrefixMapping pMap)
    {
        if ( pMap == null )
            pMap = OpPrefixesUsed.used(op, ARQConstants.getGlobalPrefixMap()) ;
        SerializationContext sCxt = new SerializationContext(pMap) ;
        out(iWriter, op, sCxt) ;
    }

    public static void out(IndentedWriter iWriter, Op op, Prologue prologue)
    {
        SerializationContext sCxt = new SerializationContext(prologue) ;
        out(iWriter, op, sCxt) ;
    }
    
    public static void out(OutputStream out, Op op, SerializationContext sCxt)
    {
        out(new IndentedWriter(out), op, sCxt) ;
    }

    public static void out(IndentedWriter iWriter, Op op, SerializationContext sCxt)
    {
        WriterOp.output(iWriter, op, sCxt) ;
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