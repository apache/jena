/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.arp.NTriple;

public class MemoryLeakTest extends TestCase {

    static public Test suite() {
        TestSuite suite = new TestSuite("ARP Memory Leak");

        suite.addTest(new MemoryLeakTest("testWineMemoryLeak"));
        return suite;
    }


    public MemoryLeakTest(String arg0) {
        super(arg0);
    }
    
    public void testWineMemoryLeak() throws IOException {
        // warmup
        Runtime rt = Runtime.getRuntime();
        loadFile("testing/wg/miscellaneous/consistent001.rdf");
        rt.gc();
        rt.gc();
        rt.gc();
        long inUse = rt.totalMemory() - rt.freeMemory();
        loadFile("testing/arp/wineRenamed.rdf");
        rt.gc();
        rt.gc();
        rt.gc();
        long leaked = rt.totalMemory() - rt.freeMemory() - inUse;
        System.err.println("Leaked: "+ leaked);
        
        
    }

    static void loadFile(String fileName)
        throws IOException {
        PrintStream 
            out = new PrintStream(new OutputStream() {

                public void write(int b) throws IOException {
                }
            });
        PrintStream oldOut = System.out;
        try {
            System.setOut(out);
               NTriple.mainEh(new String[] { "-b", "http://eg.org/", "-t", fileName }, null, null);
            out.close();        
        } finally {
            System.setOut(oldOut);
        
        }
    }

}


/*
 *  (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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
 *
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
 */
 
