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

package org.apache.jena.tdb2.sys;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.jena.atlas.logging.FmtLog;
import org.apache.jena.dboe.DBOpEnvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilenameUtils {

    static Logger LOG = LoggerFactory.getLogger(FilenameUtils.class);

    /** Find the files in this directory that have namebase as a prefix and 
     *  are then numbered.
     *  <p>
     *  Returns a sorted list from, low to high index.
     */
    public static List<Path> scanForDirByPattern(Path directory, String namebase, String nameSep) {
        Pattern pattern = Pattern.compile(Pattern.quote(namebase)+
                                          Pattern.quote(nameSep)+
                                          "[\\d]+");
        List<Path> paths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, namebase + nameSep + "*")) {
            for ( Path entry : stream ) {
                if ( !pattern.matcher(entry.getFileName().toString()).matches() ) {
                    throw new DBOpEnvException("Invalid filename for matching: "+entry.getFileName());
                    // Alternative: Skip bad trailing parts but more likely there is a naming problem.  
                    //   LOG.warn("Invalid filename for matching: {} skipped", entry.getFileName());
                    //   continue;
                }
                // Follows symbolic links.
                if ( !Files.isDirectory(entry) )
                    throw new DBOpEnvException("Not a directory: "+entry);
                paths.add(entry);
            }
        }
        catch (IOException ex) {
            FmtLog.warn(LOG, "Can't inspect directory: (%s, %s)", directory, namebase);
            throw new DBOpEnvException(ex);
        }
        Comparator<Path> comp = (f1, f2) -> {
            int num1 = extractIndex(f1.getFileName().toString(), namebase, nameSep);
            int num2 = extractIndex(f2.getFileName().toString(), namebase, nameSep);
            return Integer.compare(num1, num2);
        };
        paths.sort(comp);
        //indexes.sort(Long::compareTo);
        return paths;
    }

    /**
     * Extract the index from a version-ed filename. (Base-NNNN format). 
     * @param name
     * @param namebase
     * @param nameSep
     */
    public static int extractIndex(String name, String namebase, String nameSep) {
        int i = namebase.length()+nameSep.length();
        String numStr = name.substring(i);
        int num = Integer.parseInt(numStr);
        return num;
    }
    
    /** Construct a filename */
    public static String filename(String prefix, String sep, int N) {
        return String.format("%s%s%04d", prefix, sep, N);
    }
    
    /** Construct a filename */
    public static String filename(String prefix, String sep, String index) {
        return String.format("%s%s%s", prefix, sep, index);
    }
    


}
