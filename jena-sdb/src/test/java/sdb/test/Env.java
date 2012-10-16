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

package sdb.test;

import java.sql.Connection;

public class Env
{
    // I want parameters that span test classes. 
    static Connection test_jdbc = null ;
    static Params test_params = null ;
    static boolean verbose = false ;
    
    public static void set(Connection jdbc, Params params, boolean verboseSetting) 
    {
        test_jdbc = jdbc ;
        test_params = params ;
        verbose = verboseSetting ;
    }
}
