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

import com.hp.hpl.jena.sparql.core.TestContext ;
import com.hp.hpl.jena.sparql.core.TestDatasetDataSource ;
import com.hp.hpl.jena.sparql.core.TestDatasetGraphMem ;
import com.hp.hpl.jena.sparql.core.TestDatasetGraphMemTriplesQuads ;
import com.hp.hpl.jena.sparql.core.TestEsc ;
import com.hp.hpl.jena.sparql.expr.TestExpressions ;
import com.hp.hpl.jena.sparql.path.TestPath ;
import com.hp.hpl.jena.sparql.syntax.TestSerialization ;

public class TS_General extends TestSuite
{
    static final String testSetName         = "General" ;

    static public TestSuite suite() { return new TS_General(); }

    public TS_General()
    {
        super(TS_General.class.getName()) ;
        // Need to check each is JUnit 4 compatible then remove all .suite and use @RunWith(Suite.class) @SuiteClasses
        addTest(TestExpressions.suite()) ;
        addTest(TestPath.suite()) ;
        addTest(TestEsc.suite()) ;
        addTest(TestSerialization.suite()) ;
        addTest(TestContext.suite()) ;
        addTest(TestDatasetDataSource.suite()) ;
        addTest(TestDatasetGraphMem.suite()) ;
        addTest(TestDatasetGraphMemTriplesQuads.suite()) ;
    }
}
