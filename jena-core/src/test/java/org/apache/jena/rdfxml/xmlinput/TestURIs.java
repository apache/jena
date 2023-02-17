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

package org.apache.jena.rdfxml.xmlinput;

import java.util.ArrayList;
import java.util.List;

//// LEGACY

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.jena.irix.IRIException;
import org.apache.jena.irix.IRIx;

public class TestURIs
	extends TestCase {
    // TODO: not for 2.3 relative/absolute tests
	static public Test suite() {
		TestSuite suite = new TestSuite(TestURIs.class.getCanonicalName());
		suite.addTest(new TestURIs("testNoDomain"));
		suite.addTest(new TestURIs("testLong"));
        suite.addTest(new TestURIs("testBadScheme"));
        suite.addTest(new TestURIs("testJustScheme"));

        // TODO: not for 2.3. are these tests right?
//        suite.addTest(new URITests("testBadHost"));
//        suite.addTest(new URITests("testBadPort"));
//      suite.addTest(new URITests("testBadUserHost"));
        suite.addTest(new TestURIs("testHostPortNoSlashWithFragment"));
        suite.addTest(new TestURIs("testHostNoSlashWithFragment"));

        suite.addTest(new TestURIs("testBadAuthority"));
        suite.addTest(new TestURIs("testTwoHashes"));
        suite.addTest(new TestURIs("testTwoHashes2"));
		return suite;
	}

	TestURIs(String s) {
		super(s);
	}

	private void execTestURI(String uri, boolean ok) {
	    IRIx irix;

	    try {
	        irix = IRIx.create(uri);
	    } catch (IRIException ex) {
	        if ( ok )
	            fail("<" + uri + "> is expected to be an invalid URI, but: "+ex.getMessage());
	        return;
	    }
	    List<String> errorMessages = new ArrayList<>();
	    irix.handleViolations((isError, msg)->{
	        if ( isError )
	            errorMessages.add(msg);
	    });

	    if ( ok )
	        assertTrue("<" + uri + "> is expected to be a URI, but: "+errorMessages, errorMessages.isEmpty() );
	    else
	        assertTrue("<" + uri + "> : expected an error message", !errorMessages.isEmpty());
	}

	public void testNoDomain()  {
       execTestURI("app://calendar/event",true);
	}
	public void testLong()  {
	   execTestURI("http://46229EFFE16A9BD60B9F1BE88B2DB047ADDED785/demo.mp3",true);
	}

    public void testBadScheme()  {
           execTestURI("ht^tp://www.w3.org/demo.mp3",false);
        }

    public void testFragmentLooksLikeScheme()  {
           execTestURI("ht#tp://www.w3.org/demo.mp3",true);
        }

    public void testHostNoSlashWithFragment()  {
        execTestURI("http://www.w#3.org/demo.mp3",true);
     }
    public void testHostPortNoSlashWithFragment()  {
        execTestURI("http://www.w3.org:1#4/demo.mp3",true);
     }

    public void testBadHost()  {
        execTestURI("http://www.w+3.org/demo.mp3",false);
     }

    // TODO: not for 2.3. Is this test correct?
    public void testJustScheme()  {
        execTestURI("http:",false);
     }
    public void testBadPort()  {
        execTestURI("http://www.w3.org:1+4/demo.mp3",false);
     }
    public void testBadUserHost()  {
        execTestURI("http://jjc@www.w@3.org/demo.mp3",false);
     }
    public void testBadAuthority()  {
        execTestURI("http://jjc^3.org/demo.mp3",false);
     }
    public void testTwoHashes() {
        execTestURI("ht#tp://jjc3.org/demo.mp3#frag",false);

    }
    public void testTwoHashes2() {
        execTestURI("http://jjc#3.org/demo.mp3#frag",false);
    }
}
