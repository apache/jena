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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlConstant;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.TableDesc;
import com.hp.hpl.jena.sdb.store.TupleLoaderOne;

public class TupleLoaderSimple extends TupleLoaderOne
{
    private static Logger log = LoggerFactory.getLogger(TupleLoaderSimple.class);
    private EncoderDecoder codec ;
    
    public TupleLoaderSimple(SDBConnection connection, TableDesc tableDesc, EncoderDecoder codec)
    {
        super(connection, tableDesc) ;
        this.codec = codec ;
    }

    @Override
    public SqlConstant getRefForNode(Node node) throws SQLException
    {
        return new SqlConstant(codec.encode(node)) ;
    }

    @Override
    public SqlConstant insertNode(Node node) throws SQLException
    {
        return new SqlConstant(codec.encode(node)) ;
    }
    
    
}
