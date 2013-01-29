/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.assembler.test;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.assembler.exceptions.CannotConstructException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.test.ModelTestBase;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    A convenient base class for Assembler tests. The instance methods
    <code>modelAddFacts</code> and <code>setRequiredPrefixes</code>
    may be over-ridden in subclasses to control the parser that is used to
    construct models and the prefixes added to the model (these features
    added for Eyeball).
*/
public class AssemblerTestBase extends ModelTestBase
    {
    protected Class<? extends Assembler> getAssemblerClass()
        { throw new BrokenException( "this class must define getAssemblerClass" ); }
    
    /**
         An assembler that always returns the same fixed object.
    */
    protected static final class FixedObjectAssembler extends AssemblerBase
        {
        private final Object x;
        
        protected FixedObjectAssembler( Object x )
            { this.x = x; }
        
        
        @Override
        public Object open( Assembler a, Resource root, Mode irrelevant )
            { return x; }
        }

    /**
        An assembler that insists on being called on a given name, and always
        returns the same fixed object.
    */
    protected static class NamedObjectAssembler extends AssemblerBase
            {
            final Resource name;
            final Object result;
            
            public NamedObjectAssembler( Resource name, Object result )
                { this.name = name; this.result = result; }
            
            @Override
            public Model openModel( Resource root, Mode mode )
                { return (Model) open( this, root, mode ); }
            
            @Override
            public Object open( Assembler a, Resource root, Mode irrelevant )
                {
                assertEquals( name, root );
                return result;
                }
            }

    protected static final Model schema = JA.getSchema();

    public AssemblerTestBase( String name )
        { super( name ); }
    
    protected Model model( String string )
        { 
        Model result = createModel( );
        setRequiredPrefixes( result );
        return modelAddFacts( result, string );
        }
    
    protected Model model()
        { return model( "" ); }

    /**
     	Subclasses may override to use their choice of string-to-model
        parsers.
    */
    protected Model modelAddFacts( Model result, String string )
        { return modelAdd( result, string ); }

    /**
        Subclasses may extend to get their choice of defined prefixes.
    */
    protected Model setRequiredPrefixes( Model m )
        {
        m.setNsPrefix( "ja", JA.getURI() );
        m.setNsPrefix( "lm", LocationMappingVocab.getURI() );
        return m;
        }

    protected Resource resourceInModel( String string )
        {
        Model m = model( string );
        Resource r = resource( m, string.substring( 0, string.indexOf( ' ' ) ) );
        return r.inModel( m );        
        }

    protected void testDemandsMinimalType( Assembler a, Resource type )
        {
        try
            { a.open( resourceInModel( "x rdf:type rdf:Resource" ) ); 
            fail( "should trap insufficient type" ); }
        catch (CannotConstructException e)
            {
            assertEquals( getAssemblerClass(), e.getAssemblerClass() );
            assertEquals( type, e.getType() ); 
            assertEquals( resource( "x" ), e.getRoot() );
            }
        }

    protected void assertSamePrefixMapping( PrefixMapping wanted, PrefixMapping got )
        {
        if (!wanted.samePrefixMappingAs( got ))
            fail( "wanted: " + wanted + " but got: " + got );
        }

    /**
         assert that the property <code>p</code> has <code>domain</code> as
         its rdfs:domain.
    */
    protected void assertDomain( Resource domain, Property p )
        { 
        if (!schema.contains( p, RDFS.domain, domain ))
            fail( p + " was expected to have domain " + domain );
        }    

    /**
         assert that the property <code>p</code> has <code>range</code> as
         its rdfs:range.
    */
    protected void assertRange( Resource range, Property p )
        { 
        if (!schema.contains( p, RDFS.range, range ))
            fail( p + " was expected to have range " + range );
        }

    /**
         assert that <code>expectedSub</code> is an rdfs:subClassOf
         <code>expectedSuper</code>.
    */
    protected void assertSubclassOf( Resource expectedSub, Resource expectedSuper )
        { 
        if (!schema.contains( expectedSub, RDFS.subClassOf, expectedSuper ))
            fail( expectedSub + " should be a subclass of " + expectedSuper ); 
        }

    /**
         assert that <code>instance</code> has rdf:type <code>type</code>.
    */
    protected void assertType( Resource type, Resource instance )
        {
        if (!schema.contains( instance, RDF.type, type ))
            fail( instance + " should have rdf:type " + type );
        }
    }
