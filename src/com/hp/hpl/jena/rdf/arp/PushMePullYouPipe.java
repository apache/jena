/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import java.io.InterruptedIOException;
import java.util.*;

/**
 * @author Jeremy J. Carroll
 *
 */
class PushMePullYouPipe extends TokenPipe {

	static private final int PIPESIZE = 200;
	private volatile int writePos = 0;
	private volatile int readPos = 0;
	private volatile boolean dying = false;
	
	
	final private Thread pusher, puller;
	
	List createPipe() {
		return Arrays.asList(new Token[PIPESIZE]);
	}
	/**
	 * @param arp
	 */
	PushMePullYouPipe(XMLHandler arp, Thread pusher, Thread puller) {
		super(arp);
		this.pusher = pusher;
		this.puller = puller;
	}

	void putNextToken(Token t) {
   //    System.err.print(t.toString()+", ");
		if (dying)
			throw new FatalParsingErrorException();
		pipe.add(writePos++,t);
		if (writePos == PIPESIZE) {
			readPos = 0;
		    puller.interrupt();
		    while ( readPos < PIPESIZE && !dying) {
		    	try {
		    		Thread.sleep(1000);
		    		// TODO msg
		    	}
		    	catch (InterruptedException e){
		            // TODO msg if pos < SIZE
		    	}
		    }
			writePos = 0;
		}
	}
	
	/**
	 * The pipe needs to close things down.
	 * <q>
	 * Despair and deception<br/>
	 * those ugly little twins<br/>
	 * they came a knocking upon my door<br/>
	 * and I let them in<br/>
	 * Darling, you're the punishment for all my former sins,<br/>
	 * I let love in<br/>
	 * I let love in<br/>
	 * </q>
	 * Nick Cave, <em>I let love in</em>.
	 *
	 * Can be called after:
	 * <ul>
	 * <li>The last end element from SAX
	 * <li>Fatal RDF parsing problem
	 * <li>Fatal XML parsing problem
	 * </ul>
	 * <p>
	 * After end of XML we want RDF to continue,
	 * after fatal RDF problem, RDF won't try to continue,
	 * so we set that up to do so.
	 *  We also set a flag so that any more calls
	 * to putNextToken will throw an exception, tearing
	 * down the XML parse after an RDF error.
	 */
	void despairAndDeception() {
		dying = true;
		readPos = 0;
		puller.interrupt();
		pusher.interrupt();
	}
	
	public Token getNextToken() {


			if (readPos >= writePos) {

				pusher.interrupt();
			    while ( (!dying) && readPos >= writePos) {
			    	try {
			    		Thread.sleep(1000);
			    		// TODO msg
			    	}
			    	catch (InterruptedException e){
			            // TODO msg if pos < SIZE
			    	}
			    }
			    if (dying)
					return new Token(RDFParserConstants.EOF, null);
			}
		  
			int p = readPos++;
			Token rslt = (Token) pipe.get(p);
			// next line probably not needed.
			pipe.set(p,null);
			return rslt;
		
	}


	int getPosition() {
		return readPos;
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
 
