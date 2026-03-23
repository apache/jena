/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *   SPDX-License-Identifier: Apache-2.0
 */

package org.apache.jena.query.text.cql;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.json.JSON;
import org.apache.jena.atlas.json.JsonArray;
import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.query.text.ShaclIndexMapping;
import org.apache.jena.query.text.ShaclIndexMapping.FieldDef;
import org.apache.jena.query.text.ShaclIndexMapping.FieldType;
import org.apache.jena.query.text.TextIndexException;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LatLonShape;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.ShapeField;
import org.apache.lucene.geo.Polygon;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;

/**
 * Compiles {@link CqlExpression} trees into Lucene {@link Query} objects
 * with a pushdown/residual split.
 * <p>
 * Expressions referencing indexed fields are pushed down to Lucene;
 * non-indexed fields or unsupported operations become residual CQL
 * for post-processing. Spatial ops are always residual.
 */
public class CqlToLuceneCompiler {

    private final ShaclIndexMapping mapping;

    public record CompileResult(Query pushed, CqlExpression residual) {}

    public CqlToLuceneCompiler(ShaclIndexMapping mapping) {
        this.mapping = mapping;
    }

    public CompileResult compile(CqlExpression expr) {
        return compileExpr(expr);
    }

    private CompileResult compileExpr(CqlExpression expr) {
        return switch (expr) {
            case CqlExpression.CqlAnd and -> compileAnd(and);
            case CqlExpression.CqlOr or -> compileOr(or);
            case CqlExpression.CqlNot not -> compileNot(not);
            case CqlExpression.CqlComparison cmp -> compileComparison(cmp);
            case CqlExpression.CqlIn in -> compileIn(in);
            case CqlExpression.CqlBetween btw -> compileBetween(btw);
            case CqlExpression.CqlLike like -> compileLike(like);
            case CqlExpression.CqlSpatial spatial -> compileSpatial(spatial);
        };
    }

    private CompileResult compileAnd(CqlExpression.CqlAnd and) {
        List<Query> pushed = new ArrayList<>();
        List<CqlExpression> residual = new ArrayList<>();

        for (CqlExpression child : and.args()) {
            CompileResult r = compileExpr(child);
            if (r.pushed() != null) {
                pushed.add(r.pushed());
            }
            if (r.residual() != null) {
                residual.add(r.residual());
            }
        }

        Query pushedQuery = null;
        if (!pushed.isEmpty()) {
            if (pushed.size() == 1) {
                pushedQuery = pushed.get(0);
            } else {
                BooleanQuery.Builder bq = new BooleanQuery.Builder();
                for (Query q : pushed) {
                    bq.add(q, BooleanClause.Occur.MUST);
                }
                pushedQuery = bq.build();
            }
        }

        CqlExpression residualExpr = null;
        if (!residual.isEmpty()) {
            residualExpr = residual.size() == 1 ? residual.get(0) : new CqlExpression.CqlAnd(residual);
        }

        return new CompileResult(pushedQuery, residualExpr);
    }

    private CompileResult compileOr(CqlExpression.CqlOr or) {
        // OR can only be pushed if ALL children are pushable
        List<Query> pushed = new ArrayList<>();
        boolean allPushable = true;

        for (CqlExpression child : or.args()) {
            CompileResult r = compileExpr(child);
            if (r.pushed() != null && r.residual() == null) {
                pushed.add(r.pushed());
            } else {
                allPushable = false;
                break;
            }
        }

        if (allPushable && !pushed.isEmpty()) {
            if (pushed.size() == 1) {
                return new CompileResult(pushed.get(0), null);
            }
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            for (Query q : pushed) {
                bq.add(q, BooleanClause.Occur.SHOULD);
            }
            bq.setMinimumNumberShouldMatch(1);
            return new CompileResult(bq.build(), null);
        }

        return new CompileResult(null, or);
    }

