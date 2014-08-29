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
import org.apache.jena.iri.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import java.net.*;
public class URITests
	extends TestCase {
    // TODO: not for 2.3 relative/absolute tests
	static public Test suite() {
		TestSuite suite = new TestSuite("URIs");
		suite.addTest(new URITests("testNoDomain"));
		suite.addTest(new URITests("testLong"));
        suite.addTest(new URITests("testBadScheme"));
        suite.addTest(new URITests("testJustScheme"));
        
        // TODO: not for 2.3. are these tests right?
//        suite.addTest(new URITests("testBadHost"));
//        suite.addTest(new URITests("testBadPort"));
//      suite.addTest(new URITests("testBadUserHost"));
        suite.addTest(new URITests("testHostPortNoSlashWithFragment"));
        suite.addTest(new URITests("testHostNoSlashWithFragment"));

        suite.addTest(new URITests("testBadAuthority"));
        suite.addTest(new URITests("testTwoHashes"));
        suite.addTest(new URITests("testTwoHashes2"));
		return suite;
	}

	URITests(String s) {
		super(s);
	}

    static IRIFactory factory = IRIFactory.jenaImplementation();
//    static {
//        factory.useSpecificationRDF(false);
//    }
	public void testURI(String uri, boolean ok) {
		    IRI ref =
            factory.create(uri);
            if (ok && ref.hasViolation(false)) {
                Violation v = ref.violations(false).next();
                fail("<" + uri + "> is expected to be a URI, but: "+v.getLongMessage());
            }
			assertEquals("<" + uri + "> is"+(ok?" ":" not ")+"a URI", ok, !ref.hasViolation(false));
            
	}
	public void testNoDomain()  {
       testURI("app://calendar/event",true);
	}
	public void testLong()  {
	   testURI("http://46229EFFE16A9BD60B9F1BE88B2DB047ADDED785/demo.mp3",true);
	}
    
    public void testBadScheme()  {
           testURI("ht^tp://www.w3.org/demo.mp3",false);
        }

    public void testFragmentLooksLikeScheme()  {
           testURI("ht#tp://www.w3.org/demo.mp3",true);
        }
     
    public void testHostNoSlashWithFragment()  {
        testURI("http://www.w#3.org/demo.mp3",true);
     }
    public void testHostPortNoSlashWithFragment()  {
        testURI("http://www.w3.org:1#4/demo.mp3",true);
     }
	
    public void testBadHost()  {
        testURI("http://www.w+3.org/demo.mp3",false);
     }

    // TODO: not for 2.3. Is this test correct?
    public void testJustScheme()  {
        testURI("http:",false);
     }
    public void testBadPort()  {
        testURI("http://www.w3.org:1+4/demo.mp3",false);
     }
    public void testBadUserHost()  {
        testURI("http://jjc@www.w@3.org/demo.mp3",false);
     }
    public void testBadAuthority()  {
        testURI("http://jjc^3.org/demo.mp3",false);
     }
    public void testTwoHashes() {
        testURI("ht#tp://jjc3.org/demo.mp3#frag",false);
            
    }
    public void testTwoHashes2() {
        testURI("http://jjc#3.org/demo.mp3#frag",false);
            
    }
}
