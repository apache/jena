/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, 
  all rights reserved.
  [See end of file]
  $Id: DAMLCollection.java,v 1.1 2003-10-10 11:19:39 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.rdf.arp;

/**
 * @author Jeremy J. Carroll
 *
 */
class DAMLCollection extends CollectionAction {

	

	static URIReference first = null;
	static URIReference rest = null;
	static URIReference nil = null;
	static URIReference List = null;
	static private String damlns = "http://www.daml.org/2001/03/daml+oil#";
    

	DAMLCollection(ParserSupport x){
		super(x);
	}
    DAMLCollection(ParserSupport x,AResource r[]){
    	super(x);
    	rslt = r;
    }
    private AResource rslt[];
	static {
		try {
     		nil        = new URIReference(damlns+"nil");
            
			first = new URIReference(damlns+"first");
			rest       = new URIReference(damlns+"rest");
			List  = new URIReference(damlns+"List");
		}
		catch (MalformedURIException e) {
			System.err.println("Internal error: " + e.toString());
			e.printStackTrace();
		}
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.CollectionAction#terminate()
	 */
	void terminate() {
		rslt[0] = nil;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.CollectionAction#next(com.hp.hpl.jena.rdf.arp.AResource)
	 */
	CollectionAction next(AResource head) {
		ARPResource cell= new ARPResource(X.arp); 
		cell.setPredicateObject( first,head, null );
		cell.setType(List);
												 
		rslt[0] = cell;
		return new DAMLTailCollection(X,cell);
		
	}

}

/*
	(c) Copyright 2003 Hewlett-Packard Development Company, LP
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