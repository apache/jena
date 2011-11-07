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

package com.hp.hpl.jena.query;

import java.util.concurrent.TimeUnit ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.util.FileManager ;

/** A interface for a single execution of a query. */


public interface QueryExecution
{
    /** Set the FileManger that might be used to load files.
     *  May not be supported by all QueryExecution implementations.  
     */
    public void setFileManager(FileManager fm) ;
    
    /** Set the initial association of variables and values.
     * May not be supported by all QueryExecution implementations.
     * @param binding
     */
    public void setInitialBinding(QuerySolution binding) ;

//    /** Set a table of initial associations of variables and values.
//     * May not be supported by all QueryExecution implementations.
//     * @param binding
//     */
//    public void setInitialBindings(ResultSet table) ;
    
    /**
     * The dataset against which the query will execute.
     * May be null, implying it is expected that the query itself
     * has a dataset description. 
     */
    public Dataset getDataset() ;
    
    /** The properties associated with a query execution -  
     *  implementation specific parameters  This includes
     *  Java objects (so it is not an RDF graph).
     *  Keys should be URIs as strings.  
     *  May be null (this implementation does not provide any configuration).
     */ 
    public Context getContext() ;
    
    /** The query associated with a query execution.  
     *  May be null (QueryExecution may have been created by other means)
     */ 
    public Query getQuery() ;

    /** Execute a SELECT query */
	public ResultSet execSelect();
    
    /** Execute a CONSTRUCT query */
    public Model execConstruct();

    /** Execute a CONSTRUCT query, putting the statements into 'model'.
     *  @return Model The model argument for casaded code.
     */
    public Model execConstruct(Model model);

    /** Execute a DESCRIBE query */
    public Model execDescribe();

    /** Execute a DESCRIBE query, putting the statements into 'model'.
     *  @return Model The model argument for casaded code.
     */
    public Model execDescribe(Model model);

    /** Execute an ASK query */
    public boolean execAsk();
    
	/** Stop in mid execution.
	 *  This method can be called in parallel with other methods on the
     *  QueryExecution object.
	 *  There is no guarantee that the concrete implementation actual
     *  will stop or that it will do so immediately.
     *  No operations on the query execution or any associated
     *  result set are permitted after this call and may cause exceptions to be thrown.
	 */

	public void abort();
	
    /** Close the query execution and stop query evaluation as soon as convenient.
     *  It is important to close query execution objects in order to release
     *  resources such as working memory and to stop the query execution.
     *  Some storage subsystems require explicit ends of operations and this
     *  operation will cause those to be called where necessary.
     *  No operations on the query execution or any associated
     *  result set are permitted after this call.
     *  This method should not be called in parallel with other methods on the
     *  QueryExecution object.
     */
	public void close();
	
	/** Set a timeout on the query execution.
	 * Processing will be aborted after the timeout (which starts when the approprate exec call is made).
	 * Not all query execution systems support timeouts.
	 * A timeout of less than zero means no timeout.
	 */
	
	public void setTimeout(long timeout, TimeUnit timeoutUnits) ;
	
	/** Set time, in milliseconds 
	 * @see #setTimeout(long, TimeUnit)
	 */
	public void setTimeout(long timeout) ;
    
	/** Set timeouts on the query execution; the first timeout refers to time to first result, 
	 * the second refers to overall query execution after the first result.  
	 * Processing will be aborted if a timeout expires.
	 * Not all query execution systems support timeouts.
	 * A timeout of less than zero means no timeout; this can be used for timeout1 or timeout2.
	 */

	//public void setTimeout(long timeout1, long timeout2) ;
	public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) ;

    /** Set time, in milliseconds
     *  @see #setTimeout(long, TimeUnit, long, TimeUnit)
     */
    public void setTimeout(long timeout1, long timeout2) ;
    
    //	/** Say whether this QueryExecution is useable or not.
//	 * An active execution is one that has not been closed, ended or aborted yet.
//     * May not be supported or meaningful for all QueryExecution implementations.
//     * aborted queries may not immediate show as no longer active.
//     * This should not be called in parallel with other QueryExecution methods. 
//     */  
//    public boolean isActive() ;
}
