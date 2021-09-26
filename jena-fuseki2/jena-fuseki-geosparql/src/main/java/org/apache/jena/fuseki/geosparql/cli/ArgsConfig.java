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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.apache.jena.fuseki.geosparql.DatasetOperations.SPATIAL_INDEX_FILE;

/**
 *
 *
 */
public class ArgsConfig {

    //1) Port
    @Parameter(names = {"--port", "-p"}, description = "Port number.", order = 0)
    private int port = 3030;

    //2) Dataset name
    @Parameter(names = {"--dataset", "-d"}, description = "Dataset name.", order = 1)
    private String datsetName = "ds";

    //3) Loopback only
    @Parameter(names = {"--loopback", "-l"}, description = "Local host loopback requests only.", arity = 1, order = 2)
    private boolean loopbackOnly = true;

    //4) SPARQL update allowed
    @Parameter(names = {"--update", "-u"}, description = "SPARQL update allowed.", order = 3)
    private boolean updateAllowed = false;

    //5) TDB folder
    @Parameter(names = {"--tdb", "-t"}, description = "TDB folder of dataset. Default set to memory dataset.", converter = FileConverter.class, order = 4)
    private File tdbFile = null;

    //6) Load RDF file into dataset
    @Parameter(names = {"--rdf_file", "-rf"}, description = "Comma separated list of [RDF file path#graph name>RDF format] to load into dataset. Graph name is optional and will use default graph. RDF format is optional (default: ttl) or select from one of the following: json-ld, json-rdf, nt, nq, trig, trix, ttl, ttl-pretty, xml, xml-plain, xml-pretty.", validateWith = RDFFileParameter.class, listConverter = RDFFileParameter.class, order = 5)
    private List<FileGraphFormat> fileGraphFormats = new ArrayList<>();

    //7) Load tabular file into dataset
    @Parameter(names = {"--tabular_file", "-tf"}, description = "Comma separated list of [Tabular file path#graph name|delimiter] to load into dataset. See RDF Tables for table formatting. Graph name is optional and will use default graph. Column delimiter is optional and will default to COMMA. Any character except ':', '^' and '|'. Keywords TAB, SPACE and COMMA are also supported.", validateWith = TabFileParameter.class, listConverter = TabFileParameter.class, order = 6)
    private List<FileGraphDelimiter> fileGraphDelimiters = new ArrayList<>();

    //8) GeoSPARQL RDFS inference
    @Parameter(names = {"--inference", "-i"}, description = "Enable GeoSPARQL RDFS schema and inferencing (class and property hierarchy). Inferences will be applied to the dataset. Updates to dataset may require server restart.", order = 7)
    private boolean inference = false;

    //9) Apply default geometry to single Feature-Geometry
    @Parameter(names = {"--default_geometry", "-dg"}, description = "Apply hasDefaultGeometry to single Feature hasGeometry Geometry statements. Additional properties will be added to the dataset.", order = 8)
    private boolean applyDefaultGeometry = false;

    //10) Validate geometry literals in the data
    @Parameter(names = {"--validate", "-v"}, description = "Validate that the Geometry Literals in the dataset are valid.", order = 9)
    private boolean validateGeometryLiteral = false;

    //11) Convert Geo predicates in the data to Geometry with WKT WGS84 Point GeometryLiteral.
    @Parameter(names = {"--convert_geo", "-c"}, description = "Convert Geo predicates in the data to Geometry with WKT WGS84 Point Geometry Literal.", order = 10)
    private boolean convertGeoPredicates = false;

    //12) Remove Geo predicates in the data after combining to Geometry.
    @Parameter(names = {"--remove_geo", "-rg"}, description = "Remove Geo predicates in the data after converting to Geometry with WKT WGS84 Point Geometry Literal.", order = 10)
    private boolean removeGeoPredicates = false;

    //13) Query Rewrite enabled
    @Parameter(names = {"--rewrite", "-r"}, description = "Enable query rewrite.", arity = 1, order = 12)
    private boolean queryRewrite = true;

    //14) Indexing enabled
    @Parameter(names = {"--index", "-x"}, description = "Indexing enabled.", arity = 1, order = 13)
    private boolean indexEnabled = true;

