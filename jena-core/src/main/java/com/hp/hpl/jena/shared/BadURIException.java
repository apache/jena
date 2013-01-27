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

package com.hp.hpl.jena.shared;

/**
    A Jena exception for malformed URIs. 
    <p>
    Jena checks URIs sometimes, particularly on input and output.
    It is however possible to have a Jena model which contains
    bad URIs. Such a model cannot be written out very easily,
    particularly in RDF/XML, try N-TRIPLE.
    An input document, particularly when no base URL is known,
    may introduce relative URIs into a model. These may later
    cause this exception.
    <p>
    (This one is an unchecked
    exception, so we don't litter our code with try-catch blocks or throws
    declarations.)
*/
public class BadURIException extends JenaException
    {
    public BadURIException( String message )
        { super( message ); }
    }
