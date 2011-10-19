/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.impl.ANode;
import com.hp.hpl.jena.rdf.arp.impl.ARPResource;
import com.hp.hpl.jena.rdf.arp.impl.AbsXMLContext;

abstract class Collection extends WantDescription {
    // TODO: not for 2.3. document this carefully.    
   
    WantsObjectI nextSlot;
    public Collection(WantsObjectFrameI s, AbsXMLContext x) {
        super(s, x);
        nextSlot = s;
    }
    ANode bnode;
   @Override
public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts)  throws SAXParseException {
      FrameI fi = super.startElement(uri,localName,rawName,atts);
      ANode prevNode = bnode;
      bnode = new ARPResource(arp);
      try {
       nextSlot.theObject(bnode);
      }
      finally {
          if (prevNode != null)
            arp.endLocalScope(prevNode);
      }
      firstTriple(bnode,subject);
      final ANode thisNode = bnode;
      nextSlot = new WantsObjectI() {
          @Override
        public void theObject(ANode a) {
                 restTriple(thisNode,a);
          }
      };
      return fi;
      
    }
   /** Must use second bnode in the first triple.
       Can use either bnode in further triples.
   */
   abstract void restTriple(ANode subj,ANode obj);
   
   /** Must use both bnodes in the first triple.
       Can use either bnode in further triples.
   */
   abstract void firstTriple(ANode subj, ANode obj);
   abstract ANode nil() ;
    

    @Override
    final public void endElement() throws SAXParseException {
        nextSlot.theObject(nil());
        if (bnode != null) {
            arp.endLocalScope(bnode);
            bnode = null;
        }
        super.endElement();
    }
    @Override
    public void abort() {
        if (bnode != null) {
            arp.endLocalScope(bnode);
            bnode = null;
        }
        super.abort();
    }
    
   

}


/*
 *  (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
 
