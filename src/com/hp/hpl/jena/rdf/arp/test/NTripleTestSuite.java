
/*
 *  (c) Copyright Hewlett-Packard Company 2002 
 * See end of file.
 */
 package com.hp.hpl.jena.rdf.arp.test;
import junit.framework.*;
import java.io.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.arp.*;

/**
 * A version of the test suite which uses the
 * ARP internal N-triple writer, and not the
 * Jena N-triple writer.
 * @author Jeremy Carroll
 *
 * 
 */
class NTripleTestSuite extends WGTestSuite {
	NTripleTestSuite(ARPTestInputStreamFactory fact, String name,boolean b) {
		super(fact, name, b);
	}

	static  TestSuite suite(URI testDir, String d, String nm) {
		try {
			return new NTripleTestSuite(new ARPTestInputStreamFactory(testDir, d), nm, true);
		} catch (RuntimeException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	static  TestSuite suite(URI testDir, URI d, String nm) {
		try {
			return new NTripleTestSuite(new ARPTestInputStreamFactory(testDir, d), nm, true);
		} catch (RuntimeException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	Model loadRDF(InputStream in, RDFErrorHandler eh, String base)
		throws IOException, RDFException {
		InputStream oldIn = System.in;
		InputStream ntIn = null;
		File ntriples = File.createTempFile("arp", ".nt");
		PrintStream out = new PrintStream(new FileOutputStream(ntriples));
		PrintStream oldOut = System.out;
		try {
			System.setIn(in);
			System.setOut(out);
			NTriple.mainEh(new String[] { "-b", base, "-s" }, new ARPSaxErrorHandler(eh));
			out.close();
			ntIn = new FileInputStream(ntriples);
			return loadNT(ntIn,base);
			
		} finally {
			System.setIn(oldIn);
			System.setOut(oldOut);
		    if (ntIn != null)
		       ntIn.close();
		    if ( ntriples != null )
		       ntriples.delete();
		}
	}

}
/*
 *  (c) Copyright Hewlett-Packard Company 2002 
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
 */