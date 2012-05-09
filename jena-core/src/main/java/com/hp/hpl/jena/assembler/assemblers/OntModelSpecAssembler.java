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

package com.hp.hpl.jena.assembler.assemblers;

import java.lang.reflect.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.ReasonerClashException;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.shared.NotFoundException;

/**
    An OntModelSpecAssembler constructs OntModelSpec's from their
    RDF description. The description allows the document manager, the
    reasoner factory, the ont language, and the import model getter to
    be specified: the default values will be those of OWL_MEM_RDFS_INF,
    unless the root is ja:SPOO for some constant SPOO of OntModelSpec,
    in which case the defaults are taken from there.
*/
public class OntModelSpecAssembler extends AssemblerBase implements Assembler
    {
    private static final OntModelSpec DEFAULT = OntModelSpec.OWL_MEM_RDFS_INF;

    @Override
    public Object open( Assembler a, Resource root, Mode irrelevant )
        {
        checkType( root, JA.OntModelSpec );
        OntModelSpec spec = new OntModelSpec( getDefault( root ) );
        OntDocumentManager dm = getDocumentManager( a, root );
        ReasonerFactory rf = getReasonerFactory( a, root );
        String lang = getLanguage( a, root );
        ModelGetter source = getModelSource( a, root );
        if (dm != null) spec.setDocumentManager( dm );
        if (rf != null) spec.setReasonerFactory( rf );
        if (lang != null) spec.setLanguage( lang );
        if (source != null) spec.setImportModelGetter( source );
        return spec;
        }
    
    private ModelGetter getModelSource( Assembler a, Resource root )
        {
        Resource source = getUniqueResource( root, JA.importSource );
        return source == null ? null : (ModelGetter) a.open( source );
        }

    private String getLanguage( Assembler a, Resource root )
        {
        Resource lang = getUniqueResource( root, JA.ontLanguage );
        return lang == null ? null : lang.getURI();
        }

    private ReasonerFactory getReasonerFactory( Assembler a, Resource root )
        {
        Resource rf = getUniqueResource( root, JA.reasonerFactory );
        Resource ru = getUniqueResource( root, JA.reasonerURL );
        if (ru != null && rf != null) throw new ReasonerClashException( root );
        if (ru != null) return ReasonerFactoryAssembler.getReasonerFactoryByURL( root, ru );
        return rf == null ? null : (ReasonerFactory) a.open( rf );
        }
    
    private OntDocumentManager getDocumentManager( Assembler a, Resource root )
        {
        Resource dm = getUniqueResource( root, JA.documentManager );
        return dm == null ? null : (OntDocumentManager) a.open( dm );
        }

    /**
        Answer the default OntModelSpec for this root, which will be
        <code>DEFAULT</code> unless <code>root</code> has the JA
        namespace and a local name which is the name of an OntModelSpec
        constant (in OntModelSpec), in which case it's that constant's value.
    */
    private OntModelSpec getDefault( Resource root )
        {
        if (root.isURIResource() && root.getNameSpace().equals( JA.uri ))
            {
            OntModelSpec oms = getOntModelSpecField( root.getLocalName() );
            return oms == null ? DEFAULT : oms;
            }
        else
            {
            Resource like = getUniqueResource( root, JA.likeBuiltinSpec );
            return like == null ? DEFAULT : getRequiredOntModelSpecField( like.getLocalName() );
            }
        }
    
    private OntModelSpec getRequiredOntModelSpecField( String name )
        {
        OntModelSpec result = getOntModelSpecField( name );
        if (result == null) throw new NotFoundException( name );
        return result;
        }

    /**
        Answer the OntModelSpec in the OntModelSpec class with the given
        member name, or null if there isn't one.
    */
    public static OntModelSpec getOntModelSpecField( String name )
        {
        try 
            { 
            Class<OntModelSpec> omc = OntModelSpec.class;
            Field f = omc.getField( name ); 
            int mods = f.getModifiers();
            if (f.getType() == omc && isConstant( mods )) 
                return (OntModelSpec) f.get( null );            
            }
        catch (Exception e) 
            {}
        return null;
        }

    protected static boolean isConstant( int mods )
        { return Modifier.isPublic( mods ) && Modifier.isFinal( mods ) && Modifier.isStatic( mods ); }
    }
