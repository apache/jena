/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev.opt;

import java.util.Map;
import java.util.Set;

import org.openjena.atlas.logging.Log ;


import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.sse.SSE;

public class Driver
{
    static String divider = "" ;
    static String nextDivider = null ;
    static void divider()
    {
        if ( nextDivider != null )
            System.out.println(nextDivider) ;
        nextDivider = "" ;
    }

    static { Log.setLog4j() ; }
 
    public static void main(String ... args)
    {
        Op op = SSE.parseOp("(join (bgp (?w :q 123)) (filter (!= ?x :X) (bgp (?x :p ?v) (?x :q ?w))))") ;
        
        Map <Op, Set<Var>> x = Scope.scopeMap(op) ;
        if ( false )
        {
            for ( Op k : x.keySet() )
            {
                Set<Var> vars = x.get(k) ;
                String s = k.toString();
                s = s.replaceAll("\n", " ") ;
                s = s.replaceAll("\r", " ") ;
                s = s.replaceAll("  +", " ") ;
                System.out.println(vars+" <==> "+s) ;
            }
        }
        
        System.out.println(op) ;
        op = Reorganise.reorganise(op, x) ;
        System.out.println(op) ;
        System.out.println("----") ;
        
        System.exit(0) ;

    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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