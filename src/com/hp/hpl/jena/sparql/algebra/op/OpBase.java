/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.algebra.op;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.sse.writers.WriterOp;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;
import com.hp.hpl.jena.sparql.util.PrintSerializableBase;

public abstract class OpBase extends PrintSerializableBase implements Op
{
    @Override
    public abstract int hashCode() ;
    public abstract boolean equalTo(Op other, NodeIsomorphismMap labelMap) ;

    @Override
    final public boolean equals(Object other)
    { 
        if ( this == other ) return true ;

        if ( ! ( other instanceof Op ) )
            return false ;
        return equalTo((Op)other, null) ;
    }

    @Override
    public void output(IndentedWriter out)
    {
        output(out, null) ;
    }

    public void output(IndentedWriter out, SerializationContext sCxt)
    {
        int line = out.getRow() ;
        WriterOp.output(out, this, sCxt) ;
        if ( line != out.getRow() )
            out.ensureStartOfLine() ;
    }

    // Constants used in hashing to stop an element and it's subelement
    // (if just one) having the same hash.  That isn't usualy any problem but
    // it's easy to avoid so we do.
    
    static final int HashBasicGraphPattern      = 0xB1 ;
//    static final int HashGroup                = 0xB2 ;
//    static final int HashUnion                = 0xB3 ;
//    static final int HashLeftJoin             = 0xB4 ;
    static final int HashDistinct               = 0xB5 ;
    static final int HashReduced                = 0xB5 ;
    static final int HashToList                 = 0xB6 ;
    static final int HashNull                   = 0xB7 ;
    static final int HashSequence               = 0xB8 ;
    static final int HashLabel                  = 0xB9 ;
    static final int HashTriple                 = 0xBA ;


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