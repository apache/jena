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

package com.hp.hpl.jena.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.hp.hpl.jena.db.impl.DBPropDatabase;
import com.hp.hpl.jena.db.impl.DBPropGraph;
import com.hp.hpl.jena.db.impl.DBType;
import com.hp.hpl.jena.db.impl.IRDBDriver;
import com.hp.hpl.jena.db.impl.SpecializedGraph;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NullIterator;


/**
* Encapsulate the specification of a jdbc connection.
* This is mostly used to simplify the calling pattern for ModelRDB factory methods.
*
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
	protected DBType m_databaseType = null;
	
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
	@Override
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
	@Override
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
	@Override
    public void cleanDB() throws SQLException {
		if (m_driver == null)
			m_driver = getDriver();
    	m_driver.cleanDB();
    }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#isFormatOK()
	 */
	@Override
    public boolean isFormatOK() {
// Removed exception trap, an exception might be a connection
// failure on a well formated database - der 24/7/04        
//		try {
			if( m_driver == null )
				m_driver = getDriver();
			return m_driver.isDBFormatOK();
//		} catch (Exception e) {
//			return false;
//		}
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#setDatabaseProperties(com.hp.hpl.jena.rdf.model.Model)
	 */
	@Override
    public void setDatabaseProperties(Model dbProperties) throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		m_driver.setDatabaseProperties( dbProperties.getGraph());
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getDatabaseProperties()
	 */
	@Override
    public Model getDatabaseProperties() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		Model resultModel = ModelFactory.createDefaultModel();
		copySpecializedGraphToModel( m_driver.getSystemSpecializedGraph(true),
			                         resultModel, Triple.ANY );
		return resultModel;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getDefaultModelProperties()
	 */
	@Override
    public Model getDefaultModelProperties() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		DBPropGraph defaultProps = m_driver.getDefaultModelProperties();
		Model resultModel = ModelFactory.createDefaultModel();
		copySpecializedGraphToModel( m_driver.getSystemSpecializedGraph(true),
			                         resultModel,
			                         Triple.createMatch(defaultProps.getNode(), null, null));
		return resultModel;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getAllModelNames()
	 */
	@Override
    public ExtendedIterator<String> getAllModelNames() throws RDFRDBException {
		if (m_driver == null)
			m_driver = getDriver();
		SpecializedGraph sg = m_driver.getSystemSpecializedGraph(false);
		ExtendedIterator<String> it;
		if ( sg == null )
			it = NullIterator.instance() ;
		else {
			DBPropDatabase dbprops = new DBPropDatabase(sg);
			it = dbprops.getAllGraphNames();
		}
		return it;
	}
	
	 /* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#containsModel(java.lang.String)
	 */
	@Override
    public boolean containsModel(String name) throws RDFRDBException {
		boolean res = false;
		if (m_driver == null)
			m_driver = getDriver();
		SpecializedGraph sg = m_driver.getSystemSpecializedGraph(false);
		if ( sg != null ) {
			DBPropGraph g = DBPropGraph.findPropGraphByName(sg,name);
			res = g == null ? false : g.isDBPropGraphOk(name);
		}
		return res;		
	 }

	 /* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#containsDefaultModel()
	 */
	@Override
    public boolean containsDefaultModel() throws RDFRDBException {
		return containsModel(GraphRDB.DEFAULT);
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
		ExtendedIterator<Triple> it = fromGraph.find( filter, complete);
		while(it.hasNext())
			toGraph.add(it.next()); 
		it.close();
	}
		
	/**
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedDatabaseException thrown if {@code databaseType} isn't a supported type 
	 */
	@Override
    public void setDatabaseType( String databaseType ) 
	{
	    if ( databaseType == null )
	    {
	        m_databaseType = null;
	        return ;
	    }
	    DBType type = DBType.fromName(databaseType);
	    if (type == null){
	    	throw UnsupportedDatabaseException.create(databaseType);
	    }
	    this.m_databaseType = type;
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getDatabaseType()
	 */
	@Override
    public String getDatabaseType() {
		return m_databaseType == null? null: m_databaseType.getDisplayName(); 
	}
	
	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#getDriver()
	 */
	@Override
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
				m_driver = (IRDBDriver) (Class.forName(m_databaseType.getDriverClassName()).newInstance());
				m_driver.setConnection( this );
			} 
		} catch (Exception e) {
            // e.printStackTrace( System.err );
			throw new RDFRDBException("Failure to instantiate DB Driver:"+ m_databaseType+ " "+ e.toString(), e);
		}

		return m_driver;
	}

    /* (non-Javadoc)
	 * @see com.hp.hpl.jena.db.IDBConnection#setDriver(com.hp.hpl.jena.db.impl.IRDBDriver)
	 */
	@Override
    public void setDriver(IRDBDriver driver) {
    	m_driver = driver;
    }
}
