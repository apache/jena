/*
 *  (c) Copyright Hewlett-Packard Company 2003 
 *  All rights reserved.
 *
 *
 */

package com.hp.hpl.jena.db;

import java.sql.*;

import com.hp.hpl.jena.db.impl.*;
import com.hp.hpl.jena.db.impl.DBPropDatabase;
import com.hp.hpl.jena.db.impl.DBPropGraph;
import com.hp.hpl.jena.db.impl.SpecializedGraph;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.mem.ModelMem;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
* Encapsulate the specification of a jdbc connection, mostly used to
* simplify the calling pattern for ModelRDB factory methods.
* *
* @author csayers (based in part on the jena 1 implementation by der).
* @version $Revision: 1.1 $
*/

public class DBConnection implements IDBConnection { 

    /** The jdbc connection being wrapped up */
    protected Connection m_connection;

    /** The url for the connection, may be null if the connection was passed in pre-opened */
    protected String m_url;

    /** The user name for the connection, may be null if the connection was passed in pre-opened */
    protected String m_user;

    /** The password for the connection, may be null if the connection was passed in pre-opened */
    protected String m_password;

	/** The database type: "Oracle", "mySQL, etc...
	 *  This is new in Jena2 - for compatability with older code we allow this to
	 * be left unspecified at the loss of some jena2 functionality.
	 */
	protected String m_databaseType = null;
	
	/** Driver to connect to this database */
	protected IRDBDriver m_driver = null;
	    
	
    /**
     * Create a connection specification based on jdbc address and
     * appropriate authentication information.
     * @param url the jdbc url for the database, note that the format of this
     * is database dependent and that the appropriate jdbc driver will need to
     * be specified via the standard pattern
     * <pre>
     *     Class.forName("my.sql.driver");
     * </pre>
     * @param user the user name to log on with
     * @param password the password corresponding to this user
     * @deprecated As of Jena 2.0, it is recommended to use one of the DBConnection
     * constructors which takes a database type as an argument.  (The DBConnection can
     * operate more efficiently if it knows the database type).
     */
    public DBConnection(String url, String user, String password) {
        this( url, user, password, null);
    }

    /**
     * Create a connection specification based on jdbc address and
     * appropriate authentication information.
     * @param url the jdbc url for the database, note that the format of this
     * is database dependent and that the appropriate jdbc driver will need to
     * be specified via the standard pattern
     * <pre>
     *     Class.forName("my.sql.driver");
     * </pre>
     * @param user the user name to log on with
     * @param password the password corresponding to this user
     * @param databaseType the type of database to which we are connecting.
     * 
	 * @since Jena 2.0
     */
    public DBConnection(String url, String user, String password, String databaseType) {
        m_url = url;
        m_user = user;
        m_password = password;
		setDatabaseType(databaseType);
    }

    /**
     * Create a connection specification that just wraps up an existing database
     * connection.
     * @param connection the open jdbc connection to use
     * @deprecated As of Jena 2.0, it is recommended to use one of the DBConnection
     * constructors which takes a database type as an argument.  (The DBConnection can
     * operate more efficiently if it knows the database type).
     */
    public DBConnection(Connection connection) {
		this(connection, null);
    }

    /**
     * Create a connection specification that just wraps up an existing database
     * connection.
     * @param connection the open jdbc connection to use
     * @param databaseType the type of database to which we are connecting.
	 * 
	 * @since Jena 2.0
     */
    public DBConnection(Connection connection, String databaseType) {
        m_connection = connection;
		setDatabaseType(databaseType);
    }
        

	/**
	 * Return the jdbc connection or null if we no longer have access to a connection.
	 */
	public Connection getConnection() throws SQLException {
		if (m_connection == null) {
			if (m_url != null) {
				m_connection =
					DriverManager.getConnection(m_url, m_user, m_password);
				m_connection.setAutoCommit(true);
			}
		}
		return m_connection;
	}

