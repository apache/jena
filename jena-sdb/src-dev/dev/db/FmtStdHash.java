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

package dev.db;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr ;
import static dev.db.DBSyntax.col ;

import java.sql.SQLException ;

import org.apache.jena.atlas.iterator.Iter ;
import org.apache.jena.atlas.iterator.Transform ;

import com.hp.hpl.jena.sdb.layout2.TableDescNodes ;
import com.hp.hpl.jena.sdb.sql.SDBConnection ;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL ;
import com.hp.hpl.jena.sdb.store.TableDesc ;

public class FmtStdHash extends StoreFormatterStd
{
    private DBSyntax syntax ;
    private static final boolean NOT_NULL = false ;
    

    public FmtStdHash(SDBConnection conn, DBSyntax syntax)
    {
        super(conn) ;
        this.syntax = syntax ;
    }

    @Override
    protected void formatTableNodes()
    {
        dropTable(TableDescNodes.name()) ;
        try { 
            connection().exec(sqlStr ("CREATE TABLE "+TableDescNodes.name()+" (",
                                      "   "+col("hash",     syntax.integer64(),     NOT_NULL),
                                      "   "+col("lex",      syntax.text(),          NOT_NULL),
                                      "   "+col("lang",     syntax.varchar(20),     NOT_NULL),
                                      "   "+col("datatype", syntax.varchar(200),    NOT_NULL),
                                      "   "+col("type",     syntax.integer32(),     NOT_NULL),
                                      "   "+syntax.primaryKey("hash"),
                                      ")"
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableDescNodes.name()+"'",ex) ;
        }
    }

    @Override
    protected void formatTablePrefixes()
    {}

    private static final String SEP = ",\n" ;
    
    protected Transform<String, String> getColDeclTransform()
    {
        return 
            new Transform<String, String>() {
                @Override
                public String convert(String colName)
                { return col(colName, syntax.integer64(), NOT_NULL) ; }
            };   
    }
    
    @Override
    protected void formatTupleTable(TableDesc tableDesc)
    {
        dropTable(tableDesc.getTableName()) ;
        try {
            
            String cols = Iter.iter(tableDesc.getColNames()).map(getColDeclTransform()).asString() ;
            cols = cols + SEP + syntax.primaryKey(tableDesc.getColNames()) ;
            String sql = String.format("CREATE TABLE %s (\n%s\n)", tableDesc.getTableName(), cols) ;
            connection().exec(sql) ;                
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+tableDesc.getTableName()+"'",ex) ;
        }
    }


    @Override
    protected void addIndexesTableQuads()
    {}

    @Override
    protected void addIndexesTableTriples()
    {}

    @Override
    protected void dropIndexesTableQuads()
    {}

    @Override
    protected void dropIndexesTableTriples()
    {}

    @Override
    protected void truncateTableNodes()
    {}

    @Override
    protected void truncateTablePrefixes()
    {}

    @Override
    protected void truncateTableQuads()
    {}

    @Override
    protected void truncateTableTriples()
    {}

}
