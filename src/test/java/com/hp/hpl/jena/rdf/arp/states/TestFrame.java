/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp.states;


import junit.framework.Assert;

import com.hp.hpl.jena.rdf.arp.impl.ANode;
import com.hp.hpl.jena.rdf.arp.impl.AbsXMLContext;
import com.hp.hpl.jena.rdf.arp.impl.XMLHandler;
import com.hp.hpl.jena.rdf.arp.states.AbsXMLLiteral;
import com.hp.hpl.jena.rdf.arp.states.HasSubjectFrameI;
import com.hp.hpl.jena.rdf.arp.states.WantsObjectFrameI;

class TestFrame extends AbsXMLLiteral implements WantsObjectFrameI,
        HasSubjectFrameI {

    public TestFrame(XMLHandler h, AbsXMLContext x) {
        super(h,x);
    }
    
    void clear() {
        rslt.setLength(0);
        oCount = 0;
        pCount = 0;
        rCount = 0;
    }

    
    int oCount;
    int pCount;
    int rCount;
    @Override
    public void endElement() {
    }

    @Override
    public void theObject(ANode a) {
        oCount++;

    }

    @Override
    public void aPredAndObj(ANode p, ANode o) {
        pCount++;
    }

    public String info() {
        return (rslt.length()==0?"":("x"+rslt.length()+" "))+
           (oCount==0?"":"O"+oCount+" ")+
           (pCount==0?"":"P"+pCount+" ")+
           (rCount==0?"":"R"+rCount+" ");
           
    }

    public void check(EventRecord r) {
        r.initCounts();
        Assert.assertEquals("object looking for s,p count",r.objects,oCount);
        Assert.assertEquals("p,o looking for s count",r.preds,pCount);
        Assert.assertEquals("reification count",r.reify,rCount);   
    }

    @Override
    public void makeSubjectReificationWith(ANode r) {
        rCount++;
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
 
