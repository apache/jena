/******************************************************************
 * File:        FrameObject.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jul-2003
 * 
 * (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: FrameObject.java,v 1.4 2005-02-21 12:17:40 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

/**
 * Base class for stack frame objects. Originally this was used to provide
 * pool-based allocated but it turns out the normal Java GC outperforms
 * manual pool-based allocation anyway.
 *  
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2005-02-21 12:17:40 $
 */
public class FrameObject {

    /** Used to link the frame to the prior frame in the (tree) stack or the pool */
    FrameObject link;
        
    /**
     * Link this frame to an existing frame. In the future this might do some ref count
     * tricks.
     */
    public void linkTo(FrameObject prior) {
        link = prior;
    }
    
    /**
     * Link this frame to an existing frame. This will never do any funny ref count tricks.
     */
    public void fastLinkTo(FrameObject prior) {
        link = prior;
    }
    
    /**
     * Return the prior frame in the tree.
     */
    public FrameObject getLink() {
        return link;
    }
    
    /**
     * Close the frame actively. This frees any internal resources, frees this frame and
     * frees the frame to which this is linked.
     */
    public void close() {
    }
    
}


/*
    (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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