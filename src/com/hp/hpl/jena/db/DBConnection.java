/*
 *  (c) Copyright 2003  Hewlett-Packard Development Company, LP
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
* Encapsulate the specification of a jdbc connection.
* This is mostly used to simplify the calling pattern for ModelRDB factory methods.
*
* @author csayers (based in part on the jena 1 implementation by der).
* @version $Revision: 1.9 $
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
        

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getConnection()
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

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#close()
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

    /* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#cleanDB()
	 */
	public void cleanDB() throws SQLException {
		if (m_driver == null)
			m_driver = getDriver();
    	m_driver.cleanDB();
    }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#isFormatOK()
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

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#setDatabaseProperties(com.hp.hpl.jena.rdf.model.Model)
	 */
	public void setDatabaseProperties(Model dbProperties) throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		m_driver.setDatabaseProperties( dbProperties.getGraph());
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getDatabaseProperties()
	 */
	public Model getDatabaseProperties() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		ModelMem resultModel = new ModelMem();
		copySpecializedGraphToModel( m_driver.getSystemSpecializedGraph(),
			                         resultModel,
			                         Triple.createMatch( null, null, null ));
		return resultModel;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getDefaultModelProperties()
	 */
	public Model getDefaultModelProperties() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		DBPropGraph defaultProps = m_driver.getDefaultModelProperties();
		ModelMem resultModel = new ModelMem();
		copySpecializedGraphToModel( m_driver.getSystemSpecializedGraph(),
			                         resultModel,
			                         Triple.createMatch(defaultProps.getNode(), null, null));
		return resultModel;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getAllModelNames()
	 */
	public ExtendedIterator getAllModelNames() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		DBPropDatabase dbprops = new DBPropDatabase( m_driver.getSystemSpecializedGraph());
		return dbprops.getAllGraphNames();		
	}
	
	 /* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#containsModel(java.lang.String)
	 */
	public boolean containsModel(String name) throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		return (DBPropGraph.findPropGraphByName(m_driver.getSystemSpecializedGraph(), name ) != null );		
	 }

	 /* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#containsDefaultModel()
	 */
	public boolean containsDefaultModel() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		return (DBPropGraph.findPropGraphByName(m_driver.getSystemSpecializedGraph(), GraphRDB.DEFAULT ) != null );		
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
		
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#setDatabaseType(java.lang.String)
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
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getDatabaseType()
	 */
	public String getDatabaseType() { return m_databaseType; }
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getDriver()
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
            // e.printStackTrace( System.err );
			throw new RDFRDBException("Failure to instantiate DB Driver:"+ m_databaseType+ " "+ e.toString());
		}

		return m_driver;
	}

    /* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#setDriver(com.hp.hpl.jena.db.impl.IRDBDriver)
	 */
	public void setDriver(IRDBDriver driver) {
    	m_driver = driver;
    }

	/**
	 * Helper function to locate and instantiate the driver class corresponding
	 * to a given layout and database name
	 * Throws an RDFRDBexception if the driver can't be instantiated
	 * @deprecated As of Jena 2.0 this call should not be used.  Instead specify the database type
	 * when constructing a DBConnection and then pass that connection to the GraphRDB.  There is
	 * no longer any need for applications to interact with the IRDBDriver.  To customize the
	 * database configuration/layout use the formatDB(propertyModel) call.
	 */
	public IRDBDriver getDriver(String layout, String database) throws RDFRDBException {
    	// the layout is not supported in Jena2 - ignore this parameter
    	setDatabaseType(database);
    	return getDriver();
    }
    
}

/*
 *  (c) Copyright 2000, 2001 Hewlett-Packard Development Company, LP
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