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

package com.hp.hpl.jena.sdb.test.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.sdb.exprmatch.*;


public class TestExprMatch
{
    // ---- Basic tests
    @Test public void match_0()
    {
        MapAction mapAction = new MapAction() ;
        match("?x", "?a", mapAction, null) ;
    }
    
    @Test public void match_1()
    {
        MapAction mapAction = new MapAction() ;
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a"), "?x") ;
        
        match("?x", "?a", mapAction, null) ;
    }
    
    @Test public void match_2()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a"), new ActionMatchVar()) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a"), ExprUtils.parse("?x")) ;
        
        match("?x", "?a", mapAction, mapResult) ;
    }
    
    @Test public void match_3()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a"), new ActionMatchNoBind()) ;
        
        MapResult mapResult = new MapResult() ;
        
        match("?x", "?a", mapAction, mapResult) ;
    }
    
    @Test public void match_4()
    {
        MapAction mapAction = new MapAction() ;
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a"), "1") ;   // Value one
        
        match("1", "?a", mapAction, mapResult) ;
    }
    
    @Test public void match_5()
    {
        MapAction mapAction = new MapAction() ;
        noMatch("?a", "1", mapAction) ;
    }
    
    @Test public void struct_1()
    {
        MapAction mapAction = new MapAction() ;
        MapResult mapResult = new MapResult() ;
        
        match("1+2=3", "(1+2)=3", mapAction, null) ;
    }
    
    @Test public void struct_2()
    {
        MapAction mapAction = new MapAction() ;
        MapResult mapResult = new MapResult() ;
        
        match("1+2+3", "(1+2)+3", mapAction, null) ;
    }
    
    @Test public void struct_3()
    {
        MapAction mapAction = new MapAction() ;
        // Different structures.
        noMatch("1+2+3", "1+(2+3)", mapAction) ;
    }
    
    // ---- Comparison tests
    @Test public void cond_1()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        mapAction.put(Var.alloc("a2"), new ActionMatchBind()) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "?x") ;
        mapResult.put(Var.alloc("a2"), "3") ;
        
        match("?x < 3",
              "?a1 < ?a2",
              mapAction, mapResult) ;
    }
    
    @Test public void cond_2()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        mapAction.put(Var.alloc("a2"), new ActionMatchBind()) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "?x") ;
        mapResult.put(Var.alloc("a2"), "3") ;
        
        noMatch("?x < 3", "?a1 > ?a2", mapAction) ;
    }
    
    
    // ---- Regex tests
    @Test public void regex_1()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        mapAction.put(Var.alloc("a2"), new ActionMatchString()) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "?x") ;
        mapResult.put(Var.alloc("a2"), "'smith'") ;
        
        match("regex(?x , 'smith')",
              "regex(?a1 , ?a2)",
              mapAction, mapResult) ;
    }
    
    @Test public void regex_2()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        mapAction.put(Var.alloc("a2"), new ActionMatchString()) ;
        mapAction.put(Var.alloc("a3"), new ActionMatchString()) ;
        
        noMatch("regex(?x , 'smith')",
                "regex(?a1 , ?a2, ?a3)",
                mapAction) ;
    }
    
    @Test public void regex_3()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        mapAction.put(Var.alloc("a2"), new ActionMatchString()) ;
        mapAction.put(Var.alloc("a3"), new ActionMatchString()) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "?x") ;
        mapResult.put(Var.alloc("a2"), "'smith'") ;
        mapResult.put(Var.alloc("a3"), "'i'") ;
        
        match("regex(?x , 'smith', 'i')",
              "regex(?a1, ?a2, ?a3)",
              mapAction, mapResult) ;
    }

    @Test public void regex_4()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        mapAction.put(Var.alloc("a2"), new ActionMatchString()) ;
        mapAction.put(Var.alloc("a3"), new ActionMatchExact("'i'")) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "?x") ;
        mapResult.put(Var.alloc("a2"), "'smith'") ;
        mapResult.put(Var.alloc("a3"), "'i'") ;
        
        match("regex(?x , 'smith', 'i')",
              "regex(?a1, ?a2, ?a3)",
              mapAction, mapResult) ;
    }

    @Test public void regex_5()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        mapAction.put(Var.alloc("a2"), new ActionMatchString()) ;
        
        noMatch("regex(?x , 'smith', 'i')",
                "regex(?a1, ?a2)",
                mapAction) ;
    }
    
    @Test public void regex_6()
    {
        MapAction mapAction = new MapAction() ;
        //mapAction.put(Var.alloc("a1"), new ActionMatch
        mapAction.put(Var.alloc("a2"), new ActionMatchString()) ;
        mapAction.put(Var.alloc("a3"), new ActionMatchExact("'i'")) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "str(?x)") ;
        mapResult.put(Var.alloc("a2"), "'smith'") ;
        mapResult.put(Var.alloc("a3"), "'i'") ;
        
        match("regex(str(?x) , 'smith', 'i')",
              "regex(?a1, ?a2, ?a3)",
              mapAction, mapResult) ;
    }

    @Test public void regex_7()
    {
        MapAction mapAction = new MapAction() ;
        //mapAction.put(Var.alloc("a1"), new ActionMatch
        mapAction.put(Var.alloc("a2"), new ActionMatchString()) ;
        mapAction.put(Var.alloc("a3"), new ActionMatchExact("'i'")) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "?x") ;
        mapResult.put(Var.alloc("a2"), "'smith'") ;
        mapResult.put(Var.alloc("a3"), "'i'") ;
        
        match("regex(str(?x) , 'smith', 'i')",
              "regex(str(?a1), ?a2, ?a3)",
              mapAction, mapResult) ;
    }
    
    
    @Test public void regex_8()
    {
        MapAction mapAction = new MapAction() ;
        //mapAction.put(Var.alloc("a1"), new ActionMatch
        mapAction.put(Var.alloc("a2"), new ActionMatchString()) ;
        mapAction.put(Var.alloc("a3"), new ActionMatchExact("'i'")) ;
        
        noMatch("regex(?x , 'smith', 'i')",
                "regex(str(?a1), ?a2, ?a3)",
                mapAction) ;
    }

    @Test public void function_1()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "?x") ;
        
        match("fn:not(?x)",
              "fn:not(?a1)",
              mapAction,
              mapResult) ;
    }
    
    @Test public void function_2()
    {
        MapAction mapAction = new MapAction() ;
        mapAction.put(Var.alloc("a1"), new ActionMatchVar()) ;
        
        MapResult mapResult = new MapResult() ;
        mapResult.put(Var.alloc("a1"), "?x") ;
        
        noMatch("fn:not(?x)",
                "fn:notNot(?a1)",
                mapAction) ;
    }
    //    //Run JUnit4 tests in a JUnit3 environment
    //    public static junit.framework.Test suite()
    //    { 
    //        return new JUnit4TestAdapter(TestExprMatch.class); 
    //    }
        
        
        private MapResult match(String expr, String pattern, MapAction aMap, MapResult expected)
        {
            MapResult rMap = ExprMatcher.match(expr, pattern, aMap) ; 
            assertNotNull(rMap) ;
            if ( expected != null )
                assertEquals(expected, rMap) ;
            return rMap ;
        }

    private void noMatch(String expr, String pattern, MapAction aMap)
    {
        MapResult rMap = ExprMatcher.match(expr, pattern, aMap) ; 
        assertNull(rMap) ;
    }
}
