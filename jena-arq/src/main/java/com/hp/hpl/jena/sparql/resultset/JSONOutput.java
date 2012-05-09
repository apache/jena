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

package com.hp.hpl.jena.sparql.resultset;

import java.io.OutputStream ;

import com.hp.hpl.jena.query.ResultSet ;

public class JSONOutput extends OutputBase
{
    public JSONOutput() {}
    
    @Override
    public void format(OutputStream out, ResultSet resultSet)
    {
        // Use direct string output - more control
    
        JSONOutputResultSet jsonOut =  new JSONOutputResultSet(out) ;
        ResultSetApply a = new ResultSetApply(resultSet, jsonOut) ;
        a.apply() ;
    }

    @Override
    public void format(OutputStream out, boolean booleanResult)
    {
        JSONOutputASK jsonOut = new JSONOutputASK(out) ;
        jsonOut.exec(booleanResult) ;
    }
}
