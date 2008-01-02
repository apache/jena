/*
 (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 [See end of file]
 $Id: TestARPStates.java,v 1.4 2008-01-02 12:05:24 andy_seaborne Exp $
 */

package com.hp.hpl.jena.rdf.arp.states.test;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author jjc
 */
public class TestARPStates extends TestCase {
    public TestARPStates() {
        super();
    }

    public static TestSuite suite() {
        TestSuite rslt = new TestSuite();
        rslt.setName("ARP state machine");
        Map tests = new HashMap();
        try {
          LineNumberReader r = new LineNumberReader(new FileReader(TestData.dataFile));
          while (true) {
              String line = r.readLine();
              if (line == null)
                  return rslt;
              int hash = line.indexOf('%');
              line = (hash==-1?line:line.substring(0,hash)).trim();
              String fields[] = line.split("  *");
              if (fields.length==0)
                  continue;
              TestSuite child = (TestSuite)tests.get(fields[0]);
              if (child==null) {
                  child = new TestSuite();
                  child.setName(TestData.stateLongName(fields[0]));
                  rslt.addTest(child);
                  tests.put(fields[0],child);
              }
              child.addTest(TestEventList.create(line,fields));
          }
        }
        catch (IOException e) {
            e.printStackTrace();
            return rslt;
        }
    }
}

/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP All
 * rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name of the author may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
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