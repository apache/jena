/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
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

 
    static Specification specs[] = (Specification[]) Specification.all
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
   
    public void runTest() {
        IRI iri = f.create(uri);
        Iterator it = iri.violations(true);
        while (it.hasNext()) {
            Violation v = (Violation) it.next();
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

/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

