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

package org.apache.jena.riot;

import org.apache.jena.riot.adapters.TS_RIOTAdapters ;
import org.apache.jena.riot.lang.TS_Lang ;
import org.apache.jena.riot.out.TS_Out ;
import org.apache.jena.riot.process.TS_Process ;
import org.apache.jena.riot.protobuf.TS_RDFProtobuf;
import org.apache.jena.riot.resultset.TS_ResultSetRIOT ;
import org.apache.jena.riot.rowset.TS_RowSetRIOT;
import org.apache.jena.riot.stream.TS_IO2 ;
import org.apache.jena.riot.system.TS_RiotSystem ;
import org.apache.jena.riot.thrift.TS_RDFThrift ;
import org.apache.jena.riot.tokens.TS_Tokens ;
import org.apache.jena.riot.web.TS_RiotWeb ;
import org.apache.jena.riot.writer.TS_RiotWriter ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TS_Tokens.class
    , TS_Out.class
    , TS_Lang.class
    , TS_RiotGeneral.class
    , TS_IO2.class
    , TS_RIOTAdapters.class
    , TS_Process.class
    , TS_RiotWriter.class
    , TS_RiotSystem.class
    , TS_RiotWeb.class
    , TS_ResultSetRIOT.class
    , TS_RDFProtobuf.class
    , TS_RDFThrift.class
    , TS_RowSetRIOT.class
    // And scripted tests.
})

public class TC_Riot
{ }
