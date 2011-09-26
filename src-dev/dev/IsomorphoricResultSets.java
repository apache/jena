/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
