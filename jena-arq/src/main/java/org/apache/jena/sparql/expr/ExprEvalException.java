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

package org.apache.jena.sparql.expr;

/** Exception for a dynamic evaluation exception.
 *  The current solution is likely to be rejected.
 *  See also QueryFatalException which aborts the whole query execution. */

public class ExprEvalException extends ExprException
{
    // Filling in the stack trace is the expensive part of a java
    // exception. But if we are using exception for flow control, we don't
    // need the stack trace.

    @Override public Throwable fillInStackTrace() { return this ; }
    
    public ExprEvalException() { super() ; }
    public ExprEvalException(Throwable cause) { super(cause) ; }
    public ExprEvalException(String msg) { super(msg) ; }
    public ExprEvalException(String msg, Throwable cause) { super(msg, cause) ; }
}
