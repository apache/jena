/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import junit.framework.TestCase;
import java.util.*;

/**
 * @author Jeremy J. Carroll
 *
 */
public class PushMePullYouTest extends TestCase {
	PushMePullYouPipe pipe;
	
	public void testBuf4()  throws Exception {
		testBuffer(4);
	}
	public void testBuf5()  throws Exception {
		testBuffer(5);
	}
	public void testBuf8()  throws Exception {
		testBuffer(8);
	}
	public void testBuf10()  throws Exception {
		testBuffer(10);
	}
	public void testBuf11()  throws Exception {
		testBuffer(11);
	}
	public void testBuf12()  throws Exception {
		testBuffer(12);
	}
	private void testBuffer(final int ii) throws Exception {
		final Vector v = new Vector();
		ARPRunnable puller = new ARPRunnable() {
			public void run() {
					for (int j=0; j<ii; j++) {
						Token t = pipe.getNextToken();
						v.add(t);
				//		System.err.println(j + " " + (t==null?-1:t.kind));
					}
			}
		};
		pipe = new PushMePullYouPipe(puller);
		
		for (int i=0;i<ii;i++){
			pipe.putNextToken(new Token(i,null));
		}
		pipe.close();
		for (int i=0;i<ii;i++)
			assertEquals("pos "+i, i, ((Token)v.get(i)).kind);
	}
		
	public void testInterrupt() {
		Thread t = Thread.currentThread();
		t.interrupt();
		assertTrue(t.isInterrupted());
		try {
			Thread.sleep(50);
			fail("Wasn't interrupted");
		}
		catch (InterruptedException e){
			
		}
	}

}


/*
 *  (c) Copyright 2004 Hewlett-Packard Development Company, LP
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
 
