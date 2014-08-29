/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdfxml.xmlinput.states;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.ANode ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.ARPResource ;
import com.hp.hpl.jena.rdfxml.xmlinput.impl.AbsXMLContext ;

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
