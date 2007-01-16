/*
 * (c) Copyright 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util;

import java.util.List;
import java.util.ArrayList ;

import com.hp.hpl.jena.query.util.IndentedWriter;
import com.hp.hpl.jena.query.util.Printable;
import com.hp.hpl.jena.sdb.util.alg.Action;
import com.hp.hpl.jena.sdb.util.alg.Filter;
import com.hp.hpl.jena.sdb.util.alg.Transform;

public class ListUtils
{
    public static <T extends Printable> void print(IndentedWriter out, List<T> list)
    {
        apply(list, new PrintAction<T>(out)) ;
    }
    
    public static <T> void apply(List<T> list, Action<T> action)
    {
        for ( T item : list )
            action.apply(item) ;
    }
    
    public static <T> List<T> filter(List<T> list, Filter<T> f)
    {
        List<T> x = new ArrayList<T>() ;
        for ( T item : list )
            if ( f.accept(item) )
                x.add(item) ;
        return x ;
    }
    
    
    public static <T, R> List<R> convert(List<T> list, Transform<T, R> converter)
    {
        List<R> x = new ArrayList<R>() ;
        for ( T item : list)
            x.add(converter.convert(item) ) ;
        return x ;
    }
    
    
    private static class PrintAction <T extends Printable> implements Action<T> 
    {
        boolean first = true ;
        IndentedWriter out ; 
        PrintAction(IndentedWriter out) { this.out = out ; }
        
        public void apply(Printable item)
        {
            if ( ! first )
                out.print(" ") ;
            first = false ;
            item.output(out) ;
        }

    } ; 
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