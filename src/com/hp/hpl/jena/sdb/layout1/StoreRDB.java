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

import java.sql.Connection;
import java.sql.SQLException;

import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.layout2.TableDescTriples;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;

/** Store class for the Jena2 databse layout : query-only,
 *  not update via this route (use ModelRDB as normal). 
 * 
 * @author Andy Seaborne
 */

public class StoreRDB extends StoreBase1
{
    private ModelRDB model ;

    public StoreRDB(StoreDesc desc, ModelRDB model)
    {
       this(desc, model, new TableDescRDB(), new CodecRDB(model)) ;
    }    
    
    private StoreRDB(StoreDesc desc, ModelRDB model, TableDescTriples triples, EncoderDecoder codec)
    {
        super(makeSDBConnection(model), desc, 
              null, // Formatter.
              null, // Loader
              new QueryCompilerFactory1(codec),
              new SQLBridgeFactory1(codec),
              new GenerateSQL(),
              triples) ;
        
        this.model = model ;
    }
    
    public static SDBConnection makeSDBConnection(ModelRDB model)
    {
        try {
           // TODO Cope with no real connection 
            Connection jdbc = model.getConnection().getConnection() ;
            return new SDBConnection(jdbc) ; 
        } catch (SQLException ex) { throw new SDBExceptionSQL("StoreRDB", ex) ; }
    }
    
    public ModelRDB getModel() { return model ; }
    
    @Override
    public long getSize() { return model.size(); }
}
