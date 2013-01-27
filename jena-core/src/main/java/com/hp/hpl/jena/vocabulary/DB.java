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

/* Vocabulary Class for DB.
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary for Database properties.
 */
public class DB {

    public static final String uri = "http://jena.hpl.hp.com/2003/04/DB#";

    /** returns the URI for this schema
     * @return the URI for this schema
     */
    public static String getURI() {
          return uri;
    }

    public static final Resource systemGraphName = ResourceFactory.createResource(uri + "SystemGraph" );
    public static final Resource layoutVersion = ResourceFactory.createResource( uri + "LayoutVersion" );
    public static final Property engineType = ResourceFactory.createProperty(uri + "EngineType" );
    public static final Property driverVersion = ResourceFactory.createProperty(uri + "DriverVersion" );
    public static final Property formatDate = ResourceFactory.createProperty(uri + "FormatDate" );
    public static final Property graph = ResourceFactory.createProperty(uri + "Graph" );
    
	public static final Property longObjectLength = ResourceFactory.createProperty(uri + "LongObjectLength" );
	public static final Property indexKeyLength = ResourceFactory.createProperty(uri + "IndexKeyLength" );
	public static final Property isTransactionDb = ResourceFactory.createProperty(uri + "IsTransactionDb" );
	public static final Property doCompressURI = ResourceFactory.createProperty(uri + "DoCompressURI" );
	public static final Property compressURILength = ResourceFactory.createProperty(uri + "CompressURILength" );
	public static final Property tableNamePrefix = ResourceFactory.createProperty(uri + "TableNamePrefix" );

    public static final Property graphName = ResourceFactory.createProperty(uri + "GraphName" );
    public static final Property graphType = ResourceFactory.createProperty(uri + "GraphType" );
    public static final Property graphLSet = ResourceFactory.createProperty(uri + "GraphLSet" );
    public static final Property graphPrefix = ResourceFactory.createProperty(uri + "GraphPrefix" );
    public static final Property graphId = ResourceFactory.createProperty(uri + "GraphId" );
    public static final Property graphDBSchema = ResourceFactory.createProperty(uri + "GraphDBSchema" );
    public static final Property stmtTable = ResourceFactory.createProperty(uri + "StmtTable" );
    public static final Property reifTable = ResourceFactory.createProperty(uri + "ReifTable" );


    public static final Property prefixValue = ResourceFactory.createProperty(uri + "PrefixValue" );
    public static final Property prefixURI = ResourceFactory.createProperty(uri + "PrefixURI" );

    public static final Property lSetName = ResourceFactory.createProperty(uri + "LSetName" );
    public static final Property lSetType = ResourceFactory.createProperty(uri + "LSetType" );
    public static final Property lSetPSet = ResourceFactory.createProperty(uri + "LSetPSet" );

    public static final Property pSetName = ResourceFactory.createProperty(uri + "PSetName" );
    public static final Property pSetType = ResourceFactory.createProperty(uri + "PSetType" );
    public static final Property pSetTable = ResourceFactory.createProperty(uri + "PSetTable" );

    public static final Resource undefined = ResourceFactory.createResource(uri + "undefined" ) ;
}
