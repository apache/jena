/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP, 
  all rights reserved.
  [See end of file]
  $Id: RDFCollection.java,v 1.3 2003-12-07 10:17:48 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.rdf.arp;

/**
 * @author Jeremy J. Carroll
 *
 */
class RDFCollection extends CollectionAction {

	

	static URIReference first = null;
	static URIReference rest = null;
	static URIReference nil = null;
    
    RDFCollection(ParserSupport x){
    	super(x);
    }
    RDFCollection(ParserSupport x, AResourceInternal r[]){
    	super(x);
    	rslt = r;
    }
    private AResourceInternal rslt[];
	static {
		try {
     		nil        = new URIReference(ARPFilter.rdfns+"nil");
            
			first = new URIReference(ARPFilter.rdfns+"first");
			rest       = new URIReference(ARPFilter.rdfns+"rest");
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
	public void cleanUp() {
		if (rslt[0]!=null) {
			X.arp.endLocalScope(rslt[0]);
			rslt[0] = null;
		}
	}
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.CollectionAction#next(com.hp.hpl.jena.rdf.arp.AResource)
	 */
	CollectionAction next(AResourceInternal head) {
		ARPResource cell= new ARPResource(X.arp); 
		cell.setPredicateObject( first,head, null );
		X.arp.endLocalScope(head);
		//cell.setPredicateObject( rest, tail, null );
		//cell.setType(DAML.List);
												 
		rslt[0] = cell;
		return new RDFTailCollection(X,cell);
		
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