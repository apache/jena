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

package com.hp.hpl.jena.graph.query;

import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.QueryStageException;

/**
    This class is a pipe between query threads, implemented as a bounded buffer.
	@author kers
*/
public class BufferPipe implements Pipe
    {
    private boolean open = true;
    private BlockingQueue<Object> buffer = new ArrayBlockingQueue<Object>( 5 );
    private Object pending = null;
    
    public static class Finished
        {
        protected RuntimeException e;
        
        public Finished() {}
        
        public Finished( Exception e ) { this.e = new QueryStageException( e ); }
        
        public RuntimeException getCause() { return e; }
        }
    
    private static final Finished finished = new Finished();
     
    public BufferPipe()
        { }

    /** put something into the pipe; take care of BoundedBuffer's checked exceptions */
    private Object fetch()
        {
        try { return buffer.take(); }
        catch (Exception e) { throw new BoundedBufferTakeException( e ); }
        }

    /** get something from the pipe; take care of BoundedBuffer's checked exceptions */
    private void putAny( Object d )
        {
        try { buffer.put( d ); return; }
        catch (Exception e) { throw new BoundedBufferPutException( e ); }
        }
        
    @Override
    public void put( Domain d )
        { putAny( d ); }

    @Override
    public void close()
        { putAny( finished );  }
    
    @Override
    public void close( Exception e )
        { putAny( new Finished( e ) ); }

    @Override
    public boolean hasNext()
        {
        if (open)
            {
            if (pending == null)
                {
                pending = fetch();
                if (pending instanceof Finished) 
                    {
                    Finished end = (Finished) pending;
                    RuntimeException cause = end.getCause();
                    if (cause == null) 
                        open = false;
                    else 
                        {
                        PatternStageBase.log.debug( "BufferPipe has recieved and rethrown an exception", cause );
                        throw cause;
                        }
                    }
                return open;
                }
            else
                return true;
            }
        else 
            return false; 
        }
        
    @Override
    public Domain get()
        {
        if (hasNext() == false) throw new NoSuchElementException(); 
        if (!(pending instanceof Domain)) throw new RuntimeException( pending.getClass().toString() );
        try { return (Domain) pending; } finally { pending = null; } 
        }

    /**
        Exception to throw if a <code>take</code> throws an exception.
    */
    public static class BoundedBufferTakeException extends JenaException
        { BoundedBufferTakeException( Exception e ) { super( e ); }  }
    
    /**
        Exception to throw if a <code>put</code> throws an exception.
    */
    public static class BoundedBufferPutException extends JenaException
        { BoundedBufferPutException( Exception e ) { super( e ); }  }
    }
