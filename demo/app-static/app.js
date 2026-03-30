/* Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0 */

// ---------------------------------------------------------------------------
// Configuration — adjust these if your Fuseki setup differs
// ---------------------------------------------------------------------------

const CONFIG_PATH = 'config.ttl';
const APP_CONFIG = window.APP_CONFIG || {};
const FUSEKI_BASE = APP_CONFIG.fusekiBase || 'http://localhost:3030';
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

function resolveFieldName(fieldUri, fieldIRIs) {
    const fragment = shortName(fieldUri);
    for (const [name, iri] of Object.entries(fieldIRIs || {})) {
        if (shortName(iri) === fragment) return name;
    }
    return fragment;
}

function renderJsonTree(obj, indent) {
    indent = indent || 0;
    if (obj === null) return '<span class="jt-null">null</span>';
    if (typeof obj === 'boolean') return `<span class="jt-bool">${obj}</span>`;
    if (typeof obj === 'number') return `<span class="jt-num">${obj}</span>`;
    if (typeof obj === 'string') return `<span class="jt-str">"${escapeHtml(obj)}"</span>`;
    if (Array.isArray(obj)) {
        if (obj.length === 0) return '<span class="jt-brace">[]</span>';
        const items = obj.map((v, i) => {
            const comma = i < obj.length - 1 ? ',' : '';
            return `<div class="jt-item">${renderJsonTree(v, indent + 1)}${comma}</div>`;
        }).join('');
        return `<details open><summary class="jt-brace">[<span class="jt-count">${obj.length}</span>]</summary><div class="jt-indent">${items}</div><span class="jt-brace">]</span></details>`;
    }
    if (typeof obj === 'object') {
        const keys = Object.keys(obj);
        if (keys.length === 0) return '<span class="jt-brace">{}</span>';
        const items = keys.map((k, i) => {
            const comma = i < keys.length - 1 ? ',' : '';
            return `<div class="jt-item"><span class="jt-key">"${escapeHtml(k)}"</span>: ${renderJsonTree(obj[k], indent + 1)}${comma}</div>`;
        }).join('');
        return `<details open><summary class="jt-brace">{<span class="jt-count">${keys.length}</span>}</summary><div class="jt-indent">${items}</div><span class="jt-brace">}</span></details>`;
    }
    return escapeHtml(String(obj));
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
function buildCqlFilter(selected, bbox, polygon, fieldIRIs) {
    const clauses = [];
    for (const [field, values] of Object.entries(selected)) {
        if (!values || values.length === 0) continue;
        const prop = (fieldIRIs && fieldIRIs[field]) || field;
        if (values.length === 1) {
            clauses.push({op: '=', args: [{property: prop}, values[0]]});
        } else {
            clauses.push({op: 'in', args: [{property: prop}, values]});
        }
    }
    if (bbox && bbox.length === 4) {
        const locProp = (fieldIRIs && fieldIRIs['location']) || 'location';
        clauses.push({
            op: 's_intersects',
            args: [{property: locProp}, {bbox: bbox}],
        });
    }
    if (polygon && polygon.length >= 4) {
        const locProp = (fieldIRIs && fieldIRIs['location']) || 'location';
        clauses.push({
            op: 's_intersects',
            args: [{property: locProp}, {type: 'Polygon', coordinates: [polygon]}],
        });
    }
    if (clauses.length === 0) return null;
    if (clauses.length === 1) return JSON.stringify(clauses[0]);
    return JSON.stringify({op: 'and', args: clauses});
}

/**
 * Parse a CQL2-JSON filter string back into app state.
 * Returns { selected: {field: [values]}, bbox, polygon }.
 */
function parseCqlFilter(cqlString, fieldIRIs) {
    const selected = {};
    let bbox = null;
    let polygon = null;
    if (!cqlString) return { selected, bbox, polygon };

    // Build reverse map: IRI → field name
    const iriToName = {};
    if (fieldIRIs) {
        for (const [name, iri] of Object.entries(fieldIRIs)) {
            iriToName[iri] = name;
            // Also map by local name for cross-base matching
            iriToName[shortName(iri)] = name;
        }
    }
    const resolve = (prop) => iriToName[prop] || iriToName[shortName(prop)] || prop;

    let cql;
    try { cql = JSON.parse(cqlString); } catch { return { selected, bbox, polygon }; }

    const clauses = (cql.op === 'and') ? cql.args : [cql];
    for (const clause of clauses) {
        if (!clause.op || !clause.args) continue;
        if (clause.op === '=' && clause.args[0]?.property) {
            const field = resolve(clause.args[0].property);
            selected[field] = [clause.args[1]];
        } else if (clause.op === 'in' && clause.args[0]?.property) {
            const field = resolve(clause.args[0].property);
            selected[field] = clause.args[1];
        } else if (clause.op === 's_intersects') {
            const geom = clause.args[1];
            if (geom?.bbox) {
                bbox = geom.bbox;
            } else if (geom?.type === 'Polygon' && geom.coordinates) {
                polygon = geom.coordinates[0];
            }
        }
    }
    return { selected, bbox, polygon };
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
    const fieldIRIs = {};
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

            const fieldIRI = propNode.termType === 'NamedNode' ? propNode.value : null;

            shape.fields.push({
                name: fieldName,
                iri: fieldIRI,
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
                if (fieldIRI) fieldIRIs[fieldName] = fieldIRI;
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
        endpoint: `${FUSEKI_BASE}/${datasetName}/query`,
        storeValues,
        maxFacetHits,
        shapes,
        facetFields,
        fieldIRIs,
        predicateToFacet,
    };
}

async function loadConfig() {
    const resp = await fetch(`${CONFIG_PATH}?t=${Date.now()}`);
    if (!resp.ok) throw new Error(`Failed to fetch ${CONFIG_PATH}: ${resp.status}`);
    const text = await resp.text();
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
        identifier: '',
        identifierSuggestions: [],
        limit: DEFAULT_LIMIT,
        resultLimits: RESULT_LIMITS,
        maxFacetValues: DEFAULT_FACET_LIMIT,
        facetLimits: FACET_LIMITS,
        selected: {},
        facetFields: [],
        fieldIRIs: {},
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
        _abortController: null,
        _identifierSuggestTimer: null,
        editorOpen: false,
        editorQuery: '',
        editorResults: '',
        editorRunning: false,
        editorError: null,
        editorEndpoint: '',
        editorView: 'table',
        editorData: null,
        cqlOpen: false,
        cqlJson: null,
        cqlRaw: '',
        cqlView: 'object',
        _cqlRight: 0,
        _cqlTop: 60,
        _cqlWidth: 0,
        _editorRight: 0,
        _editorTop: 60,
        _editorWidth: 0,

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
            this.fieldIRIs = config.fieldIRIs;
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

        logQuery(label, query, durationMs, isSparql) {
            const dur = durationMs != null ? ` (${(durationMs / 1000).toFixed(2)}s)` : '';
            const trimmed = query.trim();
            const sparql = isSparql !== false && trimmed.toUpperCase().startsWith('PREFIX');
            let isCql = false;
            if (!sparql) {
                try { const p = JSON.parse(trimmed); isCql = p && typeof p.op === 'string'; } catch {}
            }
            this.queryLog.unshift({
                time: timeStamp(),
                label: label + dur,
                query: trimmed,
                isSparql: sparql,
                isCql,
            });
        },

        // --- SPARQL editor ---

        openEditor(query) {
            this.editorQuery = query;
            this.editorEndpoint = this.endpoint;
            this.editorResults = '';
            this.editorData = null;
            this.editorError = null;
            this._editorWidth = Math.max(320, window.innerWidth * 0.5);
            this._editorRight = 0;
            this._editorTop = 60;
            this.editorOpen = true;
            this.$nextTick(() => this.runEditorQuery());
        },

        closeEditor() {
            this.editorOpen = false;
        },

        async runEditorQuery() {
            this.editorRunning = true;
            this.editorError = null;
            this.editorResults = '';
            this.editorData = null;
            try {
                const resp = await fetch(this.editorEndpoint, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/sparql-query',
                        'Accept': 'application/sparql-results+json',
                    },
                    body: this.editorQuery,
                });
                if (!resp.ok) {
                    this.editorError = `HTTP ${resp.status}: ${resp.statusText}`;
                    return;
                }
                const text = await resp.text();
                try {
                    this.editorData = JSON.parse(text);
                    this.editorResults = JSON.stringify(this.editorData, null, 2);
                } catch {
                    this.editorData = null;
                    this.editorResults = text;
                }
            } catch (e) {
                this.editorError = e.message;
            } finally {
                this.editorRunning = false;
            }
        },

        editorHasTable() {
            return this.editorData?.head?.vars && this.editorData?.results?.bindings;
        },

        editorTableVars() {
            return this.editorData?.head?.vars || [];
        },

        editorTableRows() {
            return (this.editorData?.results?.bindings || []).map(row => {
                return this.editorTableVars().map(v => {
                    const cell = row[v];
                    if (!cell) return '';
                    if (cell.type === 'uri') return shortName(cell.value);
                    return cell.value;
                });
            });
        },

        // --- CQL viewer ---

        openCql(jsonString) {
            this.cqlRaw = jsonString;
            try {
                this.cqlJson = JSON.parse(jsonString);
            } catch {
                this.cqlJson = null;
            }
            this._cqlWidth = Math.max(320, window.innerWidth * 0.4);
            this._cqlRight = 0;
            this._cqlTop = 60;
            this.cqlOpen = true;
        },

        closeCql() {
            this.cqlOpen = false;
        },

        // --- URL management ---

        pushUrl() {
            const params = new URLSearchParams();
            if (this.q.trim()) params.set('q', this.q.trim());
            if (this.identifier.trim()) params.set('id', this.identifier.trim());
            const cql = buildCqlFilter(this.selected, this.spatialBbox, this.spatialPolygon, this.fieldIRIs);
            if (cql) params.set('filter', cql);
            const qs = params.toString();
            const url = qs ? '?' + qs : window.location.pathname;
            history.pushState(null, '', url);
        },

        loadFromUrl() {
            const params = new URLSearchParams(window.location.search);
            this.q = params.get('q') || '';
            this.identifier = params.get('id') || '';
            const { selected, bbox, polygon } = parseCqlFilter(params.get('filter'), this.fieldIRIs);
            for (const f of this.facetFields) {
                this.selected[f] = selected[f] || [];
            }
            this.spatialBbox = bbox;
            this.spatialPolygon = polygon;
        },

        // --- Actions ---

        async search() {
            this.pushUrl();
            await this.executeSearch();
        },

        updateIdentifierSuggestions() {
            clearTimeout(this._identifierSuggestTimer);
            const term = this.identifier.trim();
            if (!term) {
                this.identifierSuggestions = [];
                return;
            }
            this._identifierSuggestTimer = setTimeout(() => {
                this.fetchIdentifierSuggestions(term);
            }, 150);
        },

        async fetchIdentifierSuggestions(term) {
            const trimmed = term.trim();
            if (!trimmed) {
                this.identifierSuggestions = [];
                return;
            }
            try {
                const data = await this.runSparql(this.buildIdentifierSuggestionQuery(trimmed));
                if (this.identifier.trim() !== trimmed) return;
                const seen = new Set();
                this.identifierSuggestions = (data.results?.bindings || [])
                    .map(row => row.identifier?.value)
                    .filter(value => value && !seen.has(value) && seen.add(value));
            } catch {
                if (this.identifier.trim() === trimmed) this.identifierSuggestions = [];
            }
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

        _resolveFieldName(fieldUri) {
            return resolveFieldName(fieldUri, this.fieldIRIs);
        },

        identifierFieldSpec() {
            return this.fieldIRIs.identifier || 'urn:jena:lucene:field#identifier';
        },

        // --- SPARQL execution ---

        async runSparql(query, signal) {
            const resp = await fetch(this.endpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/sparql-query',
                    'Accept': 'application/sparql-results+json',
                },
                body: query,
                signal,
            });
            if (!resp.ok) throw new Error(`SPARQL error: ${resp.status} ${resp.statusText}`);
            return resp.json();
        },

        // --- Query builders ---

        buildSearchQuery() {
            const identifier = this.identifier.trim();
            const term = identifier || this.q.trim() || '*';
            const searchField = identifier ? this.identifierFieldSpec() : 'default';
            const escaped = escapeSparql(term);
            const cqlFilter = buildCqlFilter(this.selected, this.spatialBbox, this.spatialPolygon, this.fieldIRIs);
            const filterArg = cqlFilter ? ` '${cqlFilter}'` : '';
            const facetIRIs = this.facetFields.map(f => this.fieldIRIs[f] || f);
            const facetFieldsJson = JSON.stringify(facetIRIs);

            return `${SPARQL_PREFIXES}
SELECT ?entity ?score ?totalHits ?field ?value ?count
WHERE {
    { (?entity ?score ?_lit ?totalHits) luc:query ('${searchField}' '${escaped}'${filterArg} ${this.limit}) }
    UNION
    { (?field ?value ?count) luc:facet ('${searchField}' '${escaped}' '${facetFieldsJson}'${filterArg} ${this.maxFacetValues}) }
}`;
        },

        buildIdentifierSuggestionQuery(identifier) {
            const escaped = escapeSparql(identifier);
            const fieldSpec = this.identifierFieldSpec();
            return `${SPARQL_PREFIXES}
SELECT DISTINCT ?identifier
WHERE {
    (?entity ?score) luc:query ('${fieldSpec}' '${escaped}' 8) .
    ?entity ex:identifier ?identifier .
}
ORDER BY LCASE(STR(?identifier))
LIMIT 8`;
        },

        buildDetailQuery(uris) {
            const values = uris.map(u => `<${u}>`).join(' ');
            return `${SPARQL_PREFIXES}
SELECT ?entity ?label ?p ?o ?oLabel
WHERE {
    VALUES ?entity { ${values} }
    ?entity rdfs:label ?label .
    OPTIONAL {
      ?entity ?p ?o .
      FILTER(?p != rdfs:label && !isBlank(?o) && ?o != rdfs:Resource)
      OPTIONAL { ?o rdfs:label ?oLabel }
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
                    // ?field is a URI — resolve to field name via IRI map or shortName fallback
                    const fieldUri = row.field.value;
                    const f = this._resolveFieldName(fieldUri);
                    if (!facets[f]) facets[f] = [];
                    // ?value may be a URI (KEYWORD) or literal (TEXT) —
                    // store the raw value for CQL filter matching
                    facets[f].push({
                        value: row.value.value,
                        label: row.value.type === 'uri' ? shortName(row.value.value) : row.value.value,
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
                        values[sv] = { value: sv, label: shortName(sv), count: 0 };
                    }
                }
                merged[f] = Object.values(values).sort((a, b) =>
                    b.count - a.count || (a.label || a.value).localeCompare(b.label || b.value)
                );
            }
            return merged;
        },

        parseEntityDetails(data, scores) {
            const entities = {};
            for (const row of (data.results?.bindings || [])) {
                const uri = row.entity.value;
                if (!entities[uri]) {
                    entities[uri] = {
                        uri,
                        label: row.label.value,
                        score: scores[uri] || 0,
                        identifier: null,
                        description: null,
                        properties: {},
                        rows: [],
                    };
                }
                const card = entities[uri];
                if (row.p && row.o) {
                    const pred = shortName(row.p.value);
                    const raw = row.o.value;
                    const isUri = row.o.type === 'uri';
                    const label = row.oLabel?.value;
                    const display = label || (isUri ? shortName(raw) : raw);
                    if (!card.properties[pred]) card.properties[pred] = [];
                    if (!card.properties[pred].some(e => e.raw === raw)) {
                        card.properties[pred].push({ display, raw, isUri });
                    }
                }
            }

            const cards = Object.values(entities);
            for (const card of cards) {
                if (card.properties.description) {
                    card.description = card.properties.description[0].display;
                }
                for (const [pred, values] of Object.entries(card.properties)) {
                    if (pred === 'description') continue;
                    if (pred === 'identifier') {
                        card.rows.push({
                            property: 'id',
                            values: values.map(pv => ({
                                value: pv.raw,
                                displayValue: pv.display,
                                facetField: null,
                                isActive: false,
                                clickable: false,
                                mapUri: null,
                                tooltip: '',
                                cssClass: 'prop-chip-neutral',
                            })),
                        });
                        continue;
                    }
                    if (pred === 'asWKT') {
                        for (const pv of values) {
                            const geo = parseWktForLeaflet(pv.raw);
                            if (geo) {
                                const tooltip = geo.type === 'point'
                                    ? `${geo.lat.toFixed(4)}, ${geo.lon.toFixed(4)}`
                                    : geo.coords.map(c => `${c[0].toFixed(2)},${c[1].toFixed(2)}`).join(' ');
                                card.rows.push({
                                    property: 'location',
                                    values: [{
                                        value: geo.type === 'point' ? 'Point' : 'Polygon',
                                        displayValue: geo.type === 'point' ? 'Point' : 'Polygon',
                                        facetField: null,
                                        isActive: false,
                                        clickable: false,
                                        mapUri: card.uri,
                                        tooltip,
                                        cssClass: 'prop-chip-location',
                                    }],
                                });
                            }
                        }
                        continue;
                    }
                    const facetField = this.predicateToFacet[pred] || null;
                    // Skip non-facetable literal values (e.g., depth, year, score)
                    if (!facetField && !values.some(v => v.isUri)) continue;
                    const rowValues = [];
                    for (const pv of values) {
                        // Find the matching facet value — match by label or raw IRI
                        let matchValue = pv.display;
                        if (facetField) {
                            const facetValues = this.facets[facetField] || [];
                            const match = facetValues.find(fv =>
                                fv.value === pv.raw || fv.value === pv.display ||
                                fv.label === pv.display);
                            if (match) matchValue = match.value;
                        }
                        const active = facetField ? this.isSelected(facetField, matchValue) : false;
                        rowValues.push({
                            value: matchValue,
                            displayValue: pv.display,
                            facetField,
                            isActive: active,
                            clickable: !!facetField,
                            mapUri: null,
                            tooltip: '',
                            cssClass: !facetField ? 'prop-chip-neutral'
                                : active ? 'prop-chip-active'
                                : 'prop-chip-clickable',
                        });
                    }
                    if (rowValues.length > 0) {
                        card.rows.push({
                            property: pred,
                            values: rowValues,
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
            const identifier = this.identifier.trim();
            if (identifier) {
                parts.push(`Identifier search for <strong>\u201c${escapeHtml(identifier)}\u201d</strong>`);
            } else if (q) {
                parts.push(`Search for <strong>\u201c${escapeHtml(q)}\u201d</strong>`);
            } else {
                parts.push('Showing <strong>all entities</strong>');
            }

            const filters = [];
            for (const [field, values] of Object.entries(this.selected)) {
                if (!values || values.length === 0) continue;
                const quoted = values.map(v => `\u201c${escapeHtml(shortName(v))}\u201d`);
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
            // Abort any in-flight search before starting a new one
            if (this._abortController) this._abortController.abort();
            this._abortController = new AbortController();
            const signal = this._abortController.signal;

            this.loading = true;
            this.showLoading = true;
            clearTimeout(this._loadingTimer);
            const loadStart = performance.now();
            this.error = null;

            try {
                const searchQuery = this.buildSearchQuery();
                const activeFilters = Object.entries(this.selected)
                    .filter(([, v]) => v && v.length > 0).length;
                const searchTerm = this.identifier.trim() || this.q.trim() || '*';
                const searchLabel = searchTerm
                    + (activeFilters > 0 ? ` + ${activeFilters} filter${activeFilters > 1 ? 's' : ''}` : '');

                const cqlFilter = buildCqlFilter(this.selected, this.spatialBbox, this.spatialPolygon, this.fieldIRIs);
                if (cqlFilter) {
                    this.logQuery('CQL Filter', JSON.stringify(JSON.parse(cqlFilter), null, 2));
                }

                let t0 = performance.now();
                const data = await this.runSparql(searchQuery, signal);
                const searchMs = performance.now() - t0;
                this.logQuery(`Search: ${searchLabel}`, searchQuery, searchMs);

                const { hits, facets, totalHits } = this.parseUnionResults(data);
                this.facets = this.mergeFacets(facets);

                if (hits.length > 0) {
                    const uris = hits.slice(0, this.limit).map(h => h.uri);
                    const scores = Object.fromEntries(hits.map(h => [h.uri, h.score]));
                    const detailQuery = this.buildDetailQuery(uris);

                    t0 = performance.now();
                    const detailData = await this.runSparql(detailQuery, signal);
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
                // Aborted requests are expected — silently ignore
                if (e.name === 'AbortError') return;

                console.error('executeSearch error:', e);
                if (e.message && (e.message.includes('Failed to fetch') || e.message.includes('NetworkError'))) {
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
                for (const pv of wktValues) {
                    const geo = parseWktForLeaflet(pv.raw);
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
                        const popup = `<strong>${escapeHtml(card.label)}</strong>`;
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
                this._map.fitBounds(bounds, { padding: [30, 30], maxZoom: 10, animate: false });
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
                { color: '#4db8a4', weight: 2, fillOpacity: 0.08, dashArray: '6 4', interactive: false }
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
                interactive: false,
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
        configRaw: '',
        configView: 'parsed',
        error: null,

        async init() {
            try {
                this.config = await loadConfig();
                const resp = await fetch(`${CONFIG_PATH}?t=${Date.now()}`);
                this.configRaw = await resp.text();
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
        nameMode: 'short',

        async init() {
            try {
                const config = await loadConfig();
                const endpoint = config.endpoint;
                const facetFields = config.facetFields;
                const fieldIRIs = config.fieldIRIs;
                const t0 = performance.now();

                // 1. Total entities + facet counts in one query
                const facetIRIs = facetFields.map(f => fieldIRIs[f] || f);
                const facetFieldsJson = JSON.stringify(facetIRIs);
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
                        const f = resolveFieldName(row.field.value, config.fieldIRIs);
                        if (!facets[f]) facets[f] = [];
                        facets[f].push({
                            value: row.value.value,
                            label: row.value.type === 'uri' ? shortName(row.value.value) : row.value.value,
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
                    fieldIRIs: config.fieldIRIs,
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
