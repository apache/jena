/* Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0 */

// ---------------------------------------------------------------------------
// Configuration — adjust these if your Fuseki setup differs
// ---------------------------------------------------------------------------

const CONFIG_PATH = 'config.ttl';
const FUSEKI_BASE = 'http://localhost:3030';
const RESULT_LIMITS = [10, 100, 1000, 5000, 9999];
const DEFAULT_LIMIT = 10;
const FACET_LIMITS = [10, 25, 50, 100, 500];
const DEFAULT_FACET_LIMIT = 10;

// ---------------------------------------------------------------------------
// RDF namespace constants
// ---------------------------------------------------------------------------

const RDF = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#';
const SH = 'http://www.w3.org/ns/shacl#';
const TEXT = 'http://jena.apache.org/text#';
const IDX = 'urn:jena:lucene:index#';
const FUSEKI = 'http://jena.apache.org/fuseki#';

const SPARQL_PREFIXES = `\
PREFIX luc:  <urn:jena:lucene:index#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX dct:  <http://purl.org/dc/terms/>
PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX ex:   <http://example.org/mining/>
`;

// ---------------------------------------------------------------------------
// Utility functions
// ---------------------------------------------------------------------------

function shortName(uri) {
    const h = uri.lastIndexOf('#');
    if (h >= 0) return uri.substring(h + 1);
    const s = uri.lastIndexOf('/');
    if (s >= 0) return uri.substring(s + 1);
    return uri;
}

