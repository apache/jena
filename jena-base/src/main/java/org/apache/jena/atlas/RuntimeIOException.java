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

package org.apache.jena.atlas;

/** Runtime exception used to wrap or simulate IOExceptions.
 * <p>
 *  Unlike {@link java.io.UncheckedIOException}, this did not
 *  necessarily begin with an {@link java.io.IOException}.
 */
public class RuntimeIOException extends AtlasException
{
    public RuntimeIOException()                          { super() ; }
    public RuntimeIOException(String msg)                { super(msg) ; }
    /**
     * Wrap a Throwable - this is usually an IOException.
     */
    public RuntimeIOException(Throwable th)              { super(th) ; }
    /**
     * Wrap a Throwable - this is usually an IOException.
     */
    public RuntimeIOException(String msg, Throwable th)  { super(msg, th) ; }
}
