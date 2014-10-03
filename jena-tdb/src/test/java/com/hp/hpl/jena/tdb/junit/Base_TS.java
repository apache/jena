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

package com.hp.hpl.jena.tdb.junit;

import org.apache.log4j.Level ;
import org.apache.log4j.Logger ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;

import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderLib ;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation ;
import com.hp.hpl.jena.tdb.sys.SystemTDB ;

public class Base_TS
{
    static Level level = null ;
    static ReorderTransformation rt = null ;
    
    @BeforeClass static public void beforeClass()   
    {
        rt = SystemTDB.defaultReorderTransform ;
        level = Logger.getLogger("com.hp.hpl.jena.tdb.info").getLevel() ;
        Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(Level.FATAL) ;
        SystemTDB.defaultReorderTransform = ReorderLib.identity() ;
        rt = SystemTDB.defaultReorderTransform ;
    }
    
    @AfterClass static public void afterClass()
    {
        if ( level != null )
            Logger.getLogger("com.hp.hpl.jena.tdb.info").setLevel(level) ;
        SystemTDB.defaultReorderTransform = rt ;
    }
}
