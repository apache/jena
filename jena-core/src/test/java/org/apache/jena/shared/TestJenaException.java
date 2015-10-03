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

package org.apache.jena.shared;

import junit.framework.TestSuite;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.test.JenaTestBase ;

/**
    Introduced to test that nested Jena exceptions preserve the
    caught exception's message.
*/
public class TestJenaException extends JenaTestBase
    {
    public TestJenaException(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestJenaException.class );  }
        
    public void testRethrownMessage()
        {
        Exception e = new Exception( "kings and queens" );
        JenaException j = new JenaException( e );
        assertTrue( j.getMessage().endsWith( e.getMessage() ) );
        }
}
