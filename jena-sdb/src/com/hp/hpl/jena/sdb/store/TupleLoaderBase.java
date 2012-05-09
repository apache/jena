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

package com.hp.hpl.jena.sdb.store;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionHolder;

/** Track whether multiple loads overlap. */

public abstract class TupleLoaderBase
    extends SDBConnectionHolder
    implements TupleLoader
{
    boolean active = false ;
    private int tableWidth ;
    private TableDesc tableDesc ;

    protected TupleLoaderBase(SDBConnection connection, TableDesc tableDesc)
    {
        this(connection) ;
        setTableDesc(tableDesc) ;
    }

    protected TupleLoaderBase(SDBConnection connection)
    {
        super(connection) ;
    }
    
    public String getTableName() { return tableDesc.getTableName() ; }

    @Override
    public TableDesc getTableDesc() { return tableDesc ; }
    @Override
    public void setTableDesc(TableDesc tDesc)
    { 
        this.tableDesc = tDesc ;
        this.tableWidth = tableDesc.getColNames().size() ;
    }
    
    //public List<String> getColumnNames() { return tableDesc.getColNames() ; }
    
    protected int getTableWidth() { return tableWidth ; }
    
    @Override
    public void start()
    {
        if ( active )
            throw new SDBException("Bulk loader already active") ;
        active = true ;
    }
    
    @Override
    public void finish()
    {
        active = false ;
    }
    
    @Override
    public void close()
    {
    	finish(); // make sure we're done
    }
}
