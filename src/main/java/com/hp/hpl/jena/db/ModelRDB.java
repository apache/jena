/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
*/

package com.hp.hpl.jena.db;


import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.graph.*;

/** A persistent relational database implemention of the RDF API.
 *
 * <p>This implementation of the RDF API stores all its data in a relational database.</p>
 * <p> To construct a persistent RDB Model, first load the jdbc connector for 
 * your database - in this example, mysql:</p>
 * 
 * <code> 
 *    Class.forName("com.mysql.jdbc.Driver");
 * </code>
 * <p> Then create a connection to the database: </p>
 * 
 * <code> 
 *    IDBConnection conn = new DBConnection("jdbc:mysql://localhost/test", "test", "", "MySQL");
 * </code>
 * 
 * <p> Now, using that connection, you can construct Models in the database:</p>
 * <code>
 *	  Model m = ModelRDB.createModel(conn);
 * </code>
 * 
 * @author csayers (based on ModelMem written by bwm and the Jena 1 version of Model RDB by der.)
 * @version $Revision: 1.1 $
 */

public class ModelRDB extends ModelCom implements Model {    
    
    protected GraphRDB m_graphRDB = null;

    /** 
     * A model which is stored persistently in a Relational DataBase
     * 
     * Most applications should not call the constructor - use
     * ModelRDB.createModel (to create a new model) or
     * ModelRDB.open (to open an exising model).
	 * 
     * @param p the GraphPersonality of the resulting Model
     * @param graph a GraphRDB to be exposed through the model interface
     * 
     * @since Jena 2.0
     */
    public ModelRDB( Personality<RDFNode> p, GraphRDB graph) throws RDFRDBException {
    	super( graph, p);
    	m_graphRDB = graph;
    }
    
    public ModelRDB( GraphRDB graph ) {
        super( graph ); 
        m_graphRDB = graph; 
    }

    /**
     * Open the default model from an existing rdf database. The layout and 
     * datatype type information will be dynamically loaded from the database. 
     * 
     * @param dbcon an IDBConnection specifying the database connection
     */
    public static ModelRDB open(IDBConnection dbcon) throws RDFRDBException {
        return open(dbcon, null);
    }

    /**
     * Open an existing rdf database. The layout and datatype type information
     * will be dynamically loaded from the database.
     * Will throw an RDFDBException if the database does not seem to formated.
     * @param dbcon a IDBConnection specifying the database connection
     * @param name the name of the RDF model to open
     */
    public static ModelRDB open(IDBConnection dbcon, String name) throws RDFRDBException {
        GraphRDB graph = new GraphRDB(dbcon, name, null, GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS ,false);
		return new ModelRDB(BuiltinPersonalities.model, graph);
    }

    /**
     * Create a new default model on an existing database.
     * Will format the database if it has not already been formatted.
     * @param dbcon a DBConnection specifying the database connection
     * @since Jena 2.0
     */
    public static ModelRDB createModel(IDBConnection dbcon) throws RDFRDBException {
        return createModel(dbcon, null, getDefaultModelProperties(dbcon));
    }

    /**
     * Create a new model on an existing database.
     * Will format the database if it has not already been formatted.
     *
     * <p>
	 * Use the properties to optionally customize the model - this
	 * won't change the results you see when using the model interface,
	 * but it may alter the speed with which you get them or the space
	 * required by the underlying database.</p>
	 *
	 * <p>
	 * The properties must form a complete and consistent set.
	 * The easist way to get a complete and consistent set is to call
	 * getDefaultModelProperties, modify it, and then use that as an argument
	 * in the call.</p>
	 * 
     * @param dbcon a DBConnection specifying the database connection
     * @param modelProperties a Model containing customization properties
     * @since Jena 2.0
     */
    public static ModelRDB createModel(IDBConnection dbcon, Model modelProperties) throws RDFRDBException {
        return createModel(dbcon, null, modelProperties);
    }

    /**
     * Create a new model on an existing database.
     * Will format the database if it has not already been formatted.
     * @param dbcon a DBConnectionI specifying the database connection
     * @param name the name to give the newly created model.
     * The name "DEFAULT" is reserved and may not be used for user models.
     */
    public static ModelRDB createModel(IDBConnection dbcon, String name) throws RDFRDBException {
        return createModel(dbcon, name, getDefaultModelProperties(dbcon));
    }

