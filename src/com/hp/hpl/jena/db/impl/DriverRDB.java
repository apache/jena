/*
  (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.util.Log;

//=======================================================================
/**
* Base database driver for implementing SpecializedGraphs.
* Different drivers are needed for different databases and different
* layout schemes.
* <p>
* This driver is a base implemention from which database-specific
* drivers can inherit. It is not generic in the sense that it will work
* on any minimal SQL store and so should be treated as if it were
* an abstract class.
* <p>The SQL statements which implement each of the functions are
* loaded in a separate file etc/[layout]_[database].sql from the classpath.
*
* @author hkuno modification of Jena1 code by Dave Reynolds (der)
* @version $Revision: 1.9 $ on $Date: 2003-05-06 09:43:49 $
*/

public abstract class DriverRDB implements IRDBDriver {

//=======================================================================
// Cutomization variables
// =======================================================================
   /**
    * This Graph's db properties
    */
   protected DBPropDatabase m_dbProps;
    
	/**
	* Name of this class's PSet_TripleStore_XXX class
	*/
   protected String m_psetClassName;

   /**
	* Cached name of this class's SpecializedGraph_XXX class
	*/
   protected String m_lsetClassName;
   
   /** The class name of the database driver (e.g. jdbc.sql.class)*/
   protected  String DRIVER_NAME;     
   // Dummy - needs replacing when instantiated?

   /** The name of the database type this driver supports */
   protected String DATABASE_TYPE;

   /** The maximum size of literals that can be added to Statement table */
   protected int MAX_LITERAL;

   /** The SQL type to use for storing ids (compatible with wrapDBID) */
   protected String ID_SQL_TYPE;

   /** Set to true if the insert operations already check for duplications */
   protected boolean SKIP_DUPLICATE_CHECK;

   /** Set to true if the insert operations allocate object IDs themselves */
   protected boolean SKIP_ALLOCATE_ID;
	
   /** Holds value of empty literal marker */
   protected String EMPTY_LITERAL_MARKER;
	
   /** The name of the sql definition file for this database/layout combo */
   protected String SQL_FILE;
   
   /** The name of the sql definition file for this database/layout combo */
   protected String DEFAULT_SQL_FILE = "etc/generic_generic.sql";


   /** Set to true if the insert operations should be done using the "proc" versions */
   protected boolean INSERT_BY_PROCEDURE;
   
// =======================================================================
//	Common variables
// =======================================================================
   /**
	* Holds base name of AssertedStatement table.
	* Every triple store has at least one tables for AssertedStatements.
	*/
   protected static final String ASSERTED_TABLE_BASE ="JENA_";
  
   /** Set to true to enable cache of pre-prepared statements */
   protected boolean CACHE_PREPARED_STATEMENTS = true;

   /** The name of the layout type this driver supports */
   protected String LAYOUT_TYPE = "TripleStore";

   /** Default name of the table that holds system property graph asserted statements **/
   protected final String SYSTEM_PROP_TNAME = "JENA_SYS_STMTASSERTED";
    
   /** Name of the grpah holding default properties (the one's that a newly-created
	*  graph will have by default **/
   protected final String DEFAULT_PROPS = "JENA_DEFAULT_GRAPH_PROPERTIES";
        
   /** Driver version number */
   protected final String VERSION = "2.0alpha";
    

// =======================================================================
//	Instance variables
// =======================================================================

	/**
	 * Instance of SQLCache used by Driver for hard-coded db commands
	 */
	protected SQLCache m_sql = null;

   /** 
	* Each PSet_TripleStore_RDB instance is associated with a given 
	* instance of DBPropPSet. 
	*/
   private  Hashtable m_NameMap = new Hashtable();

    /** Cache a reference to the system property graph (java) **/
    protected SpecializedGraph m_sysProperties = null;
    
    protected IDBConnection m_dbcon = null;
    
    //===================================
    // for transaction support
    //===================================
    
    
    // caches whether or not underlying connection supports transactions
    private Boolean m_transactionsSupported;
    
	/** flag to indicate that there is a transaction active on the associated connection */
	protected boolean inTransaction = false;



//	=======================================================================
//	 Constructor
//	=======================================================================


    /**
     * Create a bare instance of the driver. It is not functional until a
     * database connection has been supplied via setConnection.
     */
    public DriverRDB() {
    }
    
//	=======================================================================
//	 Methods
//	=======================================================================
	
	/**
	 * Return the connection
	 */
	public IDBConnection getConnection() {
		return m_dbcon;
	}
	
