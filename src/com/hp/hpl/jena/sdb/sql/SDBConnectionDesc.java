/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.sql;


import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;

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
    
    public String rdbType   = null ;    // ModelRDB specific
    
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

    public String getName()
    { return name ; }

    public void setName(String name)
    { this.name = name ; }

    public String getPassword()
    { return password ; }

    public void setPassword(String password)
    { this.password = password ; }

    public String getRdbType()
    { return rdbType ; }

    public void setRdbType(String rdbType)
    { this.rdbType = rdbType ; }

    public String getType()
    { return type ; }

    public void setType(String type)
    { this.type = type ; }

    public String getUser()
    { return user ; }

    public void setUser(String user)
    { this.user = user ; }

}

/*
 * (c) Copyright 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */