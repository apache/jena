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

package org.apache.jena.atlas;

import org.apache.jena.atlas.csv.TS_CSVParser;
import org.apache.jena.atlas.data.TS_Data ;
import org.apache.jena.atlas.event.TS_Event ;
import org.apache.jena.atlas.json.TS_JSON ;
import org.apache.jena.atlas.web.TS_Altas_Web ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TS_Event.class
    , TS_JSON.class
    , TS_Data.class
    , TS_Altas_Web.class
    , TS_CSVParser.class
}) 

public class TC_Atlas_ARQ
{}
