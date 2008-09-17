/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver;

import static com.hp.hpl.jena.tdb.lib.Lib.printAbbrev;

import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.ALog;

import com.hp.hpl.jena.tdb.solver.stats.PatternTriple;
import com.hp.hpl.jena.tdb.solver.stats.ReorderPatternBase;

public class ReorderVarCount extends ReorderPatternBase
{
    //TODO Test!
    @Override
    protected int chooseNext(List<PatternTriple> pTriples)
    {
        // Override.
        //boolean DEBUG = true ;
        
        if ( DEBUG )
        {
            System.out.println(">> Input") ;
            int i = -1 ;
            for ( PatternTriple pt : pTriples )
            {
                i++ ;
                if ( pt == null )
                {
                    System.out.printf("%d    : null\n", i) ;
                    continue ;
                }
                int x = countVars(pt) ;
                System.out.printf("%d : %d : %s\n", i, x, printAbbrev(pt)) ;
            }
        }
        
        int j = choose(pTriples) ;
        if ( j < 0 )
            // No weight for any remaining triples 
            ALog.fatal(this, "Oops - negative index for choosen triple") ;
        
        if ( DEBUG )
        {
            System.out.println("<< Output: "+j) ;
            String x = printAbbrev(pTriples.get(j)) ;
            System.out.println(x) ;
        }
        
        return j ;
    }

    private int choose(List<PatternTriple> components)
    {
        int idx = -1 ;
        int min = 5 ;
        int N = components.size() ;
        for ( int i = 0 ; i < N ; i++ )
        {
            PatternTriple pt = components.get(i) ;
            if ( pt == null )
                continue ;
            int x = countVars(pt) ;
            if ( x < 0 )
                System.err.println("Oops - negative") ;
            if ( x < min )
            {
                min = x ;
                idx = i ;
            }
        }
        return idx ;
    }

    private int countVars(PatternTriple pt)
    {
        int count = 0 ;
        // Var.isVar is null-safe
        if ( Var.isVar(pt.subject.getNode()) )
            count++ ;
        if ( Var.isVar(pt.predicate.getNode()) )
            count++ ;
        if ( Var.isVar(pt.object.getNode()) )
            count++ ;
        return count ;
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