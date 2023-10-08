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

package org.apache.jena.tdb1;

import org.apache.jena.shared.JenaException ;

public class TDB1Exception extends JenaException
{
    public TDB1Exception()                          { super() ; }
    public TDB1Exception(String msg)                { super(msg) ; }
    public TDB1Exception(Throwable th)              { super(th) ; }
    public TDB1Exception(String msg, Throwable th)  { super(msg, th) ; }
}
