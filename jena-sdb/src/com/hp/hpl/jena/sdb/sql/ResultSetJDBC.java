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

package com.hp.hpl.jena.sdb.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/* Indirect for possible statement management later.
 * Currectly, one statement - one result set so close both together. 
 * Later, prepared statements and or statement reference counting.
 * But the one result set per statement rule of JDBC may limit gains somewhat.
 * Also, it hides java.sql declarations. 
 */

public class ResultSetJDBC
{
    //static private Logger log = LoggerFactory.getLogger(ResultSetJDBC.class) ;
    
    private Statement statement ;
    private ResultSet resultSet ;
    public ResultSetJDBC(Statement s, ResultSet rs)
    {
        this.statement = s ;
        this.resultSet = rs ;
    }
    
    public ResultSet get() { return resultSet ; }
    
    public void close()
    {
        try {
            resultSet.close() ;
            statement.close() ;
        }
        catch (SQLException ex)
        {
            throw new SDBExceptionSQL("ResultSetJDBC.close", ex) ;
        }
        
    }
}
