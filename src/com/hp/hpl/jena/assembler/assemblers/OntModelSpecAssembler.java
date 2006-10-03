/*
 	(c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: OntModelSpecAssembler.java,v 1.7 2006-10-03 14:48:51 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.lang.reflect.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.ReasonerClashException;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.shared.JenaException;

public class OntModelSpecAssembler extends AssemblerBase implements Assembler
    {
    private static final OntModelSpec DEFAULT = OntModelSpec.OWL_MEM_RDFS_INF;

    public Object open( Assembler a, Resource root, Mode irrelevant )
        {
        if (root.hasProperty( null, (RDFNode) null )) 
            {
            checkType( root, JA.OntModelSpec );
            OntModelSpec spec = new OntModelSpec( getDefault( root ) );
            spec.setDocumentManager( getDocumentManager( a, root ) );
            spec.setReasonerFactory( getReasonerFactory( a, root ) );
            spec.setLanguage( getLanguage( a, root ) );
            spec.setImportModelGetter( getModelSource( a, root ) );
            return spec;
            }
        else
            {
            OntModelSpec oms = getOntModelSpecField( root.getLocalName() );
            if (oms == null) throw new JenaException( "no such OntModelSpec: " + root );
            return oms;                
            }
        }

    private OntModelSpec getDefault( Resource root )
        {
        if (root.getNameSpace().equals( JA.uri ))
            {
            OntModelSpec oms = getOntModelSpecField( root.getLocalName() );
            return oms == null ? DEFAULT : oms;
            }
        else
            return DEFAULT;
        }
    
    private ModelGetter getModelSource( Assembler a, Resource root )
        {
        Resource source = getUniqueResource( root, JA.importSource );
        return source == null ? DEFAULT.getImportModelGetter() : (ModelGetter) a.open( source );
        }

    private String getLanguage( Assembler a, Resource root )
        {
        Resource lang = getUniqueResource( root, JA.ontLanguage );
        return lang == null ? DEFAULT.getLanguage() : lang.getURI();
        }

    private ReasonerFactory getReasonerFactory( Assembler a, Resource root )
        {
        Resource rf = getUniqueResource( root, JA.reasonerFactory );
        Resource ru = getUniqueResource( root, JA.reasonerURL );
        if (ru != null && rf != null) throw new ReasonerClashException( root );
        if (ru != null) return ReasonerFactoryAssembler.getReasonerFactoryByURL( root, ru );
        return rf == null ? DEFAULT.getReasonerFactory() : (ReasonerFactory) a.open( rf );
        }
    
    private OntDocumentManager getDocumentManager( Assembler a, Resource root )
        { 
        Resource dm = getUniqueResource( root, JA.documentManager );
        return dm == null ? OntDocumentManager.getInstance() : (OntDocumentManager) a.open( dm );
        }
    
    /**
        Answer the OntModelSpec in the OntModelSpec class with the given
        member name, or null if there isn't one.
    */
    public static OntModelSpec getOntModelSpecField( String name )
        {
        try 
            { 
            Class omc = OntModelSpec.class;
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


/*
 * (c) Copyright 2005, 2006 Hewlett-Packard Development Company, LP
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