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

/**
    A Pipe is anything that can be used to get and put Domain objects; it can be 
    closed, and it can be tested for whether more elements are available. A pipe 
    may be closed with an exception, in which case hasNext/get will fail with that 
    exception when they are called.
    
	@author kers
*/
public interface Pipe
    {
    /**
        Answer true iff there are more elements for <code>get()</code> to get. If
        the pipe was closed with an exception, throw that exception.
    */
    public boolean hasNext();
    
    /**
        Answer the next element if there is one, otherwise throw a NoSuchElementException.
     */
    public Domain get();
    
    /**
        Put a domain element into the pipe for later extraction.
    */
    public void put( Domain d );
    
    /**
        Close the pipe. hasNext() will deliver <code>false</code>, and 
        <code>get</code> will throw an exception.
    */
    public void close();
    
    /**
         Close the pipe (see close()) and record <code>e</code> as its termination
         status. Any <code>get</code> from the pipe must then fail, throwing an
         exception.
         
         @param e the exception that caused the pipe to be closed
    */
    public void close( Exception e );
    }
