/*
  (c) Copyright 2004 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Redirect.java,v 1.1 2004-01-11 21:20:29 jeremy_carroll Exp $
*/
package com.hp.hpl.jena.ontology.tidy;
import java.util.*;
/**
 * This is a simple tility class for permitting local copies
 * of websites to be used instead of remote access.
 * @author <a href="mailto:Jeremy.Carroll@hp.com">Jeremy Carroll</a>
 *
*/
public class Redirect {
      private Vector remoteURL = new Vector();
      private Vector localURL = new Vector();
      synchronized public void add(String remote, String local){
      	remoteURL.add(remote);
      	localURL.add(local);
      }
      
      synchronized public String redirect(String rem) {
      	for (int i=0; i<remoteURL.size();i++){
      		if (rem.startsWith((String)remoteURL.get(i))) {
      		   String rslt = localURL.get(i) + rem.substring(((String)remoteURL.get(i)).length());
      		   if (rslt.endsWith(".rdf"))
      		      return rslt;
      		   else
      		      return rslt+".rdf";
      		}
      	}
      	return rem;
      }
      
      
}

/*
  (c) Copyright 2004 Hewlett-Packard Development Company, LP
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