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

package org.apache.jena.tdb2.junit;

import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderLib;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import org.apache.jena.tdb2.sys.SystemTDB;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class Base_TS
{
    static String level = null ;
    static ReorderTransformation rt = null ;
    
    @BeforeClass static public void beforeClass()
    {
        rt = SystemTDB.getDefaultReorderTransform();
        level = LogCtl.getLevel("org.apache.jena.tdb.info") ;
        LogCtl.setLevel("org.apache.jena.tdb.info", "FATAL");
        LogCtl.setLevel("org.apache.jena.tdb2.info", "FATAL");
        
        SystemTDB.setDefaultReorderTransform(ReorderLib.identity());
    }

    @AfterClass static public void afterClass()
    {
        if ( level != null ) {
            LogCtl.setLevel("org.apache.jena.tdb.info", level);
            LogCtl.setLevel("org.apache.jena.tdb2.info", level);
        }
        SystemTDB.setDefaultReorderTransform(rt);
    }
}
