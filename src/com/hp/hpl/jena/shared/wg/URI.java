

/*
 *  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * See end of file.
 */
package com.hp.hpl.jena.shared.wg;
import com.hp.hpl.jena.rdf.arp.MalformedURIException;
import java.net.URISyntaxException;


/**
 * Only for test package, not part of public API.
 * Make arp.URI look like java.net.URI.
 * @author Jeremy Carroll
 *
 * 
 */
public class URI extends com.hp.hpl.jena.rdf.arp.URI {
	private String relative;
	public static URI create(String s) {
		try {
		    return new URI(s);
		}
		catch (MalformedURIException e) {
			if ( e.toString().indexOf("No scheme")!= -1
            ||   e.toString().indexOf("Cannot initialize URI with empty parameters.")!= -1
            ) {
				try {
				return new URI(s,false);
				}
				catch (MalformedURIException ee) {
				}
			}
			RuntimeException rte = new IllegalArgumentException("Creating <"+s+">: ");
            rte.initCause(e);
            throw rte;
		}
	}
	
	private URI(String s,boolean x)  throws MalformedURIException  {
		super("http://foo");
		relative = s;
	}
	private URI(String s) throws MalformedURIException {
		super(s);
	}
	
	private URI(URI x, URI y) throws MalformedURIException {
		super(x, y.toString());
	}
    private URI(URI x, String y) throws MalformedURIException {
        super(x, y);
    }
	public boolean isAbsolute() {
		return relative == null;
	}
	public URI resolve(URI rel) {
		try {
		return new URI(this,rel);
		}
		catch (MalformedURIException e) {
			throw new IllegalArgumentException(e.toString());
		}
	}
	
	public java.net.URL toURL() throws java.net.MalformedURLException {
		return new java.net.URL(getURIString());
	}
		
	public URI relativize(URI x) {
		String me = toString();
		String xx = x.toString();
		if ( !xx.startsWith(me) ) {
			throw new IllegalArgumentException(xx + " is not relative to " + me);
		}
		String sub = xx.substring( me.length() );
        return create( sub.charAt(0) == '/' ? sub.substring( 1 ) : sub );
	}

    public String getURIString()
        { return relative == null ? super.getURIString() : relative; }
        
    public String toString() 
        { return getURIString(); }

    public URI resolve(String uri) throws URISyntaxException {
        try {
            return new URI(this,uri);
        } catch (MalformedURIException e) {
            throw asURISyntaxException(uri, e);
        }
    }

    private static URISyntaxException asURISyntaxException(String uri, MalformedURIException e)  {
        URISyntaxException urie = new URISyntaxException(uri,e.getMessage());
        urie.initCause(e);
        return urie;
    }

    public String toASCIIString() {
        // TODO: toASCIIString()
        return toString();
    }

    public static URI construct(String uri) throws URISyntaxException {
        
        try {
            return new URI(uri);
        } catch (MalformedURIException e) {
            throw asURISyntaxException(uri, e);
        }
    }
}
/*
 *  (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
