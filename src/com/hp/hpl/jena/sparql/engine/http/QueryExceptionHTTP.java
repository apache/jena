/*
 * (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.engine.http;

import com.hp.hpl.jena.query.QueryException;

/** Exception class for all operations in the Joseki client library.
 *  Error codes are as HTTP statsus codes.
 * 
 * @author      Andy Seaborne
 */
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
    
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer() ;
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


/*
 *  (c) Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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
