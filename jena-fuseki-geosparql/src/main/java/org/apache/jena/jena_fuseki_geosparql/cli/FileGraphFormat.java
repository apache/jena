/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.jena.jena_fuseki_geosparql.cli;

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
