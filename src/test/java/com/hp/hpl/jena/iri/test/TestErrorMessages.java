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

package com.hp.hpl.jena.iri.test;

import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.iri.IRI;
import com.hp.hpl.jena.iri.IRIFactory;
import com.hp.hpl.jena.iri.Violation;
import com.hp.hpl.jena.iri.ViolationCodes;
import com.hp.hpl.jena.iri.impl.IRIExamples;
import com.hp.hpl.jena.iri.impl.Specification;
import com.hp.hpl.jena.iri.impl.ViolationCodeInfo;

public class TestErrorMessages extends TestCase
   implements ViolationCodes {
    static {
        new ViolationCodes.Initialize();
    }

 
    static Specification specs[] = Specification.all
                    .values().toArray(new Specification[0]);


    String uri;

    ViolationCodeInfo violation;

   
    
    boolean good;

    public TestErrorMessages( String uri, ViolationCodeInfo info, boolean good) {
        super(escapeAndShorten(uri));
        this.uri = uri;
        this.violation = info;
        this.good = good;
    }



    private static String escapeAndShorten(String uri2) {
        StringBuffer rslt = new StringBuffer();
        int ln = uri2.length();
        if (ln > 80)
            ln = 80;
        for (int i = 0; i < ln; i++) {
            int ch = uri2.charAt(i);
            if (ch > 127 || ch < 32) {
                rslt.append("&#");
                rslt.append(ch);
                rslt.append(";");
            } else
                rslt.append((char) ch);
        }
        return rslt.toString();
    }


   

	private void printErrorMessages(Violation v) {
			System.err.println(v.getShortMessage());
		
	}


    static public IRIFactory f = IRIFactory.jenaImplementation();
   
    @Override
    public void runTest() {
        IRI iri = f.create(uri);
        Iterator<Violation> it = iri.violations(true);
        while (it.hasNext()) {
            Violation v = it.next();
            printErrorMessages(v);
            
        }
    }

    public static TestSuite suite() {
        TestSuite rslt = new TestSuite();

        rslt.setName("Error messages");
        addAllTestsFromExamples( rslt);
 
        return rslt;
    }

    private static void addAllTestsFromExamples( TestSuite spec) {
        for (int i = 0; i < ViolationCodeInfo.all.length; i++) {
            addTestsFromExamples(spec,  ViolationCodeInfo.all[i]);
        }
        for (int i = 0; i< specs.length; i++)
        	addExamples(null,specs[i],spec);
    }

    private static void addTestsFromExamples(TestSuite rslt,  ViolationCodeInfo violationCodeInfo) {
      
        if (violationCodeInfo != null) {
            TestSuite ex = new TestSuite();
            ex.setName(violationCodeInfo.getCodeName());
            addExamples(violationCodeInfo, violationCodeInfo, ex);
            if (ex.countTestCases()>0)
            rslt.addTest(ex);
        }
    }

    private static void addExamples(ViolationCodeInfo violationCodeInfo, IRIExamples examples, TestSuite ex) {
        String e[] = examples.getBadExamples();
        for (int j = 0; j < e.length; j++)
            ex.addTest(new TestErrorMessages(e[j], violationCodeInfo,false));
        e = examples.getGoodExamples();
        for (int j = 0; j < e.length; j++)
            ex.addTest(new TestErrorMessages(e[j], violationCodeInfo,true));
    }

}
