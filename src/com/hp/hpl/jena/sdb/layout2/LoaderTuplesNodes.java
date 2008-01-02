/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.StoreLoaderPlus;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sdb.store.TupleLoader;


public class LoaderTuplesNodes
    extends SDBConnectionHolder
    implements StoreLoaderPlus
{
    private static Log log = LogFactory.getLog(LoaderTuplesNodes.class);
    //private static final String classShortName = Utils.classShortName(LoaderTriplesNodes.class)  ;
    
    // Delayed initialization until first bulk load.
    private boolean initialized = false ;
    
    boolean threading = true; // Do we want to thread?
    Thread commitThread = null ; // The loader thread
    final static TupleChange flushSignal = new TupleChange(); // Signal to thread to commit
    final static TupleChange finishSignal = new TupleChange(); // Signal to thread to finish
    ArrayBlockingQueue<TupleChange> queue ; // Pipeline to loader thread
    AtomicReference<Throwable> threadException ; // Placeholder for problems thrown in the thread
    Object threadFlushing = new Object(); // We lock on this when flushing
    
    Map<String, TupleLoader> tupleLoaders;
    TupleLoader currentLoader;
    
    int count;
    int chunkSize = 20000;
    
	private Class<? extends TupleLoader> tupleLoaderClass;

	private Store store;
	
    public LoaderTuplesNodes(SDBConnection connection, Class<? extends TupleLoader> tupleLoaderClass)
    {
        super(connection) ;
        this.tupleLoaderClass = tupleLoaderClass ;
    }
    
    public void setStore(Store store) {
    	this.store = store;
    }
    
    public void startBulkUpdate()
	{
    	init() ;
	}

	public void finishBulkUpdate()
	{
		flushTriples() ;
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
    			commitThread.join();
    		}
    		else
    		{
    			flushTriples();
    		}
    	}
    	catch (Exception e)
    	{
    		log.error("Problem closing loader: " + e.getMessage());
    		throw new SDBException("Problem closing loader", e);
    	}
    	finally
    	{
    		for (TupleLoader loader: this.tupleLoaders.values()) loader.close();
    		this.initialized = false;
    		this.commitThread = null;
    		this.queue = null;
    		this.tupleLoaderClass = null;
    		this.tupleLoaders = null;
    	}
    }
    
    public void addTriple(Triple triple)
	{
    	updateStore(new TupleChange(true, store.getTripleTableDesc(), triple.getSubject(), triple.getPredicate(), triple.getObject()));
	}
    
    public void deleteTriple(Triple triple) 
    {
    	updateStore(new TupleChange(false, store.getTripleTableDesc(), triple.getSubject(), triple.getPredicate(), triple.getObject()));
    }
    
	public void addQuad(Node g, Node s, Node p, Node o) {
		updateStore(new TupleChange(true, store.getQuadTableDesc(), g, s, p, o));		
	}

	public void addTuple(TableDesc t, Node... nodes) {
		updateStore(new TupleChange(true, t, nodes));
	}

	public void deleteQuad(Node g, Node s, Node p, Node o) {
		updateStore(new TupleChange(false, store.getQuadTableDesc(), g, s, p, o));
	}

	public void deleteTuple(TableDesc t, Node... nodes) {
		updateStore(new TupleChange(false, t, nodes));
	}
    
    static class TupleChange {
    	public Node[] tuple;
    	public boolean toAdd;
    	public TableDesc table;
    	
    	public TupleChange(boolean toAdd, TableDesc table, Node... tuple) {
    		this.tuple = tuple;
    		this.toAdd = toAdd;
    		this.table = table;
    	}
    	
    	public TupleChange() {
    		tuple = null;
    		table = null;
    		toAdd = false;
    	}
    }
    
    private void updateStore(TupleChange tuple)
    {   
	    if (threading)
	    {
	        checkThreadStatus();
	        try
	        {
	        	queue.put(tuple);
	        }
	        catch (InterruptedException e)
	        {
	        	log.error("Issue adding to queue: " + e.getMessage());
	        	throw new SDBException("Issue adding to queue" + e.getMessage(), e);
	        }
	    }
	    else
	    {
			updateOneTuple(tuple);
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
	    	try {
	    		synchronized (threadFlushing) {
	    			queue.put(flushSignal);
	    			threadFlushing.wait();
	    		}
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
			commitTuples();
		}
	}

	private void init()
	{
	    if ( initialized ) return ;
	    
	    tupleLoaders = new HashMap<String, TupleLoader>();
	    currentLoader = null;
	    
	    count = 0;
	    
	    if (threading)
	    {
	        queue = new ArrayBlockingQueue<TupleChange>(chunkSize);
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
		Throwable e = threadException.getAndSet(null);
    	if (e != null)
        {
        	if (e instanceof SQLException)
        		throw new SDBExceptionSQL("Loader thread exception", (SQLException) e);
        	else if (e instanceof RuntimeException)
        		throw (RuntimeException) e;
        	else
        		throw new SDBException("Loader thread exception", e);
        }
    	if (!commitThread.isAlive())
    		throw new SDBException("Thread has died");
    }
    
    // Queue up a triple, committing if we have enough chunks
    private void updateOneTuple(TupleChange tuple)
    {
    	if (currentLoader == null || !currentLoader.getTableDesc().getTableName().equals(tuple.table.getTableName())) {
    		
    		commitTuples(); // mode is changing, so commit
    		
    		currentLoader = tupleLoaders.get(tuple.table.getTableName());
    		if (currentLoader == null) { // make a new loader
    			try {
					currentLoader =
						tupleLoaderClass.getConstructor(SDBConnection.class, TableDesc.class, 
								Integer.TYPE).newInstance(connection(), tuple.table, chunkSize);
				} catch (Exception e) {
					throw new SDBException("Problem making new tupleloader", e);
				}
				currentLoader.start();
				tupleLoaders.put(tuple.table.getTableName(), currentLoader);
    		}
    	}
    	
    	if (tuple.toAdd) currentLoader.load(tuple.tuple);
    	else currentLoader.unload(tuple.tuple);
    }
    
    private void commitTuples()
    {
    	if (currentLoader != null) {
    		currentLoader.finish();
    	}
    }
    
    public void setChunkSize(int chunkSize)            { this.chunkSize = chunkSize ; }

    public int getChunkSize()                          { return this.chunkSize ; }

    public void setUseThreading(boolean useThreading)  { this.threading = useThreading ; }

    public boolean getUseThreading()                   { return this.threading ; }

    
    // ---- Bulk loader

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
            		TupleChange tuple = queue.take();
            		if (tuple == flushSignal)
            		{
            			synchronized (threadFlushing) {
            				try {
            					commitTuples();
            				} catch (Throwable e) { handleIssue(e); }
            				
            				threadFlushing.notify();
            			}
            		}
            		else if (tuple == finishSignal)
            		{
            			try {
            				commitTuples(); // force commit
            			} catch (Throwable e) { handleIssue(e); }
            			
            			break;
            		}
            		else
            		{
            			updateOneTuple(tuple);
            		}
            	}
            	catch (Throwable e)
            	{
            		handleIssue(e);
            	}
            }
        }

		private void handleIssue(Throwable e) {
    		log.error("Error in thread: " + e.getMessage(), e);
    		threadException.set(e);
		}
    }
}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
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