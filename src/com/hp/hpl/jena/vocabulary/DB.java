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
 * @version $Revision: 1.1 $
 */
public class DB {

    protected static final String uri = "http://jena.hpl.hp.com/2003/04/DB#";

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
		   static String nGraphType = "GraphType";
	public static Property graphType;
		   static String nGraphLSet = "GraphLSet";
	public static Property graphLSet;
    
	// LSet properties
		   static String nLSetType = "LSetType";
	public static Property lSetType;
		   static String nLSetPSet = "LSetPSet";
	public static Property lSetPSet;
    
	// PSet properties
		   static String nPSetType = "PSetType";
	public static Property pSetType;    
    
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
            
            graphType = ResourceFactory.createProperty(uri + nGraphType);
            graphLSet = ResourceFactory.createProperty(uri + nGraphLSet);
            
			lSetType = ResourceFactory.createProperty(uri + nLSetType);
			lSetPSet = ResourceFactory.createProperty(uri + nLSetPSet);
            
			pSetType = ResourceFactory.createProperty(uri + nPSetType);

            undefined = ResourceFactory.createResource(uri + nundefined) ;
        } catch (Exception e) {
            ErrorHelper.logInternalError("RDF", 1, e);
        }
    }
}
