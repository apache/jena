/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;

import java.util.Arrays;
import static java.lang.String.format ;
import lib.ListUtils;
import lib.Tuple;


/** General descriptor of a reorderring (mapping) of columns indexes to columns indexes, 
 * for example, from triples to triple index order. 
 * @author Andy Seaborne
 */
public class ColumnMap
{
    // Map from tuple order to index order
    // So SPO->POS is (0->2, 1->0, 2->1)
    private int[] indexOrder ;
    
    // The mapping from index to tuple order
    // For POS->SPO, is (0->1, 1->2, 2->0)
    private int[] retrieveOrder ;
    private String label ;

    public ColumnMap(String label, int...elements)
    {
        this.label = label ;

        this.indexOrder = new int[elements.length] ;
        System.arraycopy(elements, 0, elements, 0, elements.length) ;
        Arrays.fill(indexOrder, -1) ;
        
        this.retrieveOrder = new int[elements.length] ;
        Arrays.fill(retrieveOrder, -1) ;
    
        for ( int i = 0 ; i < elements.length ; i++ )
        {
            int x = elements[i] ;
            if ( x < 0 || x >= elements.length)
                throw new IllegalArgumentException("Out of range: "+x) ;
            // Checking
            if ( indexOrder[i] != -1 || retrieveOrder[x] != -1 )
                throw new IllegalArgumentException("Inconsistent: "+ListUtils.str(elements)) ;
            
            indexOrder[i] = x ;
            retrieveOrder[x] = i ;
        }
    }
    
    /** copy over a tuple from normal order to index order */
    public <T> Tuple<T> indexOrder(Tuple<T> src)
    {
        return map(src, indexOrder) ;
    }
    
    /** copy over a tuple from mapped (index) order to normal order */
    public <T> Tuple<T> retrieveOrder(Tuple<T> src)
    {
        return map(src, indexOrder) ;
    }

    private <T> Tuple<T> map(Tuple<T> src, int[] map)
    {
        @SuppressWarnings("unchecked")
        T[] elts = (T[])new Object[src.size()] ;
        
        for ( int i = 0 ; i < src.size() ; i++ )
        {
            int j = map[i] ;
            elts[i] = src.get(j) ;
        }
        return new Tuple<T>(elts) ;
    }
    
    public int indexOrder(int i) { return indexOrder[i] ; }
    
    public int retrieveOrder(int i) { return retrieveOrder[i] ; }
    
    @Override
    public String toString()
    {
        return label ; 
        //return format("%s:%s%s", label, mapStr(indexOrder), mapStr(retrieveOrder)) ;
    }

    private Object mapStr(int[] map)
    {
        StringBuilder buff = new StringBuilder() ;
        String sep = "{" ;
        
        for ( int i = 0 ; i < map.length ; i++ )
        {
            buff.append(sep) ;
            sep = ", " ; 
            buff.append(format("%d->%d", i, map[i])) ;
        }
        buff.append("}") ;
        
        return buff.toString() ;
    }
    
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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