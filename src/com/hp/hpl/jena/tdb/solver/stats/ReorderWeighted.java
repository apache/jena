/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.tdb.solver.stats;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hp.hpl.jena.sparql.util.ALog;

public final class ReorderWeighted extends ReorderPatternBase
{
    private StatsMatcher stats ;
    
    public ReorderWeighted(StatsMatcher stats)
    {
        this.stats = stats ;
    }
    
    @Override
    protected int chooseNext(List<PatternTriple> pTriples)
    {
        if ( DEBUG )
        {
            System.out.println(">> Input") ;
            int i = -1 ;
            for ( PatternTriple pt : pTriples )
            {
                i++ ;
                if ( pt == null )
                {
                    System.out.printf("%d          : null\n", i) ;
                    continue ;
                }
                double w2 = match(pt) ;
                String x = printAbbrev(pt) ;
                System.out.printf("%d %8.0f : %s\n", i, w2, x) ;
            }
        }

        int j = minimum(pTriples) ;
        if ( j < 0 )
            // No weight for any remaining triples 
            j = first(pTriples) ;
        
        if ( DEBUG )
        {
            System.out.println("<< Output: "+j) ;
            String x = printAbbrev(pTriples.get(j)) ;
            System.out.println(x) ;
        }
        
        return  j;
    }

    // Aslo in StategGeneratorBGP
    static String printAbbrev(PatternTriple pTriple)
    {
        if ( pTriple==null )
            return "<null>" ;
        String x = pTriple.toString() ;
        Pattern p = Pattern.compile("http:[^ \n]*[#/]([^/ \n]*)") ;
        Matcher m = p.matcher(x);
        x = m.replaceAll("::$1") ;
        return x ;
 
    }
    
    private int minimum(List<PatternTriple> pTriples)
    {
        int idx = -1 ;
        double w = Double.MAX_VALUE ;
        
        for ( int i = 0 ; i < pTriples.size() ; i++ ) 
        {
            PatternTriple pt =  pTriples.get(i) ;
            if ( pt == null )
                continue ;
            double w2 = match(pt) ;
            if ( w2 < 0 )
            {
                ALog.fatal(getClass(), "w2 < 0 :: "+pt+" "+stats.patterns) ;
                // No match : use an empiricial guess.
                // P != rdf;type
                //SP?
                //?PO
            }
            
            
            if ( w2 >= 0 && w > w2 )
            {
                w = w2 ;
                idx = i ;
            }
        }
        return idx ;
    }

    
    private double match(PatternTriple item)
    {
        return stats.match(item.subject, item.predicate, item.object) ;
    }

    
    private int first(List<PatternTriple> triples)
    {
        for ( int i = 0 ; i < triples.size() ; i++ ) 
        {
            if ( triples.get(i) != null )
                return i ;
        }
        return -2 ;
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