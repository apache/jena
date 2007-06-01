package com.hp.hpl.jena.sdb.layout2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sdb.store.TupleLoaderBase;

public class Layout2TupleLoaderBase extends TupleLoaderBase {
	
	PreparedStatement insertTupleLoader;
    PreparedStatement insertNodeLoader;
    PreparedStatement insertNodes;
    PreparedStatement insertTuples;
    PreparedStatement deleteTuples;
    PreparedStatement clearTupleLoader;
    PreparedStatement clearNodeLoader;
	
    int chunkSize;
    boolean amLoading; // flag for whether we're loading or deleting
    int tupleNum;
    
    Set<Long> seenNodes; // For supressing duplicate nodes
    
	public Layout2TupleLoaderBase(SDBConnection connection,
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
		Connection conn = this.connection().getSqlConnection();
		
		// Create the temporary tables
		connection().exec(getCreateTempNodes());
		connection().exec(getCreateTempTuples());
		
		// Prepare those statements
		insertNodeLoader = conn.prepareStatement(getInsertTempTuples());
		insertTupleLoader = conn.prepareStatement(getInsertTempNodes());
		insertNodes = conn.prepareStatement(getLoadNodes());
		insertTuples = conn.prepareStatement(getLoadTuples());
		if (getClearTempNodes() != null) clearNodeLoader = conn.prepareStatement(getClearTempNodes());
		if (getClearTempTuples() != null) clearTupleLoader = conn.prepareStatement(getClearTempTuples());
	}
	
	public void load(Node[] row) {
		if (!amLoading) {
			flush();
			amLoading = true;
		}
		
		if (row.length != this.getTableWidth())
			throw new SDBException("Tuple size mismatch");
		
		try {
			for (int i = 0; i < row.length; i++) {
				PreparedNode pNode = new PreparedNode(row[i]);
				if (seenNodes.add(pNode.hash)) // if true, this is new...
					pNode.addToStatement(insertNodeLoader);
				insertTupleLoader.setLong(i, pNode.hash);
			}
			insertTupleLoader.addBatch();
		} catch (SQLException e) {
			throw new SDBException("Problem adding to prepared loader statements");
		}
		
		tupleNum++;
		if (tupleNum >= chunkSize) flush();
	}

	public void unload(Node[] row) {
		if (amLoading) {
			flush();
			amLoading = false;
		}
		
		if (row.length != this.getTableWidth())
			throw new SDBException("Tuple size mismatch");
		
		try {
			for (int i = 0; i < row.length; i++) {
				PreparedNode pNode = new PreparedNode(row[i], false);
				deleteTuples.setLong(i, pNode.hash);
			}
			deleteTuples.addBatch();
		} catch (SQLException e) {
			throw new SDBException("Problem adding to prepared delete statements");
		}
		
		tupleNum++;
		if (tupleNum >= chunkSize) flush();
	}
	
	public void flush() {
		if (tupleNum == 0)
			return;
		try {
			if (amLoading) {
				insertNodeLoader.execute();
				insertTupleLoader.execute();
				insertNodes.execute();
				insertTuples.execute();
				if (clearNodeLoader != null) clearNodeLoader.execute();
				if (clearTupleLoader != null) clearTupleLoader.execute();
			} else {
				deleteTuples.execute();
			}
			connection().getSqlConnection().commit();
		} catch (SQLException e) {
			try {
				connection().getSqlConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new SDBException("Exception flushing", e);
		} finally {
			tupleNum = 0;
		}
	}
	
	/** These are the SQL 'bits' we use to construct the loader statements **/
	
	public String[] getNodeColTypes() {
		return new String[] {"BIGINT", "TEXT", "VARCHAR(10)", "  datatype VARCHAR("+ TableDescNodes.DatatypeUriLength+ ")", "INT"};
	}
	
	public String getTupleColType() {
		return "BIGINT";
	}
	
	public String[] getCreateTempTable() {
		return new String[] { "CREATE TEMPORARY TABLE" , "" };
	}
	
	public String getNodeLoader() {
		return "NNode" + hashCode();
	}
	
	public String getTupleLoader() {
		return "N" + this.getTableName() + hashCode();
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
	
	public String getLoadTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("INSERT INTO ").append(this.getTableName()).append(" \nSELECT DISTINCT ");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" , ");
			stmt.append(getTupleLoader()).append(".").append("t").append(i);
		}
		stmt.append("\nFROM ").append(getTupleLoader()).append("\n");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append("\n");
			stmt.append("LEFT JOIN ").append(this.getTableName()).append(" ON (t").append(i);
			stmt.append("=").append(this.getTableName()).append(".").append(this.getTableDesc().getColNames().get(i)).append(")");
		}
		stmt.append("\nWHERE\n");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" OR\n");
			stmt.append(this.getTableName()).append(".").append(this.getTableDesc().getColNames().get(i)).append(" IS NULL");
		}
		
		return stmt.toString();
	}
	
	public String getDeleteTuples() {
		StringBuilder stmt = new StringBuilder();
		
		stmt.append("DELETE FROM ").append(this.getTableName()).append(" \nWHERE ");
		stmt.append("\nWHERE\n");
		for (int i = 0; i < this.getTableWidth(); i++) {
			if (i != 0) stmt.append(" AND\n");
			stmt.append(this.getTableDesc().getColNames().get(i)).append(" = ?");
		}
		
		return stmt.toString();
	}
	
	public String getClearTempNodes() {
		return "DELETE FROM " + getNodeLoader();
	}
	
	public String getClearTempTuples() {
		return "DELETE FROM " + getTupleLoader();
	}
	
	/* Encapsulate the gory internals of breaking up nodes for the database */
	
	public static class PreparedNode
    {
        public long hash;
        public String lex;
        public String lang;
        public String datatype;
        public int typeId;
        public Integer valInt;
        public Double valDouble;
        public Timestamp valDateTime;
        
        PreparedNode(Node node)
        {
        	this(node, true);
        }
        
        PreparedNode(Node node, boolean computeVals)
        {
            lex = NodeLayout2.nodeToLex(node);
            ValueType vType = ValueType.lookup(node);
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
            
            if (computeVals) // don't need this for deleting
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
            }
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