    //15) Index sizes
    @Parameter(names = {"--index_sizes", "-xs"}, description = "List of Index item sizes: [Geometry Literal, Geometry Transform, Query Rewrite]. Unlimited: -1, Off: 0", listConverter = IntegerListConverter.class, order = 14)
    private List<Integer> indexSizes = Arrays.asList(-1, -1, -1);

    //16) Index expiry
    @Parameter(names = {"--index_expiry", "-xe"}, description = "List of Index item expiry in milliseconds: [Geometry Literal, Geometry Transform, Query Rewrite]. Off: 0, Minimum: 1001", listConverter = LongListConverter.class, order = 15)
    private List<Long> indexExpiries = Arrays.asList(5000l, 5000l, 5000l);

    //17) Spatial Index file
    @Parameter(names = {"--spatial_index", "-si"}, description = "File to load or store the spatial index. Default to " + SPATIAL_INDEX_FILE + " in TDB folder if using TDB and not set. Otherwise spatial index is not stored.", converter = FileConverter.class, order = 16)
    private File spatialIndexFile = null;

    //18) TDB2
    @Parameter(names = {"--tdb2", "-t2"}, description = "Option to use TDB2, rather than TDB, for persistent storage. Default: false", order = 17)
    private boolean tdb2 = false;

    //19) Help
    @Parameter(names = {"--help", "-h"}, description = "Application help. @path/to/file can be used to submit parameters in a file.", help = true, order = 18)
    private boolean help = false;

    public int getPort() {
        return port;
    }

    public String getDatsetName() {
        return datsetName;
    }

    public File getTdbFile() {
        return tdbFile;
    }

    public boolean isTDBFileSetup() {
        return tdbFile != null;
    }

    public boolean isLoopbackOnly() {
        return loopbackOnly;
    }

    public boolean isQueryRewrite() {
        return queryRewrite;
    }

    public boolean isUpdateAllowed() {
        return updateAllowed;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public List<Integer> getIndexSizes() {
        return indexSizes;
    }

    public List<Long> getIndexExpiries() {
        return indexExpiries;
    }

    public List<FileGraphFormat> getFileGraphFormats() {
        return fileGraphFormats;
    }

    public void setFileGraphFormats(List<FileGraphFormat> fileGraphFormats) {
        this.fileGraphFormats = fileGraphFormats;
    }

    public List<FileGraphDelimiter> getFileGraphDelimiters() {
        return fileGraphDelimiters;
    }

    public void setFileGraphDelimiters(List<FileGraphDelimiter> fileGraphDelimiters) {
        this.fileGraphDelimiters = fileGraphDelimiters;
    }

    public boolean isInference() {
        return inference;
    }

    public boolean isApplyDefaultGeometry() {
        return applyDefaultGeometry;
    }

    public void setApplyDefaultGeometry(boolean applyDefaultGeometry) {
        this.applyDefaultGeometry = applyDefaultGeometry;
    }

    public boolean isValidateGeometryLiteral() {
        return validateGeometryLiteral;
    }

    public boolean isConvertGeoPredicates() {
        return convertGeoPredicates;
    }

    public boolean isRemoveGeoPredicates() {
        return removeGeoPredicates;
    }

    public File getSpatialIndexFile() {
        return spatialIndexFile;
    }

    public boolean isTDB2() {
        return tdb2;
    }

    public boolean isHelp() {
        return help;
    }

    public String getSummary() {
        return "port=" + port + ", datsetName=" + datsetName + ", loopbackOnly=" + loopbackOnly + ", updateAllowed=" + updateAllowed + ", inference=" + inference + ", applyDefaultGeometry=" + applyDefaultGeometry + ", validateGeometryLiteral=" + validateGeometryLiteral + ", convertGeoPredicates=" + convertGeoPredicates + ", removeGeoPredicates=" + removeGeoPredicates + ", queryRewrite=" + queryRewrite + ", tdbFile=" + tdbFile + ", fileGraphFormats=" + fileGraphFormats + ", fileGraphDelimiters=" + fileGraphDelimiters + ", indexEnabled=" + indexEnabled + ", indexSizes=" + indexSizes + ", indexExpiries=" + indexExpiries + ", spatialIndexFile=" + spatialIndexFile + ", tdb2=" + tdb2 + ", help=" + help;
    }

    @Override
    public String toString() {
        return "ArgsConfig{" + getSummary() + '}';
    }

}
