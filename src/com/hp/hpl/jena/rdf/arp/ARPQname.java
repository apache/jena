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
 
 * * $Id: ARPQname.java,v 1.3 2003-08-27 13:05:52 andy_seaborne Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * ARPQname.java
 *
 * Created on June 25, 2001, 10:00 PM
 */

package com.hp.hpl.jena.rdf.arp;

/**
 *
 * @author  jjc
 * @version 
 */
class ARPQname extends Token {
    final String nameSpace;
    final String local;
    final String qName;
    /** Creates new ARPQname */
    ARPQname(int kind, Location where, String ns,String name,String q) {
        super(kind,where);
        qName = q;
        nameSpace = ns;
        local = name;
    }
    ARPQname(String ns,String name) {
        super(E_OTHER,null);
        nameSpace = ns;
        local = name;
        qName = null;
    }
    String prefix() {
    	int ix = qName.indexOf(':');
    	return ix==-1?"":qName.substring(0,ix);
    }
    URIReference asURIReference(ARPFilter arp) throws ParseException {
        URIReference uri;
        try {
            uri = new URIReference(nameSpace+local);
            /*
            if (nameSpace.startsWith("#"))
                System.err.println(nameSpace+local);
                */
        }
        catch ( MalformedURIException mal ) {
            // Distinguish between relative namespaces and other problems.
            try {
            	arp.documentContext.resolve(location, nameSpace+local);
                //new URI(arp.documentContext.getURI(),nameSpace+local);
                // May have been relative namespace, since this is now OK.
                // Or maybe unqualified element.
                if ( nameSpace.length() == 0 ) {
                  arp.parseWarning(ARPFilter.WARN_UNQUALIFIED_ELEMENT,
                   location,
                   "Element node must be qualified.");
                
                } else {
                  arp.parseWarning(ARPFilter.WARN_RELATIVE_NAMESPACE_URI_DEPRECATED,
                   location,
                   "The use of relative URIs in namespaces has been deprecated by the World Wide Web Consortium.");
                }
            }
            catch ( MalformedURIException mal2 ) {
                // Was other problem
               arp.parseWarning(ARPFilter.WARN_MALFORMED_URI,location,"Bad URI <"+nameSpace+local+"> in qname: " + mal.getMessage());
            }
            uri = new BadURIReference(nameSpace+local);
        }
        return uri;
    }

}
