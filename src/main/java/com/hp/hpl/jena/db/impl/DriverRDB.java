/*
  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import java.sql.*;
import java.util.*;
import java.util.zip.CRC32;
import java.io.UnsupportedEncodingException;
import java.lang.Thread;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.query.ExpressionFunctionURIs; 

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.DB;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.xerces.util.XMLChar;

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
* @version $Revision: 1.1 $ on $Date: 2009-06-29 08:55:37 $
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
  * Name of this class's PSet_TripleStore_XXX class
  */
 protected String m_psetReifierClassName;

   /**
	* Cached name of this class's SpecializedGraph_XXX class
	*/
   protected String m_lsetClassName;
   
   /**
	* Cached name of this class's SpecializedGraphReifer_XXX class
	*/
   protected String m_lsetReifierClassName;
   
   /** The class name of the database driver (e.g. jdbc.sql.class)*/
   protected  String DRIVER_NAME;     
   // Dummy - needs replacing when instantiated?

   /** The name of the database type this driver supports */
   protected String DATABASE_TYPE;

   /** The maximum size of index key (or a component of a key) */
   protected int INDEX_KEY_LENGTH;

   /** The maximum possible value for INDEX_KEY_LENGTH (db-dependent) */
   protected int INDEX_KEY_LENGTH_MAX;

   /** true if graphs using this database instance supports transactions.
    * this is a user settable parameter. the underlying db engine may support
    * transactions but an application may prefer to run without transactions
    * for better performance. this can only be set before the db is formatted.
    */
   protected boolean IS_XACT_DB;

   
   protected boolean STRINGS_TRIMMED;
   /** true if the database engine will trim trailing spaces in strings. to
    *  prevent this, append EOS to strings that should not be trimmed.
    */
   
   protected String EOS = "";
   protected char	EOS_CHAR = ':';
   protected int	EOS_LEN = 0;
   /** EOS is appended to most RDB strings to deal with string trimming. if
    *  STRINGS_TRIMMED is false, EOS is null. otherwise, EOS is EOS_CHAR.
    *  EOS_LEN is the length of EOS (0 or 1).
    */
   
   protected char	QUOTE_CHAR = '\"';
   /** the quote character used to delimit characters and strings.
    */
   
   /**
    * Indicates whether search pattern used to select system objects by name should be upper-case.
    */
   protected  boolean DB_NAMES_TO_UPPER = false;
  

   /** true if URI's are to be compressed by storing prefixes (an approximation
    *  of a namespace) in the JENA_PREFIX table. note that "short" prefixes are
    *  not stored, i.e., the prefix length not more than URI_COMPRESS_LENGTH.
    */
   protected boolean URI_COMPRESS;

   
   protected int URI_COMPRESS_LENGTH = 100;
   /** if URI_COMPRESS is true, compress prefixes that are longer than this.

   /** The maximum size of an object that can be stored in a Statement table */
   protected int LONG_OBJECT_LENGTH;
   
   /** The maximum possible value for LONG_OBJECT_LENGTH (db-dependent) */
   protected int LONG_OBJECT_LENGTH_MAX;

   /** The SQL type to use for storing ids (compatible with wrapDBID) */
   protected String ID_SQL_TYPE;
   
   /** Set to true if the insert operations already check for duplications */
   protected boolean SKIP_DUPLICATE_CHECK;

   /** Set to true if IDs are allocated prior to insert */
   protected boolean PRE_ALLOCATE_ID;
	
   /** The name of the sql definition file for this database/layout combo */
   protected String SQL_FILE;
   
   /** The name of the sql definition file for this database/layout combo */
   protected String DEFAULT_SQL_FILE = "etc/generic_generic.sql";
      
   
// =======================================================================
//	Common variables
// =======================================================================
   /**
	* Holds prefix for names of Jena database tables.
	*/
   protected String TABLE_NAME_PREFIX = "jena_";
   
   /**
	* Holds maximum length of table and index names in database.
	*/
   protected int TABLE_NAME_LENGTH_MAX;
      
   /** Suffixes for asserted and reified table names. */
   protected String STMT_TABLE_NAME_SUFFIX = "_stmt";
   protected String REIF_TABLE_NAME_SUFFIX = "_reif";
   
   /** Maximum number of index columns. can be changed. */
   protected int MAXIMUM_INDEX_COLUMNS = 3;
  
   /** Number of required system tables. */
   protected int SYSTEM_TABLE_CNT = 0;
   
   /** Names of jena system tables. */
   public String [] SYSTEM_TABLE_NAME;
  
   /** Set to true to enable cache of pre-prepared statements */
   protected boolean CACHE_PREPARED_STATEMENTS = true;

   /** The name of the layout type this driver supports */
   protected String LAYOUT_TYPE = "TripleStore";

   /** Default name of the table that holds system property graph asserted statements **/
   protected String SYSTEM_STMT_TABLE;
   
   /** Name of the long literal table **/
   protected String LONG_LIT_TABLE;
   
   /** Name of the long URI table **/
   protected String LONG_URI_TABLE;

   /** Name of the prefix table **/
   protected String PREFIX_TABLE;

      /** Name of the graph table **/
   protected String GRAPH_TABLE;
    
   /** Name of the mutex table **/
   protected String MUTEX_TABLE;
    
	/** If not null, newly-created graphs share tables with the identified graph **/
   protected String STORE_WITH_MODEL = null;
    
   /** Name of the graph holding default properties (the one's that a newly-created
	*  graph will have by default **/
   protected final String DEFAULT_PROPS = "JENA_DEFAULT_GRAPH_PROPERTIES";
   
   /** Unique numeric identifier of the graph holding default properties **/
   protected final int DEFAULT_ID = 0;

        
   /** Driver version number */
   protected final String VERSION = "2.0alpha";
   
   /** Database layout version */
   protected String LAYOUT_VERSION = "2.0";
   
   protected static Logger logger = LoggerFactory.getLogger( DriverRDB.class );
    