function escapeSparql(text) {
    return text.replace(/\\/g, '\\\\').replace(/'/g, "\\'");
}

function escapeHtml(text) {
    const el = document.createElement('span');
    el.textContent = text;
    return el.innerHTML;
}

function timeStamp() {
    return new Date().toLocaleTimeString('en-GB', { hour12: false });
}

/**
 * Convert selected facet filters + optional spatial bbox to CQL2-JSON string.
 * Input: {field: [val1, val2], ...}, bbox: [swLon, swLat, neLon, neLat] | null
 * Returns null if no filters are active.
 */
/**
 * Convert selected facet filters + optional spatial geometry to CQL2-JSON string.
 * Input: {field: [val1, val2], ...}
 *   bbox: [swLon, swLat, neLon, neLat] | null
 *   polygon: [[lon, lat], ...] | null  (closed ring, CRS84 order)
 * Returns null if no filters are active.
 */
function buildCqlFilter(selected, bbox, polygon) {
    const clauses = [];
    for (const [field, values] of Object.entries(selected)) {
        if (!values || values.length === 0) continue;
        if (values.length === 1) {
            clauses.push({op: '=', args: [{property: field}, values[0]]});
        } else {
            clauses.push({op: 'in', args: [{property: field}, values]});
        }
    }
    if (bbox && bbox.length === 4) {
        clauses.push({
            op: 's_intersects',
            args: [{property: 'location'}, {bbox: bbox}],
        });
    }
    if (polygon && polygon.length >= 4) {
        clauses.push({
            op: 's_intersects',
            args: [{property: 'location'}, {type: 'Polygon', coordinates: [polygon]}],
        });
    }
    if (clauses.length === 0) return null;
    if (clauses.length === 1) return JSON.stringify(clauses[0]);
    return JSON.stringify({op: 'and', args: clauses});
}

// ---------------------------------------------------------------------------
// N3 store helpers
// ---------------------------------------------------------------------------

const nn = (iri) => N3.DataFactory.namedNode(iri);

function getObject(store, subject, predicateIri) {
    const objs = store.getObjects(subject, nn(predicateIri), null);
    return objs.length > 0 ? objs[0] : null;
}

function getObjects(store, subject, predicateIri) {
    return store.getObjects(subject, nn(predicateIri), null);
}

function getSubjects(store, predicateIri, objectIri) {
    return store.getSubjects(nn(predicateIri), nn(objectIri), null);
}

function getLiteral(store, subject, predicateIri) {
    const obj = getObject(store, subject, predicateIri);
    return obj ? obj.value : null;
}

function walkList(store, head) {
    const items = [];
    let current = head;
    while (current && current.value !== RDF + 'nil') {
        const first = getObject(store, current, RDF + 'first');
        if (first) items.push(first);
        current = getObject(store, current, RDF + 'rest');
    }
    return items;
}

function pathToString(store, pathNode) {
    if (pathNode.termType === 'NamedNode') {
        return shortName(pathNode.value);
    }
    const inv = getObject(store, pathNode, SH + 'inversePath');
    if (inv) {
        return '^' + shortName(inv.value);
    }
    const first = getObject(store, pathNode, RDF + 'first');
    if (first) {
        const items = walkList(store, pathNode);
        return items.map(item => shortName(item.value)).join(' / ');
    }
    return pathNode.value;
}

// ---------------------------------------------------------------------------
// Config parser — reads config.ttl via N3.js
// ---------------------------------------------------------------------------

function parseTurtle(text) {
    return new Promise((resolve, reject) => {
        const store = new N3.Store();
        const parser = new N3.Parser();
        parser.parse(text, (error, quad) => {
            if (error) { reject(error); return; }
            if (quad) { store.addQuad(quad); return; }
            resolve(store);
        });
    });
}

function extractConfig(store) {
    let indexNodes = getSubjects(store, RDF + 'type', TEXT + 'TextIndexShacl');
    if (indexNodes.length === 0) indexNodes = getSubjects(store, RDF + 'type', TEXT + 'TextIndexLucene');
    if (indexNodes.length === 0) throw new Error('No text:TextIndexShacl or text:TextIndexLucene found in config');
    const indexNode = indexNodes[0];

    const storeValues = getLiteral(store, indexNode, TEXT + 'storeValues') === 'true';
    const maxFacetHits = parseInt(getLiteral(store, indexNode, TEXT + 'maxFacetHits') || '0', 10);

    // Allow config.ttl to override FUSEKI_BASE via idx:fusekiBase on the index node
    const fusekiBase = getLiteral(store, indexNode, IDX + 'fusekiBase') || FUSEKI_BASE;

    const serviceNodes = getSubjects(store, RDF + 'type', FUSEKI + 'Service');
    let datasetName = 'dataset';
    if (serviceNodes.length > 0) {
        const name = getLiteral(store, serviceNodes[0], FUSEKI + 'name');
        if (name) datasetName = name;
    }

    const shapesHead = getObject(store, indexNode, TEXT + 'shapes');
    const shapeNodes = shapesHead ? walkList(store, shapesHead) : [];

    const shapes = [];
    const facetFields = [];
    const predicateToFacet = {};
    const seenFacets = new Set();

    for (const shapeNode of shapeNodes) {
        const targetClass = getObject(store, shapeNode, SH + 'targetClass');
        const shape = {
            name: shortName(shapeNode.value),
            targetClass: targetClass ? shortName(targetClass.value) : '?',
            fields: [],
        };

        const propNodes = getObjects(store, shapeNode, SH + 'property');
        for (const propNode of propNodes) {
            const fieldName = getLiteral(store, propNode, IDX + 'fieldName');
            const fieldType = getObject(store, propNode, IDX + 'fieldType');
            const pathNode = getObject(store, propNode, SH + 'path');
            if (!fieldName || !fieldType) continue;

            const facetable = getLiteral(store, propNode, IDX + 'facetable') === 'true';
            const multiValued = getLiteral(store, propNode, IDX + 'multiValued') === 'true';
            const defaultSearch = getLiteral(store, propNode, IDX + 'defaultSearch') === 'true';
            const sortable = getLiteral(store, propNode, IDX + 'sortable') === 'true';
            const pathStr = pathNode ? pathToString(store, pathNode) : '?';

            shape.fields.push({
                name: fieldName,
                path: pathStr,
                fieldType: shortName(fieldType.value),
                facetable,
                multiValued,
                defaultSearch,
                sortable,
            });

            if (facetable && !seenFacets.has(fieldName)) {
                seenFacets.add(fieldName);
                facetFields.push(fieldName);
            }

            if (facetable && pathNode) {
                if (pathNode.termType === 'NamedNode') {
                    predicateToFacet[shortName(pathNode.value)] = fieldName;
                } else {
                    // Sequence path: map first predicate to this facet field
                    const first = getObject(store, pathNode, RDF + 'first');
                    if (first && first.termType === 'NamedNode') {
                        predicateToFacet[shortName(first.value)] = fieldName;
                    }
                    // Inverse path: map the inverted predicate
                    const inv = getObject(store, pathNode, SH + 'inversePath');
                    if (inv && inv.termType === 'NamedNode') {
                        predicateToFacet[shortName(inv.value)] = fieldName;
                    }
                }
            }
        }

        shapes.push(shape);
    }

    return {
        endpoint: `${fusekiBase}/${datasetName}/query`,
        storeValues,
        maxFacetHits,
        shapes,
        facetFields,
        predicateToFacet,
    };
}

async function loadConfig() {
    const resp = await fetch(`${CONFIG_PATH}?t=${Date.now()}`);
    if (!resp.ok) throw new Error(`Failed to fetch ${CONFIG_PATH}: ${resp.status}`);
    const text = await resp.text();
    console.log('[loadConfig] config.ttl (first 2000 chars):', text.substring(0, 2000));
    console.log('[loadConfig] config.ttl length:', text.length);
    const store = await parseTurtle(text);
    return extractConfig(store);
}

// ---------------------------------------------------------------------------
// Test cases — loaded from tests.json (symlinked per demo scenario)
// ---------------------------------------------------------------------------

async function loadTestCases() {
    try {
        const resp = await fetch(`tests.json?t=${Date.now()}`);
        if (!resp.ok) return [];
        return resp.json();
    } catch {
        return [];
    }
}

// ---------------------------------------------------------------------------
// WKT parser — extracts Leaflet-compatible coordinates from WKT literals
// ---------------------------------------------------------------------------

function parseWktForLeaflet(wktString) {
    let wkt = wktString.trim();
    let isLatLon = false;

    // Strip CRS prefix if present
    if (wkt.startsWith('<')) {
        const close = wkt.indexOf('>');
        const crs = wkt.substring(1, close);
        wkt = wkt.substring(close + 1).trim();
        // EPSG:4326/4283/7844 use lat/lon axis order
        if (crs.includes('4326') || crs.includes('4283') || crs.includes('7844')) {
            isLatLon = true;
        }
    }
    // Bare WKT (no CRS prefix) defaults to CRS84 = lon/lat

    if (wkt.startsWith('POINT')) {
        const m = wkt.match(/POINT\s*\(\s*([-\d.]+)\s+([-\d.]+)\s*\)/);
        if (!m) return null;
        const c1 = parseFloat(m[1]), c2 = parseFloat(m[2]);
        return { type: 'point', lat: isLatLon ? c1 : c2, lon: isLatLon ? c2 : c1 };
    }

    if (wkt.startsWith('POLYGON')) {
        const m = wkt.match(/POLYGON\s*\(\((.*?)\)\)/);
        if (!m) return null;
        const coords = m[1].split(',').map(pair => {
            const [c1, c2] = pair.trim().split(/\s+/).map(Number);
            return isLatLon ? [c1, c2] : [c2, c1]; // [lat, lon] for Leaflet
        });
        return { type: 'polygon', coords };
    }

    return null;
}

// ---------------------------------------------------------------------------
// Alpine.js component: Search page
// ---------------------------------------------------------------------------

function searchApp() {
    return {
        q: '',
        limit: DEFAULT_LIMIT,
        resultLimits: RESULT_LIMITS,
        maxFacetValues: DEFAULT_FACET_LIMIT,
        facetLimits: FACET_LIMITS,
        selected: {},
        facetFields: [],
        predicateToFacet: {},
        facets: {},
        facetExpanded: {},
        cards: [],
        error: null,
        loading: false,
        showLoading: false,
        _loadingTimer: null,
        description: '',
        endpoint: '',
        queryLog: [],
        spatialBbox: null,
        spatialPolygon: null,
        drawingBbox: false,
        _drawRect: null,
        _drawStart: null,
        _bboxOverlay: null,
        drawingPolygon: false,
        polyPoints: [],
        _polyMarkers: null,
        _polyLine: null,
        _polyOverlay: null,
        mapMarkerCount: 0,
        _map: null,
        _mapLayers: null,
        _mapMarkersByUri: {},
        _highlightTimer: null,

        async init() {
            let config;
            try {
                config = await loadConfig();
            } catch (e) {
                this.error = `Failed to load config: ${e.message}`;
                return;
            }

            this.endpoint = config.endpoint;
            this.facetFields = config.facetFields;
            this.predicateToFacet = config.predicateToFacet;

            this.loadFromUrl();
            await this.executeSearch();

            window.addEventListener('popstate', async () => {
                this.loadFromUrl();
                await this.executeSearch();
            });

            // Initialize map when visible, invalidateSize on toggle
            const self = this;
            Alpine.effect(() => {
                const show = Alpine.store('app').showMap;
                if (show) {
                    setTimeout(() => {
                        if (self._map) self._map.invalidateSize();
                        else self.initMap();
                    }, 50);
                }
            });
        },

        // --- Query log ---

        logQuery(label, query, durationMs) {
            const dur = durationMs != null ? ` (${(durationMs / 1000).toFixed(2)}s)` : '';
            this.queryLog.unshift({
                time: timeStamp(),
                label: label + dur,
                query: query.trim(),
            });
        },

        // --- URL management ---

        pushUrl() {
            const params = new URLSearchParams();
            if (this.q.trim()) params.set('q', this.q.trim());
            for (const f of this.facetFields) {
                for (const v of (this.selected[f] || [])) {
                    params.append(f, v);
                }
            }
            if (this.spatialBbox) {
                params.set('bbox', this.spatialBbox.join(','));
            }
            if (this.spatialPolygon) {
                params.set('polygon', this.spatialPolygon.map(c => c.join(',')).join(';'));
            }
            const qs = params.toString();
            const url = qs ? '?' + qs : window.location.pathname;
            history.pushState(null, '', url);
        },

        loadFromUrl() {
            const params = new URLSearchParams(window.location.search);
            this.q = params.get('q') || '';
            for (const f of this.facetFields) {
                this.selected[f] = params.getAll(f);
            }
            const bboxStr = params.get('bbox');
            if (bboxStr) {
                const parts = bboxStr.split(',').map(Number);
                if (parts.length === 4 && parts.every(n => !isNaN(n))) {
                    this.spatialBbox = parts;
                } else {
                    this.spatialBbox = null;
                }
            } else {
                this.spatialBbox = null;
            }
            const polyStr = params.get('polygon');
            if (polyStr) {
                const coords = polyStr.split(';').map(p => p.split(',').map(Number));
                if (coords.length >= 3 && coords.every(c => c.length === 2 && c.every(n => !isNaN(n)))) {
                    this.spatialPolygon = coords;
                } else {
                    this.spatialPolygon = null;
                }
            } else {
                this.spatialPolygon = null;
            }
        },

        // --- Actions ---

        async search() {
            this.pushUrl();
            await this.executeSearch();
        },

        async toggleFacet(field, value) {
            if (!this.selected[field]) this.selected[field] = [];
            const idx = this.selected[field].indexOf(value);
            if (idx >= 0) {
                this.selected[field].splice(idx, 1);
            } else {
                this.selected[field].push(value);
            }
            this.pushUrl();
            await this.executeSearch();
        },

        async clearFilters() {
            for (const f of this.facetFields) {
                this.selected[f] = [];
            }
            this.clearBbox();
            this.clearPolygon();
            this.pushUrl();
            await this.executeSearch();
        },

        hasActiveFilters() {
            return this.spatialBbox != null || this.spatialPolygon != null ||
                this.facetFields.some(f => (this.selected[f] || []).length > 0);
        },

        isSelected(field, value) {
            return (this.selected[field] || []).includes(value);
        },

        visibleFacets(fieldName) {
            const all = this.facets[fieldName] || [];
            if (this.facetExpanded[fieldName] || all.length <= 5) return all;
            return all.slice(0, 5);
        },

        // --- SPARQL execution ---

        async runSparql(query) {
            const resp = await fetch(this.endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/sparql-query',
                    'Accept': 'application/sparql-results+json',
                },
                body: query,
            });
            if (!resp.ok) throw new Error(`SPARQL error: ${resp.status} ${resp.statusText}`);
            return resp.json();
        },

        // --- Query builders ---

        buildSearchQuery() {
            const term = this.q.trim() || '*';
            const escaped = escapeSparql(term);
            const cqlFilter = buildCqlFilter(this.selected, this.spatialBbox, this.spatialPolygon);
            const filterArg = cqlFilter ? ` '${cqlFilter}'` : '';
            const facetFieldsJson = JSON.stringify(this.facetFields);

            return `${SPARQL_PREFIXES}
SELECT ?entity ?score ?totalHits ?field ?value ?count
WHERE {
    { (?entity ?score ?_lit ?totalHits) luc:query ('default' '${escaped}'${filterArg} ${this.limit}) }
    UNION
    { (?field ?value ?count) luc:facet ('default' '${escaped}' '${facetFieldsJson}'${filterArg} ${this.maxFacetValues}) }
}`;
        },

        buildDetailQuery(uris) {
            const values = uris.map(u => `<${u}>`).join(' ');
            return `${SPARQL_PREFIXES}
SELECT ?entity ?label ?type ?p ?o
WHERE {
    VALUES ?entity { ${values} }
    ?entity rdfs:label ?label .
    ?entity rdf:type ?type .
    FILTER(?type != rdfs:Resource)
    OPTIONAL {
      ?entity ?p ?o .
      FILTER(?p != rdf:type && ?p != rdfs:label && !isBlank(?o))
    }
}`;
        },

        // --- Result parsing ---

        parseUnionResults(data) {
            const hits = [];
            const facets = {};
            let totalHits = null;
            for (const row of (data.results?.bindings || [])) {
                if (row.entity) {
                    hits.push({
                        uri: row.entity.value,
                        score: parseFloat(row.score.value),
                    });
                    if (totalHits === null && row.totalHits) {
                        totalHits = parseInt(row.totalHits.value, 10);
                    }
                } else if (row.field) {
                    const f = row.field.value;
                    if (!facets[f]) facets[f] = [];
                    facets[f].push({
                        value: row.value.value,
                        count: parseInt(row.count.value, 10),
                    });
                }
            }
            return { hits, facets, totalHits };
        },

        mergeFacets(rawFacets) {
            const merged = {};
            for (const f of this.facetFields) {
                const values = {};
                for (const fv of (rawFacets[f] || [])) {
                    values[fv.value] = fv;
                }
                for (const sv of (this.selected[f] || [])) {
                    if (!values[sv]) {
                        values[sv] = { value: sv, count: 0 };
                    }
                }
                merged[f] = Object.values(values).sort((a, b) =>
                    b.count - a.count || a.value.localeCompare(b.value)
                );
            }
            return merged;
        },

        parseEntityDetails(data, scores) {
            const entities = {};
            for (const row of (data.results?.bindings || [])) {
                const uri = row.entity.value;
                if (!entities[uri]) {
                    const typeUri = row.type?.value || '';
                    entities[uri] = {
                        uri,
                        label: row.label.value,
                        entityType: shortName(typeUri),
                        score: scores[uri] || 0,
                        description: null,
                        properties: {},
                        tags: [],
                    };
                }
                const card = entities[uri];
                if (row.p && row.o) {
                    const pred = shortName(row.p.value);
                    const raw = row.o.value;
                    const obj = row.o.type === 'uri' ? shortName(raw) : raw;
                    if (!card.properties[pred]) card.properties[pred] = [];
                    if (!card.properties[pred].includes(obj)) {
                        card.properties[pred].push(obj);
                    }
                }
            }

            const cards = Object.values(entities);
            for (const card of cards) {
                if (card.properties.description) {
                    card.description = card.properties.description[0];
                }
                for (const [pred, values] of Object.entries(card.properties)) {
                    if (pred === 'description') continue;
                    if (pred === 'asWKT') {
                        for (const v of values) {
                            const geo = parseWktForLeaflet(v);
                            if (geo) {
                                const tooltip = geo.type === 'point'
                                    ? `${geo.lat.toFixed(4)}, ${geo.lon.toFixed(4)}`
                                    : geo.coords.map(c => `${c[0].toFixed(2)},${c[1].toFixed(2)}`).join(' ');
                                card.tags.push({
                                    pred: 'location', value: geo.type === 'point' ? 'Point' : 'Polygon',
                                    facetField: null, isActive: false, clickable: false,
                                    cssClass: 'rtag-location',
                                    mapUri: card.uri, tooltip,
                                });
                            }
                        }
                        continue;
                    }
                    const facetField = this.predicateToFacet[pred] || null;
                    for (const v of values) {
                        const active = facetField ? this.isSelected(facetField, v) : false;
                        card.tags.push({
                            pred,
                            value: v,
                            facetField,
                            isActive: active,
                            clickable: !!facetField,
                            cssClass: !facetField ? 'rtag-neutral'
                                : active ? 'rtag-active'
                                : 'rtag-clickable',
                        });
                    }
                }
            }

            return cards.sort((a, b) => b.score - a.score);
        },

        // --- Description ---

        buildDescription(hitCount, totalHits, totalSec) {
            const parts = [];
            const q = this.q.trim();
            if (q) {
                parts.push(`Search for <strong>\u201c${escapeHtml(q)}\u201d</strong>`);
            } else {
                parts.push('Showing <strong>all entities</strong>');
            }

            const filters = [];
            for (const [field, values] of Object.entries(this.selected)) {
                if (!values || values.length === 0) continue;
                const quoted = values.map(v => `\u201c${escapeHtml(v)}\u201d`);
                if (quoted.length === 1) {
                    filters.push(`${escapeHtml(field)} = ${quoted[0]}`);
                } else {
                    filters.push(`(${escapeHtml(field)} = ${quoted.join(' OR ')})`);
                }
            }
            if (this.spatialBbox) {
                filters.push('bbox [' + this.spatialBbox.map(n => n.toFixed(1)).join(', ') + ']');
            }
            if (this.spatialPolygon) {
                filters.push('polygon [' + this.spatialPolygon.length + ' vertices]');
            }
            if (filters.length > 0) {
                parts.push('filtered by ' + filters.join(' AND '));
            }

            let result = parts.join(' ') + ' \u2014 ';
            if (totalHits != null && totalHits > hitCount) {
                result += `<strong>${hitCount.toLocaleString()}</strong> of <strong>${totalHits.toLocaleString()}</strong> results`;
            } else {
                result += `<strong>${(totalHits || hitCount).toLocaleString()}</strong> results`;
            }
            if (totalSec != null) {
                result += ` in <strong>${totalSec.toFixed(2)}s</strong>`;
            }
            return result;
        },

        // --- Search execution ---

        async executeSearch() {
            this.loading = true;
            this.showLoading = true;
            clearTimeout(this._loadingTimer);
            const loadStart = performance.now();
            this.error = null;

            try {
                const searchQuery = this.buildSearchQuery();
                const activeFilters = Object.entries(this.selected)
                    .filter(([, v]) => v && v.length > 0).length;
                const searchLabel = (this.q.trim() || '*')
                    + (activeFilters > 0 ? ` + ${activeFilters} filter${activeFilters > 1 ? 's' : ''}` : '');

                let t0 = performance.now();
                const data = await this.runSparql(searchQuery);
                const searchMs = performance.now() - t0;
                this.logQuery(`Search: ${searchLabel}`, searchQuery, searchMs);

                const { hits, facets, totalHits } = this.parseUnionResults(data);
                this.facets = this.mergeFacets(facets);

                if (hits.length > 0) {
                    const uris = hits.slice(0, this.limit).map(h => h.uri);
                    const scores = Object.fromEntries(hits.map(h => [h.uri, h.score]));
                    const detailQuery = this.buildDetailQuery(uris);

                    t0 = performance.now();
                    const detailData = await this.runSparql(detailQuery);
                    const detailMs = performance.now() - t0;
                    this.logQuery(`Details: ${uris.length} entities`, detailQuery, detailMs);

                    this.cards = this.parseEntityDetails(detailData, scores);
                } else {
                    this.cards = [];
                }

                this.updateMap();
                const totalSec = (performance.now() - loadStart) / 1000;
                this.description = this.buildDescription(hits.length, totalHits, totalSec);
            } catch (e) {
                if (e.name === 'TypeError' || (e.message && (e.message.includes('Failed to fetch') || e.message.includes('NetworkError')))) {
                    this.error = `Cannot connect to Fuseki at ${this.endpoint}. Is the server running?`;
                } else {
                    this.error = `Query failed: ${e.message}`;
                }
                this.cards = [];
            }

            this.loading = false;
            const elapsed = performance.now() - loadStart;
            const remaining = Math.max(0, 400 - elapsed);
            this._loadingTimer = setTimeout(() => { this.showLoading = false; }, remaining);
        },

        // --- Map ---

        initMap() {
            if (this._map) return;
            const el = document.getElementById('search-map');
            if (!el) return;

            const osm = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap', maxZoom: 19,
            });
            const topo = L.tileLayer('https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenTopoMap', maxZoom: 17,
            });
            const satellite = L.tileLayer(
                'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
                { attribution: '&copy; Esri', maxZoom: 19 }
            );

            this._map = L.map(el, { layers: [osm], zoomControl: true })
                .setView([-25, 134], 4);

            L.control.layers(
                { 'OpenStreetMap': osm, 'Topographic': topo, 'Satellite': satellite },
                null, { position: 'topright' }
            ).addTo(this._map);

            this._mapLayers = L.layerGroup().addTo(this._map);
            this._setupBboxDrawHandlers();
            this._setupPolygonDrawHandlers();
            this.updateMapMarkers();
            if (this.spatialBbox) this.showBboxOverlay();
            if (this.spatialPolygon) this.showPolygonOverlay();
        },

        updateMap() {
            if (!Alpine.store('app').showMap) return;
            if (!this._map) {
                this.$nextTick(() => this.initMap());
                return;
            }
            this._map.invalidateSize();
            this.updateMapMarkers();
        },

        updateMapMarkers() {
            if (!this._mapLayers) return;
            this._mapLayers.clearLayers();
            this._mapMarkersByUri = {};
            const bounds = [];
            let mapped = 0;

            for (const card of this.cards) {
                const wktValues = card.properties.asWKT || [];
                for (const wkt of wktValues) {
                    const geo = parseWktForLeaflet(wkt);
                    if (!geo) continue;

                    let layer;
                    if (geo.type === 'point') {
                        layer = L.circleMarker([geo.lat, geo.lon], {
                            radius: 7, fillColor: '#d4944c', color: '#d4944c',
                            weight: 2, opacity: 1, fillOpacity: 0.6,
                        });
                        bounds.push([geo.lat, geo.lon]);
                    } else if (geo.type === 'polygon') {
                        layer = L.polygon(geo.coords, {
                            fillColor: '#d4944c', color: '#d4944c',
                            weight: 2, opacity: 0.8, fillOpacity: 0.2,
                        });
                        bounds.push(...geo.coords);
                    }

                    if (layer) {
                        const popup = `<strong>${escapeHtml(card.label)}</strong>`
                            + `<br><span class="map-popup-type">${escapeHtml(card.entityType)}</span>`;
                        layer.bindPopup(popup);
                        const uri = card.uri;
                        layer.on('click', () => this.highlightCard(uri));
                        this._mapLayers.addLayer(layer);
                        this._mapMarkersByUri[uri] = layer;
                        mapped++;
                    }
                }
            }

            this.mapMarkerCount = mapped;
            if (bounds.length > 0) {
                this._map.fitBounds(bounds, { padding: [30, 30], maxZoom: 10 });
            }
        },

        focusMapMarker(uri) {
            const layer = this._mapMarkersByUri[uri];
            if (!layer || !this._map || !Alpine.store('app').showMap) return;
            layer.openPopup();
            if (layer.getLatLng) {
                this._map.panTo(layer.getLatLng());
            } else if (layer.getBounds) {
                this._map.panTo(layer.getBounds().getCenter());
            }
        },

        startResize(e) {
            e.preventDefault();
            const handle = e.currentTarget;
            const rightPanel = handle.nextElementSibling;
            const startX = e.clientX;
            const startWidth = rightPanel.offsetWidth;
            handle.classList.add('is-dragging');

            const onMove = (ev) => {
                const newWidth = Math.max(200, Math.min(window.innerWidth * 0.6, startWidth + (startX - ev.clientX)));
                rightPanel.style.width = newWidth + 'px';
                rightPanel.style.flex = 'none';
                if (this._map) this._map.invalidateSize();
            };
            const onUp = () => {
                handle.classList.remove('is-dragging');
                document.removeEventListener('mousemove', onMove);
                document.removeEventListener('mouseup', onUp);
                if (this._map) this._map.invalidateSize();
            };
            document.addEventListener('mousemove', onMove);
            document.addEventListener('mouseup', onUp);
        },

        startResizeLeft(e) {
            e.preventDefault();
            const handle = e.currentTarget;
            const leftPanel = handle.previousElementSibling;
            const startX = e.clientX;
            const startWidth = leftPanel.offsetWidth;
            handle.classList.add('is-dragging');

            const onMove = (ev) => {
                const newWidth = Math.max(150, Math.min(window.innerWidth * 0.4, startWidth + (ev.clientX - startX)));
                leftPanel.style.width = newWidth + 'px';
                leftPanel.style.flex = 'none';
            };
            const onUp = () => {
                handle.classList.remove('is-dragging');
                document.removeEventListener('mousemove', onMove);
                document.removeEventListener('mouseup', onUp);
            };
            document.addEventListener('mousemove', onMove);
            document.addEventListener('mouseup', onUp);
        },

        highlightCard(uri) {
            clearTimeout(this._highlightTimer);
            Alpine.store('app').highlightUri = uri;
            const el = document.querySelector(`[data-uri="${CSS.escape(uri)}"]`);
            if (el) el.scrollIntoView({ behavior: 'smooth', block: 'center' });
            this._highlightTimer = setTimeout(() => { Alpine.store('app').highlightUri = null; }, 2000);
        },

        // --- Bbox drawing ---

        enableBboxDraw() {
            if (!this._map) return;
            this.cancelPolygonDraw();
            this.drawingBbox = true;
            this._map.dragging.disable();
            this._map.getContainer().style.cursor = 'crosshair';
        },

        cancelBboxDraw() {
            if (!this._map) return;
            this.drawingBbox = false;
            this._drawStart = null;
            if (this._drawRect) {
                this._map.removeLayer(this._drawRect);
                this._drawRect = null;
            }
            this._map.dragging.enable();
            this._map.getContainer().style.cursor = '';
        },

        clearBbox() {
            this.spatialBbox = null;
            if (this._bboxOverlay && this._map) {
                this._map.removeLayer(this._bboxOverlay);
                this._bboxOverlay = null;
            }
        },

        async clearBboxAndSearch() {
            this.clearBbox();
            this.pushUrl();
            await this.executeSearch();
        },

        showBboxOverlay() {
            if (!this._map || !this.spatialBbox) return;
            if (this._bboxOverlay) this._map.removeLayer(this._bboxOverlay);
            const [swLon, swLat, neLon, neLat] = this.spatialBbox;
            this._bboxOverlay = L.rectangle(
                [[swLat, swLon], [neLat, neLon]],
                { color: '#4db8a4', weight: 2, fillOpacity: 0.08, dashArray: '6 4' }
            ).addTo(this._map);
        },

        _setupBboxDrawHandlers() {
            const map = this._map;
            const self = this;

            map.on('mousedown', function (e) {
                if (!self.drawingBbox) return;
                self._drawStart = e.latlng;
                if (self._drawRect) map.removeLayer(self._drawRect);
                self._drawRect = L.rectangle(
                    [e.latlng, e.latlng],
                    { color: '#4db8a4', weight: 2, fillOpacity: 0.12, dashArray: '6 4' }
                ).addTo(map);
            });

            map.on('mousemove', function (e) {
                if (!self.drawingBbox || !self._drawStart || !self._drawRect) return;
                self._drawRect.setBounds(L.latLngBounds(self._drawStart, e.latlng));
            });

            map.on('mouseup', async function (e) {
                if (!self.drawingBbox || !self._drawStart) return;
                const bounds = L.latLngBounds(self._drawStart, e.latlng);
                const sw = bounds.getSouthWest();
                const ne = bounds.getNorthEast();

                // Clean up drawing state
                self.drawingBbox = false;
                self._drawStart = null;
                if (self._drawRect) {
                    map.removeLayer(self._drawRect);
                    self._drawRect = null;
                }
                map.dragging.enable();
                map.getContainer().style.cursor = '';

                // Ignore tiny drags (accidental clicks)
                if (Math.abs(sw.lat - ne.lat) < 0.01 && Math.abs(sw.lng - ne.lng) < 0.01) return;

                // Clear polygon if present — only one spatial filter at a time
                self.clearPolygon();

                // Set bbox as [swLon, swLat, neLon, neLat] — CQL2 order
                self.spatialBbox = [
                    Math.round(sw.lng * 1000) / 1000,
                    Math.round(sw.lat * 1000) / 1000,
                    Math.round(ne.lng * 1000) / 1000,
                    Math.round(ne.lat * 1000) / 1000,
                ];
                self.showBboxOverlay();
                self.pushUrl();
                await self.executeSearch();
            });
        },

        // --- Polygon drawing ---

        enablePolygonDraw() {
            if (!this._map) return;
            this.cancelBboxDraw();
            this.drawingPolygon = true;
            this.polyPoints = [];
            this._polyMarkers = L.layerGroup().addTo(this._map);
            this._map.dragging.disable();
            this._map.doubleClickZoom.disable();
            this._map.getContainer().style.cursor = 'crosshair';
        },

        cancelPolygonDraw() {
            if (!this._map) return;
            this.drawingPolygon = false;
            this.polyPoints = [];
            if (this._polyMarkers) {
                this._map.removeLayer(this._polyMarkers);
                this._polyMarkers = null;
            }
            if (this._polyLine) {
                this._map.removeLayer(this._polyLine);
                this._polyLine = null;
            }
            this._map.dragging.enable();
            this._map.doubleClickZoom.enable();
            this._map.getContainer().style.cursor = '';
        },

        async finishPolygonDraw() {
            if (!this._map || !this.drawingPolygon) return;
            if (this.polyPoints.length < 3) return;

            // Build closed ring in CQL2 [lon, lat] order
            const ring = this.polyPoints.map(ll => [
                Math.round(ll.lng * 1000) / 1000,
                Math.round(ll.lat * 1000) / 1000,
            ]);
            ring.push([...ring[0]]);

            this.cancelPolygonDraw();
            this.clearBbox();

            this.spatialPolygon = ring;
            this.showPolygonOverlay();
            this.pushUrl();
            await this.executeSearch();
        },

        clearPolygon() {
            this.spatialPolygon = null;
            if (this._polyOverlay && this._map) {
                this._map.removeLayer(this._polyOverlay);
                this._polyOverlay = null;
            }
        },

        async clearPolygonAndSearch() {
            this.clearPolygon();
            this.pushUrl();
            await this.executeSearch();
        },

        showPolygonOverlay() {
            if (!this._map || !this.spatialPolygon) return;
            if (this._polyOverlay) this._map.removeLayer(this._polyOverlay);
            // spatialPolygon is [[lon,lat], ...] — Leaflet needs [lat,lon]
            const latlngs = this.spatialPolygon.map(c => [c[1], c[0]]);
            this._polyOverlay = L.polygon(latlngs, {
                color: '#4db8a4', weight: 2, fillOpacity: 0.08, dashArray: '6 4',
            }).addTo(this._map);
        },

        _setupPolygonDrawHandlers() {
            const map = this._map;
            const self = this;

            map.on('click', function (e) {
                if (!self.drawingPolygon) return;
                self.polyPoints.push(e.latlng);

                // Add vertex marker
                const marker = L.circleMarker(e.latlng, {
                    radius: 4, fillColor: '#4db8a4', color: '#4db8a4',
                    weight: 2, fillOpacity: 1,
                });
                if (self._polyMarkers) self._polyMarkers.addLayer(marker);

                // Update preview polyline
                if (self._polyLine) map.removeLayer(self._polyLine);
                if (self.polyPoints.length >= 2) {
                    self._polyLine = L.polyline(self.polyPoints, {
                        color: '#4db8a4', weight: 2, dashArray: '6 4',
                    }).addTo(map);
                }
            });
        },
    };
}

