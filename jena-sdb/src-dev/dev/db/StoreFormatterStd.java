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

package dev.db;

import com.hp.hpl.jena.sdb.layout2.TableDescQuads;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.TableUtils;
import com.hp.hpl.jena.sdb.store.StoreFormatterBase;
import com.hp.hpl.jena.sdb.store.TableDesc;

public abstract class StoreFormatterStd extends StoreFormatterBase
{
    public StoreFormatterStd(SDBConnection conn)
    {
        super(conn) ;
    }
    
//    public void create()
//    {
//        format() ;
//        addIndexes() ;
//    }

    @Override
    public void format()
    { 
        formatTablePrefixes() ;
        formatTableNodes() ;
        formatTableTriples() ;
        formatTableQuads() ;
    }
    
    @Override
    public void truncate()
    {
        truncateTablePrefixes() ;
        truncateTableNodes() ;
        truncateTableTriples() ;
        truncateTableQuads() ;
    }
    
    @Override
    public void addIndexes()
    {
        addIndexesTableTriples() ;
        addIndexesTableQuads() ;
    }
    
    @Override
    public void dropIndexes()
    {
        dropIndexesTableTriples() ;
        dropIndexesTableQuads() ;
    }

    public static TableDescQuads descQ = new TableDescQuads() ;
    public static TableDescTriples descT = new TableDescTriples() ;
    

    
    protected void formatTableTriples() { formatTupleTable(descT) ; }
    protected void formatTableQuads()   { formatTupleTable(descQ) ; }
    
    abstract protected void formatTupleTable(TableDesc tableDesc) ;
    
    abstract protected void formatTableNodes() ;
    abstract protected void formatTablePrefixes() ;
    
    abstract protected void addIndexesTableTriples() ;
    abstract protected void addIndexesTableQuads() ;
    
    abstract protected void dropIndexesTableTriples() ;
    abstract protected void dropIndexesTableQuads() ;
    
    abstract protected void truncateTableTriples() ;
    abstract protected void truncateTableQuads() ;
    abstract protected void truncateTableNodes() ;
    abstract protected void truncateTablePrefixes() ;
    
    protected void dropTable(String tableName)
    {
        TableUtils.dropTable(connection(), tableName) ;
    }
}
