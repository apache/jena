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

package org.apache.jena.atlas.junit;

import java.util.* ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.system.ErrorHandler ;
import org.apache.jena.riot.system.ErrorHandlerFactory ;
import org.junit.Assert ;

public class BaseTest extends Assert
{
    private static Deque<ErrorHandler> errorHandlers = new ArrayDeque<>() ;
    
    static public void setTestLogging(ErrorHandler errorhandler)
    {
        errorHandlers.push(ErrorHandlerFactory.getDefaultErrorHandler()) ;
        ErrorHandlerFactory.setDefaultErrorHandler(errorhandler) ;
    }
    
    static public void setTestLogging()
    {
//        if ( errorHandlers.size() != 0 )
//            Log.warn(BaseTest.class, "ErrorHandler already set for testing") ;
        setTestLogging(ErrorHandlerFactory.errorHandlerNoLogging) ;
    }

    static public void unsetTestLogging()
    {
        if ( errorHandlers.size() == 0 )
        {
            Log.warn(BaseTest.class, "ErrorHandler not set for testing") ;
            ErrorHandlerFactory.setDefaultErrorHandler(ErrorHandlerFactory.errorHandlerStd) ;  // Panic measures
            return ;
        }
        ErrorHandler errHandler = errorHandlers.pop();
        ErrorHandlerFactory.setDefaultErrorHandler(errHandler) ;
    }

    public static void assertEqualsIgnoreCase(String a, String b)
    {
        a = a.toLowerCase(Locale.ROOT) ;
        b = b.toLowerCase(Locale.ROOT) ;
        assertEquals(a, b) ;
    }

    public static void assertEqualsIgnoreCase(String msg, String a, String b)
    {
        a = a.toLowerCase(Locale.ROOT) ;
        b = b.toLowerCase(Locale.ROOT) ;
        assertEquals(msg, a, b) ;
    }
    
    public static <T> void assertEqualsUnordered(List<T> list1, List<T> list2) {
        if ( list1.size() != list2.size() )
            fail("Expected: "+list1+" : Actual: "+list2) ;
        List<T> list2a = new ArrayList<>(list2) ;
        for ( T elt : list1 )
            list2a.remove(elt) ;
        if ( list2a.size() != 0 )
            fail("Expected: "+list1+" : Actual: "+list2) ;
    }
}
