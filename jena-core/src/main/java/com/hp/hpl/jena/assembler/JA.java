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

package com.hp.hpl.jena.assembler;

import com.hp.hpl.jena.rdf.model.*;

public class JA
    {
    public static final String uri = "http://jena.hpl.hp.com/2005/11/Assembler#";
    
    public static String getURI()
        { return uri; }

    protected static Model schema;
    
    protected static Resource resource( String localName )
        { return ResourceFactory.createResource( uri + localName ); }
    
    public static Property property( String localName )
        { return ResourceFactory.createProperty( uri + localName ); }
    
    public static final Resource MemoryModel = resource( "MemoryModel" );
    
    public static final Resource DefaultModel = resource( "DefaultModel" );

    public static final Resource InfModel = resource( "InfModel" );

    public static final Resource Object = resource( "Object" );

    public static final Property reasoner = property( "reasoner" );

    public static final Property reasonerURL = property( "reasonerURL" );
    
    public static final Property baseModel = property( "baseModel" );

    public static final Property literalContent = property( "literalContent" );
    
    public static final Property rules = property( "rules" );

    public static final Resource Model = resource( "Model" );

    public static final Resource OntModel = resource( "OntModel" );

    public static final Resource NamedModel = resource( "NamedModel" );

    public static final Resource FileModel = resource( "FileModel" );

    public static final Resource PrefixMapping = resource( "PrefixMapping" );

    public static final Resource ReasonerFactory = resource( "ReasonerFactory" );

    public static final Resource HasFileManager = resource( "HasFileManager" );

    public static final Resource Content = resource( "Content" );

    public static final Resource LiteralContent = resource( "LiteralContent" );

    public static final Resource OntModelSpec = resource( "OntModelSpec" );

    public static final Resource ModelSource = resource( "ModelSource" );

    public static final Property content = property( "content" );

    public static final Resource ExternalContent = resource( "ExternalContent" );

    public static final Property externalContent = property( "externalContent" );

    public static final Property modelName = property( "modelName" );

    public static final Property ontModelSpec = property( "ontModelSpec" );

    public static final Resource This = resource( "this" );
    
    public static final Resource True = resource( "true" );
    
    public static final Resource False = resource( "false" );

    public static final Resource Expanded = resource( "Expanded" );

    public static final Property prefix = property( "prefix" );

    public static final Property namespace = property( "namespace" );

    public static final Property includes = property( "includes" );

    public static final Property directory = property( "directory" );

    public static final Property create = property( "create" );

    public static final Property strict = property( "strict" );

    public static final Property mapName = property( "mapName" );

    public static final Property documentManager = property( "documentManager" );

    public static final Property ontLanguage = property( "ontLanguage" );

    public static final Property importSource = property( "importSource" );

    public static final Property quotedContent = property( "quotedContent" );

    public static final Property contentEncoding = property( "contentEncoding" );

    public static final Property initialContent = property( "initialContent" );

    public static final Resource RuleSet = resource( "RuleSet" );

    public static final Property rule = property( "rule" );

    public static final Resource HasRules = resource( "HasRules" );

    public static final Property rulesFrom = property( "rulesFrom" );

    public static final Resource ContentItem = resource( "ContentItem" );

    public static final Resource LocationMapper = resource( "LocationMapper" );

    public static final Property locationMapper = property( "locationMapper" );

    public static final Resource FileManager = resource( "FileManager" );

    public static final Resource DocumentManager = resource( "DocumentManager" );

    public static final Property fileManager = property( "fileManager" );

    public static final Property policyPath = property( "policyPath" );

    public static final Resource UnionModel = resource( "UnionModel" );

    public static final Property subModel = property( "subModel" );

    public static final Property rootModel = property( "rootModel" );

    @Deprecated
    public static final Property reificationMode = property( "reificationMode" );

    public static final Resource minimal = resource( "minimal" );

    public static final Resource convenient = resource( "convenient" );

    public static final Resource standard = resource( "standard" );

    @Deprecated
    public static final Resource ReificationMode = resource( "ReificationMode" );

    public static final Property fileEncoding = property( "fileEncoding" );

    public static final Property assembler = property( "assembler" );
    
    public static final Property loadClass = property( "loadClass" );
    
    public static final Property imports = property( "imports" );

    public static final Property reasonerFactory = property( "reasonerFactory" );

    public static final Property reasonerClass = property( "reasonerClass" );
    
    public static final Property ja_schema = property( "schema" );

    public static final Property likeBuiltinSpec = property( "likeBuiltinSpec" );

    public static final Resource SinglePrefixMapping = resource( "SinglePrefixMapping");
    
    public static final Property prefixMapping = property( "prefixMapping" );

    public static Model getSchema()
        { // inline packagename to avoid clash with /our/ FileManager.
        if (schema == null) schema = complete( com.hp.hpl.jena.util.FileManager.get().loadModel( getSchemaPath() ) );
        return schema;
        }

    private static Model complete( Model m )
        {
        Model result = ModelFactory.createDefaultModel();
        result.add( ModelFactory.createRDFSModel( m ) );
        return result;
        }
    
    private static String getSchemaPath()
        { return "org/apache/jena/vocabulary/assembler.ttl"; }
    }