// =======================================================================
//	Instance variables
// =======================================================================

	/**
	 * Instance of SQLCache used by Driver for hard-coded db commands
	 */
	protected SQLCache m_sql = null;

    /** Cache a reference to the system property graph (java) **/
    protected SpecializedGraph m_sysProperties = null;
    
    protected IDBConnection m_dbcon = null;
    
    protected LRUCache<DBIDInt, String> prefixCache = null;
    
    public static final int PREFIX_CACHE_SIZE = 50;
    
    //===================================
    // for transaction support
    //===================================
    
    
    // caches whether or not underlying connection supports transactions
    private Boolean m_transactionsSupported;
    
	/** flag to indicate that there is a transaction active on the associated connection */
	private boolean inTransaction = false;



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
	@Override
    public IDBConnection getConnection() {
		return m_dbcon;
	}
	
	/**
	 * Return the specialized graph used to store system properties.
	 * (Constuct a new one if necessary). if the database is not
	 * properly formatted, then if doInit is true, the database will
	 * be formatted, else null is returned and the (unformatted
	 * database is unchanged).
	 */
	@Override
    public SpecializedGraph getSystemSpecializedGraph(boolean doInit) {

		SpecializedGraph res = null;
		
		if (m_sysProperties != null) {
			return m_sysProperties;
		}

		if (!isDBFormatOK()) {
			// another thread could be formatting database
			// so get the mutex and try again
			lockDB();
			if (!isDBFormatOK()) {
				if (doInit) {
					try {
						// Format the DB
						// throw new JenaException("The database is not
						// formatted.\n");
						doCleanDB(false);
						prefixCache = new LRUCache<DBIDInt, String>(PREFIX_CACHE_SIZE); 
						res = formatAndConstructSystemSpecializedGraph();
					} catch (Exception e) {
						unlockDB();
						// We see an error during format testing, might be
						// a dead connection rather than an unformated
						// database so abort
						throw new JenaException(
						"The database appears to be unformatted or corrupted and\n"
						+ "an attempt to automatically format the database has failed\n", e);
					}
				}
				unlockDB();
				return res;
			}
			// after second try, DB is found to be correctly formatted.
			unlockDB();
		}

		prefixCache = new LRUCache<DBIDInt, String>(PREFIX_CACHE_SIZE);
		getDbInitTablesParams(); //this call is a hack. it's needed because
		// it has the side effect of initializing some vars (e.g., EOS).
		IPSet pSet = createIPSetInstanceFromName(m_psetClassName,
				SYSTEM_STMT_TABLE);
		m_sysProperties = createLSetInstanceFromName(m_lsetClassName, pSet,
				DEFAULT_ID);
		m_dbProps = new DBPropDatabase(m_sysProperties);
		
		// need to get initial values for encoding parameters
		String longObjLen = m_dbProps.getInitLongObjectLength();
		String indexKeyLen = m_dbProps.getInitIndexKeyLength();
		String compURI = m_dbProps.getInitDoCompressURI();
		String compURILen = m_dbProps.getInitCompressURILength();

		if (longObjLen == null)
			throwBadFormat("long object length");
		else
			LONG_OBJECT_LENGTH = Integer.parseInt(longObjLen);
		if (indexKeyLen == null)
			throwBadFormat("index key length");
		else
			INDEX_KEY_LENGTH = Integer.parseInt(indexKeyLen);
		if (compURI == null)
			throwBadFormat("compress URIs");
		else
			URI_COMPRESS = Boolean.valueOf(compURI).booleanValue();
		if (compURILen == null)
			throwBadFormat("URI compress length");
		else
			URI_COMPRESS_LENGTH = Integer.parseInt(compURILen);

		// now reset the configuration parameters
		checkEngine(m_dbProps);
		checkDriverVersion(m_dbProps);
		checkLayoutVersion(m_dbProps);
		String val = null;
		val = m_dbProps.getIsTransactionDb();
		if (val == null)
			throwBadFormat("database supports transactions");
		else
			IS_XACT_DB = Boolean.valueOf(val).booleanValue();
		val = m_dbProps.getTableNamePrefix();
		if (val == null)
			throwBadFormat("table name prefix");
		else
			TABLE_NAME_PREFIX = val;

		return m_sysProperties;
	}
	
	private void checkEngine ( DBProp dbProps ) {
		String dbtype = m_dbProps.getEngineType();
		if ( dbtype == null ) throwBadFormat("database type");
		if ( !dbtype.equals(DATABASE_TYPE) ) {
			throw new JenaException(
			"Database created with incompatible database type for this version of Jena: "
			+ dbtype);
		}
	}
	
	private void checkDriverVersion ( DBProp dbProps ) {
		String vers = m_dbProps.getDriverVersion();
		if ( vers == null ) throwBadFormat("database version");
		if ( !vers.equals(VERSION) ) {
			throw new JenaException(
			"Models in the database were created with an incompatible version of Jena: "
			+ vers);
		}
	}
	
	private void checkLayoutVersion ( DBProp dbProps ) {
		String layout = m_dbProps.getLayoutVersion();
		if ( layout == null ) throwBadFormat("database layout");
		if ( !layout.equals(LAYOUT_VERSION) ) {
			throw new JenaException(
			"The database layout cannot be processed by this version of Jena: "
			+ layout);	
		}

	}
	
	private void throwBadFormat ( String prop ) {
		throw new JenaException(
		"The database appears to be unformatted or corrupted - could not find value\n" +
		" for \"" + prop + "\" in Jena system properties table.\n" + 
		"If possible, call IDBConnection.cleanDB(). \n" +
		"Warning: cleanDB will remove all Jena models from the databases.");
	}

	
	/**
	 * Format the database and construct a brand new system specialized graph.
	 */
	protected SpecializedGraph formatAndConstructSystemSpecializedGraph() {
		String errMsg = null;
		if (xactOp(xactIsActive))
			throw new RDFRDBException(
					"Cannot intialize database while transaction is active.\n"
							+ "Commit or abort transaction before intializing database.");

		boolean autoIsOn = xactOp(xactAutoOff);
		try {
			String[] params = getDbInitTablesParams();
			m_sql.runSQLGroup("initDBtables", params);
			m_sql.runSQLGroup("initDBgenerators");//			m_sql.runSQLGroup("initDBprocedures");
		} catch (SQLException e) {
			logger.warn("Problem formatting database", e);
			errMsg = e.toString();
		}

		if (errMsg == null)
			try {
				xactOp(xactCommit);
				xactOp(xactBegin);

				// Construct the system properties
				IPSet pSet = createIPSetInstanceFromName(m_psetClassName,
						SYSTEM_STMT_TABLE);
				m_sysProperties = createLSetInstanceFromName(m_lsetClassName,
						pSet, DEFAULT_ID);

				// The following call constructs a new set of database
				// properties and
				// adds them to the m_sysProperties specialized graph.
                
                // Ugh: m_dbcon.getDatabaseType(), not this.getDatabaseType()
				m_dbProps = new DBPropDatabase(m_sysProperties,
                                               m_dbcon.getDatabaseType(),
                                               VERSION, LAYOUT_VERSION,
                                               String.valueOf(LONG_OBJECT_LENGTH),
                                               String.valueOf(INDEX_KEY_LENGTH),
                                               String.valueOf(IS_XACT_DB),
                                               String.valueOf(URI_COMPRESS), 
                                               String.valueOf(URI_COMPRESS_LENGTH),
                                               TABLE_NAME_PREFIX);

				// Now we also need to construct the parameters that will be the
				// default settings for any graph added to this database
				DBPropGraph def_prop = new DBPropGraph(m_sysProperties,
						DEFAULT_PROPS, "generic");

				def_prop.addGraphId(DEFAULT_ID);

				xactOp(xactCommit);
				if (autoIsOn)
					xactOp(xactAutoOn);
			} catch (Exception e) {
				errMsg = e.toString();
			}

		if (errMsg != null) {
			doCleanDB(false);
			m_sysProperties = null;
			throw new RDFRDBException(errMsg);
		}

		return m_sysProperties;
	}
	
	abstract String[] getDbInitTablesParams();
	
	abstract String[] getCreateTableParams( int graphId, boolean isReif );
	
	@Override
    abstract public int graphIdAlloc ( String graphName );	
	
	
	
	/**
	 * Construct and return a new specialized graph.
	 */
	@Override
    public List<SpecializedGraph> createSpecializedGraphs(String graphName,
			Graph requestedProperties) {

		/*
		 * create the specialized graphs for the new graph. this includes
		 * updating the database for the new graph (allocating a new graph
		 * identifier, updating the jena system tables and creating tables, if
		 * necessary. this should be done atomically to avoid corrupting the
		 * database but a single transaction is not sufficient because some
		 * database engines (e.g., oracle) require create table statements to
		 * run as a separate transaction, i.e., a create table statement in the
		 * middle of a group of updates will cause an automatic commit of the
		 * updates prior to the create table statement.
		 * 
		 * fortunately, we can run most of the updates in a single transaction.
		 * however, allocation of the graph indentifier must be done prior to
		 * creating the statement tables. so, if any subsequent operation fails,
		 * we must run a compensating transaction to deallocate the graph
		 * identifier.
		 * 
		 * because of the above, we assume that there is no active transaction
		 * when this routine is called.
		 */

		// String graphName = graphProperties.getName();
		String stmtTbl = null;
		String reifTbl = null;
		String dbSchema = STORE_WITH_MODEL;
		boolean didGraphIdAlloc = false;
		boolean didTableCreate = false;
		String errMsg = null;
		DBPropGraph graphProperties = null;

		SpecializedGraph sysGraph = getSystemSpecializedGraph(false);
		// should have already create sys graph.

		if (xactOp(xactIsActive))
			throw new RDFRDBException(
					"Cannot create graph while transaction is active.\n"
							+ "Commit or abort transaction before creating graph");

		boolean autoOn = xactOp(xactAutoOff);
		int graphId = -1; // bogus initialization to make java happy

		try {
			xactOp(xactBegin);
			graphId = graphIdAlloc(graphName);
			didGraphIdAlloc = true;
			xactOp(xactCommit);
			xactOp(xactBegin);
			boolean useDefault = false;

			// dbSchema = graphProperties.getDBSchema();
			// use the default schema if:
			// 1) no schema is specified and we are creating the default
			// (unnamed) graph
			// 2) a schema is specified and it is the default (unnamed) graph
			if (((dbSchema == null) && graphName.equals(GraphRDB.DEFAULT))) {
				useDefault = true;
				dbSchema = DEFAULT_PROPS; // default graph should use default
				// tables
			}
			// else if ( ((dbSchema != null) &&
			// dbSchema.equals(GraphRDB.DEFAULT)) ) {
			// 	useDefault = true;
			//	dbSchema = DEFAULT_PROPS; // default graph should use default
			// tables
			// }
			if (dbSchema != null) {
				DBPropGraph schProp = DBPropGraph.findPropGraphByName(sysGraph,
						dbSchema);
				if (schProp != null) {
					reifTbl = schProp.getReifTable();
					stmtTbl = schProp.getStmtTable();
				}
				if (((reifTbl == null) || (stmtTbl == null))
						&& (useDefault == false))
					// schema not found. this is ok ONLY IF it's the DEFAULT
					// schema
					throw new RDFRDBException("Creating graph " + graphName
							+ ": referenced schema not found: " + dbSchema);
			}
			if ((reifTbl == null) || (stmtTbl == null)) {
				didTableCreate = true;
				reifTbl = createTable(graphId, true);
				stmtTbl = createTable(graphId, false);
				if ((reifTbl == null) || (stmtTbl == null))
					throw new RDFRDBException("Creating graph " + graphName
							+ ": cannot create tables");
			}
			xactOp(xactCommit);  // may not be needed but it doesn't hurt
		} catch (Exception e) {
			errMsg = e.toString();
		}

		// we can now start a new transaction and update the metadata.
		// we should already be committed but we commit again just in case

		if (errMsg == null)
			try {
				xactOp(xactBegin);

				graphProperties = new DBPropGraph(sysGraph, graphName,
						requestedProperties);
				graphProperties.addGraphId(graphId);
				graphProperties.addStmtTable(stmtTbl);
				graphProperties.addReifTable(reifTbl);

				DBPropDatabase dbprop = new DBPropDatabase(
						getSystemSpecializedGraph(true));
				dbprop.addGraph(graphProperties);

				// Add the reifier first
				DBPropPSet pSetReifier = new DBPropPSet(m_sysProperties,
						m_psetReifierClassName, reifTbl);
				DBPropLSet lSetReifier = new DBPropLSet(m_sysProperties,
						"LSET_" + graphProperties.getName() + "_REIFIER",
						m_lsetReifierClassName);
				lSetReifier.setPSet(pSetReifier);
				graphProperties.addLSet(lSetReifier);

				// Now add support for all non-reified triples
				DBPropPSet pSet = new DBPropPSet(m_sysProperties,
						m_psetClassName, stmtTbl);
				DBPropLSet lSet = new DBPropLSet(m_sysProperties, "LSET_"
						+ graphProperties.getName(), m_lsetClassName);
				lSet.setPSet(pSet);
				graphProperties.addLSet(lSet);

				xactOp(xactCommit);
				if (autoOn) xactOp(xactAutoOn);
			} catch (Exception e) {
				errMsg = e.toString();
			}

		if (errMsg == null)
			return recreateSpecializedGraphs(graphProperties);
		else {
			xactOp(xactCommit); // maybe not needed but doesn't hurt
			xactOp(xactBegin);
			try {
			// clean-up
			if (didGraphIdAlloc) {
				graphIdDealloc(graphId);
			}
			} catch ( Exception e ) {
			}
			if (didTableCreate) {
				// make sure the order below matches
				// the order of creation above.
				if (reifTbl != null)
					try { deleteTable(reifTbl); }
					catch ( Exception e ) {}
				if (stmtTbl != null)
					try { deleteTable(stmtTbl); }
					catch ( Exception e ) {}
			}
			xactOp(xactCommit);
			if (autoOn) xactOp(xactAutoOn);
			return null;
		}
	}
	
	/**
	 * Construct and return a list of specialized graphs to match those in the
	 * store.
	 * 
	 * @param graphProperties
	 *            A set of customization properties for the graph.
	 */
    @Override
    public List<SpecializedGraph> recreateSpecializedGraphs(DBPropGraph graphProperties) {
		
		List<SpecializedGraph> result = new ArrayList<SpecializedGraph>();
		int dbGraphId = graphProperties.getGraphId();

		// to ensure that reifier graphs occur before stmt graphs, make two passes
		String[] lsetTypes = {m_lsetClassName, m_lsetReifierClassName};
		int i;
		for(i=0;i<2;i++) {
		    Iterator<DBPropLSet> it = graphProperties.getAllLSets();
			while(it.hasNext() ) {
				DBPropLSet lSetProps = it.next();
				if ( lSetProps.getType().equals(lsetTypes[i]) ) continue;
				DBPropPSet pSetProps = lSetProps.getPset();

				IPSet pSet = createIPSetInstanceFromName(pSetProps.getType(), pSetProps.getTable());		
				result.add( createLSetInstanceFromName( lSetProps.getType(), pSet, dbGraphId));		
			}
		}		
		
		return result;		
	}
	
    /**
     * Create a new IPSet instance of the named implementation class and set the db connection.
     * 
     * @param pName name of a class that implements IPSet.
     * @return an instance of the named class with the db connection set.
     */
	private IPSet createIPSetInstanceFromName(String className, String tblName) {
		IPSet pSet = null;		
		try {
			// get PSet
			pSet = (IPSet) Class.forName(className).newInstance();
			pSet.setDriver(this);
			pSet.setSQLType(ID_SQL_TYPE);
			pSet.setSkipDuplicateCheck(SKIP_DUPLICATE_CHECK);
			pSet.setSQLCache(m_sql);
			pSet.setCachePreparedStatements(CACHE_PREPARED_STATEMENTS);
			pSet.setTblName(tblName);
		} catch (Exception e) {
			logger.warn("Unable to create IPSet instance ", e);
		}
		return pSet;
	}	
		
	private SpecializedGraph createLSetInstanceFromName(String lSetName, IPSet pset, int dbGraphID) {
		SpecializedGraph sg = null;		
		try {
			Class<?> cls = Class.forName(lSetName);
			Class<?>[] params = {IPSet.class, Integer.class};
			java.lang.reflect.Constructor<?> con = cls.getConstructor(params);
			Object[] args = {pset, new Integer(dbGraphID)};
			sg = (SpecializedGraph) con.newInstance(args);
		} catch (Exception e) {
			logger.error("Unable to create instance of SpecializedGraph ", e);
		}
		return sg;
	}

	/**
	 * Remove the specialized graph, erasing all trace of a Graph.
	 * @param graphId The identity of the Graph which these specialized graphs should hold
	 * @param graphProperties The properties for the graph to be removed.
	 */
    @Override
    public void removeSpecializedGraphs( DBPropGraph graphProperties, List<SpecializedGraph> specializedGraphs) {
			
		int graphId = graphProperties.getGraphId();
		
		if (xactOp(xactIsActive))
			throw new RDFRDBException(
					"Cannot remove graph while transaction is active.\n"
					+ "Commit or abort transaction before removing graph");

		boolean autoIsOn = xactOp(xactAutoOff);
		xactOp(xactCommit);
		xactOp(xactBegin);
		
		// remove graph metadata from jena sys table in a xact
		String stmtTbl = graphProperties.getStmtTable();
		String reifTbl = graphProperties.getReifTable();
		
		// remove from system properties table
		// It is sufficient just to remove the lSet properties (it will
		// take care of deleting any pset properties automatically).			
		m_dbProps.removeGraph(graphProperties);
		
		if ( graphId != DEFAULT_ID ) graphIdDealloc(graphId);
		
		xactOp(xactCommit);
		xactOp(xactBegin);
		
		/* now remove triples from statement tables.
		*  if the graph is stored in its own tables, we
		*  can simply delete those tables. else, the graph
		*  shares tables with other graphs so we have to
		*  remove each statement. */
		
		// check to see if statement tables for graph are shared
		boolean stInUse = true;
		boolean rtInUse = true;
        
		if ( graphId != DEFAULT_ID ) {
			stInUse = false;
			rtInUse = false;
			Iterator<DBPropGraph> it =  m_dbProps.getAllGraphs();
			while ( it.hasNext() ) {
				DBPropGraph gp = it.next();
				if ( gp.getStmtTable().equals(stmtTbl) ) stInUse = true;
				if ( gp.getReifTable().equals(reifTbl) ) rtInUse = true;
			}
		}
		// now remove the statement tables or else delete all triples.
		if ( stInUse || rtInUse ) {
			Iterator<SpecializedGraph> it = specializedGraphs.iterator();
			while (it.hasNext()){
			   SpecializedGraph sg = it.next();
			   removeSpecializedGraph(sg);
			}
		} else {
			deleteTable(stmtTbl);
			deleteTable(reifTbl);
		}
		xactOp(xactCommit);
		if ( autoIsOn ) xactOp(xactAutoOn);
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
	@Override
    public void setDatabaseProperties(Graph databaseProperties) {
		SpecializedGraph toGraph = getSystemSpecializedGraph(true);
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
	@Override
    public DBPropGraph getDefaultModelProperties() {
		SpecializedGraph sg = getSystemSpecializedGraph(true);
		DBPropGraph result = DBPropGraph.findPropGraphByName(sg, DEFAULT_PROPS);
		if (result == null) {
			logger.error("No default Model Properties found");
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
	@Override
    public boolean isDBFormatOK() throws RDFRDBException {
		boolean result = true;
		boolean[] found = new boolean[SYSTEM_TABLE_CNT];
		int i = 0;
		for (i = 0; i < SYSTEM_TABLE_CNT; i++) found[i] = false;
		try {
            for ( Iterator<String> iter = getAllTables().iterator() ; iter.hasNext(); )
            {
                String tblName = iter.next();
                for (i = 0; i < SYSTEM_TABLE_CNT; i++)
                    if (SYSTEM_TABLE_NAME[i].equals(tblName))
                        found[i] = true;
			}
            
			for (i = 0; i < SYSTEM_TABLE_CNT; i++) {
				if (!found[i]) {
					// mutex table is not required
					if (SYSTEM_TABLE_NAME[i].equals(MUTEX_TABLE))
						continue;
					result = false;
				}
			}
		} catch (Exception e1) {
			// An exception might be an unformatted or corrupt
			// db or a connection problem.
			throw new RDFRDBException("Exception while checking db format - " + e1, e1);
		}
		return result;
	}
	
	/**
	 * Converts string to form accepted by database.
	 */
	public String stringToDBname(String aName) {
		String result = (DB_NAMES_TO_UPPER) ? aName.toUpperCase() : aName;
		return(result);
	}
	
	private static final int lockTryMax = 5;  // max attempts to acquire/release lock
	
	
    /**
     * return true if the mutex is acquired, else false 
     */

    @Override
    public boolean tryLockDB() {
		boolean res = true;
		try {
			m_sql.runSQLGroup("lockDatabase", MUTEX_TABLE);
		} catch (SQLException e) {
			res = false;
		}
		return res;
    }

	
	@Override
    public void lockDB() throws RDFRDBException {
    	String err = "";
    	int cnt = 0;
    	while ( cnt++ < lockTryMax ) {
    		if ( tryLockDB() )
    			break;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				err = err + " lockDB sleep interrupted" + e;
			}
		}
		if ( cnt >= lockTryMax ) {
			err = "Failed to lock database after "+ lockTryMax + " attempts.\n"
			+ err + "\n"
			+ "Try later or else call DriverRDB.unlockDB() after ensuring\n" +
			"that no other Jena applications are using the database.";
			throw new RDFRDBException(err);
		}
    }
    
    /**
     * Release the mutex lock in the database.
     */
    
    @Override
    public void unlockDB() throws RDFRDBException {
    	String err;
    	int cnt = 0;
    	while ( cnt++ < lockTryMax ) {
		try {
			m_sql.runSQLGroup("unlockDatabase", MUTEX_TABLE);
			break;
		} catch (SQLException e) {
			err = "Failed to unlock database after "+ lockTryMax + " attempts - " + e;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				err = err + " sleep failed" + e;
			}
		}
		if ( cnt >= lockTryMax )
			throw new RDFRDBException(err);
		}	
    }
    
    
    /* return true if the mutex is held. */
    
    @Override
    public boolean DBisLocked() throws RDFRDBException {
    	try {
    		DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
    		String[] tableTypes = { "TABLE" };
    		String prefixMatch = stringToDBname(TABLE_NAME_PREFIX + "%");
    		ResultSet iter = dbmd.getTables(null, null, MUTEX_TABLE, tableTypes);
    		try { return iter.next(); } finally { iter.close(); }
    	} catch (SQLException e1) {
    		throw new RDFRDBException("Internal SQL error in driver" + e1);
    	}
    }

	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graphRDB.IRDBDriver#cleanDB()
	 */
	@Override
    public void cleanDB() {

		// assumes database lock is not held.
		try {
			lockDB();
		} catch (RDFRDBException e) {
			throw new RDFRDBException(
					"DriverRDB.cleanDB() failed to acquire database lock:\n"
							+ "("
							+ e
							+ ")\n."
							+ "Try again or call DriverRDB.unlockDB() if necessary.");
		}
		// now clean the database
		doCleanDB(true);
	}
	
	/*
	 * internal routine that does the actual work for cleanDB().
	 * it assumes that the mutex is held and throws and exception
	 * if not. it will optionally remove the mutex if dropMutex
	 * is true.
	 */
	
	protected void doCleanDB( boolean dropMutex ) throws RDFRDBException {
		try {
			if ( !DBisLocked() ) {
				throw new RDFRDBException(
				"Internal error in driver - database not locked for cleaning.\n");
			}
		} catch ( RDFRDBException e ) {
			throw new RDFRDBException(
			"Exception when checking for database lock - \n"
			+ e);
		}
		//ResultSet alltables=null;
		try {
            List<String> tablesPresent = getAllTables() ; 
            Iterator<String> it = tablesPresent.iterator();            
            // Do the MUTEX clean after all other tables.
            while (it.hasNext()) {
                String tblName = it.next();
                if ( tblName.equals(MUTEX_TABLE) )
                    continue;
                m_sql.runSQLGroup("dropTable", tblName);
            }
            
            // Mutex to be removed as well?
            if ( dropMutex && tablesPresent.contains(MUTEX_TABLE) )
                m_sql.runSQLGroup("dropTable", MUTEX_TABLE);

            if (PRE_ALLOCATE_ID)
                clearSequences();
            
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal error in driver while cleaning database\n"
					+ "(" + e1 + ").\n"
					+ "Database may be corrupted. Try cleanDB() again.");
		}
		m_sysProperties = null;
		if ( prefixCache != null ) prefixCache.clear();
		prefixCache = null;
	}	

	protected List<String> getAllTables() {
		try {
			DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			String prefixMatch = stringToDBname(TABLE_NAME_PREFIX + "%");
			ResultSet rs = dbmd.getTables(null, null, prefixMatch, tableTypes);
            List<String> tables = new ArrayList<String>() ;
            while(rs.next())
                tables.add(rs.getString("TABLE_NAME"));
            rs.close() ;
            return tables ; 
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal SQL error in driver - " + e1);
		}
	}
	
	/**
	 * Drop all Jena-related sequences from database, if necessary.
	 * Override in subclass if sequences must be explicitly deleted.
	 */
	public void clearSequences() {
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
				logger.warn("Unable to drop sequence " + seqName, e);
			}
		}
	}

	/**
	 * Check database and see if named sequence exists.
	 * @param seqName
	 */
	public boolean sequenceExists(String seqName) {
		Object[] args = {seqName};
		ResultSet rs = null;
		boolean result = false;
		PreparedStatement ps=null;
		try {
			String op = "SelectSequenceName";
			ps = m_sql.getPreparedSQLStatement(op);
			ps.setString(1,seqName);
			rs = ps.executeQuery();
			result = rs.next();
		} catch (Exception e) {
			logger.error("Unable to select sequence " + seqName, e);
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e1) {
					throw new RDFRDBException("Failed to get last inserted ID: " + e1);
				}
			if(ps!=null)m_sql.returnPreparedSQLStatement(ps);
		}
		return result;
	}

	/**
	 * Check database and see if named sequence exists.
	 * @param seqName
	 */
	public List<String> getSequences() {
		List<String> results =  new ArrayList<String>(10);
		Object[] args = {};
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			String opname = "SelectJenaSequences";
			ps = m_sql.getPreparedSQLStatement(opname, TABLE_NAME_PREFIX);
		    rs = ps.executeQuery();
		    while (rs.next()) results.add( rs.getString(1) );
            //rs.close();   //Removed after jena 2.4. 
		} catch (Exception e) {
			logger.error("Unable to select Jena sequences: ", e);
		} finally {
			if(rs != null)
                try {
                    rs.close();
                } catch (SQLException e1) {
                	throw new RDFRDBException("Failed to get last inserted ID: " + e1);
                }
           if(ps!=null)m_sql.returnPreparedSQLStatement(ps);
		}
		return results;
	}
	
	/**
	 * Create a table for storing asserted or reified statements.
	 * 
	 * @param graphId the graph which the table is created.
	 * @param isReif true if table stores reified statements.
	 * @return the name of the new table 
	 * 
	 */
	@Override
    public String createTable( int graphId, boolean isReif) { 	
		String opname = isReif ? "createReifStatementTable" : "createStatementTable";
		int i = 0;
		String params[];
		while ( true ) {
			params = getCreateTableParams(graphId, isReif);
			try {
				m_sql.runSQLGroup(opname, params);
				break;
			} catch (SQLException e) {
				i++;
				if ( i > 5 ) {
					logger.warn("Problem creating table", e);
					throw new RDFRDBException("Failed to create table: " + params[0], e);
				}
			}
		}
		return params[0];
	}


	/**
	 * Delete a table.
	 * 
	 * @param tableName the name of the table to delete.	 * 
	 */
	@Override
    public void deleteTable( String tableName ) {
		
		String opname = "dropTable";
		PreparedStatement ps = null;
		try {         			
			ps = m_sql.getPreparedSQLStatement(opname, tableName);
			ps.executeUpdate();
			return;
		} catch (Exception e1) {
			throw new RDFRDBException("Failed to delete table ", e1);
		}finally {
			if(ps!=null)m_sql.returnPreparedSQLStatement(ps);
		}
	}



	/**
	 * Throws an UnsupportedOperation exception.
	 * 
	 * @param opName name of the operation that's not supported.
	 */
	private void notSupported(String opName)
		{ throw new UnsupportedOperationException(opName); }
		

	protected static final int xactBegin = 0;
	protected static final int xactCommit = 1;
	protected static final int xactAbort = 2;
	protected static final int xactIsActive = 3;
	protected static final int xactAutoOff = 4;
	protected static final int xactAutoOn = 5;
    protected static final int xactBeginIfNone = 6;

	
	/**
	 * Perform a transaction operation.  For begin/commit/abort,
	 * return true if success, false if fail. for xactIsActive,
	 * return true if this driver has an active transaction,
	 * else return false.
     * for beginIfNone, if there is a transaction running, return false, otherwise
     * 
	 */
	protected synchronized boolean xactOp(int op) throws RDFRDBException {
		try { return xaxtOpRaw( op ); }
		catch (SQLException e) { throw new JenaException( "Transaction support failed: ", e );
		}
	}

    private boolean xaxtOpRaw( int op ) throws SQLException
        {
        boolean ret = true;
        if (op == xactBegin) {
        	// start a transaction
        	// always return true
        	if (!inTransaction) {
        		xactBegin();
        		inTransaction = true;
        	}
        } else if (op == xactBeginIfNone) {
            if (inTransaction)
                ret = false;
            else {
                xactBegin();
                inTransaction = true; }
        } else if (op == xactCommit) {
        	// commit a transaction
        	// always return true
        	if (inTransaction) {
        		xactCommit();
        		inTransaction = false;
        	}
        } else if (op == xactAbort) {
        	// rollback a transaction
        	// always return true
        	if (inTransaction) {
        		xactAbort();
        		inTransaction = false;
        	}
        } else if (op == xactIsActive) {
        	// return true if xact is active, else false
        	ret = inTransaction;
        } else if (op == xactAutoOff) {
        	// disable autocommit
        	// return true if autocommit is on, else false
        	// begins a new transaction
        	Connection c = m_sql.getConnection();
        	ret = c.getAutoCommit();
        	if ( ret )
        		xactBegin();
        	inTransaction = true;			
        } else if (op == xactAutoOn) {
        	// enable autocommit
        	// always return true
        	if ( inTransaction )
        		throw new JenaException("Can't enable AutoCommit in middle of existing transaction");
        	Connection c = m_sql.getConnection();
        	c.setAutoCommit(true);
        	ret = true;
        } else
        	throw new JenaException("Unknown transaction operation: " + op);
        return ret;
        }

	private void xactBegin() throws RDFRDBException {
		try {
			Connection c = m_sql.getConnection();
			try {
				if (c.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
					c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				}
				if (c.getAutoCommit()) {
					c.setAutoCommit(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Starting a transaction could require us to lose any
			// cached prepared statements
			// for some jdbc drivers, currently I think all the drivers
			// we use are safe and
			// is a major performance hit so commented out for now.
			//m_sql.flushPreparedStatementCache();
		} catch (SQLException e) {
			throw new JenaException("Transaction begin failed: ", e);
		}
	}
	
	private void xactAbort() throws RDFRDBException {
		try {
			Connection c = m_sql.getConnection();
			c.rollback();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			throw new JenaException("Transaction rollback failed: ", e);
		}
	}
	
	private void xactCommit() throws RDFRDBException {
		try {
			Connection c = m_sql.getConnection();
			c.commit();
			try {
				c.setAutoCommit(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// not sure why read_uncommitted is set, below. commented
			// out by kw.
			// c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		} catch (SQLException e) {
			throw new JenaException("Transaction commit failed: ", e);
		}
	}

        
	/**
	 * If the underlying database connection supports transactions, turn
	 * autocommit off, then begin a new transaction. Note that transactions are
	 * associated with connections, not with Models. This
	 */
	@Override
    public synchronized void begin() throws RDFRDBException {
		if (transactionsSupported()) {
			xactOp(xactBegin);
		} else {
			notSupported("begin transaction");
		}
	}

	/**
	 * If the underlying database connection supports transactions, call
	 * commit(), then turn autocommit on.
	 */
	@Override
    public void commit() throws RDFRDBException {
		if (transactionsSupported()) {
			xactOp(xactCommit);
		} else {
			notSupported("commit transaction");
		}
	}

	/**
	 * If underlying database connection supports transactions, call abort() on
	 * the connection, then turn autocommit on.
	 */
	@Override
    public synchronized void abort() throws RDFRDBException {
		if (transactionsSupported()) {
			xactOp(xactAbort);
		} else {
			notSupported("abort transaction");
		}
	}
        

        
	/**
	 * Return a string identifying underlying database type.
	 *
	 */
	@Override
    public String getDatabaseType() {
		return(DATABASE_TYPE);
	}

	/**
	 * Returns true if the underlying database supports transactions.
	 */
	@Override
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
				logger.error("SQL Exception caught ", e);
			}
		}
		return (false);
			
		}

    /**
     * Close the driver 
     * Nothing to do for now.
     * 
     * @throws RDFDBException if there is an access problem
     */

    @Override
    public void close() throws RDFRDBException
    { }
    
 	/*
	 * The following routines are responsible for encoding nodes
	 * as database structures. For each node type stored (currently,
	 * literals, URI, blank), there are two possible encodings
	 * depending on the node size. Small nodes may be stored
	 * within a statement table. If the node is long (will not
	 * fit within the statement table), it is be stored in a
	 * separate table for that node type.
	 * 
	 * In addition, for resources (URI, blank nodes), the URI
	 * may be optionally compressed. Below, the possibilites
	 * are enumerated.
	 * 
	 * Literal Encoding in Statement Tables
	 * 	Short Literal:	Lv:[langLen]:[datatypeLen]:[langString][datatypeString]value[:]
	 * 	Long Literal:	Lr:dbid
	 * Literal Encoding in Long Literal Table
	 * 	Literal:		Lv:[langLen]:[datatypeLen]:[langString][datatypeString]head[:] hash tail
	 * 
	 * Comments:
	 * 		L indicates a literal
	 * 		v indicates a value
	 * 		r indicates a reference to another table
	 * 		: is used as a delimiter. note that MySQL trims trailing white space for
	 * 			certain VARCHAR columns so an extra delimiter is appended when necessary
	 * 			for those columns. it is not required for dbid, however. 
	 * 		dbid references the long literal table
	 * 		langLen is the length of the language identifier for the literal
	 * 		langString is the language identifier
	 * 		datatypeLen is the length of the datatype for the literal
	 * 		datatypeString is the datatype for the literal
	 * 		value is the lexical form of the string
	 * 		head is a prefix of value that can be indexed
	 * 		hash is the CRC32 hash value for the tail
	 * 		tail is the remainder of the value that cannot be indexed
	 * 		
	 * 
	 * 
	 * URI Encoding in Statement Tables
	 * 	Short URI:	Uv:[pfx_dbid]:URI[:]
	 * 	Long URI:	Ur:[pfx_dbid]:dbid
	 * URI Encoding in Long URI Table
	 * 	URI:		Uv:head[:] hash tail
	 * 
	 * Comments:
	 * 		U indicates a URI
	 * 		pfx_dbid references the prefix table. if the prefix is too
	 * 			short (i.e., the length of the prefix is less than
	 * 			URI_COMPRESS_LENGTH), the URI is not compressed and
	 * 			pfx_dbid is null.
	 * 		URI is the complete URI
	 * 		other notation same as for literal encoding
	 * 
	 * Blank Node Encoding in Statement Tables
	 * 	Short URI:	Bv:[pfx_dbid]:bnid[:]
	 * 	Long URI:	Br:[pfx_dbid]:dbid
	 * Blank Encoding in Long URI Table
	 * 	URI:		Bv:head[:] hash tail
	 * 
	 * Comments:
	 * 		B indicates a blank node
	 * 		bnid is the blank node identifier
	 * 		other notation same as above
	 * 		Note: currently, blank nodes are always stored uncompressed (pfix_dbid is null). 
	 *
	 * Variable Node Encoding in Statement Tables
	 * 	Variable Node:	Vv:name
	 * 
	 * Comments:
	 * 		V indicates a variable node
	 * 		v indicates a value
	 * 		name is the variable name
	 * 		Note: the length must be less than LONG_OBJECT_LENGTH
	 * 
	 * ANY Node Encoding in Statement Tables
	 * 	Variable Node:	Av:
	 *  
	 * Prefix Encoding in Prefix Table
	 * 	Prefix:	Pv:val[:] [hash] [tail]
	 * 
	 * Comments:
	 * 		P indicates a prefix
	 * 		other notation same as above
	 * 		hash and tail are only required for long prefixes.
	 * 
	 */
	 
	 
	 
	protected static String RDBCodeURI = "U";
	protected static String RDBCodeBlank = "B";
	protected static String RDBCodeLiteral = "L";
	protected static String RDBCodeVariable = "V";
	protected static String RDBCodeANY = "A";
	protected static String RDBCodePrefix = "P";
	protected static String	RDBCodeValue = "v";
	protected static String RDBCodeRef = "r";
	protected static String RDBCodeDelim = ":";
	protected static char RDBCodeDelimChar = ':';
	protected static String RDBCodeInvalid = "X";


		
    
	/**
	* Convert a node to a string to be stored in a statement table.
	* @param Node The node to convert to a string. Must be a concrete node.
	* @param addIfLong If the node is a long object and is not in the database, add it.
	* @return the string or null if failure.
	*/
	@Override
    public String nodeToRDBString ( Node node, boolean addIfLong ) throws RDFRDBException {
		String res = null;
		if ( node.isURI() ) {
			String uri = new String(((Node_URI) node).getURI());
			
//			if ( uri.startsWith(RDBCodeURI) )
//				throw new RDFRDBException ("URI Node looks like a blank node: " + uri );
			
			// TO DO: need to write special version of splitNamespace for rdb.
			//		or else, need a guarantee that splitNamespace never changes.
			//		the problem is that if the splitNamespace algorithm changes,
			//		then URI's may be encoded differently. so, URI's in existing
			//		databases may become inaccessible.
			int pos = 0;
			boolean noCompress;
			String pfx;
			String qname;
			if ( URI_COMPRESS == true ) {
				pos = dbSplitNamespace(uri);
				if ( uri.startsWith(DB.uri) )
					noCompress = true;
				else
					noCompress = (pos == uri.length()) || (pos <= URI_COMPRESS_LENGTH);
			} else
				noCompress = true;
			if ( noCompress ) {
				pfx = RDBCodeDelim + RDBCodeDelim;
				qname = uri;
			} else {
				// see if it's cached
				DBIDInt pfxid = URItoPrefix(uri, pos, addIfLong);
				if ( pfxid == null ) return res;
				pfx = RDBCodeDelim + (pfxid).getIntID() + RDBCodeDelim;
				qname = uri.substring(pos);
			}
			int encodeLen = RDBCodeURI.length() + 1 + pfx.length() + EOS_LEN;
			boolean URIisLong = objectIsLong(encodeLen,qname);
			if ( URIisLong ) {
				int	dbid;
				// belongs in URI table
				DBIDInt URIid = getURIID(qname,addIfLong);
				if ( URIid == null ) return res;
				dbid = URIid.getIntID();
				res = new String(RDBCodeURI + RDBCodeRef + pfx + dbid);
			} else {
				res = RDBCodeURI + RDBCodeValue + pfx + qname + EOS;
			}
		} else if ( node.isLiteral() ){
			Node_Literal litNode = (Node_Literal) node;
			String lval = litNode.getLiteralLexicalForm();
			String lang = litNode.getLiteralLanguage();
			String dtype = litNode.getLiteralDatatypeURI();
			String ld = litLangTypeToRDBString(lang,dtype);
			int encodeLen = RDBCodeLiteral.length() + 2 + ld.length() + EOS_LEN;
			boolean litIsLong = objectIsLong(encodeLen,lval);		
            
			if ( litIsLong ) {
				int	dbid;
                
                //System.err.println("Long literal("+lval.length()+" => "+encodeLen+")") ;
                
				// belongs in literal table
				DBIDInt lid = getLiteralID(litNode,addIfLong);
				if ( lid == null ) return res;
				dbid = lid.getIntID();
				res = new String(RDBCodeLiteral + RDBCodeRef + RDBCodeDelim + dbid);
			} else {
				res = new String(RDBCodeLiteral + RDBCodeValue + RDBCodeDelim + ld + lval + EOS);
			}    		
		} else if ( node.isBlank() ) {
			String bnid = node.getBlankNodeId().toString();
			String delims = "::";
			int encodeLen = RDBCodeBlank.length() + 1 + delims.length() + EOS_LEN;
			boolean BisLong = objectIsLong(encodeLen,bnid);
			if ( BisLong ) {
				int	dbid;
				// belongs in URI table
				DBIDInt URIid = getBlankID(bnid,addIfLong);
				if ( URIid == null ) return res;
				dbid = URIid.getIntID();
				res = new String(RDBCodeBlank + RDBCodeRef + delims + dbid);
			} else {
				res = new String(RDBCodeBlank + RDBCodeValue + delims + bnid + EOS);
			}
			
		} else if ( node.isVariable() ){
			String name = ((Node_Variable)node).getName();
			int len = name.length();
			if ( (len + 3 + EOS_LEN) > LONG_OBJECT_LENGTH )
				throw new JenaException ("Variable name too long: " + name );
			res = RDBCodeVariable + RDBCodeValue + RDBCodeDelim + name + EOS;
		} else if ( node.equals(Node.ANY) ) {
			res = RDBCodeANY +  RDBCodeValue + RDBCodeDelim;
		} else {
			throw new RDFRDBException ("Expected Concrete Node, got " + node.toString() );	
		}
		return res;
	}
	
	/**
	* Convert an RDB string to the node that it encodes. Return null if failure.
	* @param RDBstring The string to convert to a node.
	* @return The node or null if failure.
	*/
	@Override
    public Node RDBStringToNode ( String RDBString ) throws RDFRDBException {	
		Node res = null;
		int len = RDBString.length();
		if ( len < 3 ) 
			throw new RDFRDBException("Bad RDBString Header: " + RDBString);
		String nodeType = RDBString.substring(0,1);
		String valType = RDBString.substring(1,2);
		if ( (!(valType.equals(RDBCodeRef) || valType.equals(RDBCodeValue))) ||
				(RDBString.charAt(2) != RDBCodeDelimChar) )
				throw new RDFRDBException("Bad RDBString Header: " + RDBString);

		int pos = 3;
		int npos;
		
		if ( nodeType.equals(RDBCodeURI) ) {
			ParseInt pi = new ParseInt(pos);
			String prefix = "";
			RDBStringParseInt(RDBString, pi, false);
			if ( pi.val != null ) {
				if ( URI_COMPRESS == false )
					throw new RDFRDBException("Bad URI: Prefix Compression Disabled: " + RDBString);
				prefix = IDtoPrefix(pi.val.intValue());
				if ( prefix == null )
					throw new RDFRDBException("Bad URI Prefix: " + RDBString);
			}
			pos = pi.pos + 1;
			String qname;
			if ( valType.equals(RDBCodeRef) ) {
				qname = IDtoURI(RDBString.substring(pos));
				if ( qname == null )
					throw new RDFRDBException("Bad URI: " + RDBString);
			} else
				qname = RDBString.substring(pos,len - EOS_LEN);

			res = Node.createURI(prefix + qname);
			
		} else if ( nodeType.equals(RDBCodeLiteral) ) {
			res = RDBLiteralStringToLiteralNode( RDBString, len, valType, pos );	 
			
		} else if ( nodeType.equals(RDBCodeBlank) ) {
			String bstr = null;
			if ( valType.equals(RDBCodeValue) ) {
				bstr = RDBString.substring(4,len-EOS_LEN);
			} else {
				bstr = IDtoBlank(RDBString.substring(4));
				if ( bstr == null )
					throw new RDFRDBException("Bad URI: " + RDBString);			
			}
			res = Node.createAnon( new AnonId (bstr) );
						
		} else if ( nodeType.equals(RDBCodeVariable) ) {
			String vname = RDBString.substring(3,len-EOS_LEN);
			res = Node.createVariable(vname);
			
		} else if ( nodeType.equals(RDBCodeANY) ) {
			res = Node.ANY;
			
		} else
			throw new RDFRDBException ("Invalid RDBString Prefix, " + RDBString );	
		return res;
	}

    /**
        Answer a literal Node constructed according to the RDB String.
        
     	@param RDBString
     	@param len
     	@param valType
     	@param pos
     	@return
    */
    protected Node RDBLiteralStringToLiteralNode( String RDBString, int len, String valType, int pos )
        {
        ParseInt pi = new ParseInt( pos );
        String litString = null;
        if ( valType.equals(RDBCodeRef) ) {
        	RDBStringParseInt(RDBString,pi,true);
        	if ( pi.val != null )
        		litString = IDtoLiteral(pi.val.intValue());
        	if ( litString == null )
        		throw new RDFRDBException("Bad Literal Reference: " + RDBString);
        } else
        	litString = RDBString.substring(pos,len-EOS_LEN);
        len = litString.length();
        pi.pos = 0;
        RDBStringParseInt(litString, pi, false);
        int langLen = pi.val == null ? 0 : pi.val.intValue(); 
        pi.pos = pi.pos + 1;
        RDBStringParseInt(litString, pi, false);	
        int dtypeLen = pi.val == null ? 0 : pi.val.intValue();
        pos = pi.pos + 1;	
        if ( (pos + langLen + dtypeLen) > len )
        		throw new RDFRDBException("Malformed Literal: " + litString);	
        String lang = litString.substring( pos, pos + langLen );
        pos = pos + langLen;
        String dtype = litString.substring( pos, pos + dtypeLen );        
        String val = litString.substring( pos + dtypeLen );
        return createLiteral( val, lang, dtype );
        }

    /**
        Answer a Node literal with the indicated lexical form, language,
        and datatype. If the datatype is the empty string, there is no
        datatype. If the language is the empty string, there is no language.
        
     	@param val
     	@param lang
     	@param dtype
     	@return
    */
    protected Node createLiteral( String val, String lang, String dtype ) {
        if (dtype.equals( "" )) {
            return Node.createLiteral( val, lang, null );
        } else {
        	RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtype);
            return Node.createLiteral( val, lang, dt );
        }
    }
	
	/** This is cuurently a copy of Util.splitNamespace.  It was
	 * copied rather than used directly for two reasons. 1) in the
	 * future it may be desirable to use a different split algorithm
	 * for persistence. 2) the util version could change at any time,
	 * which would render existing databases inaccessible. having a
	 * copy allows the db version to evolve in a controlled way.
	 * 
	 * Given an absolute URI, determine the split point between the namespace part
	 * and the localname part.
	 * If there is no valid localname part then the length of the
	 * string is returned.
	 * The algorithm tries to find the longest NCName at the end
	 * of the uri, not immediately preceeded by the first colon
	 * in the string.
	 * @param uri
	 * @return the index of the first character of the localname
	 */
	public static int dbSplitNamespace(String uri) {
		char ch;
		int lg = uri.length();
		if (lg == 0)
			return 0;
		int j;
		int i;
		for (i = lg - 1; i >= 1; i--) {
			ch = uri.charAt(i);
			if (!XMLChar.isNCName(ch))
				break;
		}
		for (j = i + 1; j < lg; j++) {
			ch = uri.charAt(j);
			if (XMLChar.isNCNameStart(ch)) {
				if (uri.charAt(j - 1) == ':'
					&& uri.lastIndexOf(':', j - 2) == -1)
					continue; // split "mailto:me" as "mailto:m" and "e" !
				else
					break;
			}
		}
		return j;
	}

	
	class ParseInt {
		int	pos;
		Integer val;	
		ParseInt(int p) {pos = p;}
	}

	protected void RDBStringParseInt ( String RDBString, ParseInt pi, boolean toEnd ) {
		int npos = toEnd ? RDBString.length() : RDBString.indexOf(RDBCodeDelimChar,pi.pos);
		if ( npos < 0 ) {
			throw new RDFRDBException("Bad RDB String: " + RDBString);
		}
		String intStr = RDBString.substring(pi.pos,npos);
		pi.pos = npos;
		if ( intStr.equals("") )
			pi.val = null;
		else try {
			pi.val = new Integer(intStr);
		} catch (NumberFormatException e1) {
			throw new RDFRDBException("Bad RDB String: " + RDBString);
		} 
		return;
	}
	
	

	DBIDInt URItoPrefix ( String uri, int pos, boolean add ) {
		DBIDInt res;
		Object key = prefixCache.getByValue(uri.substring(0,pos));
		if ( key == null ) {
			RDBLongObject	lobj = PrefixToLongObject(uri,pos);
			res = getLongObjectID(lobj, PREFIX_TABLE, add);
			if ( res != null )
				prefixCache.put(res,uri.substring(0,pos));
		} else
			res = (DBIDInt) key;
		return res;
	}
	
	protected RDBLongObject PrefixToLongObject ( String prefix, int split ) {
		RDBLongObject	res = new RDBLongObject();
		int				headLen;
		int				avail;

		res.head = RDBCodePrefix + RDBCodeValue + RDBCodeDelim;
		headLen = res.head.length();
		avail = INDEX_KEY_LENGTH - (headLen + EOS_LEN);
		if ( split > avail ) {
			res.head = res.head + prefix.substring(0,avail);
			res.tail = prefix.substring(avail,split);
			res.hash = stringToHash(res.tail);
		} else {
			res.head = res.head + prefix.substring(0,split);
			res.tail = "";
		}
		res.head = res.head + EOS;
		return res;	
	}

	/**
	* Encode a literal node's lang and datatype as a string of the
	* form ":[langLen]:[datatypeLen]:[langString][dataTypeString]"
	* @return the string.
	*/
	public String litLangTypeToRDBString ( String lang, String dtype ) throws RDFRDBException {
		String res = RDBCodeDelim;
		res = ((lang == null) ? "" : Integer.toString(lang.length())) + RDBCodeDelim;
		res = res + ((dtype == null) ? "" : Integer.toString(dtype.length())) + RDBCodeDelim;
		res = res + (lang == null ? "" : lang) + (dtype == null ? "" : dtype);
		return res;
	}
	
	/**
	* Check if an object is long, i.e., it exceeds the length
	* limit for storing in a statement table.
	* @return true if literal is long, else false.
	*/
	protected boolean objectIsLong ( int encodingLen, String objAsString ) {
		return ( (encodingLen + objAsString.length()) > LONG_OBJECT_LENGTH);
	}
	
	class RDBLongObject {
		String		head;		/* prefix of long object that can be indexed */
		long		hash;		/* hash encoding of tail */
		String		tail;		/* remainder of long object */
	}
	
	protected RDBLongObject literalToLongObject ( Node_Literal node ) {
		RDBLongObject	res = new RDBLongObject();
		int				headLen;
		int				avail;
		String 			lang = node.getLiteralLanguage();
		String 			dtype = node.getLiteralDatatypeURI();
		String 			val = node.getLiteralLexicalForm();
		String			langType = litLangTypeToRDBString(lang,dtype);

		res.head = RDBCodeLiteral + RDBCodeValue + RDBCodeDelim + langType;
		headLen = res.head.length();
		avail = INDEX_KEY_LENGTH - (headLen + EOS_LEN);
		if ( val.length() > avail ) {
			res.head = res.head + val.substring(0,avail);
			res.tail = val.substring(avail);
			res.hash = stringToHash(res.tail);
		} else {
			res.head = res.head + val;
			res.tail = "";
		}
		res.head = res.head + EOS;
		return res;
	}
	
		
	protected long stringToHash ( String str ) {
		CRC32 checksum = new CRC32();
		checksum.update(str.getBytes());
		return checksum.getValue();
	}
	
	/**
	 * Return the database ID for the URI, if it exists
	 */
	public DBIDInt getBlankID(String bstr, boolean add) throws RDFRDBException {
		RDBLongObject	lobj = URIToLongObject (bstr,RDBCodeBlank);
		return getLongObjectID(lobj, LONG_URI_TABLE, add);
	}
	
	/**
	 * Return the database ID for the URI, if it exists
	 */
	public DBIDInt getURIID(String qname, boolean add) throws RDFRDBException {
		RDBLongObject	lobj = URIToLongObject (qname,RDBCodeURI);
		return getLongObjectID(lobj, LONG_URI_TABLE, add);
	}

	protected RDBLongObject URIToLongObject ( String qname, String code ) {
		RDBLongObject	res = new RDBLongObject();
		int				headLen;
		int				avail;

		res.head = code + RDBCodeValue + RDBCodeDelim;
		headLen = res.head.length();
		avail = INDEX_KEY_LENGTH - (headLen + EOS_LEN);
		if ( qname.length() > avail ) {
			res.head = res.head + qname.substring(0,avail);
			res.tail = qname.substring(avail);
			res.hash = stringToHash(res.tail);
		} else {
			res.head = res.head + qname;
			res.tail = "";
		}
		res.head = res.head + EOS;
		return res;	
	}
			
	
	/**
	 * Return the database ID for the literal, if it exists
	 */
	public DBIDInt getLiteralID(Node_Literal lnode, boolean add) throws RDFRDBException {
		RDBLongObject	lobj = literalToLongObject (lnode);
		return getLongObjectID(lobj, LONG_LIT_TABLE, add);
	}
			
	public DBIDInt getLongObjectID(RDBLongObject lobj, String table, boolean add) throws RDFRDBException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			String opName = "getLongObjectID";
			if ( lobj.tail.length() > 0 ) 
				opName += "withChkSum";
			ps = m_sql.getPreparedSQLStatement(opName, table); 
			ps.setString(1,lobj.head);
			if ( lobj.tail.length() > 0 ) 
				ps.setLong(2, lobj.hash);
			
			rs = ps.executeQuery();
			DBIDInt result = null;
			if (rs.next()) {
				result = wrapDBID(rs.getObject(1));
			} else {
				if ( add )
					result = addRDBLongObject(lobj, table);
			}

			return result;
		} catch (SQLException e1) {
			// /* DEBUG */ System.out.println("Literal truncation (" + l.toString().length() + ") " + l.toString().substring(0, 150));
			throw new RDFRDBException("Failed to find literal", e1);
		}finally {
			if(rs != null)
                try {
                    rs.close();
                } catch (SQLException e1) {
                	throw new RDFRDBException("Failed to get last inserted ID: " + e1);
                }
             if(ps!=null)m_sql.returnPreparedSQLStatement(ps);
		}
	}
 
	/**
	 * Insert a long object into the database.  
	 * This assumes the object is not already in the database.
	 * @return the db index of the added literal 
	 */
	public DBIDInt addRDBLongObject(RDBLongObject lobj, String table) throws RDFRDBException {
		PreparedStatement ps = null;

//        // Because the long object bound has been reset to less than the actual table allocation.
//        if ( lobj.tail == null || lobj.tail.equals("") )
//            System.err.println("Unexpected : empty tail") ;
        
        
		try {
			int argi = 1;
			String opname = "insertLongObject";
            // If not pre-allocated , 1-Head / 2-Hash / 3-Tail
            // If pre-allocated , 1-Id, / 2-Head / 3-Hash [/ 4-Tail]
			ps = m_sql.getPreparedSQLStatement(opname, table);
			int dbid = 0; // init only needed to satisfy java compiler
			if ( PRE_ALLOCATE_ID ) {
				dbid = getInsertID(table);
				ps.setInt(argi++,dbid);
			} 
			 ps.setString(argi++, lobj.head);
             
             // Do the tail - this can be a large text-holding column, or a binary column
             // depending on the database.
             
             setLongObjectHashAndTail(ps, argi, lobj) ;
             argi += 2 ;        // Hash and tail.

			ps.executeUpdate();
			if ( !PRE_ALLOCATE_ID ) dbid = getInsertID(table);
			return wrapDBID(new Integer(dbid));
            
		} catch (Exception e1) {
			/* DEBUG */ System.out.println("Problem on long object (l=" + lobj.head + ") " + e1 );
			// System.out.println("ID is: " + id);
			throw new RDFRDBException("Failed to add long object ", e1);
		} finally {
			if(ps!=null)m_sql.returnPreparedSQLStatement(ps);
		}
	}
	
    // Common way to get the sequence ID, even though the SQL statements are quite different. 
    // MySQL is different (it overrides this in Driver_MySQL).
    
	@Override
    public int getInsertID(String tableName)
	{
	    DBIDInt result = null;
        PreparedStatement ps = null ;
	    try {
	        ps = m_sql.getPreparedSQLStatement("getInsertID",tableName);
	        ResultSet rs = ps.executeQuery();
	        if (rs.next()) {
	            result = wrapDBID(rs.getObject(1));
	        } else
	            throw new RDFRDBException("No insert ID");
	    } catch (SQLException e) {
	        throw new RDFRDBException("Failed to insert ID: " + e);
	    } finally { 
            if ( ps != null )
                m_sql.returnPreparedSQLStatement(ps) ;
        }
	    return result.getIntID();
	}

    
    // Different ways of inserting the tail value
    // 1/ As a text field, using .setString and letting JDBC encode the characters
    // 2/ As a binary field, using a BLOB of UTF-8 encoded bytes
    
    protected void setLongObjectHashAndTail(PreparedStatement ps, int argi, RDBLongObject lobj)
    throws SQLException
    {
        setLongObjectHashAndTail_Text(ps, argi, lobj) ;
    }
    
    protected void setLongObjectHashAndTail_Text(PreparedStatement ps, int argi, RDBLongObject lobj)
    throws SQLException
    {
        if ( lobj.tail.length() > 0 ) {
            ps.setLong(argi++, lobj.hash);
            ps.setString(argi++, lobj.tail);
        } else {
            ps.setNull(argi++,java.sql.Types.BIGINT);
            ps.setNull(argi++,java.sql.Types.VARCHAR);     
        }

    }
    
    protected void setLongObjectHashAndTail_Binary(PreparedStatement ps, int argi, RDBLongObject lobj)
    throws SQLException
    {
        if ( lobj.tail.length() > 0 )
            ps.setLong(argi++, lobj.hash);
        else
            ps.setNull(argi++,java.sql.Types.BIGINT);
        
        byte[] b = null ;
        try { b = lobj.tail.getBytes("UTF-8") ; }
        catch (UnsupportedEncodingException ex)
        {
            // Can't happen - UTF-8 is required by Java.
            throw new RDFRDBException("No UTF-8 encoding (setLongObjectHashAndTail_Binary)") ;
        }
        //System.out.println("bytes in : "+b.length) ;
        ps.setBytes(argi++, b) ;
    }
    
	/**
	 * Return the prefix string that has the given prefix id.
	 * @param prefixID - the dbid of the prefix.
	 * @return the prefix string or null if it does not exist.
	 */
	protected String IDtoPrefix ( int prefixID ) {
		// check cache
		DBIDInt dbid = new DBIDInt(prefixID);
		String res = prefixCache.get(dbid);
		if ( res != null)
			return res;
		else {
            res = IDtoString ( prefixID, PREFIX_TABLE, RDBCodePrefix);
            prefixCache.put(dbid,res);
			return res;
        }
	}
	
	/**
	* Return the Blank node string that has the given database id.
	* @param bnID - the dbid of the blank node, as a string.
	* @return the Blank node string or null if it does not exist.
	*/
	protected String IDtoBlank(String bnID) {
		return IDtoString(bnID, LONG_URI_TABLE, RDBCodeBlank);
	}
	/**
		* Return the URI string that has the given database id.
		* @param uriID - the dbid of the uri, as a string.
		* @return the uri string or null if it does not exist.
		*/
	protected String IDtoURI(String uriID) {
		return IDtoString(uriID, LONG_URI_TABLE, RDBCodeURI);
	}

	/**
	* Return the long literal string that has the given database id.
	* @param litID - the dbid of the literal..
	* @return the long literal string or null if it does not exist.
	*/
	protected String IDtoLiteral ( int litID ) {
		return IDtoString ( litID, LONG_LIT_TABLE, RDBCodeLiteral);
	}
	

	
	protected String IDtoString ( String dbidAsString, String table, String RDBcode ) {
		int	dbID;
		String res = null;
		try {
			dbID = Integer.parseInt(dbidAsString);
		} catch (NumberFormatException e1) {
			throw new RDFRDBException("Invalid Object ID: " + dbidAsString);
		}
		return IDtoString (dbID, table, RDBcode);
	}

	protected String IDtoString ( int dbID, String table, String RDBcode ) {
		String res = null;
		RDBLongObject lobj = IDtoLongObject(dbID, table);
		if ( lobj == null )
			throw new RDFRDBException("Invalid Object ID: " + dbID);
		// debug check
		if ( !lobj.head.substring(0,3).equals(RDBcode + RDBCodeValue + RDBCodeDelim) )
			throw new RDFRDBException("Malformed URI in Database: " + lobj.head);
		res = lobj.head.substring(3,lobj.head.length() - EOS_LEN);
		if ( lobj.tail != null )
			res = res + lobj.tail;	
		return res;
	}

	// Get whatever is strong under a long id. 
	protected RDBLongObject IDtoLongObject ( int dbid, String table ) {
		RDBLongObject	res = null;
		ResultSet rs=null;
		PreparedStatement ps = null;
		try {
			String opName = "getLongObject";
			ps = m_sql.getPreparedSQLStatement(opName, table);
			ps.setInt(1, dbid);
			rs = ps.executeQuery();
			if (rs.next()) {
                res = new RDBLongObject();
				res.head = rs.getString(1);
				int colType = rs.getMetaData().getColumnType(2) ;
                switch (colType)
                {
                    case Types.VARCHAR:
                    case Types.LONGVARCHAR:
                    //case Types.LONGNVARCHAR:        // JDBC 4 - Types.LONGNVARCHAR -16 (const in Java 1.6)
                    case -16:                           
                    //case Types.NVARCHAR:            // JDBC 4 - Types.NVARCHAR -9 (const in Java 1.6)
                    case -9: 
                    case Types.CHAR:
                        res.tail = rs.getString(2) ;
                        if ( res.tail == null )
                            res.tail = "" ;
                        break ;
                    case Types.BLOB:
                    case Types.LONGVARBINARY:
                    //case Types.NCLOB:               // JDBC 4 - Types.NCLOB 2011 (const in Java 1.6)
                    case 2011:                          
                        byte[] b2 = rs.getBytes(2) ;
                        if ( b2 == null )
                            // The meaning of "" is mixed in SQL. 
                            // Should not happen - we never store empty strings it the tail
                            res.tail = "" ;
                        else
                            try
                            { res.tail = new String(b2, 0, b2.length, "UTF-8") ; }
                            catch (UnsupportedEncodingException ex)
                            { ex.printStackTrace(); }
                        break;
                    default:
                        logger.error("Long object is of unexpected SQL type: "+rs.getMetaData().getColumnType(2)) ;
                        throw new RDFRDBException("Long object is of unexpected SQL type: "+rs.getMetaData().getColumnType(2));
                }
			}
		} catch (SQLException e1) {
			// /* DEBUG */ System.out.println("Literal truncation (" + l.toString().length() + ") " + l.toString().substring(0, 150));
			throw new RDFRDBException("Failed to find literal", e1);
		}finally {
			if(rs != null)
                try {
                    rs.close();
                } catch (SQLException e1) {
                	throw new RDFRDBException("Failed to get last inserted ID: " + e1);
                }
             if(ps!=null)m_sql.returnPreparedSQLStatement(ps);
		}		
		return res;
	}
	
	protected RDBLongObject IDtoLongObject ( String idAsString, String table ) {
		RDBLongObject res = null;
		int dbid;
		try {
			dbid = Integer.parseInt(idAsString);
		} catch (NumberFormatException e1) {
			throw new RDFRDBException("Invalid Object ID: " + idAsString);
		}
		return IDtoLongObject(dbid,table);
	}
    
 
	/**
	 * Convert the raw SQL object used to store a database identifier into a java object
	 * which meets the DBIDInt interface.
	 */
	public DBIDInt wrapDBID(Object id) throws RDFRDBException {
		if (id instanceof Number) {
			return new DBIDInt(((Number)id).intValue());
		} else if (id == null) {
			return null;
		} else {
			throw new RDFRDBException("Unexpected DB identifier type: " + id);
			//return null;
		}
	}
	
	@Override
    public String genSQLReifQualStmt () {
		return "Stmt = ?";
	}
	
	@Override
    public String genSQLReifQualAnyObj( boolean objIsStmt) {
		return "( Subj = ? OR Prop = ? OR Obj = ?" + (objIsStmt ? " OR HasType = " +
			QUOTE_CHAR + "T" + QUOTE_CHAR + " )" : " )");		
	}
	
	@Override
    public String genSQLReifQualObj ( char reifProp, boolean hasObj ) {
		String qual = "";
		if ( reifProp == 'T' ) {
			qual = "HasType = " + QUOTE_CHAR + "T" + QUOTE_CHAR;
		} else {
			String cmp = (hasObj ? " = ?" : " is not null");
			String col = null;
			if ( reifProp == 'S' ) col = "Subj";
			else if ( reifProp == 'P' ) col = "Prop";
			else if ( reifProp == 'O' ) col = "Obj";
			else throw new JenaException("Undefined reification property");
		
			qual = col + cmp;
		}
		return qual;	
	}
	
	protected String colidToColname ( char colid ) {
		if ( colid == 'G' ) return "GraphID";
		if ( colid == 'P' ) return "Prop";
		if ( colid == 'S' ) return "Subj";
		if ( colid == 'O' ) return "Obj";
		if ( colid == 'N' ) return "Stmt";
		if ( colid == 'T' ) return "HasType";
		throw new JenaException("Invalid column identifer: '" + colid + "\'");
	}
	
	protected String aliasToString ( int alias ) {
		return "A" + alias;
	}
	
	protected String colAliasToString ( int alias, char colid ) {
		return aliasToString(alias) + "." + colidToColname(colid);
	}

    /** Apply SQL escapes to a string */
    private String escapeQuoteSQLString(String str)
    {
        StringBuffer sBuff = new StringBuffer(str.length()+10) ;
        sBuff.append(QUOTE_CHAR) ;
        for ( int i = 0 ; i < str.length() ; i++ ) 
        {
            char ch = str.charAt(i) ;
            // Double up quotes
            if ( ch == QUOTE_CHAR )
                sBuff.append(QUOTE_CHAR) ;
            sBuff.append(ch) ;
        }
        sBuff.append(QUOTE_CHAR) ;
        return sBuff.toString() ;
    }
    
	/*
	 * there's a bug in the code below in that the literal is converted to
	 * a string BEFORE the query is run. consequently, there's a race
	 * condition. if the (long) literal is not in the database
	 * when the query is compiled but is added prior to running the
	 * query, then the query will (incorrectly) return no results.
	 * for now, we'll ignore this case and document it as a bug.
	 */
	
	@Override
    public String genSQLQualConst ( int alias, char pred, Node lit ) {
		String val = nodeToRDBString(lit, false);
		if ( val == null )
			// constant not in database.
			// should really optimize this and not
			// even run the query but ok for now.
			val = RDBCodeInvalid;
        String qval = escapeQuoteSQLString(val) ;
		return colAliasToString(alias,pred) + "=" + qval ;		
	}

	@Override
    public String genSQLReifQualConst ( int alias, char pred, Node lit ) {
		String val = "";
		if ( (pred == 'T') && (lit.equals(RDF.Nodes.Statement)) )
			val = "T";
		else
			val = nodeToRDBString(lit, false);
        String qval = escapeQuoteSQLString(val) ;
		return colAliasToString(alias,pred) + "=" + qval ;		
	}
	
	@Override
    public String genSQLQualParam( int alias, char pred ) {
		return colAliasToString(alias,pred) + "=?";			
	}

	@Override
    public String genSQLQualGraphId( int alias, int graphId ) {
		return colAliasToString(alias,'G') + "=" + graphId;			
	}

	@Override
    public String genSQLJoin( int lhsAlias, char lhsCol,
		int rhsAlias, char rhsCol ) {
			return colAliasToString(lhsAlias,lhsCol) + "=" +
			colAliasToString(rhsAlias,rhsCol);
	}

	@Override
    public String genSQLStringMatch( int alias, char col,
		String fun, String stringToMatch ) {
		boolean ignCase = 
		   fun.equals(ExpressionFunctionURIs.J_startsWithInsensitive) ||
		   fun.equals(ExpressionFunctionURIs.J_endsWithInsensitive) ||
           fun.equals(ExpressionFunctionURIs.J_containsInsensitive) ;
		boolean pfxMatch = 
		   fun.equals(ExpressionFunctionURIs.J_startsWith) ||
		   fun.equals(ExpressionFunctionURIs.J_startsWithInsensitive);
		String var = colAliasToString(alias,col);
		// generate string match operation for short literal or URI
		String qual = " ( " + genSQLStringMatchLHS(ignCase,var);
		qual += " " + genSQLStringMatchOp(ignCase,fun);
		qual += " " + genSQLStringMatchRHS(ignCase,pfxMatch,stringToMatch);
		// now match long URI or Bnode or, if object col, long literal
		qual += " " + genSQLOrKW() + genSQLStringMatchLHS(false,var);
		qual += " " + genSQLStringMatchOp(false,fun);
		qual += " " + genSQLStringMatchLong() + " )";
	
		return qual;
	}
	
	@Override
    public String genSQLStringMatchLHS( boolean ignCase, String var ) {
		return ignCase ? genSQLStringMatchLHS_IC(var): var;
	}

	public String genSQLStringMatchLong( ) {
		return QUOTE_CHAR + stringMatchAnyChar() + stringMatchLongObj() + 
				stringMatchAllChar() + QUOTE_CHAR;
	}

	@Override
    public String genSQLStringMatchOp( boolean ignCase, String fun ) {
		return ignCase ? genSQLStringMatchOp_IC(fun): 
		                 genSQLStringMatchOp(fun);
	}

	@Override
    public String stringMatchAllChar() { return "%"; }
	public String stringMatchAnyChar() { return "_"; }
	@Override
    public String stringMatchEscapeChar() { return "\\\\"; }
	public String stringMatchLongObj() { return "r"; }
	public String stringMatchShortObj() { return "v"; }

	@Override
    public String genSQLStringMatchRHS( boolean ignCase, boolean pfxMatch,
									String strToMatch ) {
		boolean isEscaped = stringMatchNeedsEscape(strToMatch);
		if ( isEscaped ) strToMatch = addEscape(strToMatch);
		// for now, don't optimize for prefix match
		/*
		strToMatch = pfxMatch ? strToMatch + stringMatchAllChar() : 
						stringMatchAllChar() + strToMatch;
		strToMatch = stringMatchAllChar() + strToMatch;
		strToMatch = nodeToRDBString(Node.createLiteral(strToMatch),false);
		if ( pfxMatch && STRINGS_TRIMMED ) 
			strToMatch = strToMatch.substring(0,strToMatch.length()-1);
		*/
		strToMatch = stringMatchAnyChar() + stringMatchShortObj() + 
				stringMatchAllChar() + strToMatch + stringMatchAllChar();
		strToMatch = QUOTE_CHAR + strToMatch + QUOTE_CHAR;
		String qual = ignCase ? genSQLStringMatchRHS_IC(strToMatch): strToMatch;
		if ( isEscaped ) qual += genSQLStringMatchEscape();

		return qual;
	}
	
	@Override
    public String genSQLStringMatchLHS_IC(String var) {
		return var;
	}

	@Override
    public String genSQLStringMatchRHS_IC(String strToMatch) {
		return strToMatch;
	}

	@Override
    public String genSQLStringMatchOp( String fun ) {
		return genSQLLikeKW();
	}

	@Override
    public String genSQLStringMatchOp_IC( String fun ) {
		return genSQLLikeKW();
	}
	
	@Override
    public boolean stringMatchNeedsEscape ( String strToMatch ) {
		return strToMatch.indexOf('_') >= 0;
	}

	@Override
    public String addEscape ( String strToMatch ) {
		int i = strToMatch.indexOf('_');
		return strToMatch.substring(0,i) + stringMatchEscapeChar() + 
					strToMatch.substring(i);
	}
	
	@Override
    public String genSQLStringMatchEscape() {
		return "";
	}
	
	@Override
    public String genSQLResList( int resIndex[], VarDesc[] binding ) {
		String resList = "";
		int i,j;
		for(i=0,j=0;i<binding.length;i++) {
			VarDesc b = binding[i];
			if ( !b.isArgVar() ) {
				// next result variable
				resList += (j>0?", ":"") + colAliasToString(b.alias,b.column);
				if ( j >= resIndex.length )
					throw new JenaException("Too many result columns");
				resIndex[j++] = b.mapIx;
			}
		}
		return resList;
	}
	
	@Override
    public String genSQLFromList( int aliasCnt, String table ) {
		int i;
		String resList = "";
		for(i=0;i<aliasCnt;i++) {
			resList += (i>0?", ":"") + table + " " + aliasToString(i);
		}
		return resList;

	}
	
	@Override
    public String genSQLLikeKW() {
		return "Like ";
	}

	@Override
    public String genSQLEscapeKW() {
		return "Escape ";
	}

	public String genSQLSelectKW() {
		return "Select ";
	}
	
	public String genSQLFromKW() {
		return "From ";
	}
	
	public String genSQLWhereKW() {
		return "Where ";
	}
	
	public String genSQLOrKW() {
		return "Or ";
	}
	

	
	@Override
    public String genSQLSelectStmt( String res, String from, String qual ) {
		return genSQLSelectKW() + res + " " + 
			genSQLFromKW() + from + " " +
			(qual.length() == 0 ? qual :genSQLWhereKW()) + qual;
	}

	
	protected int getTableCount(int graphId) {
		ResultSet alltables = null;
		try {
			DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			int res = 0;
			String tblPattern =
				TABLE_NAME_PREFIX + "g" + Integer.toString(graphId) + "%";
			tblPattern = stringToDBname(tblPattern);
			alltables = dbmd.getTables(null, null, tblPattern, tableTypes);
			while (alltables.next()) {
				res += 1;
			}
			return res;
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal SQL error in driver - " + e1);
		} finally {
			if(alltables != null)
                try {
                	alltables.close();
                } catch (SQLException e1) {
                	throw new RDFRDBException("Failed to get last inserted ID: " + e1);
                }
		}
	}
	
	/*
	 * getters and setters for database options
	 */
	 
	 @Override
    public int getLongObjectLengthMax () {
		 	return LONG_OBJECT_LENGTH_MAX;
		 }
		 
	 @Override
    public int getLongObjectLength () {
	 	return LONG_OBJECT_LENGTH;
	 }
	 
	 @Override
    public void setLongObjectLength ( int len ) {
		checkDbUninitialized();
		if ( len > LONG_OBJECT_LENGTH_MAX )
			throw new JenaException("LongObjectLength exceeds maximum value for database (" +
					+ LONG_OBJECT_LENGTH_MAX + ")");
		LONG_OBJECT_LENGTH = len;
	}

	 @Override
    public int getIndexKeyLengthMax () {
		 	return INDEX_KEY_LENGTH_MAX;
		 }
		 
	@Override
    public int getIndexKeyLength () {
   		return INDEX_KEY_LENGTH;
	}
	 
	@Override
    public void setIndexKeyLength ( int len ) {
		checkDbUninitialized();
		if ( len > INDEX_KEY_LENGTH_MAX )
			throw new JenaException("IndexKeyLength exceeds maximum value for database ("
					+ INDEX_KEY_LENGTH_MAX + ")");
		INDEX_KEY_LENGTH = len;
	}
	
	@Override
    public boolean getIsTransactionDb () {
		return IS_XACT_DB;
	}
	 
	@Override
    public void setIsTransactionDb ( boolean bool ) {
		checkDbUninitialized();
		if ( bool == false )
			throw new JenaException("setIsTransactionDb unsupported for this database engine");
	}

	@Override
    public boolean getDoCompressURI () {
			return URI_COMPRESS;
	}
	
	@Override
    public void setDoCompressURI ( boolean bool ) {
		checkDbUninitialized();
		URI_COMPRESS = bool;
	}
	
	@Override
    public int getCompressURILength() {
		return URI_COMPRESS_LENGTH;
	}
	
	@Override
    public void setCompressURILength ( int len ) {
		checkDbUninitialized();
		URI_COMPRESS_LENGTH = len;
	}
	
	@Override
    public boolean getDoDuplicateCheck() {
		return !SKIP_DUPLICATE_CHECK;
	}
	
	@Override
    public void setDoDuplicateCheck(boolean bool) {
		SKIP_DUPLICATE_CHECK = !bool;
	}

	protected boolean dbIsOpen() {
		return (m_sysProperties != null);
	}

	protected void checkDbIsOpen() {
		if ( !dbIsOpen() )
			throw new JenaException("Database not open");
	}
	
	protected void checkDbUninitialized() {
		if ( dbIsOpen() || isDBFormatOK() )
			throw new JenaException("Database configuration option cannot be set after database is formatted");
	}

	@Override
    public String getTableNamePrefix() {
		return TABLE_NAME_PREFIX;
	}

	@Override
    public void setTableNamePrefix ( String prefix ) {
		if ( dbIsOpen() )
			throw new JenaException("Table name prefix must be set before opening or connecting to a model.");
		/* sanity check that the new prefix length is not too long.
		 * we have to add a few characters to the given prefix to
		 * account for the index names (see the createStatementTable
		 * template in the etc/<db>.sql files).
		 */
		String sav = TABLE_NAME_PREFIX;
		String testpfx = prefix;
		int i;
		for ( i=0;i<MAXIMUM_INDEX_COLUMNS;i++) testpfx += "X";
		setTableNames(testpfx);
		// now see if the table names will be too long with this "prefix".
		try {
			String s = genTableName(10,10,true);
			s = genTableName(10,10,false);
		} catch ( RDFRDBException e ) {
			setTableNames(sav);
			throw new JenaException("New prefix (\"" + prefix +
				"\") is too long and will cause table names \n" +
				"to exceed maximum length for database (" + TABLE_NAME_LENGTH_MAX + ").");
		}
		// all ok. switch to the new prefix.
		setTableNames(prefix);
	}
	
	
	/** generate a table name and verify that it does not
	 * exceed the maximum length.
	 */
	
	protected String genTableName( int graphId, int tblId, boolean isReif )
	{
		String res = stringToDBname(TABLE_NAME_PREFIX + 
				"g" + Integer.toString(graphId) +
				"t" + Integer.toString(tblId) +
				(isReif ? REIF_TABLE_NAME_SUFFIX : STMT_TABLE_NAME_SUFFIX));
		if ( res.length() > TABLE_NAME_LENGTH_MAX )
			throw new RDFRDBException("New table name (\"" + res +
			"\") exceeds maximum length for database (" + TABLE_NAME_LENGTH_MAX + ").");
		return res;
	}
	
	
	   /** Names of jena system tables.
	   protected String [] SYSTEM_TABLE_NAME; */
	
	protected void setTableNames ( String prefix ) {
		TABLE_NAME_PREFIX = stringToDBname(prefix);
		int i = 0;
		SYSTEM_TABLE_NAME = new String[6];
		SYSTEM_TABLE_NAME[i++] = SYSTEM_STMT_TABLE = stringToDBname(TABLE_NAME_PREFIX + "sys_stmt");
		SYSTEM_TABLE_NAME[i++] = LONG_LIT_TABLE = stringToDBname(TABLE_NAME_PREFIX + "long_lit");
		SYSTEM_TABLE_NAME[i++] = LONG_URI_TABLE = stringToDBname(TABLE_NAME_PREFIX + "long_uri");
		SYSTEM_TABLE_NAME[i++] = PREFIX_TABLE = stringToDBname(TABLE_NAME_PREFIX + "prefix");
		SYSTEM_TABLE_NAME[i++] = GRAPH_TABLE = stringToDBname(TABLE_NAME_PREFIX + "graph");
		SYSTEM_TABLE_NAME[i++] = MUTEX_TABLE = stringToDBname(TABLE_NAME_PREFIX + "mutex");
		SYSTEM_TABLE_CNT = i;
	}
	
	/**
	 * Return the number of system tables.
	 */

	@Override
    public int getSystemTableCount() {
		return SYSTEM_TABLE_CNT;
	}
	
	/**
	 * Return the name of a system table
	 */

	@Override
    public String getSystemTableName ( int i ) {
		return ((i < 0) || (i >= SYSTEM_TABLE_CNT)) ?
			null : SYSTEM_TABLE_NAME[i];
	}

	
	@Override
    public String getStoreWithModel() {
		return STORE_WITH_MODEL;
	}

	@Override
    public void setStoreWithModel(String modelName) {
		String name = null;
		if ((modelName != null) && !modelName.equals(""))
			name = modelName;
		STORE_WITH_MODEL = name;
	}

	@Override
    public int getCompressCacheSize() {
		checkDbIsOpen();
		return prefixCache.getLimit();
	}

	@Override
    public void setCompressCacheSize(int count) {
		checkDbIsOpen();
		prefixCache.setLimit(count);
	}

}



/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
