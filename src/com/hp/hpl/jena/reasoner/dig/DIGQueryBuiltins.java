/*****************************************************************************
 * Source code information
 * -----------------------
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            July 19th 2003
 * Filename           $RCSfile: DIGQueryBuiltins.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2004-05-01 16:23:37 $
 *               by   $Author: ian_dickinson $
 *
 * (c) Copyright 2001, 2002, 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 * ****************************************************************************/

// Package
///////////////
package com.hp.hpl.jena.reasoner.dig;


// Imports
///////////////
import java.util.*;

import org.w3c.dom.Document;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.util.iterator.*;
import com.hp.hpl.jena.vocabulary.*;


/**
 * <p>
 * Knowledge of some triples is built-in to the DIG adapter, for example 
 * that <code>owl:Thing</code> is a class et.
 * </p>
 *
 * @author Ian Dickinson, HP Labs (<a href="mailto:Ian.Dickinson@hp.com">email</a>)
 * @version Release @release@ ($Id: DIGQueryBuiltins.java,v 1.1 2004-05-01 16:23:37 ian_dickinson Exp $)
 */
public class DIGQueryBuiltins 
    extends DIGQueryTranslator
{

    // Constants
    //////////////////////////////////

    private static List S_BUILTINS = Arrays.asList( new Object[] {
            new TriplePattern( OWL.Nothing.getNode(),               RDF.type.getNode(), OWL.Class.getNode() ),
            new TriplePattern( OWL.Thing.getNode(),                 RDF.type.getNode(), OWL.Class.getNode() ),
            
            new TriplePattern( OWL.AllDifferent.getNode(),          RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.AnnotationProperty.getNode(),    RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.Class.getNode(),                 RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.DataRange.getNode(),             RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.DatatypeProperty.getNode(),      RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.DeprecatedClass.getNode(),       RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.DeprecatedProperty.getNode(),    RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.FunctionalProperty.getNode(),    RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.InverseFunctionalProperty.getNode(), RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.Nothing.getNode(),               RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.ObjectProperty.getNode(),        RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.Ontology.getNode(),              RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.OntologyProperty.getNode(),      RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.Restriction.getNode(),           RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.SymmetricProperty.getNode(),     RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.Thing.getNode(),                 RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( OWL.TransitiveProperty.getNode(),    RDF.type.getNode(), RDFS.Class.getNode() ),

            new TriplePattern( OWL.allValuesFrom.getNode(),         RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.backwardCompatibleWith.getNode(), RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.cardinality.getNode(),           RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.complementOf.getNode(),          RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.differentFrom.getNode(),         RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.disjointWith.getNode(),          RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.distinctMembers.getNode(),       RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.equivalentClass.getNode(),       RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.equivalentProperty.getNode(),    RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.hasValue.getNode(),              RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.imports.getNode(),               RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.incompatibleWith.getNode(),      RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.intersectionOf.getNode(),        RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.inverseOf.getNode(),             RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.maxCardinality.getNode(),        RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.minCardinality.getNode(),        RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.oneOf.getNode(),                 RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.onProperty.getNode(),            RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.priorVersion.getNode(),          RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.sameAs.getNode(),                RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.someValuesFrom.getNode(),        RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.unionOf.getNode(),               RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( OWL.versionInfo.getNode(),           RDF.type.getNode(), RDF.Property.getNode() ),

            new TriplePattern( DAML_OIL.Nothing.getNode(),               RDF.type.getNode(), DAML_OIL.Class.getNode() ),
            new TriplePattern( DAML_OIL.Thing.getNode(),                 RDF.type.getNode(), DAML_OIL.Class.getNode() ),
            
            new TriplePattern( DAML_OIL.Class.getNode(),                 RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.Datatype.getNode(),              RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.DatatypeProperty.getNode(),      RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.UniqueProperty.getNode(),        RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.UnambiguousProperty.getNode(),   RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.Nothing.getNode(),               RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.ObjectProperty.getNode(),        RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.Ontology.getNode(),              RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.Restriction.getNode(),           RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.Thing.getNode(),                 RDF.type.getNode(), RDFS.Class.getNode() ),
            new TriplePattern( DAML_OIL.TransitiveProperty.getNode(),    RDF.type.getNode(), RDFS.Class.getNode() ),

            new TriplePattern( DAML_OIL.toClass.getNode(),               RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.cardinality.getNode(),           RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.complementOf.getNode(),          RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.differentIndividualFrom.getNode(), RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.disjointWith.getNode(),          RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.sameClassAs.getNode(),           RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.samePropertyAs.getNode(),        RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.hasValue.getNode(),              RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.imports.getNode(),               RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.intersectionOf.getNode(),        RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.inverseOf.getNode(),             RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.maxCardinality.getNode(),        RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.minCardinality.getNode(),        RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.oneOf.getNode(),                 RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.onProperty.getNode(),            RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.equivalentTo.getNode(),          RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.hasClass.getNode(),              RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.unionOf.getNode(),               RDF.type.getNode(), RDF.Property.getNode() ),
            new TriplePattern( DAML_OIL.versionInfo.getNode(),           RDF.type.getNode(), RDF.Property.getNode() ),
    } );
    
    // Static variables
    //////////////////////////////////

    // Instance variables
    //////////////////////////////////

    // Constructors
    //////////////////////////////////

    /**
     * <p>Construct a query module that will answer tests on the OWL/DAML builtins.</p>
     */
    public DIGQueryBuiltins() {
        super( null, null, null );
    }
    

    // External signature methods
    //////////////////////////////////


    /**
     * <p>Since known concept names are cached by the adapter, we can just look up the
     * current set and map directly to triples</p>
     * @param pattern The pattern to translate to a DIG query
     * @param da The DIG adapter through which we communicate with a DIG reasoner
     */
    public ExtendedIterator find( TriplePattern pattern, DIGAdapter da ) {
        return (ExtendedIterator) new SingletonIterator( pattern.asTriple() );
    }
    
    /** For this translation, we ignore premises */
    public ExtendedIterator find( TriplePattern pattern, DIGAdapter da, Model premises ) {
        return find( pattern, da );
    }
    
    
    public Document translatePattern( TriplePattern pattern, DIGAdapter da ) {
        // not used
        return null;
    }


    public Document translatePattern( TriplePattern pattern, DIGAdapter da, Model premises ) {
        // not used
        return null;
    }

    public ExtendedIterator translateResponse( Document response, TriplePattern query, DIGAdapter da ) {
        // not used
        return null;
    }

    public boolean checkTriple( TriplePattern pattern, DIGAdapter da, Model premises ) {
        return S_BUILTINS.contains( pattern );
    }
    

    // Internal implementation methods
    //////////////////////////////////

    //==============================================================================
    // Inner class definitions
    //==============================================================================

}


/*
 *  (c) Copyright 2001-2004 Hewlett-Packard Development Company, LP
 *  All rights reserved.
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
