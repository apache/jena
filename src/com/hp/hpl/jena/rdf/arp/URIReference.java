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
 
 * * $Id: URIReference.java,v 1.6 2003-12-05 17:46:34 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * URIReference.java
 *
 * Created on June 25, 2001, 9:58 PM
 */

package com.hp.hpl.jena.rdf.arp;




/**
 *
 * @author  jjc
 
 */
class URIReference  implements AResource 
{

    
    /** Creates new URIReference */
    final private String uri;
    URIReference(Location l, XMLContext ctxt,String uri) throws MalformedURIException, ParseException {
        
//        this.uri = new URI(ctxt.getURI(),URIref.encode(uri));
        this.uri = ctxt.resolve(l, uri);
    }
    URIReference(String uri)  throws MalformedURIException  {
//        this.uri = new URI(URIref.encode(uri));
        this.uri = new URI(uri).getURIString();
    }
    URIReference() {
        uri = null;
    }
    public String toString() {
        return uri;
    }

    public boolean isAnonymous() {
        return false;
    }
    
    public String getAnonymousID() {
        return null;
    }
    
    public String getURI() {
        return uri;
    }
    
    private Object userData;
    public Object getUserData() {
        return userData;
    }
    
    public void setUserData(Object d) {
        userData = d;
    }
    /**
     * Does not compare userData field, only URI.
     */
    public boolean equals(Object o) {
        return o != null 
        && (o instanceof URIReference) 
        && uri.equals(((URIReference)o).uri );
    }
    
    public int hashCode() {
        return uri.hashCode();
    }
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.AResource#hasNodeID()
	 */
	public boolean hasNodeID() {
		return false;
	}
}