	/**
	 * Return the specialized graph used to store system properties.
	 * (Constuct a new one if necessary).
	 */
	public SpecializedGraph getSystemSpecializedGraph() {
		
		if (m_sysProperties != null) {
			return m_sysProperties;
		}
		
		if( !isDBFormatOK() ) {
			// Format the DB
			return formatAndConstructSystemSpecializedGraph();
		}
		
		// The database has already been formatted - just grab the properties
		IPSet pSet = createIPSetInstanceFromName(m_psetClassName);
		pSet.setASTname(SYSTEM_PROP_TNAME);
		m_sysProperties = createLSetInstanceFromName(m_lsetClassName, pSet);
		m_dbProps = new DBPropDatabase(m_sysProperties);
		return m_sysProperties;		
	}
	
	/**
	 * Format the database and construct a brand new system specialized graph.
	 */
	protected SpecializedGraph formatAndConstructSystemSpecializedGraph() {

		try {
			m_sql.runSQLGroup("initDBtables");
			if (!SKIP_ALLOCATE_ID) {
				Iterator seqIt = getSequences().iterator();
				while (seqIt.hasNext()) {
						removeSequence((String)seqIt.next());
				}
			}
			m_sql.runSQLGroup("initDBgenerators");
			m_sql.runSQLGroup("initDBprocedures");
		} catch (SQLException e) {
			com.hp.hpl.jena.util.Log.warning("Problem formatting database", e);
			throw new RDFRDBException("Failed to format database", e);
		}
		
		// Construct the system properties
		IPSet pSet = createIPSetInstanceFromName(m_psetClassName);
		pSet.setASTname(SYSTEM_PROP_TNAME);
		m_sysProperties = createLSetInstanceFromName(m_lsetClassName, pSet);
						
		// The following call constructs a new set of database properties and
		// adds them to the m_sysProperties specialized graph.
		m_dbProps = new DBPropDatabase( m_sysProperties, createUniqueIdentifier(), 
							m_dbcon.getDatabaseType(), VERSION, String.valueOf(MAX_LITERAL));
			
		// Now we also need to construct the parameters that will be the
		// default settings for any graph added to this database
		new DBPropGraph( m_sysProperties, DEFAULT_PROPS, "generic");

		return m_sysProperties;		
	}
	
	/**
	 * Construct and return a list of specialized graphs to match those in the store.
	 * @param graphProperties A set of customization properties for the graph.
	 */
	public List recreateSpecializedGraphs(DBPropGraph graphProperties) {
		
		List result = new ArrayList();

		Iterator it = graphProperties.getAllLSets();
		while(it.hasNext() ) {
			DBPropLSet lSet = (DBPropLSet)it.next();
			result.add(recreateSpecializedGraph( lSet ));
		}
		
		return result;
		
	}
	
	/**
	 * Construct and return a new specialized graph.
	 * @param graphProperties A set of customization properties for the specialized graph.
	 */
	public List createSpecializedGraphs(DBPropGraph graphProperties) {
		
		List result = new ArrayList();

		// In the future we should do something more sophisticated,
		// but for now we keep things simple.

		// First choose a pSet to hold things - for now, assume a new
		// pSet for each new Graph
		DBPropPSet pSet = new DBPropPSet(m_sysProperties, "PSET_"+graphProperties.getName(), m_psetClassName);
		DBPropLSet lSet = new DBPropLSet(m_sysProperties, "LSET_"+graphProperties.getName(), m_lsetClassName);
		lSet.setPSet(pSet);
		graphProperties.addLSet(lSet);
		result.add(createSpecializedGraph(lSet));
		return result;
	}
	
	private SpecializedGraph createLSetInstanceFromName(String lSetName, IPSet pset) {
		SpecializedGraph sg = null;		
		try {
			Class cls = Class.forName(lSetName);
			Class[] params = {IPSet.class};
			java.lang.reflect.Constructor con = cls.getConstructor(params);
			Object[] args = {pset};
			sg = (SpecializedGraph) con.newInstance(args);
		} catch (Exception e) {
			Log.severe("Unable to create instance of SpecializedGraph " + e);
		}
		return sg;
	}

