/* Vocabulary Class for DB.
 *
 */

package com.hp.hpl.jena.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.impl.ErrorHelper;

/**
 * Vocabulary for Database properties.
 * 
 * @author csayers
 * @version $Revision: 1.4 $
 */
public class DB {

    public static final String uri = "http://jena.hpl.hp.com/2003/04/DB#";

    /** returns the URI for this schema
     * @return the URI for this schema
     */
    public static String getURI() {
          return uri;
    }

	// Database properties
	        static String nSystemGraphName = "SystemGraph";
    public static Resource systemGraphName;
           static String nEngineType = "EngineType";
    public static Property engineType;
           static String nDriverVersion = "DriverVersion";
    public static Property driverVersion;
           static String nFormatDate = "FormatDate";
    public static Property formatDate;
           static String nGraph = "Graph";
    public static Property graph;
           static String nMaxLiteral = "MaxLiteral";
    public static Property maxLiteral;


    
	// Graph properties
	       static String nGraphName = "GraphName";
	public static Property graphName;
	       static String nGraphType = "GraphType";
	public static Property graphType;
		   static String nGraphLSet = "GraphLSet";
	public static Property graphLSet;
		   static String nGraphPrefix = "GraphPrefix";
	public static Property graphPrefix;
	   		static String nGraphId = "GraphId";
	public static Property graphId;
			static String nGraphDBSchema = "GraphDBSchema";
	public static Property graphDBSchema;
			static String nStmtTable = "StmtTable";
	public static Property stmtTable;
			static String nReifTable = "ReifTable";
	public static Property reifTable;

	

    
    // Prefix properties
		   static String nPrefixValue = "PrefixValue";
 	public static Property prefixValue;
   		   static String nPrefixURI = "PrefixURI";
	public static Property prefixURI;
    
	// LSet properties
	       static String nLSetName = "LSetName";
	public static Property lSetName;
	       static String nLSetType = "LSetType";
	public static Property lSetType;
		   static String nLSetPSet = "LSetPSet";
	public static Property lSetPSet;
    
	// PSet properties
	   static String nPSetName = "PSetName";
	public static Property pSetName;    
	   static String nPSetType = "PSetType";
	public static Property pSetType;
		static String nPSetTable = "PSetTable";
 	public static Property pSetTable;    
  
    
    // Added - schemagen does not generate this
           static String nundefined = "undefined" ;
    public static Resource undefined ; 
    

    static {
        try {
            systemGraphName = ResourceFactory.createResource(uri + nSystemGraphName);
            engineType = ResourceFactory.createProperty(uri + nEngineType);
            driverVersion = ResourceFactory.createProperty(uri + nDriverVersion);
            formatDate = ResourceFactory.createProperty(uri + nFormatDate);
            graph = ResourceFactory.createProperty(uri + nGraph);
            maxLiteral = ResourceFactory.createProperty(uri + nMaxLiteral);
            
			graphName = ResourceFactory.createProperty(uri + nGraphName);
			graphType = ResourceFactory.createProperty(uri + nGraphType);
			graphLSet = ResourceFactory.createProperty(uri + nGraphLSet);
			graphPrefix = ResourceFactory.createProperty(uri + nGraphPrefix);
			graphId = ResourceFactory.createProperty(uri + nGraphId);
			graphDBSchema = ResourceFactory.createProperty(uri + nGraphDBSchema);
			stmtTable = ResourceFactory.createProperty(uri + nStmtTable);
			reifTable = ResourceFactory.createProperty(uri + nReifTable);

            
			prefixValue = ResourceFactory.createProperty(uri + nPrefixValue);
			prefixURI = ResourceFactory.createProperty(uri + nPrefixURI);
            
			lSetName = ResourceFactory.createProperty(uri + nLSetName);
			lSetType = ResourceFactory.createProperty(uri + nLSetType);
			lSetPSet = ResourceFactory.createProperty(uri + nLSetPSet);
            
			pSetName = ResourceFactory.createProperty(uri + nPSetName);
			pSetType = ResourceFactory.createProperty(uri + nPSetType);
			pSetTable = ResourceFactory.createProperty(uri + nPSetTable);

            undefined = ResourceFactory.createResource(uri + nundefined) ;
        } catch (Exception e) {
            ErrorHelper.logInternalError("RDF", 1, e);
        }
    }
}
