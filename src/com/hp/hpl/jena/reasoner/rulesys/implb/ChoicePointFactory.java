/******************************************************************
 * File:        ChoicePointFactory.java
 * Created by:  Dave Reynolds
 * Created on:  22-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: ChoicePointFactory.java,v 1.2 2003-07-23 16:24:17 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

/**
 * Factory for Environment frames. 
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-07-23 16:24:17 $
 */
public class ChoicePointFactory extends FrameObjectFactory {

    /** The single instance of the factory */
    protected static final ChoicePointFactory theFactory = new ChoicePointFactory();
    
    /** Private factory constructor */
    private ChoicePointFactory() {}
    
    /**
     * Return a newly constructed or cached environment frame.
     */
    public static ChoicePointFrame create() {
        return theFactory.getFrame();
    }
    
    /**
     * Find or allocate a new frame.
     */
    private ChoicePointFrame getFrame() {
        ChoicePointFrame env = (ChoicePointFrame)getFree();
        if (env == null) {
            env = new ChoicePointFrame(this);
        } else {
            env.fastLinkTo(null);
        }
        return env;
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