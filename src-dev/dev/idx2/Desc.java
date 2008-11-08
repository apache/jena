/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.idx2;


// Generalized descriptor.
public class Desc
{
    // Tuples are 0, 1, 2, ...
    
    
    // Map from tuple order to index order
    // So POS is 1,2,0
    private int[] indexOrder ;
    
    // The mapping from index to tuple order
    // For POS, that is 2,0,1 (S=2, P=0, O=1) 
    private int[] retrieveOrder ;

    public Desc(int...elements)
    {
        this.indexOrder = new int[elements.length] ;
        System.arraycopy(elements, 0, elements, 0, elements.length) ;
        
        this.retrieveOrder = new int[elements.length] ;
    
        for ( int i = 0 ; i < elements.length ; i++ )
        {
            int x = elements[i] ;
            if ( x < 0 || x >= elements.length)
                throw new IllegalArgumentException("Out of range: "+x) ;
            indexOrder[i] = x ;
            retrieveOrder[x] = i ;
        }
        
    }
    
    public int indexOrder(int i) { return indexOrder[i] ; }
    
    public int retrieveOrder(int i) { return retrieveOrder[i] ; }
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