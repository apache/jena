/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

/**
 * Used to set event handlers and options
 * on {@link ARP}, {@link SAX2Model}, and
 * {@link SAX2RDF} instances.
 * @author Jeremy J. Carroll
 *
 */
public interface ARPConfig {
	/**
	 * The handlers used during parsing.
	 * The handlers can be changed by calling this method
	 * and then using the <code>set..Handler</code> methods
	 * in {@link ARPHandlers}.
	 * The handlers can be copied onto another ARP instance
	 * using the {@link #setHandlersWith} method.
	 * @see ARPHandlers#setStatementHandler(StatementHandler)
	 * @see ARPHandlers#setErrorHandler(ErrorHandler)
	 * @see ARPHandlers#setExtendedHandler(ExtendedHandler)
	 * @see ARPHandlers#setNamespaceHandler(NamespaceHandler)
	 * @see #setHandlersWith
	 * @return The handlers used during parsing.
	 */
	public ARPHandlers getHandlers();

	/**
	 * Copies the handlers from the argument
	 * to be used by this instance.
	 * To make further modifications it is necessary
	 * to call {@link #getHandlers} to retrieve this
	 * instance's copy of the handler information.
	 * @param handlers The new values to use.
	 */
	public void setHandlersWith(ARPHandlers handlers);

	/**
	 * The options used during parsing.
	 * The options can be changed by calling this method
	 * and then using the <code>set..</code> methods
	 * in {@link ARPOptions}.
	 * The options can be copied onto another ARP instance
	 * using the {@link #setOptionsWith} method.
	 * @see ARPOptions#setDefaultErrorMode()
	 * @see ARPOptions#setLaxErrorMode()
	 * @see ARPOptions#setStrictErrorMode()
	 * @see ARPOptions#setStrictErrorMode(int)
	 * @see ARPOptions#setEmbedding(boolean)
	 * @see ARPOptions#setErrorMode(int, int)
	 * 
	 * @see #setOptionsWith
	 * @return The handlers used during parsing.
	 */
	public ARPOptions getOptions();

	/**
	 * Copies the options from the argument
	 * to be used by this instance.
	 * To make further modifications it is necessary
	 * to call {@link #getOptions} to retrieve this
	 * instance's copy of the options.
	 * @param opts The new values to use.
	 */
	public void setOptionsWith(ARPOptions opts);
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

