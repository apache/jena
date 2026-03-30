#!/usr/bin/env python3
"""Generate synthetic mining RDF data for demo and load testing.

Usage:
    python generate.py --count 100              # 100 entities to stdout
    python generate.py --count 1000 > big.ttl   # 1000 entities to file
    python generate.py --count 500 --seed 42    # reproducible output
"""

import argparse
import random
import textwrap
from dataclasses import dataclass


@dataclass
class Region:
    name: str
    state: str
    lat_min: float
    lat_max: float
    lon_min: float
    lon_max: float
    commodities: list[str]
    weight: int
    geometry: str  # "point", "mix"


REGIONS = [
    Region("Pilbara", "WA", -23.5, -20.5, 116.0, 120.5, ["Iron Ore", "Manganese"], 18, "mix"),
    Region("Goldfields-Esperance", "WA", -33.0, -29.0, 119.0, 124.0, ["Gold", "Nickel"], 14, "mix"),
    Region("Kimberley", "WA", -18.5, -14.5, 124.0, 129.0, ["Gold"], 4, "point"),
    Region("Mount Isa", "QLD", -22.0, -19.5, 138.0, 141.0, ["Copper", "Lead", "Zinc"], 10, "mix"),
    Region("Bowen Basin", "QLD", -24.5, -21.0, 147.0, 150.0, ["Coal"], 10, "mix"),
    Region("North Queensland", "QLD", -20.0, -16.5, 143.0, 147.0, ["Gold", "Copper"], 6, "point"),
    Region("Hunter Valley", "NSW", -33.0, -32.0, 150.5, 151.5, ["Coal"], 6, "mix"),
    Region("Broken Hill", "NSW", -32.5, -31.0, 141.0, 142.5, ["Lead", "Zinc", "Silver"], 5, "mix"),
    Region("Lachlan Fold Belt", "NSW", -34.5, -32.5, 147.0, 149.5, ["Gold", "Copper"], 5, "mix"),
    Region("Gawler Craton", "SA", -32.5, -29.5, 135.0, 138.0, ["Copper", "Uranium", "Gold"], 6, "mix"),
    Region("Pine Creek", "NT", -14.5, -12.5, 131.0, 133.0, ["Gold", "Uranium"], 5, "point"),
    Region("Tasmania", "TAS", -43.0, -41.0, 145.0, 148.0, ["Tin", "Zinc"], 4, "point"),
    Region("Gippsland", "VIC", -38.5, -37.5, 145.5, 148.5, ["Coal", "Gold"], 4, "point"),
    Region("Tanami", "NT", -21.5, -19.5, 129.0, 131.5, ["Gold"], 3, "point"),
]

REGION_WEIGHTS = [r.weight for r in REGIONS]

# All commodities that can appear as wildcards
ALL_COMMODITIES = [
    "Gold", "Copper", "Iron Ore", "Zinc", "Lead", "Silver",
    "Uranium", "Nickel", "Lithium", "Bauxite", "Coal", "Manganese", "Tin",
]

OPERATORS = [
    "BHP", "Rio Tinto", "Glencore", "Newmont", "Newcrest",
    "Fortescue", "South32", "Mineral Resources", "IGO", "Pilbara Minerals",
]

STATUSES = ["Active", "Historical", "Care and Maintenance", "Exploration"]

REPORT_STATUSES = ["Current", "Historical", "Superseded"]

SITE_ADJECTIVES = [
    "Creek", "Hill", "Valley", "Range", "Flat", "Ridge", "Gully",
    "Plains", "Crossing", "Downs", "Springs", "Bluff", "Knob", "Gap",
]

SITE_NAMES = [
    "Wattle", "Mulga", "Ironbark", "Spinifex", "Coolibah", "Kurrajong",
    "Brolga", "Kookaburra", "Echidna", "Goanna", "Wedgetail", "Barramundi",
    "Kakadu", "Uluru", "Tanami", "Kimberley", "Nullarbor", "Darling",
    "Murray", "Cooper", "Fitzroy", "Mitchell", "Gilbert", "Palmer",
]

