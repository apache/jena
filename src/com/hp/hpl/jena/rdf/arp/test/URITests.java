/*
 *  (c)     Copyright 2000-2004 Hewlett-Packard Development Company, LP
 *   All rights reserved.
 * [See end of file]
 *  $Id: URITests.java,v 1.2 2004-07-05 16:09:42 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.rdf.arp.test;
import org.apache.commons.logging.*;
import junit.framework.*;

import com.hp.hpl.jena.rdf.arp.*;
//import java.net.*;
/**
 * @author jjc
 *
 */
public class URITests
	extends TestCase {
	static private Log logger = LogFactory.getLog(URITests.class);
	static public Test suite() {
		TestSuite suite = new TestSuite("URIs");
		suite.addTest(new URITests("testNoDomain"));
	//	suite.addTest(new URITests("testLong"));
		return suite;
	}

	URITests(String s) {
		super(s);
	}

	public void testURI(String uri, boolean ok) {
		try {
			new URI(uri);
			assertTrue("<" + uri + "> is not a URI", ok);
		}
		catch (MalformedURIException e){
	//	catch (URISyntaxException e)	{
		 assertFalse("<" + uri + "> is a URI", ok);
		}		
	}
	public void testNoDomain()  {
       testURI("app://calendar/event",true);
	}
	public void testLong()  {
	   testURI("http://46229EFFE16A9BD60B9F1BE88B2DB047ADDED785/demo.mp3",true);
	}
	
}

/*
    (c) Copyright 2000-2004 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/