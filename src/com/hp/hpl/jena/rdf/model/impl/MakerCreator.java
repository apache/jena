/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: MakerCreator.java,v 1.3 2003-09-08 11:28:22 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model.impl;

import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.vocabulary.JMS;

/**
    Convenience superclass for the standard ModelMakerCreator classes,
    with the method for extracting the Reifier style from a description.
    
 	@author hedgehog
*/
class MakerCreator
    {
    /**
        Answer the reification style possessed by <code>root</code> in 
        <code>desc</code> under the property <code>JMS.reificationMode</code>.
        If no such property exists, default to <code>Standard</code>.
        
    	@param desc the model in which to search
    	@param root the entity which may have the property
    	@return the reification style given by the property, or Standard if none
     */
    public ReificationStyle style( Model desc, Resource root )
        {
        Statement st = desc.getProperty( root, JMS.reificationMode );
        return st == null ? ReificationStyle.Standard : JMS.findStyle( st.getObject() );
        } 
    }

/*
    (c) Copyright 2003 Hewlett-Packard Development Company, LP
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