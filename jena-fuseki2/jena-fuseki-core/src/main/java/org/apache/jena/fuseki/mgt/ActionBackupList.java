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

import org.apache.jena.atlas.json.JsonBuilder;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.FusekiServer;
import org.apache.jena.fuseki.servlets.ServletOps;
import org.apache.jena.web.HttpSC;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import static org.apache.jena.riot.WebContent.charsetUTF8;
import static org.apache.jena.riot.WebContent.contentTypeTextPlain;

/**
 * A JSON API to list all the backups in the backup directory
 * Created by Yang Yuanzhe on 6/26/15.
 */
public class ActionBackupList extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        doCommon(req, resp);
    }

    protected void doCommon(HttpServletRequest request, HttpServletResponse response) {
        JsonBuilder builder = new JsonBuilder() ;
        builder.startObject("top") ;
        builder.key("backups") ;
        builder.startArray() ;

        ArrayList<String> fileNames = new ArrayList<>();
        if (Files.isDirectory(FusekiServer.dirBackups)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(FusekiServer.dirBackups)) {
                for (Path path : stream) {
                    fileNames.add(path.getFileName().toString());
                }
            } catch (IOException ex) {
                Fuseki.serverLog.warn("backup file list :: IOException :: "+ex.getMessage());
            }
        }

        Collections.sort(fileNames);
        for (String str : fileNames) {
            builder.value(str);
        }

        builder.finishArray() ;
        builder.finishObject("top") ;

        try {
            ServletOps.setNoCache(response) ;
            response.setContentType(contentTypeTextPlain);
            response.setCharacterEncoding(charsetUTF8) ;
            response.setStatus(HttpSC.OK_200);
            ServletOutputStream out = response.getOutputStream() ;
            out.println(builder.build().toString());
        } catch (IOException ex) {
            Fuseki.serverLog.warn("backup file list :: IOException :: "+ex.getMessage());
        }
    }
}
