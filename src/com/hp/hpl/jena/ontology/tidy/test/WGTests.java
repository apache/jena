/*
 *  (c) Copyright 2001, 2002 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
 *
*
 * ARPTests.java
 *
 * Created on September 18, 2001, 7:50 PM
 */

package com.hp.hpl.jena.ontology.tidy.test;

import com.hp.hpl.jena.shared.wg.URI;

import junit.framework.TestSuite;
import junit.framework.Test;
import java.io.*;
//import java.util.*;
/**
 * The JUnit test suite for ARP.
 *
 * @author  jjc

 */
public class WGTests extends java.lang.Object {
	/**
	 * Setting this field to true uses the tests found
	 * on the W3C web site.
	 * The default value false uses a cached corrected
	 * copy of the tests.
	 */
	static public boolean internet = false;
	static private URI wgTestDir =
		URI.create("http://www.w3.org/2002/03owlt/");
	static public Test suite() {
		TestSuite s = new TestSuite("OWL-Syntax");
		/*
		if (internet) {
			s.addTest(NTripleTestSuite.suite(wgTestDir, wgTestDir, "WG Parser Tests"));
		} else {
			s.addTest(WGTestSuite.suite(wgTestDir, "wg",
			//    URI.create(
			//    "file://src/com/hp/hpl/jena/rdf/arp/test/data/wg/"),
			"WG Parser Tests"));
			s.addTest(WGTestSuite.suite(arpTestDir, "arp",
			//  URI.create(
			//  "file://src/com/hp/hpl/jena/rdf/arp/test/data/arp-bugs/"),
			"ARP Tests"));
			s.addTest(NTripleTestSuite.suite(wgTestDir, "wg",
			//    URI.create(
			//    "file://src/com/hp/hpl/jena/rdf/arp/test/data/wg/"),
			"NTriple WG Tests"));
		}
		*/
		return s;
	}
	/*
    static int cnt = 0;
    static String toJava(Test s,PrintWriter pw, String wgparent) {
        String name = "test"+cnt++;
        if ( s instanceof TestSuite ) {
            TestSuite ts = (TestSuite)s;
            if ( s instanceof WGTestSuite ) {
            pw.println("WGTestSuite "+name + " = " + ((WGTestSuite)s).createMe+";");
             wgparent = name;
            } else {
            pw.println("TestSuite "+name + " = new TestSuite(\""+ts.getName()+"\");");
            }
            Enumeration ee = ts.tests();
            while ( ee.hasMoreElements() ) {
                Test tt = (Test)ee.nextElement();
                if ( tt == null )
                   continue;
                String sub = toJava(tt,pw ,wgparent);
                pw.println(name+".addTest("+sub+");");
            }
        }
        else if ( s instanceof WGTestSuite.Test ) {
            String className = s.getClass().getName();
            String localPart = className.substring(className.lastIndexOf('$')+1);
            pw.println("Test "+ name + " = " + wgparent +".create" + localPart +"(" 
+ ((WGTestSuite.Test)s).createMe() + ");");
        }
        else {
            pw.println(name + " is of class " + s.getClass().getName());
        }
            
           
        return name;
    }
    */
	static public void main(String args[]) throws IOException {
		Test ts = suite();
        PrintWriter pw = new PrintWriter(new FileWriter("src/com/hp/hpl/jena/rdf/arp/test/TestPackage.java"));
        pw.println("/*");
        pw.println(" *  (c) Copyright 2002-2003 Hewlett-Packard Development Company, LP") ;
        pw.println(" *  All rights reserved.");
        pw.println(" *");
        pw.println(" * Redistribution and use in source and binary forms, with or without");
        pw.println(" * modification, are permitted provided that the following conditions");
        pw.println(" * are met:");
        pw.println(" * 1. Redistributions of source code must retain the above copyright");
        pw.println(" *    notice, this list of conditions and the following disclaimer.");
        pw.println(" * 2. Redistributions in binary form must reproduce the above copyright");
        pw.println(" *    notice, this list of conditions and the following disclaimer in the");
        pw.println(" *    documentation and/or other materials provided with the distribution.");
        pw.println(" * 3. The name of the author may not be used to endorse or promote products");
        pw.println(" *    derived from this software without specific prior written permission.");
        pw.println("");
        pw.println(" * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR");
        pw.println(" * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES");
        pw.println(" * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.");
        pw.println(" * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,");
        pw.println(" * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT");
        pw.println(" * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,");
        pw.println(" * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY");
        pw.println(" * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT");
        pw.println(" * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF");
        pw.println(" * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.");
        pw.println(" *");
        pw.println(" */");
        
        pw.println("package com.hp.hpl.jena.rdf.arp.test;");
        pw.println("import junit.framework.TestSuite;");
        pw.println("import junit.framework.Test;");
        pw.println("public class TestPackage{");
        pw.println("static public Test suite() {");
   //     String tsname = toJava(ts, pw, "xx");
   //    pw.println("return " + tsname+ ";");
        pw.println("} }");
        pw.println("");
        pw.flush();
	}

}