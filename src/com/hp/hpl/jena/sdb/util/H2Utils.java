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

package com.hp.hpl.jena.sdb.util;
/* H2 contribution from Martin HEIN (m#)/March 2008 */
import java.sql.SQLException;

import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;

public class H2Utils
{
    // H2 requires a clean shutdown to flush logs to disk.
    public static void shutdown(Store store) { shutdown(store.getConnection()) ; }

    public static void shutdown(SDBConnection sdb)
    {
        try {
            if ( sdb.hasSQLConnection() )
                sdb.exec("SHUTDOWN COMPACT;");
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
    
    public static void checkpoint(Store store) { checkpoint(store.getConnection()) ; }

    public static void checkpoint(SDBConnection sdb)
    {
        try {
            if ( sdb.hasSQLConnection() )
                sdb.exec("CHECKPOINT DEFRAG;");
        } catch (SQLException ex)
        { throw new SDBExceptionSQL(ex) ; }
    }
}
