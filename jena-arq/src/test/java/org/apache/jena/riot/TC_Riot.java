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
import org.apache.jena.riot.stream.TS_IO2 ;
import org.junit.BeforeClass ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;
import org.openjena.riot.RIOT ;
import org.openjena.riot.TS_Riot1 ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
      TS_Riot1.class
      , TS_LangSuite.class
      , TS_ReaderRIOT.class
      , TS_IO2.class
      , TS_RIOTAdapters.class
})


public class TC_Riot
{
    @BeforeClass public static void beforeClass()
    { 
        RIOT.init() ;
    }
}
