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

package com.hp.hpl.jena.sparql;


import junit.framework.TestSuite ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

import com.hp.hpl.jena.query.TS_ParamString ;
import com.hp.hpl.jena.sparql.algebra.TS_Algebra ;
import com.hp.hpl.jena.sparql.algebra.optimize.TS_Optimization ;
import com.hp.hpl.jena.sparql.api.TS_API ;
import com.hp.hpl.jena.sparql.core.TS_Core ;
import com.hp.hpl.jena.sparql.engine.TS_Engine ;
import com.hp.hpl.jena.sparql.engine.binding.TS_Binding ;
import com.hp.hpl.jena.sparql.expr.E_Function ;
import com.hp.hpl.jena.sparql.expr.NodeValue ;
import com.hp.hpl.jena.sparql.expr.TS_Expr ;
import com.hp.hpl.jena.sparql.function.user.TS_UserFunctions ;
import com.hp.hpl.jena.sparql.graph.TS_Graph ;
import com.hp.hpl.jena.sparql.lang.TS_Lang ;
import com.hp.hpl.jena.sparql.modify.TS_Update ;
import com.hp.hpl.jena.sparql.negation.TS_Negation;
import com.hp.hpl.jena.sparql.path.TS_Path ;
import com.hp.hpl.jena.sparql.resultset.TS_ResultSet ;
import com.hp.hpl.jena.sparql.solver.TS_Solver ;
import com.hp.hpl.jena.sparql.syntax.TS_SSE ;
import com.hp.hpl.jena.sparql.syntax.TS_Serialization ;
import com.hp.hpl.jena.sparql.transaction.TS_Transaction ;
import com.hp.hpl.jena.sparql.util.TS_Util ;


@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TS_SSE.class
    , TS_Lang.class
    , TS_Graph.class
    , TS_Util.class
    
    , TS_Expr.class
    , TS_UserFunctions.class
    
    , TS_ResultSet.class
    , TS_Binding.class
    , TS_Engine.class
    , TS_Negation.class
    , TS_Solver.class
    , TS_Algebra.class
    , TS_Optimization.class
    , TS_ResultSet.class
    , TS_Serialization.class
    , TS_API.class
    , TS_Core.class
    , TS_Path.class
    , TS_ParamString.class
    , TS_Update.class
    , TS_Transaction.class
})

public class TC_General extends TestSuite
{
    @BeforeClass public static void beforeClass()
    {
        NodeValue.VerboseWarnings = false ;
        E_Function.WarnOnUnknownFunction = false ;
    }
    
    @AfterClass  public static void afterClass()
    {
        
    }
}
