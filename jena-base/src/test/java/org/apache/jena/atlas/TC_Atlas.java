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

import org.apache.jena.atlas.io.TS_IO ;
import org.apache.jena.atlas.iterator.TS_Iterator ;
import org.apache.jena.atlas.lib.TS_Lib ;
import org.apache.jena.atlas.lib.persistent.TS_Persistent;
import org.apache.jena.atlas.lib.tuple.TS_Tuple ;
import org.apache.jena.atlas.net.TS_Net;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
@Suite
@SelectClasses({
    // Library
      TS_Lib.class
    , TS_Tuple.class
    , TS_Iterator.class
    , TS_IO.class
    , TS_Persistent.class
    , TS_Net.class
})

public class TC_Atlas
{}