    /**
     * Create a new IPSet instance of the named implementation class and set the db connection.
     * 
     * @param pName name of a class that implements IPSet.
     * @return an instance of the named class with the db connection set.
     */
	private IPSet createIPSetInstanceFromName(String pName) {
		IPSet pSet = null;		
		try {
			Class cls = Class.forName(pName);	
			// get PSet
			pSet = (IPSet) Class.forName(pName).newInstance();
			pSet.setDriver(this);
			pSet.setMaxLiteral(MAX_LITERAL);
			pSet.setSQLType(ID_SQL_TYPE);
			pSet.setSkipDuplicateCheck(SKIP_DUPLICATE_CHECK);
			pSet.setSkipAllocateId(SKIP_ALLOCATE_ID);
			pSet.setEmptyLiteralMarker(EMPTY_LITERAL_MARKER);
			pSet.setSQLCache(m_sql);
			pSet.setInsertByProcedure(INSERT_BY_PROCEDURE);
			pSet.setCachePreparedStatements(CACHE_PREPARED_STATEMENTS);

		} catch (Exception e) {
			Log.warning("Unable to create IPSet instance " + e);
		}
		return pSet;
	}
		 
	/**
	 * Create a new SpecializedGraph instance from properties.
	 * 
	 * @param lProp logical property set.
	 * @return an instance of a class that implements SpecializedGraph.
	 */
	public SpecializedGraph createSpecializedGraph(DBPropLSet lProp) {
		DBPropPSet pProp = lProp.getPset();
		IPSet pset = findOrCreatePSet(pProp);
		
		SpecializedGraph g = null;
		
		try { 
		 // get SpecializedGraph
		 Class cls = Class.forName(lProp.getType());
		 Class[] params2 = {IPSet.class};
		 java.lang.reflect.Constructor con = cls.getConstructor(params2);
		 Object[] args2 = {pset};
		 g = (SpecializedGraph)con.newInstance(args2);
		} catch (Exception e) {
			Log.severe("createSpecializedGraph: " + e);
		}
		return g;
	}
	
//	=======================================================================
//	 static methods

	/** 
	 * adds the IPSet instance to the m_NameMap hash table, using the
	 * name of the DBPropPSet as the key.
	 */
	protected  void addPSet(String name,  IPSet pSet){
		m_NameMap.put(name, pSet);
	}


	/** 
	 * retrieves the IPSet instance to the m_NameMap hash table, using the
	 * name of the DBPropPSet as the key.
	 * @return IPSet that was found
	 */
	protected  IPSet findPSet(String name){
		return (IPSet) m_NameMap.get(name);
	}

	/**
	 * Given the name of an instance of a class that implements IPSet, 
	 * find or create an instance of it.
	 * (This name is obtained from the DBPropPSet instance.)
	 * 
	 * @param pName is the name of the IPSet instance.
	 * @return IPSet that was found or created
	 */
	public IPSet findOrCreatePSet(String pName){
		IPSet pSet = findPSet(pName);
		if (pSet == null) {
			pSet = createIPSetInstanceFromName(m_psetClassName);
			pSet.setASTname(ASSERTED_TABLE_BASE+pName);
			addPSet(pName, pSet);
		}
		return pSet;
	}

	/**
	 * Given an instance of DBPropPSet (PSet properties), 
	 * get the property set's name find or create an instance of it.
	 * 
	 * @param pProp is the DBPropPSet instance to be
	 * associated with the new IPSet.
	 *
	 * @return IPSet that was found or created
	 */
	public IPSet findOrCreatePSet(DBPropPSet pProp){
		String pName = pProp.getName();
		return findOrCreatePSet(pName);
	}
	
	/**
	 * Construct and return a specialized graph that already exists in the db.
	 * @param graphProperties
	 * @return SpecializedGraph which was recreated.
	 */
	public SpecializedGraph recreateSpecializedGraph(DBPropLSet lProp) {
		return createSpecializedGraph(lProp);
	}
	
	/**
	 * Remove the specialized graph, erasing all trace of a Graph.
	 * @param graphId The identity of the Graph which these specialized graphs should hold
	 * @param graphProperties The properties for the graph to be removed.
	 */
	public void removeSpecializedGraphs( DBPropGraph graphProperties,
		List specializedGraphs) {
			
		Iterator it = specializedGraphs.iterator();
		while (it.hasNext()){
		   removeSpecializedGraph((SpecializedGraph) it.next());
		}

		// remove from system properties table
		// It is sufficient just to remove the lSet properties (it will
		// take care of deleting any pset properties automatically).			
		m_dbProps.removeGraph(graphProperties);
	}
	
	
	/**
	 * Remove specialized graph from the datastore.
	 * @param graph is the graph to be removed.
	 */
	private void removeSpecializedGraph(SpecializedGraph graph) {
		graph.clear();		
	}

