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

package org.apache.jena.sdb.store;

import org.apache.jena.sdb.Store ;
import org.apache.jena.sdb.StoreDesc ;
import org.apache.jena.sdb.compiler.QueryCompilerFactory ;
import org.apache.jena.sdb.core.sqlnode.GenerateSQL ;
import org.apache.jena.sdb.layout2.StoreBase ;
import org.apache.jena.sdb.layout2.TableDescNodes ;
import org.apache.jena.sdb.layout2.TableDescQuads ;
import org.apache.jena.sdb.layout2.TableDescTriples ;
import org.apache.jena.sdb.sql.SDBConnection ;
import org.apache.jena.sdb.util.HSQLUtils ;
import org.apache.jena.sdb.util.StoreUtils ;



public abstract class StoreBaseHSQL extends StoreBase
{
    protected boolean currentlyOpen = true ;
    
    public StoreBaseHSQL(SDBConnection connection, StoreDesc desc,
                         StoreFormatter formatter,
                         StoreLoader loader,
                         QueryCompilerFactory compilerF,
                         SQLBridgeFactory sqlBridgeF,
                         TableDescTriples tripleTableDesc,
                         TableDescQuads quadTableDesc,
                         TableDescNodes nodeTableDesc)
    {
        super(connection, desc, formatter, loader, compilerF, sqlBridgeF, 
              new GenerateSQL(), tripleTableDesc, quadTableDesc, nodeTableDesc) ;
    }

    @Override 
    public void close()
    {
        if ( currentlyOpen ) {
        	super.close() ;
            // This interacts with JDBC connection management 
            HSQLUtils.shutdown(getConnection()) ;
        }
        
        currentlyOpen = false ; 
        super.close();
    }

    public static void close(Store store)
    {
        if ( StoreUtils.isHSQL(store) )
            ((StoreBaseHSQL)store).close() ;
    }
    
    public static void checkpoint(Store store)
    {
        if ( StoreUtils.isHSQL(store) )
            ((StoreBaseHSQL)store).checkpoint() ;
    }
    
    public void checkpoint()
    { 
        if ( currentlyOpen ) 
            HSQLUtils.checkpoint(getConnection()) ;
    }
    
}
