/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: JMS.java,v 1.9 2003-08-24 16:23:41 chris-dollin Exp $
*/

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;

/**
    The Jena Model Specification vocabulary, schema, and some conversion methods.
    TODO ensure these have explicit tests [they were developed implicitly from the
    ModelSpec tests].
    
 	@author kers
*/
public class JMS
    {
    public static final String baseURI = "http://jana.hpl.hp.com/2003/08/jms#";
    
    public static String getURI()
        { return baseURI; }
    
    public static final Property ontLanguage = property( "ontLanguage" );
    public static final Property docManager = property( "docManager" );
    public static final Property importMaker = property( "importMaker" );
    public static final Property reasonsWith = property( "reasonsWith" );
    
    public static final Property dbUser = property( "dbUser" );
    public static final Property dbPassword = property( "dbPassword" );
    public static final Property dbURL = property( "dbURL" );
    public static final Property dbType = property( "dbType" );
    public static final Property dbClass = property( "dbClass" );
    
    public static final Property maker = property( "maker" );
    
    public static final Property reificationMode = property( "reificationMode" );
    public static final Property reasoner = property( "reasoner" );
    public static final Property fileBase = property( "fileBase" );
    
    public static final Resource MakerSpec = resource( "MakerSpec" );
    public static final Resource FileMakerSpec = resource( "FileMakerSpec" );
    public static final Resource MemMakerSpec = resource( "MemMakerSpec" );
    public static final Resource RDBMakerSpec = resource( "RDBMakerSpec" );
    
    public static final Resource PlainModelSpec = resource( "PlainModelSpec" );
    public static final Resource InfModelSpec = resource( "ReasonerSpec" );
    public static final Resource OntModelSpec = resource( "OntModelSpec" );
    
    public static final Resource rsStandard = resource( "rsStandard" );
    public static final Resource rsMinimal = resource( "rsMinimal" );
    public static final Resource rsConvenient = resource( "rsConvenient" );

    /**
        The JMS schema encoded into a model. 
        
        TODO make this model immutable once created. 
    */
    static final public Model schema = ModelFactory.createDefaultModel()
        .add( JMS.MemMakerSpec, RDFS.subClassOf, JMS.MakerSpec )
        .add( JMS.FileMakerSpec, RDFS.subClassOf, JMS.MakerSpec )
        .add( JMS.RDBMakerSpec, RDFS.subClassOf, JMS.MakerSpec )
        .add( JMS.InfModelSpec, RDFS.subClassOf, JMS.PlainModelSpec )
        .add( JMS.OntModelSpec, RDFS.subClassOf, JMS.InfModelSpec )
        .add( JMS.reificationMode, RDFS.domain, JMS.MakerSpec )
        .add( JMS.ontLanguage, RDFS.domain, JMS.OntModelSpec )
        .add( JMS.reasoner, RDFS.domain, JMS.InfModelSpec )
        .add( JMS.importMaker, RDFS.subClassOf, JMS.maker )
        ;
    
    protected static Literal literal( String lex )
        { return ResourceFactory.createPlainLiteral( lex ); }
        
    protected static Resource resource( String ln )
        { return ResourceFactory.createResource( baseURI + ln ); }
        
    protected static Property property( String ln )
        { return ResourceFactory.createProperty( baseURI + ln ); }

    /**
        Answer the Node which corresponds to the supplied reification style. [Node,
        not resource, purely because the use happens in BaseGraphMaker, ie at the
        Graph level.]
        
        @param style the reification style for which the JMS representation is required
        @return the Node version of the appropriate JMS.rs[name] vocabulary item
    */
    public static Node styleAsJMS( Reifier.Style style )
        {
        if (style == Reifier.Minimal) return JMS.rsMinimal.asNode();
        if (style == Reifier.Convenient) return JMS.rsConvenient.asNode();
        if (style == Reifier.Standard) return JMS.rsStandard.asNode();
        return null;
        }

    /**
        Answer the Reifier.Style value named by the argument, which should be a
        JMS.rs[something] value
        
        @param style the JMS name of the reifier style
        @return the actual Reifier.Style value
    */
    public static Reifier.Style findStyle( RDFNode style )
        {
        if (style.equals(JMS.rsStandard )) return Reifier.Standard;    
        if (style.equals(JMS.rsMinimal)) return Reifier.Minimal;    
        if (style.equals( JMS.rsConvenient)) return Reifier.Convenient;
        return null;
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