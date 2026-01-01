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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.fuseki.main.runner;

import java.net.BindException;

import org.apache.jena.fuseki.FusekiException;

public class FusekiBindException extends FusekiException
{
    public FusekiBindException(String msg, BindException cause)  { super(msg, cause); }
//    public FusekiBindException(String msg)                     { super(msg); }
//    public FusekiBindException(BindException cause)            { super(cause); }
//    public FusekiBindException()                               { super(); }
}
