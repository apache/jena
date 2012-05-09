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

package com.hp.hpl.jena.sdb.shared;


public class Access
{
    private static String get(String key1, String key2, String defValue)
    {
        try {
            if ( System.getenv(key1) != null )
                return System.getenv(key1) ;
        } catch (SecurityException ex) {}

        try {
            if ( System.getProperty(key2) != null )
                return System.getProperty(key2) ;
        } catch (SecurityException ex) {}

        return defValue ;
    }
    
    public static String getUser()     { return get("SDB_USER", "jena.db.user", "user") ; } 
    public static String getPassword() { return get("SDB_PASSWORD", "jena.db.password", "password") ; }
}
