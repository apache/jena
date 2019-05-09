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
package org.apache.jena.fuseki.geosparql.cli;

import java.io.File;
import java.util.Objects;
import org.apache.jena.riot.RDFFormat;

/**
 *
 *
 */
public class FileGraphFormat {

    private final File rdfFile;
    private final String graphName;
    private final RDFFormat rdfFormat;

    public FileGraphFormat(File rdfFile, String graphName, RDFFormat rdfFormat) {
        this.rdfFile = rdfFile;
        this.graphName = graphName;
        this.rdfFormat = rdfFormat;
    }

    public File getRdfFile() {
        return rdfFile;
    }

    public String getGraphName() {
        return graphName;
    }

    public RDFFormat getRdfFormat() {
        return rdfFormat;
    }

    @Override
    public String toString() {
        return "FileGraphFormat{" + "rdfFile=" + rdfFile + ", graphName=" + graphName + ", rdfFormat=" + rdfFormat + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.rdfFile);
        hash = 73 * hash + Objects.hashCode(this.graphName);
        hash = 73 * hash + Objects.hashCode(this.rdfFormat);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileGraphFormat other = (FileGraphFormat) obj;
        if (!Objects.equals(this.graphName, other.graphName)) {
            return false;
        }
        if (!Objects.equals(this.rdfFile, other.rdfFile)) {
            return false;
        }
        return Objects.equals(this.rdfFormat, other.rdfFormat);
    }

}