// ---------------------------------------------------------------------------
// Alpine.js component: Config page
// ---------------------------------------------------------------------------

function configApp() {
    return {
        config: null,
        error: null,

        async init() {
            try {
                this.config = await loadConfig();
                console.log('[configApp] shapes:', this.config.shapes.length);
                for (const s of this.config.shapes) {
                    console.log(`[configApp] ${s.name}: ${s.fields.length} fields →`, s.fields.map(f => f.name));
                }
            } catch (e) {
                this.error = `Failed to load config: ${e.message}`;
            }
        },
    };
}

// ---------------------------------------------------------------------------
// Alpine.js component: Stats page
// ---------------------------------------------------------------------------

function statsApp() {
    return {
        stats: null,
        error: null,
        loading: true,

        async init() {
            try {
                const config = await loadConfig();
                const endpoint = config.endpoint;
                const facetFields = config.facetFields;
                const t0 = performance.now();

                // 1. Total entities + facet counts in one query
                const facetFieldsJson = JSON.stringify(facetFields);
                const statsQuery = `${SPARQL_PREFIXES}
SELECT ?entity ?score ?totalHits ?field ?value ?count
WHERE {
    { (?entity ?score ?_lit ?totalHits) luc:query ('default' '*' 0) }
    UNION
    { (?field ?value ?count) luc:facet ('default' '*' '${facetFieldsJson}' 0) }
}`;
                const statsData = await this.runSparql(endpoint, statsQuery);
                const statsMs = performance.now() - t0;

                // Parse union results
                let totalHits = 0;
                const facets = {};
                for (const row of (statsData.results?.bindings || [])) {
                    if (row.totalHits && totalHits === 0) {
                        totalHits = parseInt(row.totalHits.value, 10);
                    }
                    if (row.field) {
                        const f = row.field.value;
                        if (!facets[f]) facets[f] = [];
                        facets[f].push({
                            value: row.value.value,
                            count: parseInt(row.count.value, 10),
                        });
                    }
                }
                // Sort each facet by count desc
                for (const f of Object.keys(facets)) {
                    facets[f].sort((a, b) => b.count - a.count);
                }

                // 2. Triple count
                const t1 = performance.now();
                const countQuery = `SELECT (COUNT(*) AS ?count) WHERE { ?s ?p ?o }`;
                const countData = await this.runSparql(endpoint, countQuery);
                const countMs = performance.now() - t1;
                const tripleCount = parseInt(
                    countData.results?.bindings?.[0]?.count?.value || '0', 10
                );

                const totalMs = performance.now() - t0;

                this.stats = {
                    totalEntities: totalHits,
                    totalTriples: tripleCount,
                    shapes: config.shapes.length,
                    facetableFields: facetFields.length,
                    facets,
                    facetFields,
                    statsQueryMs: statsMs,
                    countQueryMs: countMs,
                    totalMs,
                };
            } catch (e) {
                if (e.name === 'TypeError' || (e.message && (e.message.includes('Failed to fetch') || e.message.includes('NetworkError')))) {
                    this.error = `Cannot connect to Fuseki. Is the server running?`;
                } else {
                    this.error = `Query failed: ${e.message}`;
                }
            }
            this.loading = false;
        },

        async runSparql(endpoint, query) {
            const resp = await fetch(endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/sparql-query',
                    'Accept': 'application/sparql-results+json',
                },
                body: query,
            });
            if (!resp.ok) throw new Error(`SPARQL error: ${resp.status} ${resp.statusText}`);
            return resp.json();
        },
    };
}
