/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.sql.SQLUtils;
import com.hp.hpl.jena.sdb.store.StoreLoader;


public abstract class LoaderTriplesNodes
    extends SDBConnectionHolder
    implements StoreLoader, LoaderFmt
{
    private static Log log = LogFactory.getLog(LoaderTriplesNodes.class);
    //private static final String classShortName = Utils.classShortName(LoaderTriplesNodes.class)  ;
    
    // Delayed initialization until first bulk load.
    private boolean initialized = false ;
    
    // Are we adding or removing?
    private Boolean removingTriples = null ;
    
    boolean threading = true; // Do we want to thread?
    Thread commitThread = null ; // The loader thread
    final static PreparedTriple flushSignal = new PreparedTriple(); // Signal to thread to commit
    final static PreparedTriple finishSignal = new PreparedTriple(); // Signal to thread to finish
    ArrayBlockingQueue<PreparedTriple> queue ; // Pipeline to loader thread
    AtomicReference<Throwable> threadException ; // Placeholder for problems thrown in the thread
    AtomicBoolean threadFlushing ; // We spin lock on this when flushing (not ideal, but works)
    
    int count;
    int chunkSize = 20000;
    
    private boolean autoCommit ;  // State of the connection
    
    // The following do all the database work
    protected PreparedStatement insertTripleLoaderTable;
    protected PreparedStatement insertNodeLoaderTable;
    protected PreparedStatement insertNodes;
    protected PreparedStatement insertTriples;
    protected PreparedStatement deleteTriples;
    protected PreparedStatement clearTripleLoaderTable;
    protected PreparedStatement clearNodeLoaderTable;
    
    private Set<PreparedNode> seenNodes; // Contains nodes seen in a chunk, to suppress dupes
    
    public LoaderTriplesNodes(SDBConnection connection)
    {
        super(connection) ;
    }
    
    public void startBulkUpdate()
	{
    	try
    	{
    		init() ;
	        // Record the state of the JDBC connection as we start 
	        autoCommit = connection().getSqlConnection().getAutoCommit() ;
	        
	        if ( autoCommit )
	            connection().getSqlConnection().setAutoCommit(false) ;
	    }
	    catch (SQLException ex)
	    {
	    	log.error("Failure while starting bulk load: " + ex.getMessage());
	    	throw new SDBExceptionSQL("Failed to start up bulk loader", ex);
	    }
	}

	public void finishBulkUpdate()
	{
		flushTriples() ;
	    try {
	        if ( autoCommit )
	            connection().getSqlConnection().setAutoCommit(autoCommit) ;
	    } catch (SQLException ex)
	    { throw new SDBExceptionSQL("Failed to reset connection", ex) ; }
	}

	/**
     * Close this loader and finish the thread (if required)
     *
     */
    public void close()
    {
    	if (!initialized) return;
    	
    	try
    	{
    		if (threading && commitThread.isAlive())
    		{
    			queue.put(finishSignal);
    			while (commitThread.isAlive()) Thread.sleep(100);
    		}

    		insertTripleLoaderTable.close();
    		insertNodeLoaderTable.close();
    		insertNodes.close();
    		insertTriples.close();
    		deleteTriples.close();
    		if (clearTripleLoaderTable != null)
    			clearTripleLoaderTable.close();
    		if (clearNodeLoaderTable != null)
    			clearNodeLoaderTable.close();
    	}
    	catch (Exception e)
    	{
    		log.error("Problem closing loader: " + e.getMessage());
    		throw new SDBException("Problem closing loader", e);
    	}
    	finally
    	{
    		this.initialized = false;
    		this.commitThread = null;
    		this.seenNodes = null;
    		this.queue = null;
    	}
    }
    
    public void addTriple(Triple triple)
	{
    	updateStore(triple, false);
	}
    
    public void deleteTriple(Triple triple) 
    {
    	updateStore(triple, true);
    }

    private void updateStore(Triple triple, boolean forRemoval)
    {
		// Prepare our triple for loading. Helps with threaded loader.
	    PreparedTriple pTriple = new PreparedTriple();
	    pTriple.subject = new PreparedNode(triple.getSubject(), forRemoval);
	    pTriple.predicate = new PreparedNode(triple.getPredicate(), forRemoval);
	    pTriple.object = new PreparedNode(triple.getObject(), forRemoval);
	    pTriple.forRemoval = forRemoval;
	    
	    if (threading)
	    {
	        checkThreadStatus();
	        try
	        {
	        	queue.put(pTriple);
	        }
	        catch (InterruptedException e)
	        {
	        	log.error("Issue adding to queue: " + e.getMessage());
	        	throw new SDBException("Issue adding to queue" + e.getMessage(), e);
	        }
	    }
	    else
	    {
			try 
			{
				updateOneTriple(pTriple);
			} 
			catch (SQLException e)
			{
				try 
				{
					connection().getSqlConnection().rollback();
				} 
				catch (SQLException e1)
				{
					log.error("Error rolling back",e);
				}
				log.error("Problem adding triple: " + e.getMessage());
				throw new SDBExceptionSQL("Problem adding triple", e);
			}
	    }
	}

	/**
	 * Flush remain triples in queue to database. If threading this blocks until flush is complete.
	 */
	private void flushTriples()
	{
		if (threading)
	    {
			if (!commitThread.isAlive()) throw new SDBException("Thread has died");
	    	// finish up threaded load
	    	threadFlushing.set(true);
	    	try {
				queue.put(flushSignal);
				while (threadFlushing.get()) Thread.sleep(100);
	    	}
	    	catch (InterruptedException e)
	    	{
	    		log.error("Problem sending flush signal: " + e.getMessage());
	    		throw new SDBException("Problem sending flush signal", e);
	    	}
	    	checkThreadStatus();
	    }
		else
		{
			try 
			{
				commitTriples();
			} 
			catch (SQLException e) 
			{
				try 
				{
					connection().getSqlConnection().rollback();
				} 
				catch (SQLException e1) 
				{
					log.error("Error rolling back: " + e.getMessage());
					log.error("Error rolling back", e);
				}
				log.error("Problem commiting: " + e.getMessage());
				throw new SDBExceptionSQL("Problem commiting", e);
			}
		}
	}

	private void init() throws SQLException
	{
	    if ( initialized ) return ;
	    
	    createLoaderTable();
	    
		Connection conn = this.connection().getSqlConnection();
		insertTripleLoaderTable = conn.prepareStatement(getInsertTripleLoaderTable());
	    insertNodeLoaderTable = conn.prepareStatement(getInsertNodeLoaderTable());
	    insertNodes = conn.prepareStatement(getInsertNodes());
	    insertTriples = conn.prepareStatement(getInsertTriples());
	    deleteTriples = conn.prepareStatement(getDeleteTriples());
	    // These may be null for RDBs which support ON COMMIT DELETE ROWS
	    if (getClearTripleLoaderTable() != null)
	    	clearTripleLoaderTable = conn.prepareStatement(getClearTripleLoaderTable());
	    if (getClearNodeLoaderTable() != null)
	    	clearNodeLoaderTable = conn.prepareStatement(getClearNodeLoaderTable());
	    
	    count = 0;
	
	    seenNodes = new HashSet<PreparedNode>();
	    
	    if (threading)
	    {
	        queue = new ArrayBlockingQueue<PreparedTriple>(chunkSize);
	        threadException = new AtomicReference<Throwable>();
	        threadFlushing = new AtomicBoolean();
	        commitThread = new Thread(new Commiter());
	        commitThread.setDaemon(true);
	        commitThread.start();
	        log.debug("Threading started");
	    }
	    
	    initialized = true;
	}

	private void checkThreadStatus()
    {
    	if (threadException.get() != null)
        {
        	Throwable e = threadException.getAndSet(null);
        	if (e instanceof SQLException)
        		throw new SDBExceptionSQL("Loader thread exception", (SQLException) e);
        	else
        		throw new SDBException("Loader thread exception", e);
        }
    	if (!commitThread.isAlive())
    		throw new SDBException("Thread has died");
    }
    
    // Queue up a triple, committing if we have enough chunks
    private void updateOneTriple(PreparedTriple triple) throws SQLException
    {
    	// Ensure order of addition / removal
    	if (removingTriples != null && !removingTriples.equals(triple.forRemoval))
    	{
    		commitTriples();
    		removingTriples = triple.forRemoval;
    	}
    	if (removingTriples == null) removingTriples = triple.forRemoval;
    	
    	count++;
    	
    	if (!removingTriples)
    	{
    		addToInsert(insertNodeLoaderTable, triple.subject);
    		addToInsert(insertNodeLoaderTable, triple.predicate);
    		addToInsert(insertNodeLoaderTable, triple.object);
    	}
    	
    	insertTripleLoaderTable.setLong(1, triple.subject.hash);
    	insertTripleLoaderTable.setLong(2, triple.predicate.hash);
    	insertTripleLoaderTable.setLong(3, triple.object.hash);
    	insertTripleLoaderTable.addBatch();
    	
    	if (count >= chunkSize)
    		commitTriples();
    }

    private void addToInsert(PreparedStatement s, PreparedNode node)
        throws SQLException
    {
    	if (seenNodes.contains(node)) return; // Suppress dupes in batches
    	seenNodes.add(node);
    	
        s.setLong(1, node.hash);
        s.setString(2, node.lex);
        s.setString(3, node.lang);
        s.setString(4, node.datatype);
        s.setInt(5, node.typeId);
        if (node.valInt != null)
        	s.setInt(6, node.valInt);
        else
        	s.setNull(6, Types.INTEGER);
        if (node.valDouble  != null)
        	s.setDouble(7, node.valDouble);
        else
        	s.setNull(7, Types.DOUBLE);
        if (node.valDateTime != null)
        	s.setTimestamp(8, node.valDateTime);
        else
        	s.setNull(8, Types.TIMESTAMP);
        
        s.addBatch();
    }
    
    // Put the queued triples into the database
    private void commitTriples() throws SQLException
    {
        count = 0;
        seenNodes = new HashSet<PreparedNode>();
        
        if (removingTriples == null) // Nothing happened
        	return;
        
        insertTripleLoaderTable.executeBatch();
        
        if (!removingTriples)
        {
        	insertNodeLoaderTable.executeBatch();
        	insertNodes.execute();
        	insertTriples.execute();
        	if (clearNodeLoaderTable != null)
        		clearNodeLoaderTable.execute() ;
        }
        else
        {
        	deleteTriples.execute() ;
        }
        
        if (clearTripleLoaderTable != null)
        	clearTripleLoaderTable.execute();
        removingTriples = null;
        if ( autoCommit )
            // Commit the transaction if started outside of a transaction 
            connection().getSqlConnection().commit();
    }

    public void setChunkSize(int chunkSize)            { this.chunkSize = chunkSize ; }

    public int getChunkSize()                          { return this.chunkSize ; }

    public void setUseThreading(boolean useThreading)  { this.threading = useThreading ; }

    public boolean getUseThreading()                   { return this.threading ; }

    
    // ---- Bulk loader

    /**
     * We use these so the preparation (especially hashing) happens away from
     * the db load thread
     */

    static class PreparedTriple
    {
        PreparedNode subject;
        PreparedNode predicate;
        PreparedNode object;
        boolean forRemoval;
    }

    static class PreparedNode
    {
        long hash;
        String lex;
        String lang;
        String datatype;
        int typeId;
        Integer valInt;
        Double valDouble;
        Timestamp valDateTime;
        
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

    /**
     * The (very minimal) thread code
     */

    class Commiter implements Runnable
    {

        public void run()
        {
            log.debug("Running loader thread");
            threadException.set(null);
            while (true)
            {
            	try
            	{
            		PreparedTriple triple = queue.take();
            		if (triple == flushSignal)
            		{
            			commitTriples(); // force commit
            			threadFlushing.set(false);
            		}
            		else if (triple == finishSignal)
            		{
            			commitTriples(); // force commit
            			break;
            		}
            		else
            		{
            			updateOneTriple(triple);
            		}
            	}
            	catch (Throwable e)
            	{
            		try
            		{
						connection().getSqlConnection().rollback();
					} 
            		catch (SQLException e1) 
            		{
						log.error("Problem rolling back", e1);
					}
            		log.error("Error in thread: " + e.getMessage(), e);
            		threadException.set(e);
            	}
            }
        }
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
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
 *
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