/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class Trans
{
    
    // Map : (left) node to link ; ensure all right nodes have an entry
    static class Links extends HashMap<Integer, ArrayList<Integer>>
    {
        void add(Integer i, Integer j)
        {
            ArrayList<Integer> x = ensure(i) ;
            x.add(j) ;
            ensure(j) ;
        }
        
        private ArrayList<Integer> ensure(Integer i)
        {
            ArrayList<Integer> x = get(i) ;
            if ( x != null )
                return x ;
            x = new ArrayList<Integer>() ;
            put(i, x) ;
            return x ; 
        }
        
        boolean contains(Integer i, Integer j)
        {
            ArrayList<Integer> x = get(i) ;
            if ( x == null ) return false ;
            return x.contains(j) ;
        }
        
        void print()
        {
            for ( Integer i : keySet() )
            {
                //System.out.printf("%2d : ", i) ;
                //for ( Integer j : get(i) )
                //    System.out.printf("%2d", j) ;
                for ( Integer j : get(i) )
                    System.out.printf("%2d %2d\n", i, j) ;
            }
        }
    }
    
    /* http://datastructures.itgo.com/graphs/transclosure.htm
     * transclosure( int adjmat[max][max], int path[max][max])
        {
         for(i = 0; i < max; i++)
          for(j = 0; j < max; j++)
              path[i][j] = adjmat[i][j];
        
         for(i = 0;i <max; i++)
          for(j = 0;j < max; j++)
           if(path[i][j] == 1)
            for(k = 0; k < max; k++)
              if(path[j][k] == 1)
                path[i][k] = 1;
        }
     */
    // Hard if the 2-D array is stored compactly because Java collections
    // So collect nodes firsts.
    
    // can't be modified as they are iterated over.  Leads to taking copies,
    // which costs and may change the algorithm complexity.
    // And we are expanding a tree-ish things anyway which is connection-sparse. 
    
    // For every node:
    //   If there exists a 2-path, make the 1-path
    
    // Walshalls algorithm 
    // http://datastructures.itgo.com/graphs/transclosure.htm
    // and many other places.
    // Adapted only in that it works on the sparse graph representation
    // used to record the links.
    
    static void expand(Links pairs)
    {
        // All the nodes (whether they have links initially or not).
        // Because this is not updated by any new additions (they only link
        // existing ndoes), it is safe to iterate over the set while adding
        // the arrays which record the links.
        
        Set<Integer> allNodes = pairs.keySet() ;

        for ( Integer i : allNodes )
        {
            for ( Integer j : allNodes )
            {
                if ( pairs.contains(i,j) )
                {
                    for ( Integer k : allNodes )
                        if ( pairs.contains(j, k))
                            pairs.add(i, k) ;
                }
            }
        }
    }
    
//    // Calculate the closure from start.
//    static void closure(Links links, Integer start, Set<Integer>visited)
//    {
//        if ( visited.contains(start) ) 
//            return ;
//        visited.add(start) ;
//        
//        for ( Integer n : links.get(start) )
//        {
//            closure(links, n, visited) ;
//        }
//    }

    public static void main(String[]a)
    {
        // BUG
        Links pairs = new Links() ;
        pairs.add(1,2) ;
        pairs.add(2,3) ;
        pairs.add(3,4) ;
        pairs.add(4,1) ;
        //pairs.add(4,5) ;
        
        
        
        System.out.println( ) ;
        expand(pairs) ;
        System.out.println("====") ;
        pairs.print() ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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