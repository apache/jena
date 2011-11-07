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

package com.hp.hpl.jena.regression;

import com.hp.hpl.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author  bwm
 * @version $Revision: 1.1 $
 */
public class testNTripleReader extends Object {
   
    
    protected static void doTest(Model m1) {
        (new testNTripleReader()).test(m1);
    }
    
    protected static Logger logger = LoggerFactory.getLogger( testNTripleReader.class );
    
    void test(Model m1) {

        String  test = "testNTripleReader";
        String  filebase = "testing/regression/" + test + "/";
    //    System.out.println("Beginning " + test);
        int n = 0;
        try {
                 empty(m1);
            n++; m1.read(ResourceReader.getInputStream(filebase + "1.nt"), "", "N-TRIPLE");
                 if (m1.size() != 5) error(test, n);
                 StmtIterator iter = 
                     m1.listStatements( null, null, "foo\"\\\n\r\tbar" );
            n++; if (! iter.hasNext()) error(test, n);
        } catch (Exception e) {
            inError = true;
            logger.error( " test " + test + "[" + n + "]", e);
        }
  //      System.out.println("End of " + test);        
    }
    
    protected void empty(Model m) {
        StmtIterator iter = m.listStatements();
        while (iter.hasNext()) {
            iter.nextStatement();
            iter.remove();
        }
    }
    private boolean inError = false;
       
    protected void error(String test, int n) {
        System.out.println(test + ": failed test " + Integer.toString(n));
        inError = true;
    }
    
    public boolean getErrors() {
        return inError;
    }
     
}
