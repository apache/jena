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

package com.hp.hpl.jena.sparql.engine.http;

import com.hp.hpl.jena.query.QueryException ;

/** Exception class for all operations in the SPARQL client library.
 *  Error codes are as HTTP status codes. */
public class QueryExceptionHTTP extends QueryException
{
    private static final long serialVersionUID = 99L;  // Serilizable.
    public static final int noResponseCode = -1234 ;
    private int responseCode = noResponseCode ;
    private String responseMessage = null ;

    // Codes for extra errors.  We use HTTP error codes so
    // these are negative to avoid clashes
    public static final int NoServer = -404 ;

    /**
     * Constructor for QueryExceptionHTTP.
     * @param responseCode
     * @param responseMessage
     */
    public QueryExceptionHTTP(int responseCode, String responseMessage)
    {
        super(responseMessage) ;
        this.responseCode = responseCode ;
        this.responseMessage = responseMessage ;
    }
    

    /**
     * Constructor for QueryExceptionHTTP.
     * @param responseCode
     */
    public QueryExceptionHTTP(int responseCode)
    {
        super() ;
        this.responseCode = responseCode ;
        this.responseMessage = null ;
    }
    
    /** The code for the reason for this exception
     * @return responseCode
     */  
    public int getResponseCode() { return responseCode ; }
    
    
    /** The messge for the reason for this exception
     * @return message
     */  
    public String getResponseMessage() { return responseMessage ; }

    /**
     * Constructor for HttpException used for some unexpected execution error.
     * @param cause
     */
    public QueryExceptionHTTP(Throwable cause)
    {
        super(cause);
        this.responseCode = noResponseCode ;
        this.responseMessage = null ;
    }
    
    public QueryExceptionHTTP(String msg, Throwable cause)
    {
        super(msg, cause);
        this.responseCode = noResponseCode ;
        this.responseMessage = msg ;
    }
    
    public QueryExceptionHTTP(int responseCode, String message, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder() ;
        sb.append("HttpException: ") ;
        int code = getResponseCode() ;
        if ( code != QueryExceptionHTTP.noResponseCode )
        {
            sb.append(code) ;
            if ( getResponseMessage() != null )
            {
                sb.append(" ") ;
                sb.append(getResponseMessage()) ;
            }
        }
        else
        {
            sb.append(getCause().toString()+": "+getMessage()) ;
        }
        return sb.toString() ; 
    }
}
