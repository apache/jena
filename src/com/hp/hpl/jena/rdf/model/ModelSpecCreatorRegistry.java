/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: ModelSpecCreatorRegistry.java,v 1.1 2003-08-25 11:54:14 chris-dollin Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.vocabulary.*;

import java.util.*;

/**
    The registry of creators appropriate for different ModelSpec types.
 	@author hedgehog
 */
public class ModelSpecCreatorRegistry
    {
    private static Map creators = new HashMap();     
    
    public static ModelSpecCreator findCreator( Resource type )
        { return (ModelSpecCreator) creators.get( type ); }
    
    public static void register( Resource type, ModelSpecCreator c )
        { creators.put( type, c ); }
        
   static class InfSpecCreator implements ModelSpecCreator
        {
        public ModelSpec create( Model desc ) { return new InfModelSpec( desc ); }    
        }
        
    static class PlainSpecCreator implements ModelSpecCreator
        {
        public ModelSpec create( Model desc ) { return new PlainModelSpec( desc ); }    
        }
            
    static class OntSpecCreator implements ModelSpecCreator
        {
        public ModelSpec create( Model desc ) { return new OntModelSpec( desc ); }    
        }
                        
    static
        {
        ModelSpecCreatorRegistry.register( JMS.InfModelSpec, new InfSpecCreator() );  
        ModelSpecCreatorRegistry.register( JMS.OntModelSpec, new OntSpecCreator() );  
        ModelSpecCreatorRegistry.register( JMS.PlainModelSpec, new PlainSpecCreator() );    
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