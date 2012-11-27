/*
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

package com.hp.hpl.jena.sparql.solver;

import org.apache.jena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.BasicPattern ;
import com.hp.hpl.jena.sparql.engine.optimizer.StatsMatcher ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;

public class TestSolverLib
{
    public static BasicPattern bgp(String str)
    {
        String s1 = "(prefix ((: <http://example/>)) " ;
        String s2 = ")" ;
        return SSE.parseBGP(s1+str+s2) ;
    }
    
//    private static StatsMatcher matcher(String str)
//    {
//        String s1 = "(prefix ((: <http://example/>))\n(stats " ;
//        String s2 = "))" ;
//        Item item = SSE.parse(s1+str+s2) ;
//        return new StatsMatcher(item) ; 
//    }
//    
    public static StatsMatcher matcher(String... str)
    {
        String s1 = "(prefix ((: <http://example/>))\n(stats " ;
        String s2 = "))" ;
        
        String x = StrUtils.strjoinNL(str) ;
        
        Item item = SSE.parse(s1+x+s2) ;
        return new StatsMatcher(item) ; 
    }

    public static Triple triple(String str)
    {
        String s1 = "(prefix ((: <http://example/>)) " ;
        String s2 = ")" ;
        return SSE.parseTriple(s1+str+s2) ;
    }

}
