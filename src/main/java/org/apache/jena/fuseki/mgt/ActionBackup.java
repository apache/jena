/**
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

package org.apache.jena.fuseki.mgt ;

import static java.lang.String.format ;

import java.io.* ;
import java.util.concurrent.Callable ;
import java.util.concurrent.ExecutorService ;
import java.util.concurrent.Executors ;
import java.util.zip.GZIPOutputStream ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.FileOps ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.fuseki.FusekiException ;
import org.apache.jena.fuseki.FusekiLib ;
import org.apache.jena.fuseki.server.DatasetRef ;
import org.apache.jena.fuseki.server.DatasetRegistry ;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletBase ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFDataMgr ;
import org.apache.jena.web.HttpSC ;

import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Utils ;

public class ActionBackup extends ServletBase
{
    public ActionBackup() { super() ; }

    // Limit to one backup at a time.
    public static final ExecutorService backupService = Executors.newFixedThreadPool(1) ;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String dataset = FusekiLib.safeParameter(request, "dataset") ;
        if ( dataset == null )
        {
            response.sendError(HttpSC.BAD_REQUEST_400, "Required parameter missing: ?dataset=") ;
            return ;
        }
        
        if ( ! dataset.startsWith("/") )
            dataset="/"+dataset ;
        
        // HttpSession session = request.getSession(true) ;
        // session.setAttribute("dataset", dataset) ;
        // session.setMaxInactiveInterval(15*60) ; // 10 mins

        boolean known = DatasetRegistry.get().isRegistered(dataset) ;
        if (!known)
        {
            response.sendError(HttpSC.BAD_REQUEST_400, "No such dataset: " + dataset) ;
            return ;
        }
        
        long id = allocRequestId(request, response);
        HttpAction action = new HttpAction(id, request, response, false) ;
        DatasetRef ref = DatasetRegistry.get().get(dataset) ;
        action.setDataset(ref);
        scheduleBackup(action) ;
    }

    static final String BackupArea = "backups" ;  
    
    private void scheduleBackup(final HttpAction action)
    {
        String dsName = action.dsRef.name ;
        final String ds = dsName.startsWith("/")? dsName : "/"+dsName ;
        
        String timestamp = Utils.nowAsString("yyyy-MM-dd_HH-mm-ss") ;
        final String filename = BackupArea + ds + "_" + timestamp ;
        FileOps.ensureDir(BackupArea) ;
        
        try {
            final Callable<Boolean> task = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception
                {
                    log.info(format("[%d] Start backup %s to '%s'", action.id, ds, filename)) ;
                    action.beginRead() ;
                    try {
                        backup(action.getActiveDSG(), filename) ;
                        log.info(format("[%d] Finish backup %s to '%s'", action.id, ds, filename)) ;
                    }
                    catch ( RuntimeException ex )
                    {
                        log.info(format("[%d] Exception during backup: ", action.id, ex.getMessage()), ex) ;
                        return Boolean.FALSE ;
                    }
                    finally {
                        action.endRead() ;
                    }
                    return Boolean.TRUE ;
                }} ;
            
            log.info(format("[%d] Schedule backup %s to '%s'", action.id, ds, filename)) ;                
            backupService.submit(task) ;
        } 
        //catch (FusekiException ex)
        catch (RuntimeException ex)
        {
            log.warn("Unanticipated exception", ex) ;
            try { action.response.sendError(HttpSC.INTERNAL_SERVER_ERROR_500, ex.getMessage()) ; }
            catch (IOException e) { IO.exception(e) ; }
            return ;            
        }
        
        successPage(action, "Backup scheduled - see server log for details") ;
    }
    
    // Share with new ServletBase.
    protected static void successPage(HttpAction action, String message)
    {
        try {
            action.response.setContentType("text/html");
            action.response.setStatus(HttpSC.OK_200);
            PrintWriter out = action.response.getWriter() ;
            out.println("<html>") ;
            out.println("<head>") ;
            out.println("</head>") ;
            out.println("<body>") ;
            out.println("<h1>Success</h1>");
            if ( message != null )
            {
                out.println("<p>") ;
                out.println(message) ;
                out.println("</p>") ;
            }
            out.println("</body>") ;
            out.println("</html>") ;
            out.flush() ;
        } catch (IOException ex) { IO.exception(ex) ; }
    }
    
    public static void backup(DatasetGraph dsg, String backupfile)
    {
        if ( ! backupfile.endsWith(".nq") )
            backupfile = backupfile+".nq" ;
        
        OutputStream out = null ;
        try
        {
            if ( true )
            {
                // This seems to achive about the same as "gzip -6"
                // It's not too expensive in elapsed time but it's not zero cost.
                // GZip, large buffer.
                out = new FileOutputStream(backupfile+".gz") ;
                out = new GZIPOutputStream(out, 8*1024) ;
                out = new BufferedOutputStream(out) ;
            }
            else
            {
                out = new FileOutputStream(backupfile) ;
                out = new BufferedOutputStream(out) ;
            }
            
            RDFDataMgr.write(out, dsg,Lang.NQUADS) ;
            out.close() ;
            out = null ;
        } 
        catch (FileNotFoundException e)
        {
            Log.warn(ActionBackup.class, "File not found: "+backupfile) ;
            throw new FusekiException("File not found: "+backupfile) ;
        } 
        catch (IOException e) { IO.exception(e) ; }
        finally {
            try { if (out != null) out.close() ; }
            catch (IOException e) { /* ignore */ }
        }
    }
}
