/*
 *  (c)     Copyright Hewlett-Packard Company 2000-2003
 *   All rights reserved.
 * [See end of file]
 *  $Id: MoreTests.java,v 1.4 2003-04-15 21:13:07 jeremy_carroll Exp $
 */

package com.hp.hpl.jena.rdf.arp.test;
import junit.framework.*;
import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.mem.ModelMem;
import java.io.*;
/**
 * @author jjc
 *
 */
public class MoreTests extends TestCase implements RDFErrorHandler, ARPErrorNumbers {
   static public Test suite() {
    TestSuite suite = new TestSuite("ARP Plus");
    suite.addTest(new MoreTests("testEncodingMismatch1"));
    suite.addTest(new MoreTests("testEncodingMismatch2"));
    return suite;
   }
   MoreTests(String s){
    super(s);
   }
   public void testEncodingMismatch1() throws IOException {
      Model m = new ModelMem();
      RDFReader rdr = m.getReader();
      FileReader r = new FileReader("testing/wg/rdfms-syntax-incomplete/test001.rdf");
     if ( r.getEncoding().startsWith("UTF")) {
       System.err.println("WARNING: Encoding mismatch tests not executed on platform with default UTF encoding.");
       return;
     }
      rdr.setErrorHandler(this);
    expected = new int[]{WARN_ENCODING_MISMATCH};
      rdr.read(m,r,"http://example.org/");
      //System.err.println(m.size() + " triples read.");
      checkExpected();
      
   }
    public void testEncodingMismatch2() throws IOException {
       Model m = new ModelMem();
       RDFReader rdr = m.getReader();
       FileReader r = new FileReader("testing/wg/rdf-charmod-literals/test001.rdf");
       if ( r.getEncoding().startsWith("UTF")) {
        // see above for warning message.
         return;
       }
       rdr.setErrorHandler(this);
       expected = new int[]{WARN_ENCODING_MISMATCH,ERR_ENCODING_MISMATCH};
       rdr.read(m,r,"http://example.org/");

        checkExpected();
    }
    private void checkExpected() {
        for (int i=0; i<expected.length; i++)
          if (expected[i]!=0) {
            fail("Expected error: "
            + JenaReader.errorCodeName(expected[i]) + " but it did not occur.");
          }
    }
    public void warning(Exception e) {
        error(0, e);
    }
    public void error(Exception e) {
        error(1, e);
    }
    public void fatalError(Exception e) {
        error(2, e);
    }
    private void error(int level, Exception e) {
        //System.err.println(e.getMessage());
        if (e instanceof ParseException) {
            int eCode = ((ParseException) e).getErrorNumber();
            onError(level, eCode);
        } else {
            fail("Not expecting an Exception: " + e.getMessage());
        }
    }
    private int expected[];
    private void println(String m) {
        System.err.println(m);
    }
    void onError(int level, int num) {
        for (int i=0; i<expected.length; i++)
          if (expected[i]==num) {
            expected[i] = 0;
            return;
          }
        String msg =
            "Parser reports unexpected "
                + WGTestSuite.errorLevelName[level]
                + ": "
                + JenaReader.errorCodeName(num);
        println(msg);
        fail(msg);
    }
}

/*
    (c) Copyright Hewlett-Packard Company 2000-2003
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