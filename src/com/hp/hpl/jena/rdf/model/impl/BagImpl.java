/*
 *  (c) Copyright Hewlett-Packard Company 2000 
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
 *
 * BagImpl.java
 *
 * Created on 08 August 2000, 17:09
 */

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.rdf.model.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.enhanced.*;

/** An implementation of Bag
 *
 * @author  bwm
 * @version  Release='$Name: not supported by cvs2svn $' Revision='$Revision: 1.5 $' Date='$Date: 2003-06-17 12:51:38 $' 
 */
public class BagImpl extends ContainerImpl implements Bag {
    
    final static public Implementation factory = new Implementation() {
        public boolean canWrap( Node n, EnhGraph eg )
            { return true; }
        public EnhNode wrap(Node n,EnhGraph eg) {
            return new BagImpl(n,eg);
        }
    };
        
    /** Creates new BagMem */
    public BagImpl(Model model)  {
        super(model);
    }
    
    public BagImpl(String uri, Model model)  {
        super(uri, model);
    }
    
    public BagImpl(Resource r, Model m)  {
        super(r, m);
    }
    
    public BagImpl(Node n, EnhGraph g) {
        super(n,g);
    }
    

}