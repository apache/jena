/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.util;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.ARQConstants;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;

public class PrintUtils
{
    // ---- Printable
    public static String toString(Printable f)
    { 
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        IndentedWriter out = buff.getIndentedWriter() ;
        f.output(out) ;
        return buff.toString() ;
    }
    
    // ---- PrintSerializable
    public static String toString(PrintSerializable item, PrefixMapping pmap)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        IndentedWriter out = buff.getIndentedWriter() ;
        SerializationContext sCxt = new SerializationContext(pmap) ;
        item.output(out, sCxt) ;
        return buff.toString() ;
    }

    public static String toString(PrintSerializable item)
    { return toString(item, ARQConstants.getGlobalPrefixMap()) ; }
    
    public static void output(PrintSerializable item, IndentedWriter out)
    { 
        out.print(Plan.startMarker) ;
        out.print(Utils.className(item)) ;
        out.print(Plan.finishMarker) ;
    }
    
    // ----
    public static interface Fmt { String fmt(Object thing) ; }
    
    private static Fmt itemFmt = new Fmt(){
        public String fmt(Object thing)
        {
            if ( thing == null ) return "<null>" ;
            return thing.toString() ;
        }} ;
    
    // Prints a collection (a List usually).  
        
    public static void printList(IndentedWriter out, Collection list, 
                                 String sep, Fmt itemFmt)
    {
        String sep$ = "" ; 
        for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
        {
            out.print(sep$) ;
            sep$ = sep ;
            Object obj = iter.next() ;
            out.print(itemFmt.fmt(obj)) ;
        }
    }

    public static void printList(IndentedWriter out, Collection list, String sep)
    { printList(out, list, sep, itemFmt) ; }

    public static void printList(IndentedWriter out, Collection list)
    { printList(out, list, " ") ; }

    public static void printList(PrintStream out, Collection list, 
                                 String sep, Fmt itemFmt)
    {
        String sep$ = "" ; 
        for ( Iterator iter = list.iterator() ; iter.hasNext() ; )
        {
            out.print(sep$) ;
            sep$ = sep ;
            Object obj = iter.next() ;
            out.print(itemFmt.fmt(obj)) ;
        }
    }

    public static void printList(PrintStream out, List list, String sep)
    { printList(out, list, sep, itemFmt) ; }

    public static void printList(PrintStream out, List list)
    { printList(out, list, " ") ; }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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