/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.core.assembler;

import com.hp.hpl.jena.assembler.JA ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.ResourceFactory ;

public class DatasetAssemblerVocab
{
    public static final String NS = JA.getURI() ;
    public static String getURI() { return NS ; }
    
    public static final Resource tDataset            = ResourceFactory.createResource(NS+"RDFDataset") ;
    public static final Resource tGraphStore         = ResourceFactory.createResource(NS+"GraphStore") ;
    public static final Property pDefaultGraph       = ResourceFactory.createProperty(NS, "defaultGraph") ;
    public static final Property pNamedGraph         = ResourceFactory.createProperty(NS, "namedGraph") ;
    
    public static final Property pGraphName          = ResourceFactory.createProperty(NS, "graphName") ;
    public static final Property pGraph              = ResourceFactory.createProperty(NS, "graph") ;
    public static final Property pGraphAlt           = ResourceFactory.createProperty(NS, "graphData") ;

    public static final Property pIndex              = ResourceFactory.createProperty(NS, "textIndex") ;
}

/*
 * (c) Copyright 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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