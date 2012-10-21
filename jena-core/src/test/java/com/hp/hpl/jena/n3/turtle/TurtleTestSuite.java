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

package com.hp.hpl.jena.n3.turtle;

import junit.framework.* ;

public class TurtleTestSuite extends TestSuite
{
    static public TestSuite suite() {
        return new TurtleTestSuite() ;
    }
	
	private TurtleTestSuite()
	{
		super("Turtle") ;
        addTest(TestTurtleReader.suite()) ;
        addTest(TurtleInternalTests.suite()) ;
        addTest(TurtleTestFactory.make("testing/Turtle/manifest.ttl")) ;
//		addTest(new N3ExternalTests()) ;
//		addTest(new N3JenaReaderTests()) ;
//		addTest(new N3JenaWriterTests()) ;
	}
}
