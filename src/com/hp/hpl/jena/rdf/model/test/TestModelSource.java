/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	[See end of file]
*/

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.ModelSource;

import junit.framework.TestSuite;

/**
     Test cases driving ModelSource
     @author kers
*/
public class TestModelSource extends ModelTestBase
    {


    public TestModelSource( String name )
        { super( name ); }

    public static TestSuite suite()
        { return new TestSuite( TestModelSource.class ); }
    
    public ModelSource getModelSource()
        { return new ModelSourceImpl(); }
    
    public void testMethodsExist()
        { 
        ModelSource s = getModelSource();
        // s.getModel();
        s.createModel();
        }    
    
    /**
        Minimal test implementation of ModelSource. There should be more of
        these.
    
        @author hedgehog
    */
    public static class ModelSourceImpl implements ModelSource
        {
        public Model openModel( String name )
            { return ModelFactory.createDefaultModel(); }
    
        public Model getExistingModel(String name)
            { return null; }
    
        Model theDefaultModel = ModelFactory.createDefaultModel();
        
        public Model getModel()
            { return theDefaultModel; }

        public Model createModel()
            { return getModel(); }
        }
    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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