/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.hp.hpl.jena.sdb.SDBException;

import org.apache.commons.logging.LogFactory;

/** Wrapper around SQL ResultSets to aid tracking and abstract away from JDBC details */ 

public class SDBResultSet
{
    private ResultSet resultSet ;
    private boolean markedClosed = false ;
    
    public SDBResultSet(ResultSet resultSet) { this.resultSet = resultSet ; }
    
    public ResultSet getResultSet()
    { 
        if ( markedClosed )
        {
            LogFactory.getLog(SDBResultSet.class).warn("Result set already closed") ;
            return null ;
        }
        return resultSet ;
    }
    
    public boolean next()
    { 
        try { return resultSet.next() ; }
        catch (SQLException ex)
        { throw exception("next", ex) ; }
    }
    
    public boolean previous()
    { 
        try { return resultSet.previous() ; }
        catch (SQLException ex)
        { throw exception("previous", ex) ; }
    }
    
    
    
    public int getInt(String name)
    {   checkState() ;
        try { 
            return resultSet.getInt(name) ;
        } catch (SQLException ex) { throw exception("getInt", ex) ; }
    }
    
    private SDBException exception(String who, SQLException ex)
    {
        return new SDBException("SQLException: SDBResultSet."+who+": "+ex.getMessage()) ;
    }
    
    private void checkState()
    {
        if ( markedClosed )
            throw new SDBException("ResultSet already closed") ;
    }
    
    public void close()
    { 
        RS.close(resultSet) ;
        markedClosed = true ;
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