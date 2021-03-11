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

package org.apache.jena.sparql.resultset;

import java.io.ByteArrayOutputStream ;
import java.nio.charset.StandardCharsets ;

import org.apache.jena.query.ResultSet ;

@Deprecated
public abstract class OutputBase implements OutputFormatter
{
    @Override
    public String asString(ResultSet resultSet)
    {
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        format(arr, resultSet) ;
        return new String(arr.toByteArray(), StandardCharsets.UTF_8) ;
    }

    public String asString(boolean booleanResult)
    {
        ByteArrayOutputStream arr = new ByteArrayOutputStream() ;
        format(arr, booleanResult) ;
        return new String(arr.toByteArray(), StandardCharsets.UTF_8) ;
    }
}
