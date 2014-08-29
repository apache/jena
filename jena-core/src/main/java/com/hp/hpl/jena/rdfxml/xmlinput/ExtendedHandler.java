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

/* This file includes contributions by:
 * (c) Copyright 2003, Plugged In Software 
 * See end of file for BSD-style license.
 */

package com.hp.hpl.jena.rdfxml.xmlinput;

/**Extended callbacks from a reader to an RDF application.
 * This reports:
 * <ul>
 * <li> when a blank node goes out of scope, allowing its anonymous
 * ID to be freed by external applications.
 * <li> when a rdf:RDF start and end tag is seen
 */
public interface ExtendedHandler {
	/** After this call, no more triples will be reported
	 * which use <code>bnode</code>.
	 * This is called exactly once for each blank nodes. 
	 * Whether this includes nodes  with an <code>rdf:nodeID</code>
	 * is controlled by {@link #discardNodesWithNodeID}.
	 *
	 *<p>
	 *The contract is robust against syntax errors in input, 
	 *and exceptions being thrown by the StatementHandler.
	 *  @param bnode A blank node going out of scope.
	 */
	public void endBNodeScope(AResource bnode );
	/**
	 * This method is used to modify the behaviour
	 * of ARP concerning its reporting of bnode scope
	 * {@link #endBNodeScope}.
	 * <p>
	 * If this returns true then blank nodes with an <code>rdf:nodeID</code>
	 *  are not reported as they go out of scope at the end
	 * of file. This eliminates the unbounded memory cost
	 * of remembering such nodes.
	 * <p>
	 * If this returns false then the contract of 
	 * {@link #endBNodeScope} is honoured uniformly
	 * independent of whether a blank node has a nodeID or not.
	 * <p>
	 * If this method returns different values during the parsing
	 * of a single file, then the behaviour is undefined.
	 * @return Desired behaviour of endBNodeScope.
	 */
	public boolean discardNodesWithNodeID();
	
	/**
	 * Called when the &lt;rdf:RDF&gt; tag is seen.
	 * (Also called when there is an implicit start of RDF content
	 * when the file consists only of RDF but omits the rdf:RDF tag).
	 */
	public void startRDF();
	
	/**
	 * Called when the &lt;/rdf:RDF&gt; tag is seen.
	 * (Also called when there is an implicit end of RDF content
	 * e.g. when the file consists only of RDF but omits the rdf:RDF tag,
	 * or if there is an unrecoverable syntax error mid-file).
	 *<p>
	 *Robust against syntax errors in input, 
	 *and exceptions being thrown by the StatementHandler. 
	 */
	public void endRDF();
	
}

/*
 *  (c) Copyright 2003, Plugged In Software 
 *
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
