/*
  (c) Copyright 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: JMS.java,v 1.30 2005-03-09 20:00:43 chris-dollin Exp $
*/

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.util.FileManager;

/**
    The Jena Model Specification vocabulary, schema, and some conversion methods.
    See the modelspec and modelspec-details HOWTOs for discussion on how these
    fit into the ModelSpec design and implementation.
    
    TODO ensure these have explicit tests [they were developed implicitly from the
    ModelSpec tests].
    
 	@author kers
*/
public class JMS
    {
    /**
        The base URI for all the JMS vocabulary items. 
    */
    public static final String baseURI = "http://jena.hpl.hp.com/2003/08/jms#";
    
    /**
        Answer the base URI for the JMS vocabulary items.
     */
    public static String getURI()
        { return baseURI; }
    
    /**
     	The property of a ModelSpec that specifies the URI(s) from which it is
     	to be loaded when it has been created.
    */
    public static final Property loadWith = property( "loadWith" );
    
    /** 
        The property of an OntModelSpec that gives the language URI string.
    */
    public static final Property ontLanguage = property( "ontLanguage" );
    
    /**
        The property of an OntModelSpec that gives the document manager resource.
    */
    public static final Property docManager = property( "docManager" );
    
    /**
        The property of an OntModelSpec that gives the MakerSpec used when 
        manufacturing models for imports.
    */
    public static final Property importMaker = property( "importMaker" );
    
    /**
        The property of an InfModelSpec that specifies the ReasonerSpec to use.
    */
    public static final Property reasonsWith = property( "reasonsWith" );
    
    /**
         The property of a reasoner spec that specifies a rule-set URL.
    */
    public static final Property ruleSetURL = property( "ruleSetURL" );
    
    /**
         The property of a reasoner spec that specifies in-line rule-sets.
    */
    public static final Property ruleSet = property( "ruleSet" );

    /**
        The property of a ModelSpec that specifies the model name.
    */
    public static final Property modelName = property( "modelName" );
    
    /**
     	The property of a reasoner spec that specifies a schema to load
    */
    public static final Property schemaURL = property( "schemaURL" );
    
    /**
         The property of a RuleSet that specifies a rule.
    */
    public static final Property hasRule = property( "hasRule" );
    
    /**
        The property of a document manager that gives its policy path string.
     */
    public static final Property policyPath = property( "policyPath" );
    
    /**
        The property of an RDBMakerSpec that gives the string to use for the user name
        when making the connection.
    */
    public static final Property dbUser = property( "dbUser" );
    
    /**
        The property of an RDBMakerSpec that gives the string to use for the password
        when making the connection.
    */
    public static final Property dbPassword = property( "dbPassword" );
    
    /**
        The property of an RDBMakerSpec that gives the string to use for the database URL
        when making the connection.
    */
    public static final Property dbURL = property( "dbURL" );
    
    /**
        The property of an RDBMakerSpec that gives the string to use for the database
        type when making the connection.
    */
    public static final Property dbType = property( "dbType" );
    
    /**
        The property of an RDBMakerSpec that gives the string to use for class to load
        [if any] when making the connection.
    */
    public static final Property dbClass = property( "dbClass" );
    
    /**
        The property of a ModelSpec that specifies the resource which describes the maker.
    */
    public static final Property maker = property( "maker" );
    
    /**
        The property of a MakerSpec that gives the reification mode for all its models.
    */
    public static final Property reificationMode = property( "reificationMode" );
    
    /**
        The property of a ReasonerSpec that gives the resource who's URI is that of
        the reasoner to use.
    */
    public static final Property reasoner = property( "reasoner" );
    
    /**
        The property of a FileMakerSpec that gives the fileBase [root directory] of the
        FileModelMaker.
    */
    public static final Property fileBase = property( "fileBase" );
    
    /**
        The property of some subclass of jms:ModelSpec that specifies the name of the
        Java class that implements that ModelSpec.
    */
    public static final Property typeCreatedBy = property( "typeCreatedBy" );
    
    /**
        The class of MakerSpec resources.
    */
    public static final Resource MakerSpec = resource( "MakerSpec" );
    
    /**
        The class of FileMakerSpec resources [subclass of MakerSpec].
    */
    public static final Resource FileMakerSpec = resource( "FileMakerSpec" );
    
    /**
        The class of MemMakerSpec resources [subclass of MakerSpec].
    */
    public static final Resource MemMakerSpec = resource( "MemMakerSpec" );
    
    /**
        The class of RDBMakerSpec resources [subclass of MakerSpec].
    */
    public static final Resource RDBMakerSpec = resource( "RDBMakerSpec" );
    
    /**
        The class of ModelSpec resources.
    */
    public static final Resource ModelSpec = resource( "ModelSpec" );
        
    /**
         DefaultModelSpec, a dynamic default ModelSpec
    */
    public static final Resource DefaultModelSpec = resource( "DefaultModelSpec" );
    
    /**
        The class of PlainModelSpec resources [subclass of ModelSpec].
    */
    public static final Resource PlainModelSpec = resource( "PlainModelSpec" );
    
    /**
        The class of FileModel specifications.
    */
    public static Resource FileModelSpec = resource( "FileModelSpec" );
    
    /**
        The class of InfModelSpec resources [subclass of ModelSpec].
    */
    public static final Resource InfModelSpec = resource( "InfModelSpec" );
    
    /**
        The class of OntModelSpec resources [subclass of InfModelSpec].
    */
    public static final Resource OntModelSpec = resource( "OntModelSpec" );
    
    /**
        The resource representing reification mode Standard.
    */
    public static final Resource rsStandard = resource( "rsStandard" );
    
    /**
        The resource representing reification mode Minimal.
    */    
    public static final Resource rsMinimal = resource( "rsMinimal" );
    
    /**
        The resource representing reification mode Convenient.
    */
    public static final Resource rsConvenient = resource( "rsConvenient" );

    /**
       The JMS schema; accessed by <code>getSchema()</code>.  
    */
    static protected Model schema = null;

    /**
        Answer the JMS schema encoded into a model. This defines the subclass 
        hierarchy and the essential domains of the properties. ["Essential" 
        means "relied on by the ModelSpec engines".] The schema is not loaded 
        until its first use.
        
        TODO make this model immutable once created. 
    */
    public static Model getSchema()
        {
        if (schema == null) 
            schema = FileManager.get().loadModel( "vocabularies/jena-model-spec.n3" );
        return schema;
        }
    /**
        Utility: answer a plain literal string with the given value.
     */
    protected static Literal literal( String lex )
        { return ResourceFactory.createPlainLiteral( lex ); }
        
    /**
        Utility: answer a resource in the jms namespace with the given local name.
     */
    protected static Resource resource( String ln )
        { return ResourceFactory.createResource( baseURI + ln ); }
        
    /**
        Utility: answer a property in the jms namespave with the given local name.
     */
    protected static Property property( String ln )
        { return ResourceFactory.createProperty( baseURI + ln ); }

    /**
        Answer the Node which corresponds to the supplied reification style. [Node,
        not resource, purely because the use happens in BaseGraphMaker, ie at the
        Graph level.]
        
        @param style the reification style for which the JMS representation is required
        @return the Node version of the appropriate JMS.rs[name] vocabulary item
    */
    public static Node styleAsJMS( ReificationStyle style )
        {
        if (style == ReificationStyle.Minimal) return JMS.rsMinimal.asNode();
        if (style == ReificationStyle.Convenient) return JMS.rsConvenient.asNode();
        if (style == ReificationStyle.Standard) return JMS.rsStandard.asNode();
        return null;
        }

    /**
        Answer the Reifier.ReificationStyle value named by the argument, which should be a
        JMS.rs[something] value
        
        @param style the JMS name of the reifier style
        @return the actual Reifier.ReificationStyle value
    */
    public static ReificationStyle findStyle( RDFNode style )
        { return findStyle( style.asNode() ); }    
        
    /**
        Answer the Reifier.ReificationStyle value named by the argument, which should be a
        JMS.rs[something] Node
        
        @param style the JMS name of the reifier style
        @return the actual Reifier.ReificationStyle value
    */
    public static ReificationStyle findStyle( Node style )
        {
        if (style.equals(JMS.rsStandard.asNode() )) return ReificationStyle.Standard;    
        if (style.equals(JMS.rsMinimal.asNode() )) return ReificationStyle.Minimal;    
        if (style.equals( JMS.rsConvenient.asNode() )) return ReificationStyle.Convenient;
        return null;
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