/*
 *  (c) Copyright 2001, 2003,2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: AResource.java,v 1.13 2008-01-02 12:06:46 andy_seaborne Exp $
*/
package com.hp.hpl.jena.rdf.arp;

/**
 * A URI or blank node reported to a {@link StatementHandler}.
 * Note: equality (<code>.equals</code>) rather than identity
 * (<code>==</code>) must be used to compare <code>AResource</code>s.
 * 
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
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

/*
*  (c) Copyright 2001, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:

	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.

	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.

	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/