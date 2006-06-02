package com.hp.hpl.jena.sdb.layout2;

import static com.hp.hpl.jena.sdb.util.StrUtils.strjoin;
import static com.hp.hpl.jena.sdb.util.StrUtils.strjoinNL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sdb.sql.SQLUtils;

public class Schema2PG //extends Schema2Bulk
{
    // TODO Needs porting to Store structures

	private static Log log = LogFactory.getLog(Schema2PG.class) ;
    
	private void reformatPrefixes(SDBConnection conn)
    {
        try {
        	if (SQLUtils.hasTable(conn.getSqlConnection(), TablePrefixes.tableName))
        		conn.execAny("DROP TABLE "+TablePrefixes.tableName) ;
        	
            conn.execAny(strjoinNL(
                                 "CREATE TABLE "+TablePrefixes.tableName+" (",
                                 "    prefix VARCHAR("+TablePrefixes.prefixColWidth+") BINARY NOT NULL ,",
                                 "    uri VARCHAR("+TablePrefixes.uriColWidth+") BINARY NOT NULL ,", 
                                 "    PRIMARY KEY  (prefix)",
                                 ")"            
                    )) ;
        } catch (SQLException ex)
        {
            log.warn("Exception resetting table "+TablePrefixes.tableName) ;
            throw new SDBExceptionSQL("SQLException resetting table '"+TablePrefixes.tableName+"'",ex) ;
        }
        
    }

	private void reformatNodesTable(SDBConnection conn)
    { 
        try {
        	if (SQLUtils.hasTable(conn.getSqlConnection(), TableNodes.tableName))
        		conn.execAny("DROP TABLE "+TableNodes.tableName) ;

            conn.execAny(strjoinNL(
                                 "CREATE TABLE "+TableNodes.tableName+" (",
                                 "   id SERIAL,",
                                 "   hash BIGINT NOT NULL,",
                                 "   lex TEXT NOT NULL default '',",
                                 "   lang varchar NOT NULL default '',",
                                 "   datatype varchar("+TableNodes.UriLength+") NOT NULL default '',",
                                 "   type integer NOT NULL default '0',",
                                 "   vInt integer NOT NULL default '0',", 
                                 "   vDouble double precision NOT NULL default '0',", 
                                 "   vDateTime timestamp NOT NULL default '-infinity',",
                                 "   PRIMARY KEY (id)",
                                 ")"            
                    )) ;
            conn.execAny("CREATE UNIQUE INDEX Hash ON " + TableNodes.tableName + " (hash)");
        } catch (SQLException ex)
        {
            log.warn("Exception resetting table "+TableNodes.tableName) ;
            throw new SDBExceptionSQL("SQLException resetting table '"+TableNodes.tableName+"'",ex) ;
        }
    }
    
    private void reformatTriplesTable(SDBConnection conn)
    {
        try {
        	if (SQLUtils.hasTable(conn.getSqlConnection(), TableTriples.tableName))
        		conn.execAny("DROP TABLE "+TableTriples.tableName) ;
        	
            conn.execAny(strjoinNL(
                                 "CREATE TABLE "+TableTriples.tableName+" (",
                                 "    s integer NOT NULL default '0',",
                                 "    p integer NOT NULL default '0',",
                                 "    o integer NOT NULL default '0',",
                                 "    PRIMARY KEY (s, p, o)",
                                 ")"                
                    )) ;
            //conn.execAny("ALTER TABLE "+TableNameTriples+" ADD INDEX SPO (`s`, `p`, `o`);") ;
            conn.execAny("CREATE INDEX SO ON " + TableTriples.tableName + "(s, o);") ;
            conn.execAny("CREATE INDEX OP ON " + TableTriples.tableName + "(o, p);") ;
            //conn.execAny("ALTER TABLE "+TableNameTriples+" ADD INDEX P   (`p`);") ;
            
        } catch (SQLException ex)
        {
            log.warn("Exception resetting table "+TableTriples.tableName) ; 
            throw new SDBExceptionSQL("SQLException resetting table '"+TableTriples.tableName+"'",ex) ;
        }
    }
    
    //@Override
    protected void createLoaderTable(SDBConnection connection) throws SQLException
    {
    	Connection conn = connection.getSqlConnection();
    	
    	if (SQLUtils.hasTable(conn, "NTrip")) return;
    		
    	PreparedStatement stmt;
    	stmt = conn
		.prepareStatement(strjoin(
				"\n",
				"CREATE TEMPORARY TABLE NTrip",
				"(",
				"shash BIGINT NOT NULL,",
				"slex TEXT NOT NULL default '',",
				"stype integer NOT NULL default '0',",
				"phash BIGINT NOT NULL,",
				"plex TEXT NOT NULL default '',",
				"ptype integer NOT NULL default '0',",
				"hash BIGINT NOT NULL,",
				"lex TEXT NOT NULL default '',",
				"lang varchar(10) NOT NULL default '',",
				"datatype varchar(" + TableNodes.UriLength
						+ ") NOT NULL default '',",
				"type integer NOT NULL default '0',",
				"vInt integer NOT NULL default '0',",
				"vDouble double precision NOT NULL default '0',",
				"vDateTime timestamp NOT NULL default '-infinity'",
				")"));
    	stmt.execute();
    }
}