    private CompileResult compileNot(CqlExpression.CqlNot not) {
        CompileResult inner = compileExpr(not.arg());
        if (inner.pushed() != null && inner.residual() == null) {
            BooleanQuery q = new BooleanQuery.Builder()
                .add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST)
                .add(inner.pushed(), BooleanClause.Occur.MUST_NOT)
                .build();
            return new CompileResult(q, null);
        }
        return new CompileResult(null, not);
    }

    private CompileResult compileComparison(CqlExpression.CqlComparison cmp) {
        FieldDef field = findField(cmp.property());
        if (field == null || !field.isIndexed()) {
            return new CompileResult(null, cmp);
        }

        String op = cmp.op();
        Object value = cmp.value();
        FieldType ft = field.getFieldType();

        Query q = switch (op) {
            case "=" -> buildEqualQuery(field.getFieldName(), ft, value);
            case "<>" -> {
                Query eq = buildEqualQuery(field.getFieldName(), ft, value);
                yield new BooleanQuery.Builder()
                    .add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST)
                    .add(eq, BooleanClause.Occur.MUST_NOT)
                    .build();
            }
            case "<" -> buildRangeQuery(field.getFieldName(), ft, null, value, false, false);
            case "<=" -> buildRangeQuery(field.getFieldName(), ft, null, value, false, true);
            case ">" -> buildRangeQuery(field.getFieldName(), ft, value, null, false, false);
            case ">=" -> buildRangeQuery(field.getFieldName(), ft, value, null, true, false);
            default -> null;
        };

        if (q == null) {
            return new CompileResult(null, cmp);
        }
        return new CompileResult(q, null);
    }

    private CompileResult compileIn(CqlExpression.CqlIn in) {
        FieldDef field = findField(in.property());
        if (field == null || !field.isIndexed()) {
            return new CompileResult(null, in);
        }

        FieldType ft = field.getFieldType();
        String fieldName = field.getFieldName();

        if (ft == FieldType.KEYWORD || ft == FieldType.TEXT) {
            List<BytesRef> refs = new ArrayList<>();
            for (Object v : in.values()) {
                refs.add(new BytesRef(String.valueOf(v)));
            }
            return new CompileResult(new TermInSetQuery(fieldName, refs), null);
        }

        // Numeric IN: OR of exact queries
        if (in.values().isEmpty()) {
            return new CompileResult(new MatchNoDocsQuery(), null);
        }

        BooleanQuery.Builder bq = new BooleanQuery.Builder();
        for (Object v : in.values()) {
            Query eq = buildEqualQuery(fieldName, ft, v);
            bq.add(eq, BooleanClause.Occur.SHOULD);
        }
        bq.setMinimumNumberShouldMatch(1);
        return new CompileResult(bq.build(), null);
    }

    private CompileResult compileBetween(CqlExpression.CqlBetween btw) {
        FieldDef field = findField(btw.property());
        if (field == null || !field.isIndexed()) {
            return new CompileResult(null, btw);
        }

        Query q = buildRangeQuery(field.getFieldName(), field.getFieldType(),
            btw.lower(), btw.upper(), true, true);
        if (q == null) {
            return new CompileResult(null, btw);
        }
        return new CompileResult(q, null);
    }

    private CompileResult compileLike(CqlExpression.CqlLike like) {
        FieldDef field = findField(like.property());
        if (field == null || !field.isIndexed()) {
            return new CompileResult(null, like);
        }

        FieldType ft = field.getFieldType();
        if (ft != FieldType.KEYWORD && ft != FieldType.TEXT) {
            return new CompileResult(null, like);
        }

        // Convert CQL LIKE pattern (% and _) to Lucene WildcardQuery (* and ?)
        String lucenePattern = like.pattern()
            .replace("*", "\\*")  // escape literal *
            .replace("?", "\\?")  // escape literal ?
            .replace("%", "*")    // CQL % → Lucene *
            .replace("_", "?");   // CQL _ → Lucene ?

        return new CompileResult(
            new WildcardQuery(new Term(field.getFieldName(), lucenePattern)), null);
    }

    private CompileResult compileSpatial(CqlExpression.CqlSpatial spatial) {
        // Only s_intersects is supported for now
        if (!"s_intersects".equals(spatial.op())) {
            return new CompileResult(null, spatial);
        }

        FieldDef field = findField(spatial.property());
        if (field == null || field.getFieldType() != FieldType.LATLON) {
            return new CompileResult(null, spatial);
        }

        String fieldName = field.getFieldName();
        String geomJson = String.valueOf(spatial.geometry());

        try {
            JsonObject geomObj = JSON.parse(geomJson);

            if (geomObj.hasKey("bbox")) {
                JsonArray bbox = geomObj.get("bbox").getAsArray();
                if (bbox.size() != 4) {
                    throw new TextIndexException("bbox must have exactly 4 values [swLon, swLat, neLon, neLat], got " + bbox.size());
                }
                double swLon = bbox.get(0).getAsNumber().value().doubleValue();
                double swLat = bbox.get(1).getAsNumber().value().doubleValue();
                double neLon = bbox.get(2).getAsNumber().value().doubleValue();
                double neLat = bbox.get(3).getAsNumber().value().doubleValue();

                Query q = LatLonShape.newBoxQuery(fieldName, ShapeField.QueryRelation.INTERSECTS,
                    swLat, neLat, swLon, neLon);
                return new CompileResult(q, null);
            }

            if (geomObj.hasKey("type") && "Polygon".equals(geomObj.get("type").getAsString().value())) {
                JsonArray coordinates = geomObj.get("coordinates").getAsArray();
                // First element is the exterior ring; CQL2/GeoJSON uses [lon, lat] order
                JsonArray ring = coordinates.get(0).getAsArray();
                double[] lats = new double[ring.size()];
                double[] lons = new double[ring.size()];
                for (int i = 0; i < ring.size(); i++) {
                    JsonArray coord = ring.get(i).getAsArray();
                    lons[i] = coord.get(0).getAsNumber().value().doubleValue();
                    lats[i] = coord.get(1).getAsNumber().value().doubleValue();
                }
                Polygon poly = new Polygon(lats, lons);
                Query q = LatLonShape.newGeometryQuery(fieldName, ShapeField.QueryRelation.INTERSECTS, poly);
                return new CompileResult(q, null);
            }

            return new CompileResult(null, spatial);
        } catch (TextIndexException e) {
            throw e;
        } catch (Exception e) {
            throw new TextIndexException("Failed to parse spatial geometry JSON: " + e.getMessage(), e);
        }
    }

    private Query buildEqualQuery(String fieldName, FieldType ft, Object value) {
        return switch (ft) {
            case KEYWORD, TEXT -> new TermQuery(new Term(fieldName, String.valueOf(value)));
            case INT -> IntPoint.newExactQuery(fieldName, toInt(value));
            case LONG -> LongPoint.newExactQuery(fieldName, toLong(value));
            case DOUBLE -> DoublePoint.newExactQuery(fieldName, toDouble(value));
            case LATLON -> throw new TextIndexException("Equality queries not supported on LATLON field '" + fieldName + "'");
        };
    }

    private Query buildRangeQuery(String fieldName, FieldType ft,
                                  Object lower, Object upper,
                                  boolean lowerInclusive, boolean upperInclusive) {
        return switch (ft) {
            case INT -> {
                int lo = lower != null ? (lowerInclusive ? toInt(lower) : Math.addExact(toInt(lower), 1)) : Integer.MIN_VALUE;
                int hi = upper != null ? (upperInclusive ? toInt(upper) : Math.addExact(toInt(upper), -1)) : Integer.MAX_VALUE;
                yield IntPoint.newRangeQuery(fieldName, lo, hi);
            }
            case LONG -> {
                long lo = lower != null ? (lowerInclusive ? toLong(lower) : Math.addExact(toLong(lower), 1L)) : Long.MIN_VALUE;
                long hi = upper != null ? (upperInclusive ? toLong(upper) : Math.addExact(toLong(upper), -1L)) : Long.MAX_VALUE;
                yield LongPoint.newRangeQuery(fieldName, lo, hi);
            }
            case DOUBLE -> {
                double lo = lower != null
                    ? (lowerInclusive ? toDouble(lower) : Math.nextUp(toDouble(lower)))
                    : Double.NEGATIVE_INFINITY;
                double hi = upper != null
                    ? (upperInclusive ? toDouble(upper) : Math.nextDown(toDouble(upper)))
                    : Double.POSITIVE_INFINITY;
                yield DoublePoint.newRangeQuery(fieldName, lo, hi);
            }
            case KEYWORD, TEXT -> null; // Range queries on keywords not supported
            case LATLON -> null; // Range queries not applicable to spatial fields
        };
    }

    private FieldDef findField(String fieldIRI) {
        return mapping.findField(fieldIRI);
    }

    private static int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(String.valueOf(v));
    }

    private static long toLong(Object v) {
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(String.valueOf(v));
    }

    private static double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(String.valueOf(v));
    }
}
