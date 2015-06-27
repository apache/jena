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

package org.apache.jena.sdb.layout1;


import java.util.List;

import org.apache.jena.sdb.core.SDBRequest ;
import org.apache.jena.sdb.core.sqlnode.SqlNode ;
import org.apache.jena.sdb.store.SQLBridge ;
import org.apache.jena.sdb.store.SQLBridgeFactory ;
import org.apache.jena.sparql.core.Var ;

public class SQLBridgeFactory1 implements SQLBridgeFactory
{
    private EncoderDecoder codec ;

    public SQLBridgeFactory1(EncoderDecoder codec)
    {
        this.codec = codec ;
    }
    
    @Override
    public SQLBridge create(SDBRequest request, SqlNode sqlNode, List<Var> projectVars)
    {
        return new SQLBridge1(request, sqlNode, projectVars, codec) ;
    }
}
