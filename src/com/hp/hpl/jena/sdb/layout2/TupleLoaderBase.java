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

package com.hp.hpl.jena.sdb.layout2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.TableUtils;
import com.hp.hpl.jena.sdb.store.TableDesc;

public abstract class TupleLoaderBase extends com.hp.hpl.jena.sdb.store.TupleLoaderBase implements TupleLoaderBasics {
	
	PreparedStatement insertTupleLoader;
    PreparedStatement insertNodeLoader;
    String insertNodes;
    String insertTuples;
    PreparedStatement deleteTuples;
    PreparedStatement deleteAllTuples;
    PreparedStatement clearTupleLoader;
    PreparedStatement clearNodeLoader;
	
    int chunkSize;
    boolean amLoading; // flag for whether we're loading or deleting
    int tupleNum;
    
    Set<Long> seenNodes; // For suppressing duplicate nodes
    
	public TupleLoaderBase(SDBConnection connection,
			TableDesc tableDesc, int chunkSize) {
		super(connection, tableDesc);
		this.chunkSize = chunkSize;
		this.amLoading = true;
		this.tupleNum = 0;
		this.seenNodes = new HashSet<Long>();
		try {
			init();
		} catch (SQLException e) {
			throw new SDBException("Problem initialising loader for [" + tableDesc + "]", e);
		}
	}
	
	protected void init() throws SQLException {
	    ensureTempTables() ;
		// Prepare those statements
		insertNodeLoader = connection().prepareStatement(getInsertTempNodes());
		insertTupleLoader = connection().prepareStatement(getInsertTempTuples());
		insertNodes = getLoadNodes();
		insertTuples = getLoadTuples();
		deleteTuples = connection().prepareStatement(getDeleteTuples());
		deleteAllTuples = connection().prepareStatement(getDeleteAllTuples());
		clearNodeLoader = connection().prepareStatement(getClearTempNodes());
		clearTupleLoader = connection().prepareStatement(getClearTempTuples());
	}
	
	public int getArity() {
		return this.getTableWidth();
	}
	
	@Override
    public void load(Node... row) {
		if (!amLoading) {
			flush();
			amLoading = true;
		}
		
		if (row.length != this.getTableWidth())
			throw new IllegalArgumentException("Tuple size mismatch");
		
		try {
			for (int i = 0; i < row.length; i++) {
				PreparedNode pNode = new PreparedNode(row[i]);
				if (seenNodes.add(pNode.hash)) // if true, this is new...
					pNode.addToStatement(insertNodeLoader);
				insertTupleLoader.setLong(i + 1, pNode.hash);
			}
			insertTupleLoader.addBatch();
		} catch (SQLException e) {
			throw new SDBException("Problem adding to prepared loader statements", e);
		}
		
		tupleNum++;
		if (tupleNum >= chunkSize) flush();
	}

	@Override
    public void unload(Node... row) {
		if (amLoading) {
			flush();
			amLoading = false;
		}
		
		// Overloading unload, so this is messy
		// If arity mismatch then see if this is a massDelete
		// TODO rethink overloading
		if (row.length != this.getTableWidth()) {
			if ((row.length == 0 && this.getTableWidth() == 3) ||
					(row.length == 1)) {
				massDelete(row);
				return;
			}
			else {
				throw new IllegalArgumentException("Tuple size mismatch");
			}
		}
		
		try {
			for (int i = 0; i < row.length; i++) {
				PreparedNode pNode = new PreparedNode(row[i]); //, false);
				deleteTuples.setLong(i + 1, pNode.hash);
			}
			deleteTuples.addBatch();
		} catch (SQLException e) {
			throw new SDBException("Problem adding to prepared delete statements", e);
		}
		
		tupleNum++;
		if (tupleNum >= chunkSize) flush();
	}
	
