/*
  (c) Copyright 2003, Hewlett-Packard Development Company, LP
  [See end of file]
*/

package com.hp.hpl.jena.db.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.db.GraphRDB;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.RDFRDBException;
import com.hp.hpl.jena.db.impl.DBIDInt;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.impl.LiteralLabel;

import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.shared.*;

import com.hp.hpl.jena.vocabulary.RDF;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
* @version $Revision: 1.41 $ on $Date: 2004-07-25 14:31:08 $
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
   
   /**
	* Holds the length of the longest jena table or index name.
	* This is really a hack and should be better architected.
	* The currently known longest possible name is:
	* <prefix>GnTm_StmtXSP   where prefix is the table
	* name prefix (which isn't counted here), n is the
	* graph identifier, m is the table number within that
	* graph and XSP refers to the subject-predicate index.
	* If we assume n and m might be two digits, we get 14.
	*/
   protected int JENA_LONGEST_TABLE_NAME_LENGTH = 14;
  
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
   
   protected static Log logger = LogFactory.getLog( PSet_ReifStore_RDB.class );
    
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
    
    protected LRUCache prefixCache = null;
    
    public static final int PREFIX_CACHE_SIZE = 50;
    
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
		setTableNames(TABLE_NAME_PREFIX);
			
                try {
                    if( !isDBFormatOK() ) {
                      // Format the DB
                      cleanDB();
                      prefixCache = new LRUCache(PREFIX_CACHE_SIZE);
                      return formatAndConstructSystemSpecializedGraph();
                    }
                } catch (Exception e) {
                    System.out.println("TEMP error flag");
                    // We see an error during format testing, might be a dead
                    // connection rather than an unformated database so abort
                    throw new JenaException("The database appears to be unformatted or corrupted.\n" +
                        "If possible, call IDBConnection.cleanDB(). \n" +
                        "Warning: cleanDB will remove all Jena models from the databases.");
                }
                
		prefixCache = new LRUCache(PREFIX_CACHE_SIZE);
        getDbInitTablesParams();  //this call is a hack. it's needed because
        // it has the side effect of initializing some vars (e.g., EOS).
		IPSet pSet = createIPSetInstanceFromName(m_psetClassName, SYSTEM_STMT_TABLE);
		m_sysProperties = createLSetInstanceFromName(m_lsetClassName, pSet, DEFAULT_ID);
		m_dbProps = new DBPropDatabase(m_sysProperties);
		
		// now reset the configuration parameters
		checkEngine(m_dbProps);
		checkDriverVersion(m_dbProps);
		checkLayoutVersion(m_dbProps);
		String val = m_dbProps.getLongObjectLength();
		if ( val == null ) throwBadFormat("long object length");
		else LONG_OBJECT_LENGTH = Integer.parseInt(val);
		val = m_dbProps.getIndexKeyLength();
		if ( val == null ) throwBadFormat("index key length");
		else INDEX_KEY_LENGTH = Integer.parseInt(val);
		val = m_dbProps.getIsTransactionDb(); 
		if ( val == null ) throwBadFormat("database supports transactions");
		else IS_XACT_DB = Boolean.valueOf(val).booleanValue();
		val = m_dbProps.getDoCompressURI();
		if ( val == null ) throwBadFormat("compress URIs");
		else URI_COMPRESS = Boolean.valueOf(val).booleanValue();
		val = m_dbProps.getCompressURILength();
		if ( val == null ) throwBadFormat("URI compress length");
		else URI_COMPRESS_LENGTH = Integer.parseInt(val);
		val = m_dbProps.getTableNamePrefix();
		if ( val == null ) throwBadFormat("table name prefix");
		else TABLE_NAME_PREFIX = val;
		
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

		try {
			String [] params = 	getDbInitTablesParams();
			m_sql.runSQLGroup("initDBtables", params);
			m_sql.runSQLGroup("initDBgenerators");//			m_sql.runSQLGroup("initDBprocedures");
		} catch (SQLException e) {
			logger.warn("Problem formatting database", e);
			throw new RDFRDBException("Failed to format database", e);
		}
		
		// Construct the system properties
		IPSet pSet = createIPSetInstanceFromName(m_psetClassName, SYSTEM_STMT_TABLE);
		m_sysProperties = createLSetInstanceFromName(m_lsetClassName, pSet, DEFAULT_ID);
						
		// The following call constructs a new set of database properties and
		// adds them to the m_sysProperties specialized graph.
		m_dbProps = new DBPropDatabase( m_sysProperties, m_dbcon.getDatabaseType(), 
		        VERSION, LAYOUT_VERSION,String.valueOf(LONG_OBJECT_LENGTH), 
		        String.valueOf(INDEX_KEY_LENGTH), String.valueOf(IS_XACT_DB), 
                        String.valueOf(URI_COMPRESS), String.valueOf(URI_COMPRESS_LENGTH),
				TABLE_NAME_PREFIX);
		
		// Now we also need to construct the parameters that will be the
		// default settings for any graph added to this database
		DBPropGraph def_prop = new DBPropGraph( m_sysProperties, DEFAULT_PROPS, "generic");
		
		def_prop.addGraphId(DEFAULT_ID);

		return m_sysProperties;		
	}
	
	abstract String[] getDbInitTablesParams();
	
	abstract String[] getCreateTableParams( int graphId, boolean isReif );
	
	abstract public int graphIdAlloc ( String graphName );	
	
	
	
	/**
	 * Construct and return a new specialized graph.
	 * @param graphProperties A set of customization properties for the specialized graph.
	 */
	public List createSpecializedGraphs(DBPropGraph graphProperties) {
		
		String graphName = graphProperties.getName();
		String stmtTbl = null;
		String reifTbl = null;
		String dbSchema = STORE_WITH_MODEL;
		int graphId = graphIdAlloc(graphName);
		graphProperties.addGraphId(graphId);
		boolean useDefault = false;
				
		// dbSchema = graphProperties.getDBSchema();
		// use the default schema if:
		// 1) no schema is specified and we are creating the default (unnamed) graph
		// 2) a schema is specified and it is the default (unnamed) graph
		if ( ((dbSchema == null) && graphName.equals(GraphRDB.DEFAULT)) ) {
			useDefault = true;
			dbSchema = DEFAULT_PROPS;  // default graph should use default tables
		}
		// else if ( ((dbSchema != null) && dbSchema.equals(GraphRDB.DEFAULT)) ) {
		// 	useDefault = true;
		//	dbSchema = DEFAULT_PROPS;  // default graph should use default tables
		// }
		if ( dbSchema != null ) {
			DBPropGraph schProp = DBPropGraph.findPropGraphByName(getSystemSpecializedGraph(),
												dbSchema );
			if ( schProp != null ) {
				reifTbl = schProp.getReifTable();
				stmtTbl = schProp.getStmtTable();
			}
			if ( ((reifTbl == null) || (stmtTbl == null)) && (useDefault == false) )
				// schema not found. this is ok ONLY IF it's the DEFAULT schema
				throw new RDFRDBException("Creating graph " + graphName +
					": referenced schema not found: " + dbSchema);
		}
		if ( (reifTbl == null) || (stmtTbl == null) ) {
			reifTbl = createTable(graphId, true);	
			stmtTbl = createTable(graphId, false);	
			if ( (reifTbl == null) || (stmtTbl == null) )
				throw new RDFRDBException("Creating graph " + graphName +
					": cannot create tables");
		}
		graphProperties.addStmtTable(stmtTbl);
		graphProperties.addReifTable(reifTbl);
			
		// Add the reifier first
		DBPropPSet pSetReifier = new DBPropPSet(m_sysProperties, m_psetReifierClassName, reifTbl);
		DBPropLSet lSetReifier = new DBPropLSet(m_sysProperties, "LSET_"+graphProperties.getName()+"_REIFIER", m_lsetReifierClassName);
		lSetReifier.setPSet(pSetReifier);
		graphProperties.addLSet(lSetReifier);
		
		// Now add support for all non-reified triples
		DBPropPSet pSet = new DBPropPSet(m_sysProperties, m_psetClassName, stmtTbl);
		DBPropLSet lSet = new DBPropLSet(m_sysProperties, "LSET_"+graphProperties.getName(), m_lsetClassName);
		lSet.setPSet(pSet);
		graphProperties.addLSet(lSet);

		return recreateSpecializedGraphs( graphProperties );
	}
	
	/**
	 * Construct and return a list of specialized graphs to match those in the store.
	 * @param graphProperties A set of customization properties for the graph.
	 */
	public List recreateSpecializedGraphs(DBPropGraph graphProperties) {
		
		List result = new ArrayList();
		int dbGraphId = graphProperties.getGraphId();

		// to ensure that reifier graphs occur before stmt graphs, make two passes
		String[] lsetTypes = {m_lsetClassName, m_lsetReifierClassName};
		int i;
		for(i=0;i<2;i++) {
			Iterator it = graphProperties.getAllLSets();
			while(it.hasNext() ) {
				DBPropLSet lSetProps = (DBPropLSet)it.next();
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
			Class cls = Class.forName(lSetName);
			Class[] params = {IPSet.class, Integer.class};
			java.lang.reflect.Constructor con = cls.getConstructor(params);
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
	public void removeSpecializedGraphs( DBPropGraph graphProperties,
		List specializedGraphs) {
			
		int graphId = graphProperties.getGraphId();
		Iterator it = specializedGraphs.iterator();
		while (it.hasNext()){
		   SpecializedGraph sg = (SpecializedGraph) it.next();
		   removeSpecializedGraph(sg);
		}
		
		String stmtTbl = graphProperties.getStmtTable();
		String reifTbl = graphProperties.getReifTable();
		
		// remove from system properties table
		// It is sufficient just to remove the lSet properties (it will
		// take care of deleting any pset properties automatically).			
		m_dbProps.removeGraph(graphProperties);
		
		// drop the tables if they are no longer referenced
		if ( graphId != DEFAULT_ID ) {
			boolean stInUse = false;
			boolean rtInUse = false;
			it =  m_dbProps.getAllGraphs();
			while ( it.hasNext() ) {
				DBPropGraph gp = (DBPropGraph) it.next();
				if ( gp.getStmtTable().equals(stmtTbl) ) stInUse = true;
				if ( gp.getReifTable().equals(reifTbl) ) rtInUse = true;
			}		
			if ( stInUse == false ) deleteTable(stmtTbl);
			if ( rtInUse == false ) deleteTable(reifTbl);
			graphIdDealloc(graphId);
		}
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
	public boolean isDBFormatOK() {
			boolean result = false;
			try {
					ResultSet alltables = getAllTables();
					int i = 0;
					while ( alltables.next() ) i++;
					alltables.close();
					result = i >= 5;
			} catch (Exception e1) {
                            throw new JenaException("DB connection problem while testing formating", e1);
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



	/* (non-Javadoc)
	 * @see com.hp.hpl.jena.graphRDB.IRDBDriver#cleanDB()
	 */
	public void cleanDB() {
		try {
			ResultSet alltables = getAllTables();
			List tablesPresent = new ArrayList(10);
			while (alltables.next()) {
				tablesPresent.add(alltables.getString("TABLE_NAME"));
			}
			alltables.close();
			Iterator it = tablesPresent.iterator();
			while (it.hasNext()) {
				m_sql.runSQLGroup("dropTable", (String) it.next());
			}
			if (PRE_ALLOCATE_ID) {
				clearSequences();
			}
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal SQL error in driver", e1);
		}
		m_sysProperties = null;
		if ( prefixCache != null ) prefixCache.clear();
		prefixCache = null;
	}
	
	private ResultSet getAllTables() {
		try {
			DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			String prefixMatch = stringToDBname(TABLE_NAME_PREFIX + "%");
			return dbmd.getTables(null, null, prefixMatch, tableTypes);
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal SQL error in driver", e1);
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
		try {
			String op = "SelectSequenceName";
			PreparedStatement ps = m_sql.getPreparedSQLStatement(op);
			ps.setString(1,seqName);
			rs = ps.executeQuery();
			result = rs.next();
			m_sql.returnPreparedSQLStatement(ps);
		} catch (Exception e) {
		  logger.error("Unable to select sequence " + seqName,  e);
			}
		return result;
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
			String opname = "SelectJenaSequences";
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opname, TABLE_NAME_PREFIX);
		    ResultSet rs = ps.executeQuery();
		    while (rs.next()) {
		    	results.add(rs.getString(1));
		    }
		    //rs.close();
		    m_sql.returnPreparedSQLStatement(ps);
		} catch (Exception e) {
		  logger.error("Unable to select Jena sequences: ", e);
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
	 * Create a table for storing asserted or reified statements.
	 * 
	 * @param graphId the graph which the table is created.
	 * @param isReif true if table stores reified statements.
	 * @return the name of the new table 
	 * 
	 */
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
	public void deleteTable( String tableName ) {
		
		String opname = "dropTable"; 
		try {         			
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opname, tableName);
			ps.executeUpdate();
			return;
		} catch (Exception e1) {
			throw new RDFRDBException("Failed to delete table ", e1);
		}
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
				throw new JenaException("Transaction support failed: ", e);
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
					// not sure why read_uncommitted is set, below. commented out by kw.
					// c.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
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
				logger.error("SQL Exception caught ", e);
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
	public String nodeToRDBString ( Node node, boolean addIfLong ) throws RDFRDBException {
		String res = null;
		if ( node.isURI() ) {
			String uri = new String(((Node_URI) node).getURI());
			if ( uri.startsWith(RDBCodeURI) ) {
				throw new RDFRDBException ("URI Node looks like a blank node: " + uri );
			}
			// TODO: need to write special version of splitNamespace for rdb.
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
				pfx = RDBCodeDelim + ((DBIDInt)pfxid).getIntID() + RDBCodeDelim;
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
			// TODO: may need to encode literal value when datatype is not a string.
			Node_Literal litNode = (Node_Literal) node;
			LiteralLabel ll = litNode.getLiteral();
			String lval = ll.getLexicalForm();
			String lang = ll.language();
			String dtype = ll.getDatatypeURI();
			String ld = litLangTypeToRDBString(lang,dtype);
			int encodeLen = RDBCodeLiteral.length() + 2 + ld.length() + EOS_LEN;
			boolean litIsLong = objectIsLong(encodeLen,lval);		
			if ( litIsLong ) {
				int	dbid;
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
			ParseInt pi = new ParseInt(pos);
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
			String lang;
			String dtype;
			int langLen = 0;
			int dtypeLen = 0;
			LiteralLabel llabel;
			pi.pos = 0;
			RDBStringParseInt(litString, pi, false);
			if ( pi.val == null ) langLen = 0; 
			else langLen = pi.val.intValue(); 
			pi.pos = pi.pos + 1;
			RDBStringParseInt(litString, pi, false);	
			if ( pi.val == null ) dtypeLen = 0;
			else dtypeLen = pi.val.intValue();
			pos = pi.pos + 1;	
			if ( (pos + langLen + dtypeLen) > len )
					throw new RDFRDBException("Malformed Literal: " + litString);	
			lang = litString.substring(pos,pos+langLen);
			pos = pos + langLen;
			dtype = litString.substring(pos,pos+dtypeLen);
			pos = pos + dtypeLen;
			
			String val = litString.substring(pos);
			
			if ( (dtype == null) || (dtype.equals(""))  ) {
				llabel = new LiteralLabel(val, lang == null ? "" : lang);
			} else {
				RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(dtype);
				llabel = new LiteralLabel(val, lang == null ? "" : lang, dt);
			}	 
			res = Node.createLiteral(llabel);
			
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
		LiteralLabel 	l = node.getLiteral();
		String 			lang = l.language();
		String 			dtype = l.getDatatypeURI();
		String 			val = l.getLexicalForm();
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
		try {
			String opName = "getLongObjectID";
			if ( lobj.tail.length() > 0 ) 
				opName += "withChkSum";
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opName, table); 
			ps.setString(1,lobj.head);
			if ( lobj.tail.length() > 0 ) 
				ps.setLong(2, lobj.hash);
			
			ResultSet rs = ps.executeQuery();
			DBIDInt result = null;
			if (rs.next()) {
				result = wrapDBID(rs.getObject(1));
			} else {
				if ( add )
					result = addRDBLongObject(lobj, table);
			}
		    m_sql.returnPreparedSQLStatement(ps);
			return result;
		} catch (SQLException e1) {
			// /* DEBUG */ System.out.println("Literal truncation (" + l.toString().length() + ") " + l.toString().substring(0, 150));
			throw new RDFRDBException("Failed to find literal", e1);
		}
	}
 
	/**
	 * Insert a long object into the database.  
	 * This assumes the object is not already in the database.
	 * @return the db index of the added literal 
	 */
	public DBIDInt addRDBLongObject(RDBLongObject lobj, String table) throws RDFRDBException {
		try {
			int argi = 1;
			String opname = "insertLongObject";           			
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opname, table);
			int dbid = 0; // init only needed to satisy java compiler
			if ( PRE_ALLOCATE_ID ) {
				dbid = getInsertID(table);
				ps.setInt(argi++,dbid);
			} 
			 ps.setString(argi++, lobj.head);
			 if ( lobj.tail.length() > 0 ) {
			 	ps.setLong(argi++, lobj.hash);
			 	ps.setString(argi++, lobj.tail);
			 } else {
			 	ps.setNull(argi++,java.sql.Types.BIGINT);
				ps.setNull(argi++,java.sql.Types.VARCHAR);     
			 }
/*			if (isBlob || (len == 0) ) {
				// First convert the literal to a UTF-16 encoded byte array
				// (this wouldn't be needed for jdbc 2.0 drivers but not all db's have them)
				byte[] temp = lit.getBytes("UTF-8");
				int lenb = temp.length;
				//System.out.println("utf-16 len = " + lenb);
				byte[] litData = new byte[lenb + 4];
				litData[0] = (byte)(lenb & 0xff);
				litData[1] = (byte)((lenb >> 8) & 0xff);
				litData[2] = (byte)((lenb >> 16) & 0xff);
				litData[3] = (byte)((lenb >> 24) & 0xff);
				System.arraycopy(temp, 0, litData, 4, lenb);
                
				// Oracle has its own way to insert Blobs
				if (isBlob && m_driver.getDatabaseType().equalsIgnoreCase("Oracle")) {
					//TODO fix to use Blob
					// For now, we do not support Blobs under Oracle
					throw new RDFRDBException("Oracle driver does not currently support large literals.");
				} else {
					ps.setBinaryStream(argi++, new ByteArrayInputStream(litData), litData.length);
				}
			} 
*/            
			ps.executeUpdate();
			//m_sql.returnPreparedSQLStatement(ps,opname);
			if ( !PRE_ALLOCATE_ID ) dbid = getInsertID(table);
			return wrapDBID(new Integer(dbid));
		} catch (Exception e1) {
			/* DEBUG */ System.out.println("Problem on long object (l=" + lobj.head + ") " + e1 );
			// System.out.println("ID is: " + id);
			throw new RDFRDBException("Failed to add long object ", e1);
		}
	}
	
	/**
	 * Return the prefix string that has the given prefix id.
	 * @param prefixID - the dbid of the prefix.
	 * @return the prefix string or null if it does not exist.
	 */
	protected String IDtoPrefix ( int prefixID ) {
		// check cache
		DBIDInt dbid = new DBIDInt(prefixID);
		Object res = prefixCache.get(dbid);
		if ( res != null)
			return (String) res;
		else
			return IDtoString ( prefixID, PREFIX_TABLE, RDBCodePrefix);
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

	
	protected RDBLongObject IDtoLongObject ( int dbid, String table ) {
		RDBLongObject	res = null;
		try {
			String opName = "getLongObject";
			PreparedStatement ps = m_sql.getPreparedSQLStatement(opName, table); 
			ps.setInt(1,dbid);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				res = new RDBLongObject();
				res.head = rs.getString(1);
				res.tail = rs.getString(2);			
			}
			m_sql.returnPreparedSQLStatement(ps);
		} catch (SQLException e1) {
			// /* DEBUG */ System.out.println("Literal truncation (" + l.toString().length() + ") " + l.toString().substring(0, 150));
			throw new RDFRDBException("Failed to find literal", e1);
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
	
	public String genSQLReifQualStmt () {
		return "stmt = ?";
	}
	
	public String genSQLReifQualAnyObj( boolean objIsStmt) {
		return "( subj = ? OR prop = ? OR obj = ?" + (objIsStmt ? " OR hasType = " +
			QUOTE_CHAR + "T" + QUOTE_CHAR + " )" : " )");		
	}
	
	public String genSQLReifQualObj ( char reifProp, boolean hasObj ) {
		String qual = "";
		if ( reifProp == 'T' ) {
			qual = "hasType = " + QUOTE_CHAR + "T" + QUOTE_CHAR;
		} else {
			String cmp = (hasObj ? " = ?" : " is not null");
			String col = null;
			if ( reifProp == 'S' ) col = "subj";
			else if ( reifProp == 'P' ) col = "prop";
			else if ( reifProp == 'O' ) col = "obj";
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

	/*
	 * there's a bug in the code below in that the literal is converted to
	 * a string BEFORE the query is run. consequently, there's a race
	 * condition. if the (long) literal is not in the database
	 * when the query is compiled but is added prior to running the
	 * query, then the query will (incorrectly) return no results.
	 * for now, we'll ignore this case and document it as a bug.
	 */
	
	public String genSQLQualConst ( int alias, char pred, Node lit ) {
		String val = nodeToRDBString(lit, false);
		if ( val == "" )
			// constant not in database.
			// should really optimize this and not
			// even run the query but ok for now.
			val = RDBCodeInvalid;
		return colAliasToString(alias,pred) + "=" + QUOTE_CHAR + val + QUOTE_CHAR;		
	}

	public String genSQLReifQualConst ( int alias, char pred, Node lit ) {
		String val = "";
		if ( (pred == 'T') && (lit.equals(RDF.Nodes.Statement)) )
			val = "T";
		else
			val = nodeToRDBString(lit, false);
		return colAliasToString(alias,pred) + "=" + QUOTE_CHAR + val + QUOTE_CHAR;		
	}
	
	public String genSQLQualParam( int alias, char pred ) {
		return colAliasToString(alias,pred) + "=?";			
	}

	public String genSQLQualGraphId( int alias, int graphId ) {
		return colAliasToString(alias,'G') + "=" + graphId;			
	}

	public String genSQLJoin( int lhsAlias, char lhsCol,
		int rhsAlias, char rhsCol ) {
			return colAliasToString(lhsAlias,lhsCol) + "=" +
			colAliasToString(rhsAlias,rhsCol);
	}
	
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
	
	public String genSQLFromList( int aliasCnt, String table ) {
		int i;
		String resList = "";
		for(i=0;i<aliasCnt;i++) {
			resList += (i>0?", ":"") + table + " " + aliasToString(i);
		}
		return resList;

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
	

	
	public String genSQLSelectStmt( String res, String from, String qual ) {
		return genSQLSelectKW() + res + " " + 
			genSQLFromKW() + from + " " +
			(qual.length() == 0 ? qual :genSQLWhereKW()) + qual;
	}

	
	protected int getTableCount(int graphId) {
		try {
			DatabaseMetaData dbmd = m_dbcon.getConnection().getMetaData();
			String[] tableTypes = { "TABLE" };
			int res = 0;
			String tblPattern =
				TABLE_NAME_PREFIX + "g" + Integer.toString(graphId) + "%";
			tblPattern = stringToDBname(tblPattern);
			ResultSet alltables =
				dbmd.getTables(null, null, tblPattern, tableTypes);
			while (alltables.next()) {
				res += 1;
			}
			alltables.close();
			return res;
		} catch (SQLException e1) {
			throw new RDFRDBException("Internal SQL error in driver", e1);
		}
	}
	
	/*
	 * getters and setters for database options
	 */
	 
	 public int getLongObjectLength () {
	 	return LONG_OBJECT_LENGTH;
	 }
	 
	 public void setLongObjectLength ( int len ) {
		checkDbUninitialized();
		if ( len > LONG_OBJECT_LENGTH_MAX )
			throw new JenaException("IndexKeyLength exceeds maximum value for database");
		LONG_OBJECT_LENGTH = len;
	}

	public int getIndexKeyLength () {
   		return INDEX_KEY_LENGTH;
	}
	 
	public void setIndexKeyLength ( int len ) {
		checkDbUninitialized();
		if ( len > INDEX_KEY_LENGTH_MAX )
			throw new JenaException("IndexKeyLength exceeds maximum value for database");
		INDEX_KEY_LENGTH = len;
	}
	
	public boolean getIsTransactionDb () {
		return IS_XACT_DB;
	}
	 
	public void setIsTransactionDb ( boolean bool ) {
		checkDbUninitialized();
		if ( bool == false )
			throw new JenaException("setIsTransactionDb unsupported for this database engine");
	}

	public boolean getDoCompressURI () {
			return URI_COMPRESS;
	}
	
	public void setDoCompressURI ( boolean bool ) {
		checkDbUninitialized();
		URI_COMPRESS = bool;
	}
	
	public int getCompressURILength() {
		return URI_COMPRESS_LENGTH;
	}
	
	public void setCompressURILength ( int len ) {
		checkDbUninitialized();
		URI_COMPRESS_LENGTH = len;
	}
	
	public boolean getDoDuplicateCheck() {
		return !SKIP_DUPLICATE_CHECK;
	}
	
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
		if ( dbIsOpen() || (isDBFormatOK() == true))
			throw new JenaException("Database configuration option cannot be set after database is formatted");
	}

	public String getTableNamePrefix() {
		return TABLE_NAME_PREFIX;
	}

	public void setTableNamePrefix ( String prefix ) {
		if ( (prefix.length() + JENA_LONGEST_TABLE_NAME_LENGTH) >
										TABLE_NAME_LENGTH_MAX )
			throw new JenaException("TableNamePrefix exceeds maximum length for database: "				+ TABLE_NAME_LENGTH_MAX);
		if ( dbIsOpen() )
			throw new JenaException("Table name prefix must be set before opening or connecting to a model.");
		setTableNames(prefix);
	}

	private void setTableNames ( String prefix ) {
		TABLE_NAME_PREFIX = stringToDBname(prefix);
		SYSTEM_STMT_TABLE = TABLE_NAME_PREFIX + "sys_stmt";
		LONG_LIT_TABLE = TABLE_NAME_PREFIX + "long_lit";
		LONG_URI_TABLE = TABLE_NAME_PREFIX + "long_uri";
		PREFIX_TABLE = TABLE_NAME_PREFIX + "prefix";
		GRAPH_TABLE = TABLE_NAME_PREFIX + "graph";
	}
	
	public String getStoreWithModel() {
		return STORE_WITH_MODEL;
	}

	public void setStoreWithModel(String modelName) {
		String name = null;
		if ((modelName != null) && !modelName.equals(""))
			name = modelName;
		STORE_WITH_MODEL = name;
	}

	public int getCompressCacheSize() {
		checkDbIsOpen();
		return prefixCache.getLimit();
	}

	public void setCompressCacheSize(int count) {
		checkDbIsOpen();
		prefixCache.setLimit(count);
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
