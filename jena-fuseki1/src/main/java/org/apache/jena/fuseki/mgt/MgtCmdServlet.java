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

/** A servlet that dumps its request
 */

// Could be neater - much, much neater!

package org.apache.jena.fuseki.mgt ;

import java.io.IOException ;
import java.io.PrintWriter ;

import javax.servlet.http.HttpServlet ;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.fuseki.Fuseki ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.SPARQLServer ;
import org.apache.jena.fuseki.server.ServiceRef ;
import org.apache.jena.web.HttpSC ;
import org.slf4j.Logger ;

import com.hp.hpl.jena.Jena ;
import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.tdb.TDB ;

/** Control functions for a Fuskei server */

public class MgtCmdServlet extends HttpServlet
{
    // Experimental - likely to change. 
    private static Logger log = Fuseki.serverLog ;

    public MgtCmdServlet()
    {

    }

    @Override
    public void init()
    {
        return ;
    }

    public static String paramCmd     = "cmd" ;
    public static String cmdBackup    = "backup" ;          // &dataset=/datasetname
    public static String cmdRestart   = "restart" ;         // Not implemented.
    public static String cmdShutdown  = "shutdown" ;        // Server stops, no questions asked. (Not implemented)

    ActionBackup         actionBackup = new ActionBackup() ;

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        // Commands format:
        // ?cmd=backup&<other args per command>

        String[] args = req.getParameterValues(paramCmd) ;
        if ( args == null ) {
            resp.setContentType("text/plain") ;
            resp.setStatus(HttpSC.BAD_REQUEST_400) ;

            return ;
        }
        for ( String cmd : args ) {
            if ( log.isInfoEnabled() )
                log.info("Management command: " + cmd) ;

            if ( cmd.equalsIgnoreCase(cmdBackup) ) {
                actionBackup.doPost(req, resp) ;
                continue ;
            }
            if ( cmd.equalsIgnoreCase(cmdRestart) ) {

                continue ;
            }
            if ( cmd.equalsIgnoreCase(cmdShutdown) ) {
                Fuseki.getServer().stop() ;
                continue ;
            }
            log.warn("Unrecognized command : " + cmd) ;

        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
    {
        try {
            // serverLog.info("Fuseki Server Config servlet") ;

            PrintWriter out = resp.getWriter() ;
            resp.setContentType("text/plain") ;
            SPARQLServer server = Fuseki.getServer() ;

            out.println("Software:") ;
            String fusekiVersion = Fuseki.VERSION ;
            if ( fusekiVersion.equals("${project.version}") )
                fusekiVersion = "(development)" ;

            out.printf("  %s %s\n", Fuseki.NAME, fusekiVersion) ;
            out.printf("  %s %s\n", TDB.NAME, TDB.VERSION) ;
            out.printf("  %s %s\n", ARQ.NAME, ARQ.VERSION) ;
            out.printf("  %s %s\n", Jena.NAME, Jena.VERSION) ;

            // out.printf("Port: %s\n",
            // server.getServer().getConnectors()[0].getPort()) ;
            out.println() ;

            for ( DatasetRef dsRef : server.getDatasets() ) {
                datasetRefDetails(out, dsRef) ;
                out.println() ;
            }
        }
        catch (IOException ex) {}
    }

    private static void datasetRefDetails(PrintWriter out, DatasetRef dsRef)
    {
        if ( dsRef.name != null )
            out.println("Name = " + dsRef.name) ;
        else
            out.println("Name = <unset>") ;

        endpointDetail(out, "Query", dsRef, dsRef.query) ;
        endpointDetail(out, "Update", dsRef, dsRef.update) ;
        endpointDetail(out, "Upload", dsRef, dsRef.upload) ;
        endpointDetail(out, "Graphs(Read)", dsRef, dsRef.readGraphStore) ;
        endpointDetail(out, "Graphs(RW)", dsRef, dsRef.readWriteGraphStore) ;
    }

    private static void endpointDetail(PrintWriter out, String label, DatasetRef dsRef, ServiceRef service)
    {
        boolean first = true ;
        out.printf("   %-15s :: ", label) ;

        for ( String s : service.endpoints ) {
            if ( !first )
                out.print(" , ") ;
            first = false ;
            s = "/" + dsRef.name + "/" + s ;
            out.print(s) ;
        }
        out.println() ;
    }

    @Override
    public String getServletInfo()
    {
        return "Fuseki Control Servlet" ;
    }
}
