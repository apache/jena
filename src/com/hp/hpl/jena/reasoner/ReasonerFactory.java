/******************************************************************
 * File:        ReasonerFactory.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jan-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: ReasonerFactory.java,v 1.2 2003-02-01 13:35:01 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * The interface through which a reasoner (inference engine) can be
 * instantiated. Instances of this are registered with the global
 * ReasonerRegistry.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-02-01 13:35:01 $
 */
public interface ReasonerFactory {

    /**
     * Constructor method that builds an instance of the associated Reasoner
     * @param configuration a set of arbitrary configuration information to be 
     * passed the reasoner encoded within an RDF model.
     */
    public Reasoner create(Model configuration);

    /**
     * Return a description of the capabilities of this reasoner encoded in
     * RDF. These capabilities may be static or may depend on configuration
     * information supplied at construction time. May be null if there are
     * no useful capabilities registered.
     */
    public Model getCapabilities();
    
    /**
     * Return the URI labelling this type of reasoner
     */
    public String getURI();
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

