/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db;


import com.hp.hpl.jena.rdf.model.impl.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.db.impl.DBQueryHandler;
import com.hp.hpl.jena.enhanced.*;
import com.hp.hpl.jena.mem.*;
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
 * @version $Revision: 1.9 $
 */
public class ModelRDB extends ModelCom implements Model {    
    
    protected GraphRDB m_graphRDB = null;

    /**
     * Construct a model which is stored persistently in a Relational DataBase
     * 
     * If a model already exists in the database, then it is opened, otherwise
     * a new model with default name and formatting is inserted and opened.
     * @param dbcon a Connection specifying the database connection
     * @deprecated Since Jena 2.0, this call is not recommended - 
     * in the short-term use ModelRDB.open or ModelRDB.createModel;
     * in the longer-term use factory methods to construct persistent models.
     */
     public ModelRDB( IDBConnection dbcon) throws RDFRDBException {
		this(BuiltinPersonalities.model, new GraphRDB(dbcon, null, null, !dbcon.containsDefaultModel()));
     }


    /**
     * Construct a model which is stored persistently in a Relational DataBase
     * 
     * If a model with the specified identifier already exists in the 
     * database, then it is opened, otherwise a new model with default 
     * formatting is inserted and opened.
     * @param dbcon a Connection specifying the database connection
     * @param modelID is the identifier of an RDF model within the database.
     * The modelID "DEFAULT" is reserved and may not be used for user models.
     * @deprecated Since Jena 2.0, this call is not recommended -
     * in the short-term use ModelRDB.open or ModelRDB.createModel;
     * in the longer-term use factory methods to construct persistent models.
     */
     public ModelRDB( IDBConnection dbcon, String modelID) throws RDFRDBException {
		this(BuiltinPersonalities.model, new GraphRDB(dbcon, modelID, null, !dbcon.containsDefaultModel()));
     }


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
    public ModelRDB(GraphPersonality p, GraphRDB graph) throws RDFRDBException {
    	super( graph, p);
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
     * Create a new database suitable for storing RDF data. In fact the database has
     * to exist since jdbc can't create an empty database from a vacuum but it can be empty
     * and this call will format it with appropriate tables and stored procedures.
     * <p>
     * The appropriate RDF-RDB driver to use is assumed to be the class Driver<DatabaseType><LayoutType>.
     * If that can't be found it defaults to looking for a property file in /etc/Driver<DatabaseType><LayoutType>.config
     * and uses that to determine the driver class and parameters.</p>
     *
     * @param dbcon a DBConnection specifying the database connection
     * @param layoutType the name of the layout style to use. Currently one of:
     * "Generic", "Hash", "MMGeneric", "MMHash", "Proc", "ThinProc".
     * @param databaseType the name of the database type. Currently one of:
     * "Interbase" "Postgresql" "Mysql" "Oracle". This may seem a little redundant
     * given that the jdbc uri implicitly contains this information but there is no
     * standard way of extracting this (esp. if the user connects via a bridge).
     * @deprecated Since Jena 2.0 this call is no longer needed - it is preferable 
     * to specify the database type when constructing the DBConnection and to modify
     * the layout by using the properties in the DBConnection.  Then use the 
     * call ModelRDB.createModel(IDBConnection)
     */
    public static ModelRDB create(IDBConnection dbcon, String layoutType, String databaseType) throws RDFRDBException {
        dbcon.setDatabaseType(databaseType);
        return createModel(dbcon, null, getDefaultModelProperties(dbcon));
    }

    /**
     * Create a new database suitable for storing RDF data. In fact the database has
     * to exist since jdbc can't create an empty database from a vacuum but it can be empty
     * and this call will format it with appropriate tables and stored procedures.
     * <p>
     * Uses a default layout format which is able to support multiple models in a single database.
     * </p>
     * @param dbcon a DBConnectionI specifying the database connection
     * @param databaseType the name of the database type. Currently one of:
     * "Interbase" "Postgresql" "Mysql" "Oracle". This may seem a little redundant
     * given that the jdbc uri implicitly contains this information but there is no
     * standard way of extracting this (esp. if the user connects via a bridge).
     * @deprecated Since Jena 2.0 this call is no longer needed - it is preferable to 
     * specify the database type when constructing the DBConnection.  Then use the call 
     * ModelRDB.createModel(IDBConnection)
     */

    public static ModelRDB create(IDBConnection dbcon, String databaseType) throws RDFRDBException {
        dbcon.setDatabaseType(databaseType);
        return createModel(dbcon, null, getDefaultModelProperties(dbcon));
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
		Model m = new ModelMem();
		ExtendedIterator it = m_graphRDB.getPropertyTriples();
		while(it.hasNext())
			m.getGraph().add( (Triple)it.next());
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
     
     public static ExtendedIterator listModels(IDBConnection dbcon) throws RDFRDBException {
        return dbcon.getAllModelNames();
     }

    /** Close the Model and free up resources held.
     *
     *  <p>Not all implementations of Model require this method to be called.  But
     *     some do, so in general its best to call it when done with the object,
     *     rather than leave it to the finalizer.</p>
     */
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
	 * Remove all the statements from the database which are associated with just this model.
	 * This no longer reformats the database (which makes it safer and useful for multi-model
	 * databases) but means that it is not guaranteed to garbage collect the resource table.
     * @deprecated Since Jena 2.0 this call is not recommended (it's name
     * is misleading) - to clear an entire database use DBConnection.cleanDB, 
     * to remove just this Model use Model.remove().
     */
     public void clear() throws RDFRDBException {
     	remove();
     }

	/**
	 * Remove a named model from an existing multi-model database.
	 * Will throw an RDFDBException if the database layout does not support
	 * multiple models or if the database does not seem to formated.
	 * @param dbcon a DBConnectionI specifying the database connection
	 * @param name the name to give the newly created model
	 * @deprecated Since Jena 2.0, to remove a model use the ModelRDB.remove()
	 */
	public static void deleteModel(IDBConnection dbcon, String name) throws RDFRDBException {
		ModelRDB modelToDelete = ModelRDB.open(dbcon, name);
		modelToDelete.remove();
	}

	/**
	 * Loads all the statements for this model into an in-memory model.
	 * @return a ModelMem containing the whole of the RDB model
	 * @deprecated Since Jena 2.0, this call is not recommended.  Instead use
	 * the soon-to-be-released bulk-load functions.
	 */
	public Model loadAll()  {
		ModelMem m = new ModelMem();
		for (StmtIterator i = this.listStatements(); i.hasNext(); ) {
			m.add((Statement)i.next());
		}
		return m;
	}
	
	/**
	* Get the value of DoDuplicateCheck
	* @return bool boolean
	*/
	public boolean getDoDuplicateCheck() {
		return m_graphRDB.m_driver.getDoDuplicateCheck();
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
		((DBQueryHandler)m_graphRDB.queryHandler()).setDoFastpath(val);
	}
	
	/**
	 * Get the value of DoFastpath.
	 * @return boolean
	 */
	public boolean getDoFastpath () {
		return ((DBQueryHandler)m_graphRDB.queryHandler()).getDoFastpath();
	}

	/**
	 * Set the value of QueryOnlyAsserted.
	 * @param opt boolean
	 */
	public void setQueryOnlyAsserted ( boolean opt ) {
		((DBQueryHandler)m_graphRDB.queryHandler()).setQueryOnlyAsserted(opt);
	}

	/**
	 * Get the value of QueryOnlyAsserted.
	 * @return boolean
	 */
	public boolean getQueryOnlyAsserted() {
		return ((DBQueryHandler)m_graphRDB.queryHandler()).getQueryOnlyAsserted();
	}

	/**
	 * Set the value of QueryOnlyReified.
	 * @param opt boolean
	 */
	public void setQueryOnlyReified ( boolean opt ) {
		((DBQueryHandler)m_graphRDB.queryHandler()).setQueryOnlyReified(opt);
	}

	/**
	 * Get the value of QueryOnlyReified.
	 * @return boolean
	 */
	public boolean getQueryOnlyReified() {
		return ((DBQueryHandler)m_graphRDB.queryHandler()).getQueryOnlyReified();
	}

	/**
	 * Set the value of QueryFullReified.
	 * @param opt boolean
	 */
	public void setQueryFullReified ( boolean opt ) {
		((DBQueryHandler)m_graphRDB.queryHandler()).setQueryFullReified(opt);
	}

	/**
	 * Get the value of QueryFullReified.
	 * @return boolean
	 */
	public boolean getQueryFullReified() {
		return ((DBQueryHandler)m_graphRDB.queryHandler()).getQueryFullReified();
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
