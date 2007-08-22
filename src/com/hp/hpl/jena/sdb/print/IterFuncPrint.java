/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.print;
import static com.hp.hpl.jena.sdb.iterator.IterFunc.* ;

public class IterFuncPrint
{
    // ---- Common operations 
    
    public static <T extends Printable> String printString(Iterable<? extends T> struct)
    {
        return printString(struct, " ") ;
    }
    
    public static <T extends Printable> String printString(Iterable<? extends T> struct, String sep)
    {
        IndentedLineBuffer b = new IndentedLineBuffer() ;
        apply(struct, new PrintAction<T>(b.getIndentedWriter(), sep)) ;
        return b.asString() ; 
    }
    
    public static <T extends Printable> void print(IndentedWriter out, Iterable<? extends T> struct)
    {
        apply(struct, new PrintAction<T>(out)) ;
    }

    public static <T extends Printable> void print(IndentedWriter out, 
                                                   Iterable<? extends T> struct,
                                                   String sep)
    {
        apply(struct, new PrintAction<T>(out, sep)) ;
    }

}

/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
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