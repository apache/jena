package org.apache.jena.geosparql.geof.nontopological.filter_functions;

import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.geosparql.implementation.datatype.GeoJSONDatatype;
import org.apache.jena.geosparql.implementation.vocabulary.SRS_URI;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

public class AsGeoJSONFF extends FunctionBase1 {

    private final GeoJsonWriter writer;

    public AsGeoJSONFF() {
        writer = new GeoJsonWriter();
        writer.setForceCCW(true);
        // removed from GeoJSON 2016
        writer.setEncodeCRS(false);
    }

    @Override
    public NodeValue exec(NodeValue v) {
        try {
            GeometryWrapper gw = GeometryWrapper.extract(v);
            // GeoJSON 2016 removed support for other crs, need to transform to CRS 84
            GeometryWrapper convertedGeom = gw.transform(SRS_URI.DEFAULT_WKT_CRS84);

            String json = writer.write(convertedGeom.getParsingGeometry());

            Node node = NodeFactory.createLiteralByValue(json, GeoJSONDatatype.INSTANCE);
            NodeValue result = NodeValue.makeNode(node);

            return result;
        } catch (MismatchedDimensionException | TransformException | FactoryException ex) {
            throw new ExprEvalException(ex.getMessage(), ex);
        }
    }
}