	/**
	 * Method setDatabaseProperties.
	 * 
	 * Sets the current properties for the database.
	 * 
	 * @param databaseProperties is a Graph containing a full set of database properties
	 */
	public void setDatabaseProperties(Graph databaseProperties) {
		SpecializedGraph toGraph = getSystemSpecializedGraph();
		// really need to start a transaction here

		// Here add code to check if the database has been used - if so,
		// it's too late to change the properties, so throw an exception

		toGraph.clear();
		SpecializedGraph.CompletionFlag complete = new SpecializedGraph.CompletionFlag();
		toGraph.add(databaseProperties, complete);

		// Now test the properties to see if it's a valid set - if not,
		// throw an exception - it's okay to check some things later (there's
		// no guarantee that every error will be caught here).

		// end transaction here.
	}

		
	/**
	 * Method getDefaultModelProperties 
	 * 
	 * Return the default properties for a new model stored in this database.
	 * If none are stored, then load default properties into the database.
	 * @return Graph containg the default properties for a new model
	 */
	public DBPropGraph getDefaultModelProperties() {
		SpecializedGraph sg = getSystemSpecializedGraph();
		DBPropGraph result = DBPropGraph.findPropGraph(sg, DEFAULT_PROPS);
		if (result == null) {
			Log.severe("No default Model Properties found");
			// Construct the parameters that will be the
			// default settings for any graph added to this database
			//new DBPropGraph( m_sysProperties, "default", "generic");
			//result = DBPropGraph.findPropGraph(sg, "default");	
		}
		return result;
	}

