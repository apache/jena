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

package com.hp.hpl.jena.sdb.test.misc;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import com.hp.hpl.jena.sdb.util.RegexUtils;

public class TestRegex
{
    @Test public void like_00() { testRegexLike("foo",      "%foo%") ; }
    @Test public void like_01() { testRegexLike("^foo",     "foo%") ; }
    @Test public void like_02() { testRegexLike("^foo$",    "foo") ; }
    @Test public void like_03() { testRegexLike("foo$",     "%foo") ; }
    
    @Test public void like_04() { testRegexLike("^fo?o$",   null) ; }
    @Test public void like_05() { testRegexLike("fo.o",     null) ; }
    
    private void testRegexLike(String regexPattern, String likePattern)
    {
        String p = RegexUtils.regexToLike(regexPattern) ;
        if ( p == null && likePattern == null ) return ;
        assertEquals(likePattern ,p) ;
    }
}
