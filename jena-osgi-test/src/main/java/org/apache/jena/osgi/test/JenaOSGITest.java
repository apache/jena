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

package org.apache.jena.osgi.test;

import org.junit.Test;

/** 
 * OSGi service interface for test using osgi-testrunner-junit4 
 * <p>
 * Junit annotations like @Test must be made here.
 * The implementation is in JenaOSGITestImpl, which is registered
 * with OSGi by JenaOSGIActiviator.
 * 
 * @author stain
 *
 */
public interface JenaOSGITest {

	@Test
	public void testJenaIRI() throws Exception;

	@Test
	public void testJenaCore() throws Exception;
	
	@Test
	public void testJenaArq() throws Exception;
	
	@Test
	public void testJenaTdb() throws Exception;
	
}
