/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdf.arp.impl.ANode;
import com.hp.hpl.jena.rdf.arp.impl.ARPResource;
import com.hp.hpl.jena.rdf.arp.impl.XMLContext;

abstract class Collection extends WantDescription {
    // TODO: document this carefully.
    // TODO: refactor this class a bit: drop AbortableWantsObjectI ?
    
    protected interface AbortableWantsObjectI extends WantsObjectI {
        void abort();
        void init();
    }
    WantsObjectI nextSlot;
    public Collection(WantsObjectFrameI s, XMLContext x) {
        super(s, x);
        nextSlot = s;
    }
    
   public FrameI startElement(String uri, String localName, String rawName,
            Attributes atts)  throws SAXParseException {
      FrameI fi = super.startElement(uri,localName,rawName,atts);
      final ANode bnode = new ARPResource(arp);
      WantsObjectI prevSlot = nextSlot;
      nextSlot = null;
      nextSlot = listNode(bnode);
      prevSlot.theObject(bnode);
      ((AbortableWantsObjectI)nextSlot).init();
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
    

    final public void endElement() throws SAXParseException {
        WantsObjectI prevSlot = nextSlot;
        nextSlot = null;
        prevSlot.theObject(nil());
        super.endElement();
    }
    public void abort() {
        if (nextSlot != null && nextSlot instanceof AbortableWantsObjectI)
            ((AbortableWantsObjectI)nextSlot).abort();
        super.abort();
    }
    
    /**
     * The implementor of listNode is responsible for calling
     * endLocalScope on bnode.
     * @param bnode
     * @return
     */
   final AbortableWantsObjectI listNode(final ANode bnode) {
        // Do not use the bnode.
        // Put first use in the init method.
        return new AbortableWantsObjectI() {
            public void theObject(ANode a) {
                try {
//                    System.err.print(((AResource)bnode).getAnonymousID()+".");
//                    System.err.println(((AResource)a).getAnonymousID());
                   restTriple(bnode,a);
                }
                finally {
                   arp.endLocalScope(bnode);
                }
            }
            public void abort() {
                arp.endLocalScope(bnode);
            }
            public void init() {
                firstTriple(bnode,subject);
            }
          };
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
 