	private void massDelete(Node... row) {
		flush();
		boolean handleT = startTransaction(connection());
		try {
			if (row.length == 0) deleteAllTuples.execute();
			else {
				PreparedNode pNode = new PreparedNode(row[0]);
				deleteAllTuples.setLong(1, pNode.hash);
				deleteAllTuples.addBatch();
				deleteAllTuples.executeBatch();
				deleteAllTuples.clearBatch();
			}
			endTransaction(connection(), handleT);
		} catch (SQLException e) {
			if (handleT) {
				try {
					connection().getSqlConnection().rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			throw new SDBException("Exception mass deleting", e);
		}
	}
	
	@Override
	public void finish() {
		super.finish();
		flush();
	}
	
	@Override
	public void close()
	{
	    super.close();
	    try { 
	        // Close prepared statements - important on Oracle because there is an associated cursor
	        // and cuyrsors are a scarce resource that need managing carefully.
	        connection().closePreparedStatement(insertTupleLoader) ;
	        connection().closePreparedStatement(insertNodeLoader);
	        connection().closePreparedStatement(deleteTuples);
	        connection().closePreparedStatement(deleteAllTuples);
	        connection().closePreparedStatement(clearTupleLoader);
	        connection().closePreparedStatement(clearNodeLoader);
	    } catch (SQLException ex) {}
	}

    // Start a transaction if required
	private static boolean startTransaction(SDBConnection connection) {
		boolean handleTransaction = false; // is somebody handling transactions already?
		try {
			handleTransaction = connection.getSqlConnection().getAutoCommit();
			if (handleTransaction) connection.getSqlConnection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new SDBException("Failed to get autocommit status", e);
		}
		return handleTransaction;
	}
	
	// Complete transaction
	private static void endTransaction(SDBConnection connection, boolean handle) throws SQLException {
		if (!handle) return;
		connection.getSqlConnection().commit();
		connection.getSqlConnection().setAutoCommit(true); // back on
	}
	
	protected void flush() {
		if (tupleNum == 0) return;
		
		boolean handleTransaction = startTransaction(connection());
		
		try {
			if (amLoading) {
				insertNodeLoader.executeBatch();
				insertTupleLoader.executeBatch();
				connection().execUpdate(insertNodes);
				connection().execUpdate(insertTuples);
				if (!handleTransaction || !clearsOnCommit()) {
					clearNodeLoader.execute();
					clearTupleLoader.execute();
				}
			} else {
				deleteTuples.executeBatch();
			}
			
			endTransaction(connection(), handleTransaction);
		} catch (SQLException e) {
			if (handleTransaction)
				try {
					connection().getSqlConnection().rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			throw new SDBException("Exception flushing", e);
		} finally {
			tupleNum = 0;
			seenNodes = new HashSet<Long>();
		}
	}
	
	/** These are the SQL 'bits' we use to construct the loader statements **/
	
	public String getNodeLoader() {
		return "NNode" + this.getTableName();
	}
    
	public String getTupleLoader() {
		return "N" + this.getTableName();
	}
	
	public String getCreateTempNodes() {
		StringBuilder stmt = new StringBuilder();
		
		String[] tempBookends = getCreateTempTable();
		
		stmt.append(tempBookends[0]).append(" ").append(getNodeLoader()).append(" \n(");
		
		String[] nodeColTypes = getNodeColTypes();
		
		for (int i = 0; i < nodeColTypes.length; i++) {
			if (i != 0) stmt.append(" , \n");
			stmt.append("n").append(i).append(" ").append(nodeColTypes[i]);
		}
		stmt.append("\n) ").append(tempBookends[1]);
		
		return stmt.toString();
	}
	
	public String getCreateTempTuples() {
		StringBuilder stmt = new StringBuilder();
		
		String[] tempBookends = getCreateTempTable();
		
		stmt.append(tempBookends[0]).append(" ").append(getTupleLoader()).append(" \n(");
		
		int width = this.getTableWidth();
		
		for (int i = 0; i < width; i++) {
			if (i != 0) stmt.append(" , \n");
			stmt.append("t").append(i).append(" ").append(getTupleColType());
		}
		stmt.append("\n) ").append(tempBookends[1]);
		
		return stmt.toString();
	}
	
	public String getInsertTempNodes() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO ").append(getNodeLoader()).append(" VALUES (");
		for (int i = 0; i < getNodeColTypes().length; i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append("?");
		}
		stmt.append(" )");
		
		return stmt.toString();
	}
	
	public String getInsertTempTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO ").append(getTupleLoader()).append(" VALUES (");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append("?");
		}
		stmt.append(" )");
		
		return stmt.toString();
	}
	
	public String getLoadNodes() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO Nodes (hash, lex, lang, datatype, type) \nSELECT ");
		for (int i = 0; i < getNodeColTypes().length; i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append(getNodeLoader()).append(".").append("n").append(i);
		}
		stmt.append("\nFROM ").append(getNodeLoader()).append(" LEFT JOIN Nodes ON (");
		stmt.append(getNodeLoader()).append(".n0=Nodes.hash) \nWHERE Nodes.hash IS NULL"); 
		return stmt.toString();
	}
	
	@Override
    public String getClearTempNodes() {
		return "DELETE FROM " + getNodeLoader();
	}
	
	@Override
    public String getClearTempTuples() {
		return "DELETE FROM " + getTupleLoader();
	}
	
	@Override
    public boolean clearsOnCommit() { return false; }
	
	// ---- Temporary table creation.
	// Some databases (MySQL, MS SQL) do not make the temnporary tables visible to a metadata probe.
	// 
	private void createTempTables() throws SQLException
    {
        connection().exec(getCreateTempNodes());
        connection().exec(getCreateTempTuples());
    }

    private void ensureTempTables() throws SQLException
    {
        // Pick one.
        ensureTempTables1() ;
        //ensureTempTables2()
        }

    // Optimistic scheme - create the tables, if fails, delete (ignoring errors) and try once again. 
    private void ensureTempTables1() throws SQLException
    {
        boolean b = connection().loggingSQLExceptions() ;
        try {
            connection().setLogSQLExceptions(false) ;
            createTempTables() ;
        } catch (SQLException ex)
        {
            // Some problem - due to the differences in databases we didn't check whether tables existed first.
            // So attempt to cleanup, then tryagain to create the temporary tables.  
            TableUtils.dropTableSilent(connection(), getNodeLoader()) ;
            TableUtils.dropTableSilent(connection(), getTupleLoader()) ;
            createTempTables() ;    // Allow this to throw the SQLException
        }
        finally { connection().setLogSQLExceptions(b) ; }
    }

    // Pessimistic scheme - probe for table existence and create if necessary.
    // Need to cope with invisible temporary tables.
    private void ensureTempTables2() throws SQLException
    {
        try {
            // execSilent - because exceptions happen (e.g. systems that do not expose temporary tables to the DB metadata).
            if (!TableUtils.hasTable(connection().getSqlConnection(), getNodeLoader()))
                connection().execSilent(getCreateTempNodes());
            if (!TableUtils.hasTable(connection().getSqlConnection(), getTupleLoader()))
                connection().execSilent(getCreateTempTuples());
        } catch (SQLException e) { 
            // Work around for MySQL issue, which won't say if temp table exists
            // This is also the case for MS Server SQL
            // Testing the message is as good as it gets without needing the DB-specific 
            String msg = e.getMessage() ;
            String className = e.getClass().getName() ;
    
            boolean ignore = false ;
    
            // MS-SQL
            if ( className.equals("com.microsoft.sqlserver.jdbc.SQLServerException")
                && msg.matches("There is already an object named '#.*' in the database."))
                ignore = true ;
    
            // MySQL : com.mysql.jdbc.exceptions.MySQLSyntaxErrorException (at least in 5.0)
            if ( msg.matches("Table.*already exists") )
                ignore = true ;
    
            if ( ! ignore )
                throw e;
        }
    }

    /* Encapsulate the gory internals of breaking up nodes for the database */
    public static class PreparedNode
    {
        public long hash;
        public String lex;
        public String lang;
        public String datatype;
        public int typeId;
        //public Integer valInt;
        //public Double valDouble;
        //public Timestamp valDateTime;
        
        //PreparedNode(Node node)
        //{
        //	this(node, true);
        //}
        
        PreparedNode(Node node) //, boolean computeVals)
        {
            lex = NodeLayout2.nodeToLex(node);
            //ValueType vType = ValueType.lookup(node);
            typeId = NodeLayout2.nodeToType(node);

            lang = "";
            datatype = "";

            if (node.isLiteral())
            {
                lang = node.getLiteralLanguage();
                datatype = node.getLiteralDatatypeURI();
                if (datatype == null)
                    datatype = "";
            }

            hash = NodeLayout2.hash(lex, lang, datatype, typeId);
            
            /*if (computeVals) // don't need this for deleting
            {
            	// Value of the node
            	valInt = null;
            	if (vType == ValueType.INTEGER)
            		valInt = Integer.parseInt(lex);
            	
            	valDouble = null;
            	if (vType == ValueType.DOUBLE)
            		valDouble = Double.parseDouble(lex);
            	
            	valDateTime = null;
            	if (vType == ValueType.DATETIME)
            	{
            		String dateTime = SQLUtils.toSQLdatetimeString(lex);
            		valDateTime = Timestamp.valueOf(dateTime);
            	}
            }*/
        }
        
        public void addToStatement(PreparedStatement s)
        	throws SQLException
        {
        	s.setLong(1, hash);
        	s.setString(2, lex);
        	s.setString(3, lang);
        	s.setString(4, datatype);
        	s.setInt(5, typeId);
        	/*if (valInt != null)
        		s.setInt(6, valInt);
        	else
        		s.setNull(6, Types.INTEGER);
        	if (valDouble  != null)
        		s.setDouble(7, valDouble);
        	else
        		s.setNull(7, Types.DOUBLE);
        	if (valDateTime != null)
        		s.setTimestamp(8, valDateTime);
        	else
        		s.setNull(8, Types.TIMESTAMP);*/
        	s.addBatch();
        }
        
        @Override
        public int hashCode()
        {
        	return (int) (hash & 0xFFFF);
        }
        
        @Override
        public boolean equals(Object other)
        {
        	return ((PreparedNode) other).hash == hash;
        }
    }
}
