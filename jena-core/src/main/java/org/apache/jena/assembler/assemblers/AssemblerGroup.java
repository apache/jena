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

package org.apache.jena.assembler.assemblers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.AssemblerHelp;
import org.apache.jena.assembler.JA;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.exceptions.AmbiguousSpecificTypeException;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.assembler.exceptions.NoImplementationException;
import org.apache.jena.assembler.exceptions.NoSpecificTypeException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;

public abstract class AssemblerGroup extends AssemblerBase implements Assembler
    {    
    public abstract AssemblerGroup implementWith( Resource type, Assembler a );

    public abstract Assembler assemblerFor( Resource type );

    @Override public Model openModel( Resource resource )
        { return (Model) open( resource ); }
    
    public static AssemblerGroup create()
        { return new ExpandingAssemblerGroup(); }
    
    public AssemblerGroup copy()
        {
        ExpandingAssemblerGroup result = (ExpandingAssemblerGroup) create();
        result.internal.mappings.putAll( ((ExpandingAssemblerGroup) this).internal.mappings );
        return result;
        }
    
    public static class Frame
        {
        public final Resource root;
        public final Resource type;
        public final Class< ? extends Assembler> assembler;
        
        public Frame( Resource root, Resource type, Class< ? extends Assembler> assembler )
            { this.root = root; this.type = type; this.assembler = assembler; }
        
        @Override public boolean equals( Object other )
            { return other instanceof Frame && same( (Frame) other ); }
        
        protected boolean same( Frame other )
            { 
            return root.equals( other.root )
                && type.equals( other.type )
                && assembler.equals( other.assembler )
                ; 
            }
        
        @Override public String toString()
            { return "root: " + root + " with type: " + type + " assembler class: " + assembler; }
        }

    public static class ExpandingAssemblerGroup extends AssemblerGroup
        {
        PlainAssemblerGroup internal = new PlainAssemblerGroup();
        Model implementTypes = ModelFactory.createDefaultModel();
        
        @Override public Object open( Assembler a, Resource suppliedRoot, Mode mode )
            {
            Resource root = AssemblerHelp.withFullModel( suppliedRoot );
            loadClasses( root.getModel() );
            root.getModel().add( implementTypes );
            return internal.open( a, root, mode );
            }

        public void loadClasses( Model model )
            {
            AssemblerHelp.loadArbitraryClasses( this, model );
            AssemblerHelp.loadAssemblerClasses( this, model ); 
            }

        @Override public AssemblerGroup implementWith( Resource type, Assembler a )
            {
            // This is called during Jena-wide initialization.
            // Use function for constant (JENA-1294)
            implementTypes.add( type, RDFS.Init.subClassOf(), JA.Object );
            internal.implementWith( type, a );
            return this;
            }

        @Override public Assembler assemblerFor( Resource type )
            { return internal.assemblerFor( type ); }
        
        public Set<Resource> implementsTypes()
            { 
            return implementTypes.listStatements().mapWith( Statement::getSubject ).toSet(); }
            }
    
    static class PlainAssemblerGroup extends AssemblerGroup
        {
        Map<Resource, Assembler> mappings = new HashMap<>();

        @Override public Object open( Assembler a, Resource root, Mode mode )
            {
            Set <Resource>types = AssemblerHelp.findSpecificTypes( root, JA.Object );
            if (types.size() == 0) {
                // Does it exist as a subject in the model? Mistyped?
                boolean noSuchSubject = ! root.listProperties().hasNext() ;
                if ( noSuchSubject ) {
                    String s ;
                    if ( root.isURIResource() )
                        s = "<"+root.getURI()+">" ;
                    else if ( root.isAnon() )
                        s = "_:"+root.getId() ;
                    else
                        s = String.valueOf(root) ;
                    throw new AssemblerException(root, "Can't find "+s+" as a subject") ;
                }
                throw new NoSpecificTypeException( root );
            }
            else if (types.size() > 1)
                throw new AmbiguousSpecificTypeException( root, new ArrayList<>( types ) );
            else
                return openBySpecificType( a, root, mode, types.iterator().next() );
            }

        private Object openBySpecificType( Assembler a, Resource root, Mode mode, Resource type )
            {
            Assembler toUse = assemblerFor( type );
            Class<? extends Assembler> aClass = toUse == null ? null : toUse.getClass();
            Frame frame = new Frame( root, type, aClass );
            try 
                { 
                if (toUse == null)
                    throw new NoImplementationException( this, root, type );
                else
                    return toUse.open( a, root, mode ); 
                }
            catch (AssemblerException e) 
                { 
                throw e.pushDoing( frame ); 
                }
            catch (Exception e) 
                { 
                AssemblerException x = new AssemblerException( root, "caught: " + e.getMessage(), e ); 
                throw x.pushDoing( frame );
                }
            }
        
        @Override public AssemblerGroup implementWith( Resource type, Assembler a )
            {
            mappings.put( type, a );
            return this;
            }

        @Override public Assembler assemblerFor( Resource type )
            { return mappings.get( type ); }
        }
    }