DRILL_TYPES = ["DDH", "RC", "AC", "RAB"]

REPORT_TYPES = [
    "Resource Estimation", "Feasibility Study", "Exploration Summary",
    "Production Report", "Ore Reserves Statement", "Geological Assessment",
    "Drilling Program Results", "Environmental Baseline Study",
    "Geophysical Survey Report", "Metallurgical Testwork Report",
]

AUTHOR_GIVEN_NAMES = [
    "Sarah", "Wei", "James", "Priya", "Emma", "Michael", "Aisha", "Liam",
    "Olivia", "Noah", "Charlotte", "Arjun",
]

AUTHOR_FAMILY_NAMES = [
    "Jones", "Chen", "Williams", "Patel", "Nguyen", "Taylor", "Singh",
    "Murphy", "Campbell", "Roberts", "Brown", "Evans",
]

AUTHOR_TITLES = ["Dr", "Prof", ""]

AFFILIATIONS = [
    "CSIRO Mineral Resources",
    "Geoscience Australia",
    "BHP Technical Services",
    "Rio Tinto Technical Services",
    "South32 Geology",
    "Mineral Resources Exploration",
    "University of Queensland",
    "Curtin Centre for Exploration Targeting",
]

STATE_CODE_TO_NAME = {
    "WA": "Western Australia",
    "QLD": "Queensland",
    "NSW": "New South Wales",
    "SA": "South Australia",
    "NT": "Northern Territory",
    "TAS": "Tasmania",
    "VIC": "Victoria",
}


@dataclass
class Author:
    iri_local: str
    name: str
    affiliation: str


