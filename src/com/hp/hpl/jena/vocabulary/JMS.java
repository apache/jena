/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
  $Id: JMS.java,v 1.4 2003-08-19 15:13:07 chris-dollin Exp $
*/

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
    The Jena Model Specification vocabulary.
    
 	@author kers
*/
public class JMS
    {
    public static final String baseURI = "jms:";
    
    public static final Resource current = resource( "this" );
    
    public static final Property ontLanguage = property( "ontLanguage" );
    public static final Property docManager = property( "docManager" );
    public static final Property importMaker = property( "importMaker" );
    public static final Property reasonsWith = property( "reasonsWith" );
    
    public static final Property reificationMode = property( "reificationMode" );
    public static final Property reasoner = property( "reasoner" );
    public static final Property fileBase = property( "fileBase" );
    
    public static final Resource TypeMemMaker = resource( "type/MemMaker");
    
    public static final Resource MakerClass = resource( "class/ModelMaker" );
    public static final Resource FileMakerClass = resource( "class/FileModelMaker" );
    public static final Resource MemMakerClass = resource( "class/MemModelMaker" );
    public static final Resource RDBMakerClass = resource( "class/RDBModelMaker" );
    
    public static final Literal rsStandard = literal( "Standard" );
    public static final Literal rsMinimal = literal( "Minimal" );
    public static final Literal rsConvenient = literal( "Convenient" );
    
    public static Literal literal( String lex )
        { return ResourceFactory.createPlainLiteral( lex ); }
        
    public static Resource resource( String ln )
        { return ResourceFactory.createResource( baseURI + ln ); }
        
    public static Property property( String ln )
        { return ResourceFactory.createProperty( baseURI + ln ); }
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