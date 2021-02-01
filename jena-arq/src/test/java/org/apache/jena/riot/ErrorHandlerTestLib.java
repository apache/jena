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

package org.apache.jena.riot;

import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.riot.system.ErrorHandler ;

/** Error handler to convert anything to an exception */
public class ErrorHandlerTestLib
{
    public static class ExFatal extends RuntimeException { ExFatal(String msg) { super(msg) ; } }

    public static class ExError extends RuntimeException { ExError(String msg) { super(msg) ; } }

    public static class ExWarning extends RuntimeException { ExWarning(String msg) { super(msg) ; } }

    public static class ErrorHandlerEx implements ErrorHandler
    {
        public ErrorHandlerEx() {}

        @Override
        public void warning(String message, long line, long col)
        { throw new ExWarning(message) ; }

        @Override
        public void error(String message, long line, long col)
        { throw new ExError(message) ; }

        @Override
        public void fatal(String message, long line, long col)
        { throw new ExFatal(message) ; }
    }

    // Error handler that records messages
    public static class ErrorHandlerMsg implements ErrorHandler
    {
        public List<String> msgs = new ArrayList<>() ;

        @Override
        public void warning(String message, long line, long col)
        { msgs.add(message) ; }

        @Override
        public void error(String message, long line, long col)
        { msgs.add(message) ; }

        @Override
        public void fatal(String message, long line, long col)
        { msgs.add(message) ; throw new ExFatal(message) ; }
    }

}
