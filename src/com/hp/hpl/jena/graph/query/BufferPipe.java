/*
  (c) Copyright 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: BufferPipe.java,v 1.7 2005-02-21 11:52:15 andy_seaborne Exp $
*/

package com.hp.hpl.jena.graph.query;

import EDU.oswego.cs.dl.util.concurrent.*;
import com.hp.hpl.jena.shared.*;

import java.util.*;

/**
    This class is a pipe between query threads, implemented as a bounded buffer.
	@author kers
*/
public class BufferPipe implements Pipe
    {
    private boolean open = true;
    private BoundedBuffer buffer = new BoundedBuffer( 5 );
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
        
    public void put( Domain d )
        { putAny( d ); }

    public void close()
        { putAny( finished );  }
    
    public void close( Exception e )
        { putAny( new Finished( e ) ); }

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
                    if (cause == null) open = false;
                    else throw cause;
                    }
                return open;
                }
            else
                return true;
            }
        else 
            return false; 
        }
        
    public Domain get()
        {
        if (hasNext() == false) throw new NoSuchElementException(); 
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
/*
    (c) Copyright 2002, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
