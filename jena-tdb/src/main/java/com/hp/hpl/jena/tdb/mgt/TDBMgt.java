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

package com.hp.hpl.jena.tdb.mgt;

import com.hp.hpl.jena.sparql.mgt.SystemInfo ;
import com.hp.hpl.jena.tdb.TDB ;

public class TDBMgt
{
    public static void init()
    {
        String NS = TDB.PATH ;
        SystemInfo sysInfo = new SystemInfo(TDB.tdbIRI, TDB.PATH, TDB.VERSION, TDB.BUILD_DATE) ;
//        ARQMgt.register(NS+".system:type=SystemInfo", sysInfo) ;
//        ARQMgt.register(NS+".system2:type=SystemInfo", new TDBSystemInfo()) ; 
    }
}
