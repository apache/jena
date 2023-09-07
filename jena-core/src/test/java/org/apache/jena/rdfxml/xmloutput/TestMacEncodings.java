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

package org.apache.jena.rdfxml.xmloutput;



import java.io.*;

import junit.framework.TestSuite;
import org.apache.jena.rdf.model.Model ;
import org.apache.jena.rdf.model.test.ModelTestBase ;
import org.apache.jena.rdfxml.xmlinput1.TestsARP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMacEncodings  extends ModelTestBase
{
	private static Logger logger = LoggerFactory.getLogger( TestMacEncodings.class );

	public TestMacEncodings( String name )
		{ super( name ); }

	// JENA-1537
	// Character encoding checks removed due to lack of support in JDK XML parser APIs.
	public static TestSuite inactive_suite()
    	{
	    TestSuite suite = new TestSuite( TestMacEncodings.class );
        suite.setName("Encodings - particular MacRoman etc.");

        try {
            OutputStream out = new ByteArrayOutputStream();

            new OutputStreamWriter(out,"MacRoman");
            InUse = true;
        } catch (Exception e){
            InUse = false;
        }
     if (!InUse){
         logger.warn("MacRoman not supported on this Java installation: mac encoding tests suppressed.");
        return suite;
     }
		suite.addTest(new TestsARP("testARPMacRoman"));
		suite.addTest(new TestsARP("testARPMacArabic"));
	    return suite; }
    static private boolean InUse = false;
    /*
    public void test00InitMacTests() {
        try {
            OutputStream out = new ByteArrayOutputStream();

            Writer wrtr = new OutputStreamWriter(out,"MacRoman");
            InUse = true;
        } catch (Exception e){
            InUse = false;
        }
     if (!InUse){
         logger.warn("MacRoman not supported on this Java installation: mac encoding tests suppressed.");

     }

    }
     */

    public void testXMLWriterMacRoman() throws IOException {
        if (!InUse) return;
        XMLOutputTestBase.blockLogger();
    	Model m = createMemModel();
    	OutputStream fos = new ByteArrayOutputStream();
    	Writer w = new OutputStreamWriter(fos,"MacRoman");
    	m.write(w, "RDF/XML");
    	assertTrue(XMLOutputTestBase.unblockLogger());
    }


    public void testXMLWriteMacArabic() throws IOException {
        if (!InUse) return;
        XMLOutputTestBase.blockLogger();
    	Model m = createMemModel();
    	OutputStream fos = new ByteArrayOutputStream();
    	Writer w = new OutputStreamWriter(fos,"MacRoman");
    	m.write(w, "RDF/XML");
    	assertTrue(XMLOutputTestBase.unblockLogger());
    }




}
