/*
  (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP, 
  all rights reserved.
  [See end of file]
  $Id: DAMLTailCollection.java,v 1.6 2004-12-06 13:50:16 andy_seaborne Exp $
*/
package com.hp.hpl.jena.rdf.arp;

/**
 * @author Jeremy J. Carroll
 *
 */
class DAMLTailCollection extends DAMLCollection {
	DAMLTailCollection(ParserSupport x,ARPResource cell) {
		super(x);
		last = cell;
		f = cell;
	}
	ARPResource last;
	final ARPResource f;
	private void endLastScope() {
		if (last!=f)
		   X.arp.endLocalScope(last);
	}
	void terminate() {
		last.setPredicateObject(rest, nil, null);
		endLastScope();
	}
	public void cleanUp() {
	  endLastScope();
  }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.rdf.arp.CollectionAction#next(com.hp.hpl.jena.rdf.arp.AResource)
	 */
	CollectionAction next(AResourceInternal head) {
		ARPResource cell = new ARPResource(X.arp);
		try {
			last.setPredicateObject(rest, cell, null);
			cell.setPredicateObject(first, head, null);
			cell.setType(List);
		}
		finally {
			X.arp.endLocalScope(head);
			endLastScope();
			last = cell;
		}
		return this;
	}
}

/*
	(c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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