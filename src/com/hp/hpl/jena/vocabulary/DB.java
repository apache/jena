/* Vocabulary Class for DB.
 *
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.*;

/**
 * Vocabulary for Database properties.
 *
 * @author csayers
 * @version $Revision: 1.6 $
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
    public static final Property engineType = ResourceFactory.createProperty(uri + "EngineType" );
    public static final Property driverVersion = ResourceFactory.createProperty(uri + "DriverVersion" );
	public static final Property layoutVersion = ResourceFactory.createProperty(uri + "LayoutVersion" );
    public static final Property formatDate = ResourceFactory.createProperty(uri + "FormatDate" );
    public static final Property graph = ResourceFactory.createProperty(uri + "Graph" );
    public static final Property maxLiteral = ResourceFactory.createProperty(uri + "MaxLiteral" );

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
