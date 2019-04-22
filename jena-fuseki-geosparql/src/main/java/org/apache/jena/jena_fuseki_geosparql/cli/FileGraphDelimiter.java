/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.jena.jena_fuseki_geosparql.cli;

import java.io.File;
import java.util.Objects;

/**
 *
 *
 */
public class FileGraphDelimiter {

    private final File tabFile;
    private final String graphName;
    private final String delimiter;

    public FileGraphDelimiter(File tabFile, String graphName, String delimiter) {
        this.tabFile = tabFile;
        this.graphName = graphName;
        this.delimiter = delimiter;
    }

    public File getTabFile() {
        return tabFile;
    }

    public String getGraphName() {
        return graphName;
    }

    public String getDelimiter() {
        return delimiter;
    }

    @Override
    public String toString() {
        return "FileGraphDelimiter{" + "tabFile=" + tabFile + ", graphName=" + graphName + ", delimiter=" + delimiter + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.tabFile);
        hash = 83 * hash + Objects.hashCode(this.graphName);
        hash = 83 * hash + Objects.hashCode(this.delimiter);
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
        final FileGraphDelimiter other = (FileGraphDelimiter) obj;
        if (!Objects.equals(this.graphName, other.graphName)) {
            return false;
        }
        if (!Objects.equals(this.delimiter, other.delimiter)) {
            return false;
        }
        return Objects.equals(this.tabFile, other.tabFile);
    }

}
