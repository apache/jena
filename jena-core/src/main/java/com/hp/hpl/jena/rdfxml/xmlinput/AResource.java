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

/**
 * A URI or blank node reported to a {@link StatementHandler}.
 * Note: equality (<code>.equals</code>) rather than identity
 * (<code>==</code>) must be used to compare <code>AResource</code>s.
*/
public interface AResource {
	  
	/** A string distinguishing this anonymous resource, from other anonymous resources.
	 * Undefined if {@link #isAnonymous} returns false.
	 * @return An identifier with file scope for this anonymous resource..
	 */    
	    public String getAnonymousID();
	    
	/** The URI reference for this resource, if any.
	 * Not defined if {@link #isAnonymous} returns true.
	 * @return The URI reference of this resource.
	 */    
	    public String getURI();
	/** The user data allows the RDF application to store one Object with each blank node during parsing.
	 * This may help with garbage collect strategies when parsing huge files.
	 * No references to the user data are maintained after a blank node goes out of
	 * scope.
	 * @return A user data object previously stored with {@link #setUserData}; or null if none.
	 */    
	    public Object getUserData();
	 /**
	  * True, if this is an anonymous resource with an explicit rdf:nodeID.
	  * @return true if this resource has a nodeID
	  */
	 public boolean hasNodeID();
	/** True if this resource does not have an associated URI.
	 * @return True if this resource is anonymous.
	 */    
	    public boolean isAnonymous();
	/** The user data allows the RDF application to store one Object with each blank node during parsing.
	 * This may help with garbage collect strategies when parsing huge files.
	 * No references to the user data are maintained after a blank node goes out of
	 * scope.
	 * <p>
	 * See note about large files in class documentation for {@link ARP}.
	 * @param d A user data object which may be retrieved later with {@link #getUserData}.
	 */    
	     public void setUserData(Object d);

}
