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

package org.apache.jena.shared.uuid;

import junit.framework.TestSuite;
import org.apache.jena.shared.uuid.UUID_V1_Gen ;
import org.apache.jena.shared.uuid.UUID_V4_Gen ;

public class UUIDTestSuite  extends TestSuite
{
    static UUID_V4_Gen factory4 = new UUID_V4_Gen() ;
    static UUID_V1_Gen factory1 = new UUID_V1_Gen() ;
    
    static public TestSuite suite() {
        return new UUIDTestSuite();
    }
    
    private UUIDTestSuite()
    {
        // The OS kernel can run out of entropy in which case these tests get very slow
        // These tests may not be in the test suite.
        addTestSuite(TestUUID.class) ;
        addTestSuite(TestUUID_JRE.class) ;
    }
}
