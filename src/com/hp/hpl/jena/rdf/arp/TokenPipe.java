/*
 *  (c) Copyright 2001  Hewlett-Packard Development Company, LP
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
 
 * * $Id: TokenPipe.java,v 1.7 2004-01-20 10:06:16 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * TokenPipe.java
 *
 * Created on June 22, 2001, 12:32 AM
 */

package com.hp.hpl.jena.rdf.arp;
import java.util.*;
import org.xml.sax.Locator;
import java.io.*;

/**
 *
 * @author  jjc
 
 */
// 5000: 9.7 8.0 7.8 7.9 8.0
// 2000: 9.2 7.7 7.5 7.6 7.7
// 1000: 9.0 7.1 7.1 7.15 7.1
// 800: 8.6 6.9 6.9 6.9 7.1
// 500: 8.6 7.1 7.3 6.9 7.1
// 100: 9.4 7.9 7.8 8.0 7.9
// 10: 15.1 13.7 13.0 13.1 13.0
// 2:  21.2 19.9 19.5 19.8 19.9
class TokenPipe implements TokenManager {

	final List pipe = new ArrayList();
	private int position = 0;
	final ARPFilter arp;
    private Token last;
	/** Creates new TokenPipe */
	TokenPipe(ARPFilter arp) {
		this.arp = arp;
	}

	void putNextToken(Token t) {
   //    System.err.print(t.toString()+", ");
		pipe.add(t);
	}

	private boolean atEOF = false;
	public Token getNextToken() {
		while (true) {
			if (position < pipe.size()) {
				int p = position++;
				Token rslt = (Token) pipe.get(p);
				pipe.set(p,null);
				return rslt;
			}
			if (atEOF)
				return new Token(RDFParserConstants.EOF, null);
		  if (Thread.interrupted())
		    throw new WrappedException(new InterruptedIOException("ARP interrupted"));
			position = 0;
            if ( pipe.size() > 0 )
                last = (Token)pipe.get(pipe.size()-1);
			pipe.clear();
			while (pipe.size() == 0)
				if (!arp.parseSome()) {
					atEOF = true;
					break;
				}
		}
	}
    Locator getLocator() {
        if ( pipe.size() > 0 ) {
            return ((Token)pipe.get(position-1)).location;
        } else if ( last != null )
          return  last.location;
        else 
          return null;
    }
}
