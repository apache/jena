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

package org.apache.jena.query;

/**
 * {@code QueryDeniedException} indicates an invalid condition or constraint was
 * encountered during query evaluation and the execution was abandoned.
 * <p>
 * This is not an internal error.
 * It is usually due to setup or a configuration error.
 * <p>
 * In Fuseki, it is a a bad request or a
 * causing the query execution to be aborted.
 */

public class QueryDeniedException extends QueryException
{
    public QueryDeniedException() { super() ; }
    public QueryDeniedException(Throwable cause) { super(cause) ; }
    public QueryDeniedException(String msg) { super(msg) ; }
    public QueryDeniedException(String msg, Throwable cause) { super(msg, cause) ; }
}
