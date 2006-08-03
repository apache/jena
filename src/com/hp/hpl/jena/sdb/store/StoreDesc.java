/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.util.AssemblerUtils;
import com.hp.hpl.jena.util.FileManager;

public class StoreDesc
{
    private static Log log = LogFactory.getLog(StoreDesc.class) ;
    
    // Connection + DatabaseType + Layout + Configuration 
    
    public SDBConnectionDesc connDesc  = null ;
    public DatabaseType dbType         = null ;
    
    public LayoutType layout           = null ;
    
    // Configuration
    public String modelName            = null ;     // ModelRDB specific
    public String customizerClass      = null ;
    public MySQLEngineType engineType  = null ;     // MySQL specific
    
    public static StoreDesc read(String filename)
    {
        Model m = FileManager.get().loadModel(filename) ;
        return worker(m) ;
    }
        
    public LayoutType getLayout() { return layout ; }
    
    private static StoreDesc extract(Model m)
    {
        Model mDup = ModelFactory.createDefaultModel() ;
        return worker(mDup) ;
    }

    private static StoreDesc worker(Model m)
    {
        Resource r = AssemblerUtils.getResourceByType(m, AssemblerVocab.StoreAssemblerType) ;
        if ( r == null )
            throw new SDBException("Can't find store description") ;
        return (StoreDesc)AssemblerBase.general.open(r) ;
    }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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