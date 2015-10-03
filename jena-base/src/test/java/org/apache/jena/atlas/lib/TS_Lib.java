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

package org.apache.jena.atlas.lib;


import org.apache.jena.atlas.lib.cache.TestCacheSimple;
import org.junit.runner.RunWith ;
import org.junit.runners.Suite ;

/**
 * Tests for the Atlas lib package
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses( {
    TestAlg.class
    , TestBitsLong.class
    , TestBitsInt.class
    , TestBytes.class
    , TestHex.class
    , TestListUtils.class
    , TestSetUtils.class
    , TestCache.class
    , TestCache2.class
    , TestColumnMap.class
    , TestFileOps.class
    , TestStrUtils.class
    , TestXMLLib.class
    , TestAlarmClock.class
    , TestRefLong.class
    , TestReverseComparator.class
    , TestTrie.class
    , TestFilenameProcessing.class
    , TestNumberUtils.class
    , TestDateTimeUtils.class
    , TestCacheSimple.class
} )

public class TS_Lib
{

}
