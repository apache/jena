/******************************************************************
 * File:        ReasonerFactory.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jan-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ReasonerFactory.java,v 1.9 2004-12-07 09:56:29 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.rdf.model.*;

/**
 * The interface through which a reasoner (inference engine) can be
 * instantiated. Instances of this are registered with the global
 * ReasonerRegistry.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.9 $ on $Date: 2004-12-07 09:56:29 $
 */
public interface ReasonerFactory {

    /**
     * Constructor method that builds an instance of the associated Reasoner
     * @param configuration a set of arbitrary configuration information to be 
     * passed the reasoner, encoded as RDF properties of a base configuration resource,
     * can be null in no custom configuration is required.
     */
    public Reasoner create(Resource configuration);

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
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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

