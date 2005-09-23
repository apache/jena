/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import com.hp.hpl.jena.iri.impl.XercesURI;

// TODO: not for 2.3 revisit deprecated msg
/**
 * 
 * @author Jeremy J. Carroll
 * @deprecated The code for RDF URI References and IRIs is currently being updated.
 */
public class URI extends XercesURI {

    /**
     * 
     */
    private static final long serialVersionUID = 1201410838462565669L;

    public URI() {
        super();
    }

    public URI(URI p_other) {
        super(p_other);
    }

    public URI(String p_uriSpec) throws MalformedURIException {
        super(p_uriSpec);
    }

    public URI(URI p_base, String p_uriSpec) throws MalformedURIException {
        super(p_base, p_uriSpec);
    }

    public URI(String p_scheme, String p_schemeSpecificPart)
            throws MalformedURIException {
        super(p_scheme, p_schemeSpecificPart);
    }

    public URI(String p_scheme, String p_host, String p_path,
            String p_queryString, String p_fragment)
            throws MalformedURIException {
        super(p_scheme, p_host, p_path, p_queryString, p_fragment);
    }

    public URI(String p_scheme, String p_userinfo, String p_host, int p_port,
            String p_path, String p_queryString, String p_fragment)
            throws MalformedURIException {
        super(p_scheme, p_userinfo, p_host, p_port, p_path, p_queryString,
                p_fragment);
    }

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 
