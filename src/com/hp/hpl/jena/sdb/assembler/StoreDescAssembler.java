/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.assembler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.query.util.GraphUtils;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SDBConnectionDesc;
import com.hp.hpl.jena.sdb.store.StoreDesc;

public class StoreDescAssembler extends AssemblerBase implements Assembler
{
    private static Log log = LogFactory.getLog(StoreDescAssembler.class) ;
    
    @Override
    public Object open(Assembler a, Resource root, Mode mode)
    {
        Resource c = GraphUtils.getResourceValue(root, AssemblerVocab.pConnection) ;
        if ( c == null )
            return null ;
        SDBConnectionDesc sdbConnDesc = (SDBConnectionDesc)a.open(c) ;
        
        String layoutName = GraphUtils.getStringValue(root, AssemblerVocab.pLayout) ;
        String dbType =  sdbConnDesc.type ;
        
        StoreDesc storeDesc = new StoreDesc(layoutName, dbType) ; 
        storeDesc.connDesc = sdbConnDesc ;

        // MySQL specials
        String engineName = GraphUtils.getStringValue(root, AssemblerVocab.pMySQLEngine) ;
        storeDesc.engineType = null ;
        if ( engineName != null )
            try { storeDesc.engineType= MySQLEngineType.convert(engineName) ; }
            catch (SDBException ex) {}
            
        // ModelRDB special
        storeDesc.rdbModelName = GraphUtils.getStringValue(root, AssemblerVocab.pModelRDBname) ;
        
        return storeDesc ;
    }
}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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