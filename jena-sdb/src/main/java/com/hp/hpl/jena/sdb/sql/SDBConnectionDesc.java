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


import org.apache.jena.atlas.logging.Log ;

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.sdb.SDBException ;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab ;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils ;
import com.hp.hpl.jena.util.FileManager ;

public class SDBConnectionDesc
{
    private String type      = null ;
    private String host      = null ;
    private String name      = null ;
    private String user      = null ;
    private String password  = null ;
    private String driver    = null ;
    private String jdbcURL   = null ;
    private String label     = null ;
    private int    poolSize  = 0 ;      // Less then 1 means no pool.
    
    public static SDBConnectionDesc blank()
    { return new SDBConnectionDesc() ; }
    
    public static SDBConnectionDesc none()
    {
        SDBConnectionDesc x = new SDBConnectionDesc() ;
        x.jdbcURL = JDBC.jdbcNone ;
        return x ;
    }

    private SDBConnectionDesc() {}
    
    public static SDBConnectionDesc read(String filename)
    {
        Model m = FileManager.get().loadModel(filename) ;
        return worker(m) ;
    }
    
    public static SDBConnectionDesc read(Model m)
    {
        return worker(m) ;
    }
    
    private static SDBConnectionDesc worker(Model m)
    {
        Resource r = GraphUtils.getResourceByType(m, AssemblerVocab.SDBConnectionAssemblerType) ;
        if ( r == null )
            throw new SDBException("Can't find connection description") ;
        SDBConnectionDesc desc = (SDBConnectionDesc)AssemblerBase.general.open(r) ;
        desc.initJDBC() ;
        return desc ;
    }

    private void initJDBC()
    {
        if ( jdbcURL == null )
            jdbcURL = JDBC.makeURL(type, host, name, user, password) ;
    }

//    public String getArgStr()
//    { return argStr ; }
//
//    public void setArgStr(String argStr)
//    { this.argStr = argStr ; }

    public String getDriver()
    { return driver ; }

    public void setDriver(String driver)
    { this.driver = driver ; }

    public String getHost()
    { return host ; }

    public void setHost(String host)
    { this.host = host ; }

    public String getJdbcURL()
    { 
        initJDBC() ;
        return jdbcURL ;
    }

    public void setJdbcURL(String jdbcURL)
    { this.jdbcURL = jdbcURL ; }

    public String getLabel()
    { return label ; }

    public void setLabel(String label)
    { this.label = label ; }

    public int getPoolSize()
    { return poolSize ; }
    
    public void setPoolSize(int size)
    { poolSize = size ; }

    public void setPoolSize(String str)
    { 
        if ( str == null )
        {
            poolSize = 0 ;
            return ;
        }
        try {
            poolSize = Integer.parseInt(str) ;
        } catch (NumberFormatException ex)
        {
            Log.warn(this, "Can't parse as integer: "+str) ;
        }
    }

    public String getName()
    { return name ; }

    public void setName(String name)
    { this.name = name ; }

    public String getPassword()
    { return password ; }

    public void setPassword(String password)
    { this.password = password ; }

    public String getType()
    { return type ; }

    public void setType(String type)
    { this.type = type ; }

    public String getUser()
    { return user ; }

    public void setUser(String user)
    { this.user = user ; }

}