	/**
	 * Close the jdbc connection.
	 * It might be reopend with a getConnection() if the full database uri, user and password
	 * were provided.
	 */
	public void close() throws SQLException {
		if( m_driver != null ) {
			m_driver.close();
			m_driver = null;
		}
		if (m_connection != null) {
			m_connection.close();
			m_connection = null;
		}
	}

    /**
     * Clear all RDF information from the database.
     * This is equivalent to (but faster than) calling ModelRDB.clear() 
     * on every model in the database and then deleting the underlying tables.
     */
    public void cleanDB() throws SQLException {
		if (m_driver == null)
			m_driver = getDriver();
    	m_driver.cleanDB();
    }

	/**
	 * Return true if the database seems to be formated for RDF storage.
	 * This is <em>not</em> an integrity check this is simply a flag
	 * recording that a base level table exists.
	 * Any access errors are treated as the database not being formated.
	 */
	public boolean isFormatOK() {
		try {
			if( m_driver == null )
				m_driver = getDriver();
			return m_driver.isDBFormatOK();
		} catch (Exception e) {
			return false;
		}
	}

	/** 
	 * Set the database-specific properties.
	 * 
	 * This call is only valid before the first Model is stored in the
	 * database.  After that point, the database structure is frozen.
	 * 
	 * Use the properties to optionally customize the database - this
	 * won't change the results you see when using the graph interface,
	 * but it may alter the speed with which you get them or the space
	 * required by the database.
	 *
	 * The properties must form a complete and consistent set.
	 * The easist way to get a complete and consistent set is to call
	 * getDBProperties(), modify it, and then use that as an argument
	 * in the call to format().
	 * 
	 * Note that some implementations may choose to delay actually peforming
	 * the formatting operation until at least one Graph is constructed in
	 * the database.
	 * 
	 * Throws an exception if the database cannot be suitably formatted.
	 * A database may only be formatted once.  Attempting to reformat a
	 * database causes an exception (use isFormatOK() if you need to
	 * test).
	 * 
	 * @param dbProperties is a Jena Model describing the database parameters
	 * @since Jena 2.0
	 * 
	 */
	public void setDatabaseProperties(Model dbProperties) throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		m_driver.setDatabaseProperties( dbProperties.getGraph());
	}

	/** 
	 * Returns a Jena Model containing database-specific properties.
	 * These describe the optimization/layout for the database.
	 * 
	 * If the database has not been formatted, then a default
	 * set of properties is returned.  Otherwise the actual properties
	 * are returned.
	 * 
	 * The returned Model is a copy, modifying it will have no
	 * immediate effect on the database.
	 * 
	 * 
	 * @since Jena 2.0
	 */
	public Model getDatabaseProperties() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		ModelMem resultModel = new ModelMem();
		copySpecializedGraphToModel( m_driver.getSystemSpecializedGraph(),
			                         resultModel,
			                         new StandardTripleMatch(null, null, null));
		return resultModel;
	}
	
	/**
	 * Retrieve a default set of model customization properties.
	 * 
	 * The returned default set of properties is suitable for use in a call to
	 * ModelRDB.create(..., modelProperties);
	 * 
	 * TODO this could be more efficient!  (assuming we know the URI for the default).
     * @return Model containing default properties
     */
	
	public Model getDefaultModelProperties() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		DBPropGraph defaultProps = m_driver.getDefaultModelProperties();
		ModelMem resultModel = new ModelMem();
		copySpecializedGraphToModel( m_driver.getSystemSpecializedGraph(),
			                         resultModel,
			                         new StandardTripleMatch(defaultProps.getNode(), null, null));
		return resultModel;
	}
	
	/** Retrieve a list of all graphs in the database.
	 *
	 * @return Iterator over String names for graphs.
	 * @throws RDFDBException
	 */
	public ExtendedIterator getAllModelNames() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		DBPropDatabase dbprops = new DBPropDatabase( m_driver.getSystemSpecializedGraph());
		return dbprops.getAllGraphNames();		
	}
	
	/**
	 * Test if a given model is contained in the database.
	 * 
	 * @param name the name of a model which may be in the database
	 * @return Boolean true if the model is contained in the database
	 * @throws RDFDBException
	 * @since Jena 2.0
	 */     
	 public boolean containsModel(String name) throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		return (DBPropGraph.findPropGraph(m_driver.getSystemSpecializedGraph(), name.toUpperCase() ) != null );		
	 }

	/**
	 * Test if a default model is contained in the database.
	 * 
	 * A default model is a model for which no specific name was specified.
	 * (One that was created by calling ModelRDB.createModel without specifying
	 * a name).
	 * 
	 * @return Boolean true if the model is contained in the database
	 * @throws RDFDBException
	 * @since Jena 2.0
	 */     
	 public boolean containsDefaultModel() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		return (DBPropGraph.findPropGraph(m_driver.getSystemSpecializedGraph(), GraphRDB.DEFAULT ) != null );		
	 }

	/** 
	 * Copy the contents of a specialized graph to a new Model.
	 * 
	 * This has package scope - for internal use only.
	 * 
	 * @since Jena 2.0
	 */
	static void copySpecializedGraphToModel( SpecializedGraph fromGraph, Model toModel, TripleMatch filter) throws RDFRDBException {
		Graph toGraph = toModel.getGraph();
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		ExtendedIterator it = fromGraph.find( filter, complete);
		while(it.hasNext())
			toGraph.add((Triple)(it.next())); 
		it.close();
	}
		
	/** Set the database type manually.
	 * This is not for public use (it is preferable to
	 * specify it in the constructor) - included here to handle
	 * older code, which didn't use the new constructor.
	 *
	 * @since Jena 2.0
	 */
	public void setDatabaseType( String databaseType ) {
		if (databaseType != null) {
			if (databaseType.compareToIgnoreCase("mysql") == 0) {
					m_databaseType = "MySQL";
			} else {
				m_databaseType = databaseType;
			}
		}
					
	}
	
	/** Get the database type.
	 * @return String database type, or null if unset
	 * 
	 * @since Jena 2.0
	 */
	public String getDatabaseType() { return m_databaseType; }
	
	/** Get the database-specific driver 
	 *
	 * For this to work, it needs to know the type of database being used.
	 * That may be specified in the constructor (preferred) or done later
	 * by using the setDatabaseType method (for backward compatability).
	 */
	
	public IRDBDriver getDriver() throws RDFRDBException {
		try {
			if (m_connection == null)
				getConnection();

			if (m_driver == null) {
				// need to look for a suitable driver
				if (m_databaseType == null) {
					// without knowing the database type there's not much we can do.
					throw new RDFRDBException("Error - attempt to call DBConnection.getDriver before setting the database type");
				}
				m_driver = (IRDBDriver) (Class.forName("com.hp.hpl.jena.db.impl.Driver_" + m_databaseType).newInstance());
				m_driver.setConnection( this );
			} 
		} catch (Exception e) {
			throw new RDFRDBException("Failure to instantiate DB Driver:"+ m_databaseType+ " "+ e.toString());
		}

		return m_driver;
	}

    /**
     * Set the IRDBDriver to use for this connection.
     * Useful to enable external drivers to be registered outside of the
     * standard driver package.
     */
    public void setDriver(IRDBDriver driver) {
    	m_driver = driver;
    }

	/**
	 * Helper function to locate and instantiate the driver class corresponding
	 * to a given layout and database name
	 * Throws an RDFRDBexception if the driver can't be instantiated
	 * @deprecated As of Jena 2.0 this call should not be used.  Instead 
	 * specify the database type when constructing a DBConnection and then 
	 * pass that connection to the ModelRDB.  There is no longer any need for 
	 * applications to interact directly with the IRDBDriver.  To customize the
	 * database configuration/layout use the setDatabaseProperties method.
	 */
    public IRDBDriver getDriver(String layout, String database) throws RDFRDBException {
    	// the layout is not supported in Jena2 - ignore this parameter
    	setDatabaseType(database);
    	return getDriver();
    }
    
}

/*
 *  (c) Copyright Hewlett-Packard Company 2000, 2001
 *  All rights reserved.
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