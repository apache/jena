/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package dev;

import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.query.ResultSetRewindable ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderResultSet ;

public class IsomorphoricResultSets
{
    
    /*
     * 
---------------
| x    | y    |
===============
| _:b0 | _:b1 |
| _:b2 | _:b3 |
| _:b1 | _:b0 |
---------------
Expected: 3 -----------------------------
---------------
| y    | x    |
===============
| _:b0 | _:b1 |
| _:b2 | _:b3 |
| _:b3 | _:b2 |
---------------
or
---------------
| x    | y    |
===============
| _:b1 | _:b0 |
| _:b3 | _:b2 |
| _:b2 | _:b3 |
---------------

     */
    
    // nasty result set.
    // These are the same but the first row of rs2$ throws in a wrong mapping of b0/c1
    // Right mapping is:
    // b0->c3, b1->c2, b2->c1, b3->c0
    // Currently we get:
    // b0->c1, b1->c0, b2->c3, b3->c2, then last row fails.
    
    static String[] rs1$ = {
        "(resultset (?x ?y)",
        "   (row (?x _:b0) (?y _:b1))",
        "   (row (?x _:b2) (?y _:b3))",
        "   (row (?x _:b1) (?y _:b0))",
        "   (row (?x 1) (?y 2))",
        ")"} ;
    static String[] rs2$ = {
        "(resultset (?x ?y)",
        "   (row (?x _:c1) (?y _:c0))",
        "   (row (?x _:c3) (?y _:c2))",
        "   (row (?x _:c2) (?y _:c3))",
        "   (row (?x 01) (?y 02))",
        ")"} ;
    
    
    static ResultSetRewindable rs1 = ResultSetFactory.makeRewindable(BuilderResultSet.build(SSE.parseItem(StrUtils.strjoinNL(rs1$)))) ;
    static ResultSetRewindable rs2 = ResultSetFactory.makeRewindable(BuilderResultSet.build(SSE.parseItem(StrUtils.strjoinNL(rs2$)))) ;

    public static void main(String[] argv) throws Exception
    {
        System.out.println("rs1") ;
        ResultSetFormatter.out(rs1) ;
        System.out.println("rs2") ;
        ResultSetFormatter.out(rs2) ;
        
        System.out.println("Isomorphic: "+ResultSetCompare.isomorphic(rs1, rs2)) ;
        rs1.reset() ; rs2.reset() ;
        
        System.out.println("Same value: "+ResultSetCompare.equalsByValue(rs1, rs2)) ;
        rs1.reset() ; rs2.reset() ;
        
    }
}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
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