/*
 * Copyright 2014 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.reasoner.sparqlinrules.test;

import com.hp.hpl.jena.reasoner.test.TestInfModel;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Test;


public class SparqlinRulesTest1 extends TestCase {
    
    SparqlinRulesTest2 SparqlinRulesTest2_backward;
    SparqlinRulesTest2 SparqlinRulesTest2_forwardRETE;
    SparqlinRulesTest2 SparqlinRulesTest2_hybrid;
    SparqlinRulesTest2 SparqlinRulesTest2_forward;
    
    public SparqlinRulesTest1( String name ) {
        super( name ); 
        inic_data();
    }
    
    public SparqlinRulesTest1(  ) {
        inic_data();
    }
            
    private void inic_data() {
        SparqlinRulesTest2_backward = new SparqlinRulesTest2("backward");
        SparqlinRulesTest2_forwardRETE = new SparqlinRulesTest2("forwardRETE");
        SparqlinRulesTest2_hybrid = new SparqlinRulesTest2("hybrid");
        SparqlinRulesTest2_forward = new SparqlinRulesTest2("forward");
    }
    
    public static TestSuite suite() {
        return new TestSuite(SparqlinRulesTest1.class);
    }  

    
    //@Test
    public void test_backward() {
        SparqlinRulesTest2_backward.run();
  
    }
    
    //@Test
    public void test_forwardRETE() {
        SparqlinRulesTest2_forwardRETE.run();
    }
    
    //@Test
    public void test_hybrid() {
        SparqlinRulesTest2_hybrid.run();
    }
    
    //@Test
    public void test_forward() {
        SparqlinRulesTest2_forward.run();
    }
}
