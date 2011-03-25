/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 * Includes software from the Apache Software Foundation - Apache Software License (JENA-29)
 */

package com.hp.hpl.jena.query;

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
	
	public void setTimeout(long timeout) ;
    
	/** Set timeouts on the query execution; the first timeout refers to time to first result, 
	 * the second refers to overall query execution after the first result.  
	 * Processing will be aborted if a timeout expires.
	 * Not all query execution systems support timeouts.
	 * A timeout of less than zero means no timeout; this can be used for timeout1 or timeout2.
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

/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
