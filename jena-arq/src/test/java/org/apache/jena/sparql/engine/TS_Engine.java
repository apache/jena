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

package org.apache.jena.sparql.engine;

import org.apache.jena.sparql.engine.binding.TestBindingStreams ;
import org.apache.jena.sparql.engine.http.TestQueryEngineHTTP ;
import org.apache.jena.sparql.engine.http.TestService ;
import org.apache.jena.sparql.engine.iterator.TS_QueryIterators ;
import org.apache.jena.sparql.engine.ref.TestTableJoin ;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

@RunWith(Suite.class)
@Suite.SuiteClasses( {
        TestBindingStreams.class
      , TestTableJoin.class
      , TS_QueryIterators.class
      , TestService.class
      , TestQueryEngineHTTP.class
      , TestQueryEngineMultiThreaded.class
})

public class TS_Engine {}
