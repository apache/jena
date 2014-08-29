/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdfxml.xmlinput;

import org.xml.sax.ErrorHandler;


/**
 * Used to set event handlers and options
 * on {@link ARP}, {@link SAX2Model}, and
 * {@link SAX2RDF} instances.
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
