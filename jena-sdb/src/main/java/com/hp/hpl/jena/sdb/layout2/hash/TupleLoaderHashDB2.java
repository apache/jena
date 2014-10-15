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

package com.hp.hpl.jena.sdb.layout2.hash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;

public class TupleLoaderHashDB2 extends TupleLoaderHashBase {
    
    private static Logger log = LoggerFactory.getLogger(TupleLoaderHashDB2.class);
    
    public TupleLoaderHashDB2(SDBConnection connection, TableDesc tableDesc,
            int chunkSize) {
        super(connection, tableDesc, chunkSize);
    }
    
    @Override
    public String[] getNodeColTypes() {
        return new String[] {"BIGINT", "CLOB", "VARCHAR(10)", "VARCHAR("+TableDescNodes.DatatypeUriLength+")", "INTEGER"};
    }
    
    @Override
    public String getTupleColType() {
        return "BIGINT";
    }
    
    @Override
    public String[] getCreateTempTable() {
        // Not temporary : may revisit but they do need (1) correct permissions and (2) DECLARE-ing 
        return new String[] { "CREATE TABLE " , " CCSID UNICODE" };
    }
    
    @Override
    public String getClearTempNodes() {
        return "DELETE FROM "+getNodeLoader()+" ";
    }
    
    @Override
    public String getClearTempTuples() {
        return "DELETE FROM "+getTupleLoader()+" ";
    }
    
    @Override
    public String getLoadNodes() {
        return "LOCK TABLE Nodes IN EXCLUSIVE MODE; " + super.getLoadNodes();
    }
}
