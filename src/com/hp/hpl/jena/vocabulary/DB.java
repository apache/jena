/*
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP  
 * [see end of file]
 */

/* Vocabulary Class for DB.
 *
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary for Database properties.
 *
 * @author csayers
 * @version $Revision: 1.12 $
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

/*
 * (c) Copyright 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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

