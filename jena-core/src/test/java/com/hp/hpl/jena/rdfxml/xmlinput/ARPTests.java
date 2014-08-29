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

package com.hp.hpl.jena.rdfxml.xmlinput;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * The JUnit test suite for ARP.
 */
public class ARPTests extends java.lang.Object {
	/**
	 * Setting this field to true uses the tests found
	 * on the W3C web site.
	 * The default value false uses a cached corrected
	 * copy of the tests.
	 */
	static public boolean internet = false;
	static IRI wgTestDir =
		IRIFactory.iriImplementation().create("http://www.w3.org/2000/10/rdf-tests/rdfcore/");
	static IRI arpTestDir =
        IRIFactory.iriImplementation().create("http://jcarroll.hpl.hp.com/arp-tests/");
	/** Creates new ARPTests */
	static public Test suite() {
		TestSuite s = new TestSuite("ARP");
		if (internet) {
			s.addTest(NTripleTestSuite.suite(wgTestDir, wgTestDir.toString(), "WG Parser Tests"));
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
		return s;
	}
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
            Enumeration<Test> ee = ts.tests();
            while ( ee.hasMoreElements() ) {
                Test tt = ee.nextElement();
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
	static public void main(String args[]) throws IOException {
		Test ts = suite();
        PrintWriter pw = new PrintWriter(new FileWriter("src/java/test/com/hp/hpl/jena/rdfxml/xmlinput/test/TestPackage.java"));
		pw.println("/*") ;
		pw.println(" * Licensed to the Apache Software Foundation (ASF) under one") ;
		pw.println(" * or more contributor license agreements.  See the NOTICE file") ;
		pw.println(" * distributed with this work for additional information") ;
		pw.println(" * regarding copyright ownership.  The ASF licenses this file") ;
		pw.println(" * to you under the Apache License, Version 2.0 (the") ;
		pw.println(" * \"License\"); you may not use this file except in compliance") ;
		pw.println(" * with the License.  You may obtain a copy of the License at") ;
		pw.println(" *") ;
		pw.println(" *     http://www.apache.org/licenses/LICENSE-2.0") ;
		pw.println(" *") ;
		pw.println(" * Unless required by applicable law or agreed to in writing, software") ;
		pw.println(" * distributed under the License is distributed on an \"AS IS\" BASIS,") ;
		pw.println(" * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.") ;
		pw.println(" * See the License for the specific language governing permissions and") ;
		pw.println(" * limitations under the License.") ;
		pw.println(" */") ;
		pw.println() ;
        pw.println("package com.hp.hpl.jena.rdfxml.xmlinput.test;");
        pw.println("import junit.framework.TestSuite;");
        pw.println("import junit.framework.Test;");
        pw.println("import com.hp.hpl.jena.shared.wg.*;");
        pw.println("public class TestPackage{");
        pw.println("static public Test suite() {");
        String tsname = toJava(ts, pw, "xx");
        pw.println("return " + tsname+ ";");
        pw.println("} }");
        pw.println("");
        pw.flush();
	}

}
