/******************************************************************
 * File:        FrameObject.java
 * Created by:  Dave Reynolds
 * Created on:  18-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: FrameObject.java,v 1.2 2003-07-22 16:41:42 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

/**
 * Base class for stack frame objects that
 * we might want to allocate from a pool in the future.
 *  
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-07-22 16:41:42 $
 */
public class FrameObject {

    /** Used to link the frame to the prior frame in the (tree) stack or the pool */
    protected FrameObject link;
    
    /** The parent factory to which free frames can be returned */
    protected FrameObjectFactory factory;
    
    /** 
     * Constructor The parent factory to which free frames can be returned
     * @param factory 
     */
    public FrameObject(FrameObjectFactory factory) {
        this.factory = factory;
    }
    
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
     * Signal that the frame is finished with. If not called the frame will be
     * reclaimed by garbage collection anyway. Not implemented.
     */
    public void free() {
        factory.returnFreeFrame(this);
    }
    
}


/*
    (c) Copyright Hewlett-Packard Company 2003
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