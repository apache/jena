/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: ModelMakerCreatorRegistry.java,v 1.6 2005-02-21 12:14:11 andy_seaborne Exp $
*/

package com.hp.hpl.jena.rdf.model;

import com.hp.hpl.jena.vocabulary.*;
import com.hp.hpl.jena.rdf.model.impl.*;

import java.util.*;

/**
    A registry of ways of creating ModelMakers, keyed by their JMS type. A
    ModelMakerCreator registered by type here will be used by a ModelSpec
    description which needs a ModelMaker and supplies a registered type for
    that maker.
<p>
    The Registry is pre-loaded with the three standard ModelMakerCreator's,
    for Mem, File, and RDB ModelMakers.
 
 	@author hedgehog
*/
public class ModelMakerCreatorRegistry
    {
    /**
     	No instances of this class exist- it's all static.
    */
    private ModelMakerCreatorRegistry()
        {}

    /**
        The map from JMS ModelMakerSpecs to the ModelMakerCreator.
    */
    private static Map creators = new HashMap();
    
    /**
        Answer the Creator which has been registred with the given type, or null
        if there's no such registered Creator
        
    	@param type the JMS type of the Creator
    	@return that Creator, or null
    */
    public static ModelMakerCreator findCreator( Resource type )
        { return (ModelMakerCreator) creators.get( type ); }
        
    /**
        Register the given ModelMakerCreator under the given JMS type.
        
    	@param type the type to register it as
    	@param mmc the Creator to register
    */
    public static void register( Resource type, ModelMakerCreator mmc )
        { creators.put( type, mmc ); }        
    
    /**
        The default maker-creator, hauled out here as a constant in case we
        consider changing it [and so it has an identifying name].
    */
    private static final ModelMakerCreator defaultMakerCreator =  new MemMakerCreator();
    
    /**
        Register the three standard MakerCreators under their JMS Resources.  
        We also recognise a non-specific MakerSpec as meaning a default
        maker type.
    */
    static
        {
        register( JMS.MakerSpec, defaultMakerCreator );    
        register( JMS.FileMakerSpec, new FileMakerCreator() );    
        register( JMS.MemMakerSpec, new MemMakerCreator() );    
        register( JMS.RDBMakerSpec, new RDBMakerCreator() );    
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