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

package com.hp.hpl.jena.sdb.layout1;

import java.sql.SQLException;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.store.StoreFormatterBase;

public abstract class FormatterSimple extends StoreFormatterBase
{
    public static int UriWidth = 500 ; 
    
    public FormatterSimple(SDBConnection connection)
    { 
        super(connection) ;
    }
    
    @Override
    public void addIndexes()
    {
        try {
            connection().exec("CREATE INDEX PredObj ON "+TableDescSPO.name()+" (p, o)") ;
            connection().exec("CREATE INDEX ObjSubj ON "+TableDescSPO.name()+" (o, s)") ;
        } catch (SQLException ex)
        {
            throw new SDBException("SQLException indexing table 'Triples'",ex) ;
        }
    }
    
    @Override
    public void dropIndexes()
    {
        try {
            connection().exec("DROP INDEX PredObj") ;
            connection().exec("DROP INDEX ObjSubj") ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException dropping indexes for table 'Triples'",ex) ; }
    }
}