    /**
     * Create a new model on an existing database.
     * Will format the database if it has not already been formatted.
     *
     * <p>
	 * Use the properties to optionally customize the model - this
	 * won't change the results you see when using the model interface,
	 * but it may alter the speed with which you get them or the space
	 * required by the underlying database.</p>
	 *
	 * <p>
	 * The properties must form a complete and consistent set.
	 * The easist way to get a complete and consistent set is to call
	 * getDefaultModelProperties, modify it, and then use that as an argument
	 * in the call.</p>
	 * 
     * @param dbcon a DBConnection specifying the database connection
     * @param name the name to give the newly created model.
     * The name "DEFAULT" is reserved and may not be used for user models.
     * @param modelProperties a Model containing customization properties
     * @since Jena 2.0
     */
    public static ModelRDB createModel(IDBConnection dbcon, String name, Model modelProperties) throws RDFRDBException {
    	
        GraphRDB graph;
    	if( modelProperties != null )
    		graph = new GraphRDB(dbcon, name, modelProperties.getGraph(), GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS ,true);
    	else
        	graph = new GraphRDB(dbcon, name, null, GraphRDB.OPTIMIZE_AND_HIDE_ONLY_FULL_REIFICATIONS, true);
        return new ModelRDB(BuiltinPersonalities.model, graph);
    }

	/** 
	 * Returns a Jena Model containing model-specific properties.
	 * These describe the optimization/layout for this model in the database.
	 * 
	 * The returned Model is a copy, modifying it will have no
	 * immediate effect on the database.
	 * 
	 * 
	 * @since Jena 2.0
	 */
	
	public Model getModelProperties() {
		Model m = ModelFactory.createDefaultModel();
        Graph g = m.getGraph();
		ExtendedIterator<Triple> it = m_graphRDB.getPropertyTriples();
		while (it.hasNext()) g.add( it.next());
		return m;
	}
	
	/**
	 * Retrieve a default set of model customization properties
	 * 
	 * The returned default set of properties is suitable for use in a call to
	 * ModelRDB.create(..., modelProperties);
	 * 
     * @param dbcon a DBConnectionI specifying the database connection
     * @return Model containing default properties
     */
	
	public static Model getDefaultModelProperties( IDBConnection dbcon ) {
		return dbcon.getDefaultModelProperties();
	}
	
    /**
     * List the names of all models stored in the database
     * @return ExtendedIterator over the model names.
     */
     
     public static ExtendedIterator<String> listModels(IDBConnection dbcon) throws RDFRDBException {
        return dbcon.getAllModelNames();
     }

    /** Close the Model and free up resources held.
     *
     *  <p>Not all implementations of Model require this method to be called.  But
     *     some do, so in general its best to call it when done with the object,
     *     rather than leave it to the finalizer.</p>
     */
    @Override
    public void close() {
        m_graphRDB.close();
    }
    
    /**
     * Remove all traces of this particular Model from the database.
     */
    public void remove() throws RDFRDBException {
    	m_graphRDB.remove();
    }
     
	/**
	 * A convenience function to return the connection
	 */
	public IDBConnection getConnection() {
		return m_graphRDB.getConnection();
	}
	
	/**
	* Get the value of DoDuplicateCheck
	* @return bool boolean
	*/
	public boolean getDoDuplicateCheck() {
		return m_graphRDB.getDoDuplicateCheck();
	}
	/**
	* Set the value of DoDuplicateCheck.
	* @param bool boolean
	*/
	public void setDoDuplicateCheck(boolean bool) {
		m_graphRDB.setDoDuplicateCheck(bool);
	}
	
	/**
	 * Set the value of DoFastpath.
	 * @param val boolean
	 */
	public void setDoFastpath ( boolean val ) {
		m_graphRDB.setDoFastpath(val);
	}
	
	/**
	 * Get the value of DoFastpath.
	 * @return boolean
	 */
	public boolean getDoFastpath () {
		return m_graphRDB.getDoFastpath();
	}

	/**
	 * Set the value of QueryOnlyAsserted.
	 * @param opt boolean
	 */
	public void setQueryOnlyAsserted ( boolean opt ) {
		m_graphRDB.setQueryOnlyAsserted(opt);
	}

	/**
	 * Get the value of QueryOnlyAsserted.
	 * @return boolean
	 */
	public boolean getQueryOnlyAsserted() {
		return m_graphRDB.getQueryOnlyAsserted();
	}

	/**
	 * Set the value of QueryOnlyReified.
	 * @param opt boolean
	 */
	public void setQueryOnlyReified ( boolean opt ) {
		m_graphRDB.setQueryOnlyReified(opt);
	}

	/**
	 * Get the value of QueryOnlyReified.
	 * @return boolean
	 */
	public boolean getQueryOnlyReified() {
		return m_graphRDB.getQueryOnlyReified();
	}

	/**
	 * Set the value of QueryFullReified.
	 * @param opt boolean
	 */
	public void setQueryFullReified ( boolean opt ) {
		m_graphRDB.setQueryFullReified(opt);
	}

	/**
	 * Get the value of QueryFullReified.
	 * @return boolean
	 */
	public boolean getQueryFullReified() {
		return m_graphRDB.getQueryFullReified();
	}
	
	/**
	 * Set the value of DoImplicitJoin.
	 * @param val boolean
	 */
	public void setDoImplicitJoin ( boolean val ) {
		m_graphRDB.setDoImplicitJoin(val);
	}

	
}

/*
    (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
