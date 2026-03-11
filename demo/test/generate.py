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


def escape_ttl(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


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


def generate_site(rng: random.Random, idx: int, region: Region) -> tuple[str, tuple[float, float]]:
    """Returns (turtle_block, (lat, lon)) so boreholes can cluster nearby."""
    name = f"{rng.choice(SITE_NAMES)} {rng.choice(SITE_ADJECTIVES)}"
    commodities = pick_region_commodities(rng, region)
    status = rng.choice(STATUSES)
    commodity_values = " , ".join(f'"{escape_ttl(c)}"' for c in commodities)

    # Geometry: point or polygon
    is_polygon = region.geometry == "mix" and rng.random() < 0.40
    epsg = use_epsg4326(rng)

    if is_polygon:
        verts = random_polygon_in_region(rng, region)
        wkt = format_polygon_epsg4326(verts[0][0], verts[0][1]) if False else (
            format_polygon_epsg4326(verts) if epsg else format_polygon_crs84(verts)
        )
        # Use centroid for borehole clustering
        lat = sum(v[0] for v in verts[:-1]) / (len(verts) - 1)
        lon = sum(v[1] for v in verts[:-1]) / (len(verts) - 1)
    else:
        lat, lon = random_point_in_region(rng, region)
        wkt = format_point_epsg4326(lat, lon) if epsg else format_point_crs84(lat, lon)

    ttl = textwrap.dedent(f"""\
        ex:site-{idx:04d} a ex:Site ;
            rdfs:label "{escape_ttl(name)} Mine" ;
            ex:commodity {commodity_values} ;
            ex:state "{region.state}" ;
            ex:status "{status}" ;
            geo:asWKT {wkt} .
    """)
    return (ttl, (lat, lon))


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

    commodity_values = " , ".join(f'"{escape_ttl(c)}"' for c in commodities)

    # Always POINT geometry for boreholes
    epsg = use_epsg4326(rng)
    wkt = format_point_epsg4326(lat, lon) if epsg else format_point_crs84(lat, lon)

    return textwrap.dedent(f"""\
        ex:bh-{idx:04d} a ex:Borehole ;
            rdfs:label "{label} {drill_type} Drill Hole" ;
            ex:commodity {commodity_values} ;
            ex:state "{state}" ;
            ex:depth {depth} ;
            geo:asWKT {wkt} .
    """)


def generate_report(rng: random.Random, idx: int, region: Region) -> str:
    commodities = pick_region_commodities(rng, region)
    primary = commodities[0]
    operator = rng.choice(OPERATORS)
    status = rng.choice(REPORT_STATUSES)
    year = rng.randint(1980, 2024)
    report_type = rng.choice(REPORT_TYPES)
    site_name = f"{rng.choice(SITE_NAMES)} {rng.choice(SITE_ADJECTIVES)}"
    title = f"{site_name} {primary} {report_type} {year}"
    description = (
        f"{report_type} for the {site_name} {primary.lower()} project "
        f"in {region.state}. Prepared by {operator}."
    )
    commodity_values = " , ".join(f'"{escape_ttl(c)}"' for c in commodities)
    return textwrap.dedent(f"""\
        ex:report-{idx:04d} a ex:MiningReport ;
            rdfs:label "{escape_ttl(title)}" ;
            dct:description "{escape_ttl(description)}" ;
            ex:commodity {commodity_values} ;
            ex:state "{region.state}" ;
            ex:operator "{escape_ttl(operator)}" ;
            ex:status "{status}" ;
            ex:year {year} .
    """)


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

    print("## Generated synthetic mining data")
    print(f"## {args.count} entities: {n_sites} sites, {n_boreholes} boreholes, {n_reports} reports")
    print()
    print("@prefix ex:    <http://example.org/mining/> .")
    print("@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .")
    print("@prefix dct:   <http://purl.org/dc/terms/> .")
    print("@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .")
    print("@prefix geo:   <http://www.opengis.net/ont/geosparql#> .")
    print()

    # Generate sites first — track locations for borehole clustering
    site_locations: list[tuple[float, float, Region]] = []

    print("## --- Sites ---\n")
    for i in range(n_sites):
        region = pick_region(rng)
        ttl, (lat, lon) = generate_site(rng, i, region)
        site_locations.append((lat, lon, region))
        print(ttl)

    print("## --- Boreholes ---\n")
    for i in range(n_boreholes):
        region = pick_region(rng)
        print(generate_borehole(rng, i, region, site_locations))

    print("## --- Mining Reports ---\n")
    for i in range(n_reports):
        region = pick_region(rng)
        print(generate_report(rng, i, region))


if __name__ == "__main__":
    main()
