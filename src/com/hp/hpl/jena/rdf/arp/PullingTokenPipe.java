/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import java.io.InterruptedIOException;
import java.util.*;

/**
 * @author Jeremy J. Carroll
 *
 */
class PullingTokenPipe extends TokenPipe {

	static PullingTokenPipe lastMade;
	private int position = 0;
	private boolean atEOF = false;
	final XMLHandler arp;
	final List pipe;

	PullingTokenPipe(XMLHandler arp) {
		this.arp = arp;
		pipe = createPipe();
		if (Token.COUNT)
		  lastMade = this;
		else
			lastMade = null;
	}

	void putNextToken(Token t) {
   //    System.err.print(t.toString()+", ");
		pipe.add(t);
	}

	public Token getNextToken() {
		while (true) {
			if (getPosition() < pipe.size()) {
				int p = position++;
				Token rslt = (Token) pipe.get(p);
				pipe.set(p,null);
				setLast(rslt);
				return rslt;
			}
			if (atEOF)
				return new Token(RDFParserConstants.EOF, null);
		  if (Thread.interrupted())
		    throw new WrappedException(new InterruptedIOException("ARP interrupted"));
			position=0;
           // if ( pipe.size() > 0 )
            //    setLast((Token)pipe.get(pipe.size()-1));
			pipe.clear();
			while (pipe.size() == 0)
				if (!((SingleThreadedParser)arp).parseSome()) {
					atEOF = true;
					break;
				}
		}
	}
	private List createPipe() {
		return new ArrayList();
	}
	private int getPosition() {
		return position;
	}
}


/*
 *  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
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
 
