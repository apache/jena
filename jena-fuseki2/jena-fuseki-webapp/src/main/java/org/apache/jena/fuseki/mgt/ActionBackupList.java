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

package org.apache.jena.fuseki.mgt;

import static java.lang.String.format ;

import java.io.File ;
import java.io.IOException ;
import java.nio.file.DirectoryStream ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.stream.Collectors ;

import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse ;

import org.apache.jena.atlas.json.JsonBuilder ;
import org.apache.jena.atlas.json.JsonValue ;
import org.apache.jena.fuseki.ctl.ActionCtl;
import org.apache.jena.fuseki.servlets.HttpAction ;
import org.apache.jena.fuseki.servlets.ServletOps ;
import org.apache.jena.fuseki.webapp.FusekiWebapp;

/**
 * A JSON API to list all the backups in the backup directory
 */
public class ActionBackupList extends ActionCtl {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp);
    }

    @Override
    protected void perform(HttpAction action) {
        JsonValue result = description(action) ;
        ServletOps.setNoCache(action.response) ;
        ServletOps.sendJsonReponse(action, result);
    }
        
    private static DirectoryStream.Filter<Path> filterVisibleFiles = (entry) -> {
        File f = entry.toFile() ;
        return f.isFile() && !f.isHidden() ;
    } ;

    private JsonValue description(HttpAction action) {
        if ( ! Files.isDirectory(FusekiWebapp.dirBackups) )
            ServletOps.errorOccurred(format("[%d] Backup area '%s' is not a directory", action.id, FusekiWebapp.dirBackups)) ;
        
        List<Path> paths = new ArrayList<>() ;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FusekiWebapp.dirBackups, filterVisibleFiles)) {
            stream.forEach(paths::add) ;
        } catch (IOException ex) {
            action.log.error(format("[%d] Backup file list :: IOException :: %s", action.id, ex.getMessage())) ;
            ServletOps.errorOccurred(ex);
        }

        List<String> fileNames = paths.stream().map((p)->p.getFileName().toString()).sorted().collect(Collectors.toList()) ;

        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("top") ;
        builder.key("backups") ;

        builder.startArray() ;
        fileNames.forEach(builder::value) ;
        builder.finishArray() ;

        builder.finishObject("top") ;
        return builder.build() ; 
        
    }
}