	/**
	 * Test if the database has previously been formatted.
	 * 
	 * @return boolean true if database is correctly formatted, false on any error.
	 */
	public boolean isDBFormatOK() {
		boolean result = false;
		try {
			DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			ResultSet alltables = dbmd.getTables(null, null, "JENA%", tableTypes);
			result = alltables.next();
			alltables.close();
		} catch (Exception e1) {
			;// if anything goes wrong, the database is not formatted correctly;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graphRDB.IRDBDriver#cleanDB()
	 */
	public void cleanDB() {
		try {
			DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			ResultSet alltables = dbmd.getTables(null, null, "JENA%", tableTypes);
			List tablesPresent = new ArrayList(10);
			while (alltables.next()) {
				tablesPresent.add(alltables.getString("TABLE_NAME").toUpperCase());
			}
			alltables.close();
			Iterator it = tablesPresent.iterator();
			while (it.hasNext()) {
				m_sql.runSQLGroup("dropTable", (String) it.next());
			}
			if (!SKIP_ALLOCATE_ID) {
				removeSequence("JENA_LITERALS_GEN");
				removeSequence("JENA_GRAPHS_GEN");
				/*
				Iterator seqIt = getSequences().iterator();
				while (seqIt.hasNext()) {
					removeSequence((String)seqIt.next());
				}
				*/
			}
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal SQL error in driver", e1);
		}
	}
	
	/**
	 * Removes named sequence from the database, if it exists.
	 * @param seqName
	 */
	public void removeSequence(String seqName) {
		if (sequenceExists(seqName)) {
			try {
				m_sql.runSQLGroup("DropSequence",seqName);
			} catch (Exception e) {
				Log.warning("Unable to drop sequence " + seqName + ": " + e);
			}
		}
	}
	/**
	 * Check database and see if named sequence exists.
	 * @param seqName
	 */
	public boolean sequenceExists(String seqName) {
		Object[] args = {seqName};
		ResultSetIterator it = null;
		try {
		    it = m_sql.runSQLQuery("SelectSequenceName",args);
		} catch (Exception e) {
		  Log.severe("Unable to select sequence " + seqName + ": " + e);
			}
		if (it != null) {
			return (it.hasNext());
		}		
		it.close();
		return false;
	}

	/**
	 * Check database and see if named sequence exists.
	 * @param seqName
	 */
	public List getSequences() {
		List results =  new ArrayList(10);
		Object[] args = {};
		ResultSetIterator it = null;
		try {
		    it = m_sql.runSQLQuery("SelectJenaSequences",args);
		    while (it.hasNext()) {
		    	results.add((String)it.getSingleton());
		    }
		    it.close();
		} catch (Exception e) {
		  Log.severe("Unable to select Jena sequences: " + e);
		 }
		return results;
	}
	
	/**
	 * Initialise a database ready to store RDF tables.
	 * @throws RDFDBException if the is a problem opening the connection or an internal SQL error.
	 * @deprecated Since Jena 2.0 this call is no longer needed - formatting 
	 * happens automatically as a side effect of creating Models - there should
	 * be no need for an application to interact directly with the driver.
	 */
	public void formatDB() throws RDFRDBException {
	}

	/**
	 * Throws an UnsupportedOperation exception.
	 * 
	 * @param opName name of the operation that's not supported.
	 */
	private void notSupported(String opName)
		{ throw new UnsupportedOperationException(opName); }
		
		/**
	 * If underlying database connection supports transactions, call abort()
	 * on the connection, then turn autocommit on.
	 */
	public synchronized void abort() throws RDFRDBException {
		if (transactionsSupported()) {
			try {
				if (inTransaction) {
				  Connection c = m_sql.getConnection();
				  c.rollback();
				  c.commit();
				  c.setAutoCommit(true);
				  inTransaction = false;
				}
			} catch (SQLException e) {
				throw new RDFRDBException("Transaction support failed: ", e);
			}
		} else {
		}
	}
        



        
	/**
	 * If the underlying database connection supports transactions,
	 * turn autocommit off, then begin a new transaction.
	 * Note that transactions are associated with connections, not with
	 * Models.  This 
	 */
	public synchronized void begin() throws  RDFRDBException {
	  if (transactionsSupported()) {
		try {
			if (!inTransaction) {
				// Starting a transaction could require us to lose any cached prepared statements
				// for some jdbc drivers, currently I think all the drivers we use are safe and
				// is a major performance hit so commented out for now.
			  //m_sql.flushPreparedStatementCache();
			  Connection c = m_sql.getConnection();
			  c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			  c.setAutoCommit(false);
			  inTransaction = true;
			}
		} catch (SQLException e) {
			throw new RDFRDBException("Transaction support failed: ", e);
		}
	} else
		{ notSupported("begin transaction"); }
	}
	
	/**
	 * If the underlying database connection supports transactions,
	 * call commit(), then turn autocommit on.
	 */
	public void commit() throws RDFRDBException{
		if (transactionsSupported()) {
			try {
				  if (inTransaction) {
				  	Connection c = m_sql.getConnection();
					c.commit();
					c.setAutoCommit(true);
					c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
					inTransaction =  false;
				   }
				} catch (SQLException e) {
						throw new RDFRDBException("Transaction support failed: ", e);
				}
		} else {
				  notSupported("commit transaction"); 
		}
	}
        
	/**
	 * Return a string identifying underlying database type.
	 *
	 */
	public String getDatabaseType() {
		return(DATABASE_TYPE);
	}

	/**
	 * Returns true if the underlying database supports transactions.
	 */
	public boolean transactionsSupported() { 
		if (m_transactionsSupported != null) {
			return(m_transactionsSupported.booleanValue());	
		}
		
		if (m_dbcon != null) {
			try {
				Connection c = m_sql.getConnection();
				if ( c != null) {
					m_transactionsSupported = new Boolean(c.getMetaData().supportsMultipleTransactions());
					return(m_transactionsSupported.booleanValue());
				}
			} catch (SQLException e) {
				Log.severe("SQL Exception caught " + e);
			}
		}
		return (false);
			
		}
        



    //--------------------------------------------------jena 1 backward compatability

    /**
     * Close the driver 
     * 
     * Nothing to do for now.
     * 
     * @throws RDFDBException if there is an access problem
     * @deprecated Since Jena 2.0 this call is no longer required - just 
     * close the DBConnection - there should be no need for an application
     * to interact directly with the driver.
     * 
     */

    public void close() throws RDFRDBException {
    }


    /**
     * Returns true if the database layout supports multiple RDF models
     * in the same database.
     * @deprecated Since Jena 2.0 all databases support multiple models.
     */

    public boolean supportsMultipleModels() {
    	return true;
    }

    /**
     * Returns true if the database layout supports implicit reification
     * of statements (i.e. statements can be treated as resources).
     * @deprecated Since Jena 2.0 the reification API has changed.  The
     * new API is supported in all models, but the old Jena 1 API is no
     * longer supported.  This call will return false to indicate
     * to old code that the old style of jena reification is not supported.
     */

    public boolean supportsJenaReification() {
    	return false;
    }

	/**
	 * Create a unique identifier.
	 * Use the local IP address and the date - this is not perfect, but 
	 * sufficient for our simple needs.
	 * @return String identifier.
	 */
	static String createUniqueIdentifier() {
		String localhost;
		try {
			localhost = java.net.InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			localhost = "localhost";
		}

		return localhost + "/" + (new Date()).getTime();

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
