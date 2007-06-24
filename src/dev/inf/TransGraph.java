/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.inf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hp.hpl.jena.sdb.util.Pair;

/** In-memory structure to create a transitive closure over GNodes */

public class TransGraph<GNode> extends HashMap<GNode, HashSet<GNode>> implements Iterable<Pair<GNode, GNode>>
{
    public interface LinkApply<T> { public void apply(T i, T j) ; }
    public interface NodeApply<T> { public void apply(T i) ; }
    
    public void add(GNode i, GNode j)
    {
        Set<GNode> x = ensure(i) ;
        x.add(j) ;
        ensure(j) ;
    }

    private Set<GNode> ensure(GNode i)
    {
        HashSet<GNode> x = get(i) ;
        if ( x != null )
            return x ;
        x = new HashSet<GNode>() ;
        put(i, x) ;
        return x ; 
    }

    public boolean contains(GNode i, GNode j)
    {
        Set<GNode> x = get(i) ;
        if ( x == null ) return false ;
        return x.contains(j) ;
    }
    
    public boolean contains(GNode i)
    {
        return contains(i) ;
    }
    
    /*  
        http://www.cs.duke.edu/~reynolds/149/warshall.c.html
       15: void warshall(int size) {
       16:   register int i, j, k;
       17:   for (k=size-1; k>=0; k--)
       18:     for (i=size-1; i>=0; i--)
       19:       for (j=size-1; j>=0; j--) {
       20:         double dist = graph[i][k] + graph[k][j];
       21:         if (dist < graph[i][j]) graph[i][j] = dist;
       22:       }
     */

    // Walshall's algorithm
    // Adapted only in that it works on the sparse graph
    // representation used to record the links.

    public void expand()
    {
        // Iterator over all the nodes (whether they have links initially or not).
        // Because this is not updated by any new additions (they only link
        // existing nodes), it is safe to iterate over the set while adding
        // new links.

        Set<GNode> allNodes = keySet() ;

//      for ( Integer k : allNodes )
//      for ( Integer i : allNodes )
//      for ( Integer j : allNodes )
//      if ( pairs.contains(i, k) &&  pairs.contains(k,j) )
//      pairs.add(i, j) ;

        // Move test out one level.
        for ( GNode k : allNodes )
            for ( GNode i : allNodes )
                if ( contains(i, k) )
                    for ( GNode j : allNodes )
                        if ( contains(k,j) )
                            add(i, j) ;
    }
    
    public void expandReflexive()
    {
        for ( GNode i : keySet() )
            add(i,i) ;
    }

    public Iterator<Pair<GNode, GNode>>iterator()
    {
        List<Pair<GNode, GNode>> x = new ArrayList<Pair<GNode, GNode>>() ;
        for ( GNode k : keySet() )
            for ( GNode v : get(k) )
                x.add(new Pair<GNode, GNode>(k, v)) ; 
        return x.iterator() ;
    }
    
    

    public void linkApply(LinkApply<GNode> a)
    {
        for ( GNode i : keySet() )
            for ( GNode j : get(i) )
                a.apply(i, j) ;        
    }
    
    public void nodeApply(NodeApply<GNode> a)
    {
        for ( GNode i : keySet() )
            a.apply(i) ;        
    }
    
    public void printLinks()
    {
        linkApply(new LinkApply<GNode>()
                  {
            public void apply(GNode i, GNode j)
            {
                System.out.printf("%s %s\n", i, j) ;
            } 
                  }) ;
    }
    public void print()
    {
        for ( GNode i : keySet() )
        {
            System.out.printf("%s :", i) ;
            for ( GNode j : get(i) )
                System.out.printf(" %s", j) ;
            System.out.println() ;
        }
    }
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