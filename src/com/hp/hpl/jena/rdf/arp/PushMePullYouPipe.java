/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;


import org.xml.sax.*;

import EDU.oswego.cs.dl.util.concurrent.BoundedBuffer;

import com.hp.hpl.jena.shared.JenaException;

/**
 * @author Jeremy J. Carroll
 *  
 */
class PushMePullYouPipe extends TokenPipe {

	volatile private Throwable brokenPipe = null;

	private boolean open = true;
	
	private boolean overflow = false;

	private BoundedBuffer buffer = new BoundedBuffer(100);

	private Object pending = null;

	private static final String finished = "<finished>";

	private static final RuntimeException naturalEnd = new RuntimeException();

	final private Thread puller;

	
	PushMePullYouPipe(final ARPRunnable puller) {
		this.puller =  
				new Thread() {
			public void run() {
				//		pipe.pullerSleep();
				try {
					puller.run();
					naturalDeath();
				} catch (Throwable e) {
					setException(e);
				} finally {
				}
			}

		};
		
	}
    void start() {
	   this.puller.start();
    }
	/**
	 * get something from the pipe; take care of BoundedBuffer's checked
	 * exceptions
	 */
	private Object fetch() {
		try {
			return buffer.take();
		} catch (Exception e) {
			throw new BoundedBufferTakeException(e);
		}
	}

	/**
	 * put something into the pipe; take care of BoundedBuffer's checked
	 * exceptions
	 */
	private void putAny(Object d) throws SAXParseException {
		try {
			do {
				if (d != finished)
					isPipeBroken();
			} while (!buffer.offer(d, 100));
		} catch (InterruptedException e) {
			throw new BoundedBufferPutException(e);
		}
	}

	private void isPipeBroken() throws SAXParseException {
		if (brokenPipe != null) {
			if ( brokenPipe == naturalEnd ){
				SAXParseException ee = new SAXParseException("RDF parsing finished, additional XML events",getLocator());
				//System.err.println(
				//		ParseException.formatMessage(ee));
				overflow = true;
				throw ee;
			}
			try {
				throw brokenPipe;
			} catch (RuntimeException e) {
				throw e;
			} catch (Error e) {
				throw e;
			} catch (SAXParseException e) {
				throw e;
			} catch (Exception e) {
				throw new WrappedException(e);

			} catch (Throwable t) {
				throw new RuntimeException("Exception from RDF thread.",t);
			}
			/*
			pipe().naturalDeath();
		} catch (WrappedException wrapped) {
			pipe().setException(wrapped.inner);
		} catch (ParseException parse) {
			pipe().setException(parse.rootCause());
		} catch (RuntimeException e) {
			pipe().setException(e);
		} catch (Error e) {
			pipe().setException(e);
		*/
		}
	}

	public void putNextToken(Token d) throws SAXParseException {
		putAny(d);
	}

	public void close() throws SAXParseException {
		putAny(finished);
		try {
			puller.join();
		} catch (InterruptedException e) {

		}
		if (brokenPipe != naturalEnd)
			isPipeBroken();
		/* Note check that pipe is exhausted is done
		 * on exactlyExhausted ....
		 */
	}

	private boolean hasNext() {
		if (open) {
			if (pending == null) {
				pending = fetch();
				if (pending == finished)
					open = false;
				return open;
			} else
				return true;
		} else
			return false;
	}
	
	boolean exactlyExhausted() {
		return !(overflow||hasNext());
	}

	public Token getNextToken() {
		if (hasNext() == false)
			return new Token(RDFParserConstants.EOF, null);
		try {
			return (Token) pending;
		} finally {
			pending = null;
		}
	}

	void naturalDeath() {
		setException(naturalEnd);
	}

	void setException(Throwable t) {
		brokenPipe = t;
	}

	/**
	 * Exception to throw if a <code>take</code> throws an exception.
	 */
	public static class BoundedBufferTakeException extends JenaException {
		BoundedBufferTakeException(Exception e) {
			super(e);
		}
	}

	/**
	 * Exception to throw if a <code>put</code> throws an exception.
	 */
	public static class BoundedBufferPutException extends JenaException {
		BoundedBufferPutException(Exception e) {
			super(e);
		}
	}

}

/*
 * (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
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