def escape_ttl(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def to_iri_local(s: str) -> str:
    """Convert a label to a URI-safe local name (spaces → hyphens)."""
    return s.replace(" ", "-")


def pick_region(rng: random.Random) -> Region:
    return rng.choices(REGIONS, weights=REGION_WEIGHTS)[0]


def random_point_in_region(rng: random.Random, region: Region) -> tuple[float, float]:
    lat = rng.uniform(region.lat_min, region.lat_max)
    lon = rng.uniform(region.lon_min, region.lon_max)
    return (lat, lon)


def random_polygon_in_region(rng: random.Random, region: Region) -> list[tuple[float, float]]:
    """Small rectangle (0.02-0.1 deg per side) within the region."""
    lat, lon = random_point_in_region(rng, region)
    dlat = rng.uniform(0.02, 0.1)
    dlon = rng.uniform(0.02, 0.1)
    # Clamp to region bounds
    lat0 = max(region.lat_min, lat - dlat / 2)
    lat1 = min(region.lat_max, lat + dlat / 2)
    lon0 = max(region.lon_min, lon - dlon / 2)
    lon1 = min(region.lon_max, lon + dlon / 2)
    # Rectangle vertices (closed ring)
    return [(lat0, lon0), (lat0, lon1), (lat1, lon1), (lat1, lon0), (lat0, lon0)]


def format_point_epsg4326(lat: float, lon: float) -> str:
    return f'"<http://www.opengis.net/def/crs/EPSG/0/4326> POINT({lat:.6f} {lon:.6f})"^^geo:wktLiteral'


def format_point_crs84(lat: float, lon: float) -> str:
    return f'"POINT({lon:.6f} {lat:.6f})"^^geo:wktLiteral'


def format_polygon_epsg4326(verts: list[tuple[float, float]]) -> str:
    coords = ", ".join(f"{lat:.6f} {lon:.6f}" for lat, lon in verts)
    return f'"<http://www.opengis.net/def/crs/EPSG/0/4326> POLYGON(({coords}))"^^geo:wktLiteral'


def format_polygon_crs84(verts: list[tuple[float, float]]) -> str:
    coords = ", ".join(f"{lon:.6f} {lat:.6f}" for lat, lon in verts)
    return f'"POLYGON(({coords}))"^^geo:wktLiteral'


def pick_region_commodities(rng: random.Random, region: Region) -> list[str]:
    """1-3 commodities from the region's list, with 10% chance of a wildcard."""
    pool = list(region.commodities)
    if rng.random() < 0.10:
        wildcard = rng.choice([c for c in ALL_COMMODITIES if c not in pool])
        pool.append(wildcard)
    n = rng.choices([1, 2, 3], weights=[50, 35, 15])[0]
    n = min(n, len(pool))
    return rng.sample(pool, n)


def use_epsg4326(rng: random.Random) -> bool:
    """85% EPSG:4326, 15% CRS84 (bare WKT)."""
    return rng.random() < 0.85


def collect_vocab_iris(
    commodities: set[str],
    states: set[str],
    operators: set[str],
    statuses: set[str],
) -> str:
    """Emit rdfs:label triples for all vocabulary IRIs."""
    lines = ["## --- Vocabulary IRIs ---\n"]
    for c in sorted(commodities):
        lines.append(f'commodity:{to_iri_local(c)} rdfs:label "{escape_ttl(c)}" .')
    lines.append("")
    for s in sorted(states):
        lines.append(f'state:{to_iri_local(s)} rdfs:label "{s}" .')
    lines.append("")
    for o in sorted(operators):
        lines.append(f'operator:{to_iri_local(o)} rdfs:label "{escape_ttl(o)}" .')
    lines.append("")
    for st in sorted(statuses):
        lines.append(f'status:{to_iri_local(st)} rdfs:label "{escape_ttl(st)}" .')
    lines.append("")
    return "\n".join(lines)


def make_site_identifier(state: str, idx: int) -> str:
    return f"SITE-{state}-{idx:04d}"


def make_borehole_identifier(state: str, idx: int) -> str:
    return f"BH-{state}-{idx:06d}"


def make_report_identifier(state: str, year: int, idx: int) -> str:
    return f"RPT-{state}-{year}-{idx:04d}"


def state_name(state_code: str) -> str:
    return STATE_CODE_TO_NAME.get(state_code, state_code)


def generate_authors(rng: random.Random, n_reports: int) -> list[Author]:
    count = max(6, min(18, n_reports // 12))
    authors: list[Author] = []
    used_names: set[str] = set()

    while len(authors) < count:
        title = rng.choice(AUTHOR_TITLES)
        given = rng.choice(AUTHOR_GIVEN_NAMES)
        family = rng.choice(AUTHOR_FAMILY_NAMES)
        name = " ".join(part for part in [title, given, family] if part)
        if name in used_names:
            continue
        used_names.add(name)
        authors.append(
            Author(
                iri_local=f"author-{len(authors):03d}",
                name=name,
                affiliation=rng.choice(AFFILIATIONS),
            )
        )
    return authors


def generate_site(rng: random.Random, idx: int, region: Region) -> tuple[str, tuple[float, float], list[str]]:
    """Returns (turtle_block, (lat, lon), commodities) so boreholes can cluster nearby."""
    name = f"{rng.choice(SITE_NAMES)} {rng.choice(SITE_ADJECTIVES)}"
    identifier = make_site_identifier(region.state, idx)
    commodities = pick_region_commodities(rng, region)
    status = rng.choice(STATUSES)
    commodity_values = " , ".join(f'commodity:{to_iri_local(c)}' for c in commodities)
    description = (
        f"{name} Mine is a synthetic {', '.join(c.lower() for c in commodities)} "
        f"site in {state_name(region.state)} with {status.lower()} status."
    )

    # Geometry: point or polygon
    is_polygon = region.geometry == "mix" and rng.random() < 0.40
    epsg = use_epsg4326(rng)

    if is_polygon:
        verts = random_polygon_in_region(rng, region)
        wkt = format_polygon_epsg4326(verts) if epsg else format_polygon_crs84(verts)
        # Use centroid for borehole clustering
        lat = sum(v[0] for v in verts[:-1]) / (len(verts) - 1)
        lon = sum(v[1] for v in verts[:-1]) / (len(verts) - 1)
    else:
        lat, lon = random_point_in_region(rng, region)
        wkt = format_point_epsg4326(lat, lon) if epsg else format_point_crs84(lat, lon)

    ttl = textwrap.dedent(f"""\
        ex:site-{idx:04d} a ex:Site ;
            rdfs:label "{escape_ttl(name)} Mine" ;
            ex:identifier "{identifier}" ;
            dct:description "{escape_ttl(description)}" ;
            ex:commodity {commodity_values} ;
            ex:state state:{to_iri_local(region.state)} ;
            ex:status status:{to_iri_local(status)} ;
            geo:asWKT {wkt} .
    """)
    return (ttl, (lat, lon), commodities)


def generate_borehole(
    rng: random.Random,
    idx: int,
    region: Region,
    site_locations: list[tuple[float, float, Region]],
) -> str:
    prefix = rng.choice(["DDH", "RC", "AC"])
    site_name = rng.choice(SITE_NAMES).upper()[:3]
    label = f"{site_name}-{prefix}-{idx:03d}"
    drill_type = rng.choice(DRILL_TYPES)
    depth = rng.randint(50, 1200)

    # 50% chance: place near an existing site
    if site_locations and rng.random() < 0.50:
        slat, slon, sregion = rng.choice(site_locations)
        lat = slat + rng.uniform(-0.05, 0.05)
        lon = slon + rng.uniform(-0.05, 0.05)
        commodities = pick_region_commodities(rng, sregion)
        state = sregion.state
    else:
        lat, lon = random_point_in_region(rng, region)
        commodities = pick_region_commodities(rng, region)
        state = region.state

    identifier = make_borehole_identifier(state, idx)
    commodity_values = " , ".join(f'commodity:{to_iri_local(c)}' for c in commodities)
    description = (
        f"Synthetic {drill_type.lower()} borehole {identifier} in {state_name(state)} "
        f"targeting {', '.join(c.lower() for c in commodities)} mineralisation to {depth}m."
    )

    # Always POINT geometry for boreholes
    epsg = use_epsg4326(rng)
    wkt = format_point_epsg4326(lat, lon) if epsg else format_point_crs84(lat, lon)

    return textwrap.dedent(f"""\
        ex:bh-{idx:04d} a ex:Borehole ;
            rdfs:label "{label} {drill_type} Drill Hole" ;
            ex:identifier "{identifier}" ;
            dct:description "{escape_ttl(description)}" ;
            ex:commodity {commodity_values} ;
            ex:state state:{to_iri_local(state)} ;
            ex:depth {depth} ;
            geo:asWKT {wkt} .
    """)


def generate_report(rng: random.Random, idx: int, region: Region, authors: list[Author]) -> tuple[str, int, Author]:
    commodities = pick_region_commodities(rng, region)
    primary = commodities[0]
    operator = rng.choice(OPERATORS)
    status = rng.choice(REPORT_STATUSES)
    year = rng.randint(1980, 2024)
    identifier = make_report_identifier(region.state, year, idx)
    report_type = rng.choice(REPORT_TYPES)
    site_name = f"{rng.choice(SITE_NAMES)} {rng.choice(SITE_ADJECTIVES)}"
    title = f"{site_name} {primary} {report_type} {year}"
    author = rng.choice(authors)
    description = (
        f"{report_type} for the {site_name} {primary.lower()} project "
        f"in {state_name(region.state)}. Prepared by {operator} and authored by {author.name}."
    )
    commodity_values = " , ".join(f'commodity:{to_iri_local(c)}' for c in commodities)
    ttl = textwrap.dedent(f"""\
        ex:report-{idx:04d} a ex:MiningReport ;
            rdfs:label "{escape_ttl(title)}" ;
            ex:identifier "{identifier}" ;
            dct:description "{escape_ttl(description)}" ;
            ex:commodity {commodity_values} ;
            ex:state state:{to_iri_local(region.state)} ;
            ex:operator operator:{to_iri_local(operator)} ;
            ex:status status:{to_iri_local(status)} ;
            ex:year {year} ;
            ex:authoredBy ex:{author.iri_local} .
    """)
    return (ttl, year, author)


def main():
    parser = argparse.ArgumentParser(description="Generate synthetic mining RDF data")
    parser.add_argument("--count", type=int, default=100, help="Total number of entities")
    parser.add_argument("--seed", type=int, default=None, help="Random seed for reproducibility")
    args = parser.parse_args()

    rng = random.Random(args.seed)

    # Distribute entities: ~20% sites, ~40% boreholes, ~40% reports
    n_sites = max(1, args.count // 5)
    n_boreholes = max(1, (args.count - n_sites) // 2)
    n_reports = args.count - n_sites - n_boreholes

    # Collect all vocabulary values for rdfs:label emission
    all_commodities: set[str] = set()
    all_states: set[str] = set()
    all_operators: set[str] = set()
    all_statuses: set[str] = set()

    # Pre-generate to collect vocab, then emit
    site_blocks: list[str] = []
    site_locations: list[tuple[float, float, Region]] = []
    borehole_blocks: list[str] = []
    report_blocks: list[str] = []
    author_to_reports: dict[str, list[str]] = {}
    authors = generate_authors(rng, n_reports)

    for i in range(n_sites):
        region = pick_region(rng)
        ttl, (lat, lon), commodities = generate_site(rng, i, region)
        site_locations.append((lat, lon, region))
        site_blocks.append(ttl)
        all_commodities.update(commodities)
        all_states.add(region.state)
        # Sites use STATUSES list
        # (status is embedded in ttl, but we know the full set)

    for i in range(n_boreholes):
        region = pick_region(rng)
        ttl = generate_borehole(rng, i, region, site_locations)
        borehole_blocks.append(ttl)

    for i in range(n_reports):
        region = pick_region(rng)
        ttl, _year, author = generate_report(rng, i, region, authors)
        report_blocks.append(ttl)
        author_to_reports.setdefault(author.iri_local, []).append(f"ex:report-{i:04d}")

    # Collect all possible vocab values (superset for labels)
    all_commodities.update(ALL_COMMODITIES)
    all_states.update(r.state for r in REGIONS)
    all_operators.update(OPERATORS)
    all_statuses.update(STATUSES)
    all_statuses.update(REPORT_STATUSES)

    print("## Licensed under the terms of http://www.apache.org/licenses/LICENSE-2.0")
    print()
    print("## Generated synthetic mining data")
    print(f"## {args.count} entities: {n_sites} sites, {n_boreholes} boreholes, {n_reports} reports")
    print()
    print("@prefix ex:         <http://example.org/mining/> .")
    print("@prefix commodity:  <http://example.org/mining/commodity/> .")
    print("@prefix state:      <http://example.org/mining/state/> .")
    print("@prefix operator:   <http://example.org/mining/operator/> .")
    print("@prefix status:     <http://example.org/mining/status/> .")
    print("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .")
    print("@prefix dct:   <http://purl.org/dc/terms/> .")
    print("@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .")
    print("@prefix geo:   <http://www.opengis.net/ont/geosparql#> .")
    print()

    print(collect_vocab_iris(all_commodities, all_states, all_operators, all_statuses))

    print("## --- Sites ---\n")
    for block in site_blocks:
        print(block)

    print("## --- Boreholes ---\n")
    for block in borehole_blocks:
        print(block)

    print("## --- Authors ---\n")
    for author in authors:
        reports = author_to_reports.get(author.iri_local, [])
        lines = [
            f"ex:{author.iri_local} a ex:Author ;",
            f'    rdfs:label "{escape_ttl(author.name)}" ;',
            f'    ex:name "{escape_ttl(author.name)}" ;',
            f'    ex:affiliation "{escape_ttl(author.affiliation)}"',
        ]
        if reports:
            lines[-1] += " ;"
            lines.append(f"    ex:authored {', '.join(reports)}")
        lines[-1] += " ."
        print("\n".join(lines))
        print()

    print("## --- Mining Reports ---\n")
    for block in report_blocks:
        print(block)


if __name__ == "__main__":
    main()
