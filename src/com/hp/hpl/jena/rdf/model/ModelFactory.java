/*
  (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelFactory.java,v 1.2 2003-02-03 19:11:16 der Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.mem.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rdfsReasoner1.RDFSReasonerFactory;

/**
    Factory provides methods for creating standard kinds of Model. This
    initial version provides only a single default Model with no trimmings. 
*/

public class ModelFactory
{
    /** deliver a new Model (implemented as a ModelMem, but that's secret) */
    public static Model createDefaultModel()
        { return new ModelMem(); }
        
    /**
     * Return a Model through which all the RDFS entailments 
     * derivable from the given model are accessible. Some work is done
     * when the inferenced model is created but each query will also trigger some
     * additional inference work.
     * <p> The current implementation is <em>very</em> preliminary
     * and will not scale to large models. In particular, it will make redundant 
     * passes over the data when asked a very ungrounded query (such as list all 
     * statements!). </p>
     * 
     * @param model the Model containing both instance data and schema assertions to be inferenced over
     */
    public static Model createRDFSModel(Model model) {
         ReasonerFactory rf = RDFSReasonerFactory.theInstance();
         Reasoner reasoner  = rf.create(null);
         InfGraph graph     = reasoner.bind(model.getGraph());
         return new ModelMem(graph);
    }
        
    /**
     * Return a Model through which all the RDFS entailments 
     * derivable from the given data and schema models are accessible. 
     * There is no strict requirement to separate schema and instance data between the two
     * arguments.
     * <p>Some work is donewhen the inferenced model is created. This work can be reused if the
     * same schema is to be applied to multiple datasets though use of the direct SPI.</p>
     * <p> The current implementation is <em>very</em> preliminary
     * and will not scale to large models. In particular, it will make redundant 
     * passes over the data when asked a very ungrounded query (such as list all 
     * statements!). </p>
     * 
     * @param model a Model containing instance data assertions 
     * @param schema a Model containing RDFS schema data
     */
    public static Model createRDFSModel(Model schema, Model model) {
         ReasonerFactory rf = RDFSReasonerFactory.theInstance();
         Reasoner reasoner  = rf.create(null);
         InfGraph graph     = reasoner.bindSchema(schema.getGraph()).bind(model.getGraph());
         return new ModelMem(graph);
    }
    
}
    
/*
    (c) Copyright Hewlett-Packard Company 2002
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