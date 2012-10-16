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

package com.hp.hpl.jena.sdb.graph;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.shared.JenaException;


public class TransactionHandlerSDB implements TransactionHandler
{
    static private Logger log = LoggerFactory.getLogger(TransactionHandlerSDB.class) ;

    Connection sqlConnection ;
    boolean inTransaction ;
    
    public TransactionHandlerSDB(SDBConnection connection)
    { 
        this.sqlConnection = connection.getSqlConnection() ;
        this.inTransaction = false ;
    }
    
    @Override
    public boolean transactionsSupported() { return true ; }
    
    // Simplistic
    
    @Override
    public synchronized void begin()
    {
        if ( inTransaction )
        {
            log.warn("beginTransaction: Already in a transaction") ;
            throw new SDBException("Already in transaction") ;
        }
        try {
            sqlConnection.setAutoCommit(false) ;
            inTransaction = true ;
        } catch (SQLException ex) { new SDBExceptionSQL("begin", ex) ; }
    }
    
    @Override
    public synchronized void commit()
    {
        if ( ! inTransaction )
        {
            log.warn("commit: Not in a transaction") ;
            return ;
        }
        try {
            sqlConnection.commit() ;
            sqlConnection.setAutoCommit(true) ;
            inTransaction = false ;
        } catch (SQLException ex) { new SDBExceptionSQL("commit", ex) ; }
    } 
    
    @Override
    public synchronized void abort()
    {
        if ( ! inTransaction )
        {
            log.warn("abort: Not in a transaction") ;
            return ;
        }
        try {
            sqlConnection.rollback() ;
            sqlConnection.setAutoCommit(true) ;
            inTransaction = false ;
        } catch (SQLException ex) { new SDBExceptionSQL("abort", ex) ; }
    }

    public synchronized void abortFinally()
    {
        // Abort if needed.
        if ( ! inTransaction )
            return ;
        abort() ;
    }

    public void abortSilent()
    { try { abortFinally() ; } catch (SDBExceptionSQL ex) {} } 

    
    @Override
    public Object executeInTransaction(Command c)
    {
        try {
            begin() ;
            Object result = c.execute();
            commit();
            return result;
        } 
        catch (SDBExceptionSQL e) { abortFinally() ; throw e ; } 
        catch (JenaException e)   { abortFinally() ; throw e ; }
        catch (Throwable e) { abortFinally() ; throw new SDBException(e) ; } // Pass Graph tests.
    }
}
