'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _packageReference;

function _load_packageReference() {
  return _packageReference = require('./package-reference.js');
}

var _packageRequest;

function _load_packageRequest() {
  return _packageRequest = _interopRequireDefault(require('./package-request.js'));
}

var _requestManager;

function _load_requestManager() {
  return _requestManager = _interopRequireDefault(require('./util/request-manager.js'));
}

var _blockingQueue;

function _load_blockingQueue() {
  return _blockingQueue = _interopRequireDefault(require('./util/blocking-queue.js'));
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('./lockfile/wrapper.js'));
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('./util/map.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');

class PackageResolver {
  constructor(config, lockfile) {
    this.patternsByPackage = (0, (_map || _load_map()).default)();
    this.fetchingPatterns = (0, (_map || _load_map()).default)();
    this.fetchingQueue = new (_blockingQueue || _load_blockingQueue()).default('resolver fetching');
    this.newPatterns = [];
    this.patterns = (0, (_map || _load_map()).default)();
    this.usedRegistries = new Set();
    this.flat = false;

    this.reporter = config.reporter;
    this.lockfile = lockfile;
    this.config = config;
  }

  // whether the dependency graph will be flattened


  // list of registries that have been used in this resolution


  // activity monitor


  // patterns we've already resolved or are in the process of resolving


  // new patterns that didn't exist in the lockfile


  // TODO


  // these are patterns that the package resolver was seeded with. these are required in
  // order to resolve top level peerDependencies


  // manages and throttles json api http requests


  // list of patterns associated with a package


  // lockfile instance which we can use to retrieve version info


  // a map of dependency patterns to packages


  // reporter instance, abstracts out display logic


  // environment specific config methods and options


  /**
   * TODO description
   */

  isNewPattern(pattern) {
    return this.newPatterns.indexOf(pattern) >= 0;
  }

  /**
   * TODO description
   */

  updateManifest(ref, newPkg) {
    // inherit fields
    const oldPkg = this.patterns[ref.patterns[0]];
    newPkg._reference = ref;
    newPkg._remote = ref.remote;
    newPkg.name = oldPkg.name;

    // update patterns
    for (const pattern of ref.patterns) {
      this.patterns[pattern] = newPkg;
    }

    return Promise.resolve();
  }

  /**
   * Given a list of patterns, dedupe them to a list of unique patterns.
   */

  dedupePatterns(patterns) {
    const deduped = [];
    const seen = new Set();

    for (const pattern of patterns) {
      const info = this.getResolvedPattern(pattern);
      if (seen.has(info)) {
        continue;
      }

      seen.add(info);
      deduped.push(pattern);
    }

    return deduped;
  }

  /**
   * Get a list of all manifests by topological order.
   */

  getTopologicalManifests(seedPatterns) {
    const pkgs = new Set();
    const skip = new Set();

    const add = seedPatterns => {
      for (const pattern of seedPatterns) {
        const pkg = this.getStrictResolvedPattern(pattern);
        if (skip.has(pkg)) {
          continue;
        }

        const ref = pkg._reference;
        invariant(ref, 'expected reference');
        skip.add(pkg);
        add(ref.dependencies);
        pkgs.add(pkg);
      }
    };

    add(seedPatterns);

    return pkgs;
  }

  /**
   * Get a list of all manifests by level sort order.
   */

  getLevelOrderManifests(seedPatterns) {
    const pkgs = new Set();
    const skip = new Set();

    const add = seedPatterns => {
      const refs = [];

      for (const pattern of seedPatterns) {
        const pkg = this.getStrictResolvedPattern(pattern);
        if (skip.has(pkg)) {
          continue;
        }

        const ref = pkg._reference;
        invariant(ref, 'expected reference');

        refs.push(ref);
        skip.add(pkg);
        pkgs.add(pkg);
      }

      for (const ref of refs) {
        add(ref.dependencies);
      }
    };

    add(seedPatterns);

    return pkgs;
  }

  /**
   * Get a list of all package names in the depenency graph.
   */

  getAllDependencyNamesByLevelOrder(seedPatterns) {
    const names = new Set();
    for (const _ref of this.getLevelOrderManifests(seedPatterns)) {
      const name = _ref.name;

      names.add(name);
    }
    return names;
  }

  /**
   * Retrieve all the package info stored for this package name.
   */

  getAllInfoForPackageName(name) {
    const infos = [];
    const seen = new Set();

    for (const pattern of this.patternsByPackage[name]) {
      const info = this.patterns[pattern];
      if (seen.has(info)) {
        continue;
      }

      seen.add(info);
      infos.push(info);
    }

    return infos;
  }

  /**
   * Get a flat list of all package references.
   */

  getPackageReferences() {
    const refs = [];

    for (const manifest of this.getManifests()) {
      const ref = manifest._reference;
      if (ref) {
        refs.push(ref);
      }
    }

    return refs;
  }

  /**
   * Get a flat list of all package info.
   */

  getManifests() {
    const infos = [];
    const seen = new Set();

    for (const pattern in this.patterns) {
      const info = this.patterns[pattern];
      if (seen.has(info)) {
        continue;
      }

      infos.push(info);
      seen.add(info);
    }

    return infos;
  }

  /**
   * Make all versions of this package resolve to it.
   */

  collapseAllVersionsOfPackage(name, version) {
    const patterns = this.dedupePatterns(this.patternsByPackage[name]);
    const human = `${ name }@${ version }`;

    // get manifest that matches the version we're collapsing too
    let collapseToReference;
    let collapseToManifest;
    let collapseToPattern;
    for (const pattern of patterns) {
      const _manifest = this.patterns[pattern];
      if (_manifest.version === version) {
        collapseToReference = _manifest._reference;
        collapseToManifest = _manifest;
        collapseToPattern = pattern;
        break;
      }
    }
    invariant(collapseToReference && collapseToManifest && collapseToPattern, `Couldn't find package manifest for ${ human }`);

    for (const pattern of patterns) {
      // don't touch the pattern we're collapsing to
      if (pattern === collapseToPattern) {
        continue;
      }

      // remove this pattern
      const ref = this.getStrictResolvedPattern(pattern)._reference;
      invariant(ref, 'expected package reference');
      const refPatterns = ref.patterns.slice();
      ref.addVisibility((_packageReference || _load_packageReference()).REMOVED_ANCESTOR);
      ref.prune();

      for (const action in ref.visibility) {
        collapseToReference.visibility[action] += ref.visibility[action];
      }

      // add pattern to the manifest we're collapsing to
      for (const pattern of refPatterns) {
        collapseToReference.addPattern(pattern, collapseToManifest);
      }
    }

    return collapseToPattern;
  }

  /**
   * TODO description
   */

  addPattern(pattern, info) {
    this.patterns[pattern] = info;

    const byName = this.patternsByPackage[info.name] = this.patternsByPackage[info.name] || [];
    byName.push(pattern);
  }

  /**
   * TODO description
   */

  removePattern(pattern) {
    const pkg = this.patterns[pattern];
    if (!pkg) {
      return;
    }

    const byName = this.patternsByPackage[pkg.name];
    if (!byName) {
      return;
    }

    byName.splice(byName.indexOf(pattern), 1);
    delete this.patterns[pattern];
  }

  /**
   * TODO description
   */

  getResolvedPattern(pattern) {
    return this.patterns[pattern];
  }

  /**
   * TODO description
   */

  getStrictResolvedPattern(pattern) {
    const manifest = this.getResolvedPattern(pattern);
    invariant(manifest, 'expected manifest');
    return manifest;
  }

  /**
   * TODO description
   */

  getExactVersionMatch(name, version) {
    const patterns = this.patternsByPackage[name];
    if (!patterns) {
      return null;
    }

    for (const pattern of patterns) {
      const info = this.getStrictResolvedPattern(pattern);
      if (info.version === version) {
        return info;
      }
    }

    return null;
  }

  /**
   * TODO description
   */

  find(req) {
    var _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const fetchKey = `${ req.registry }:${ req.pattern }`;
      if (_this.fetchingPatterns[fetchKey]) {
        return;
      } else {
        _this.fetchingPatterns[fetchKey] = true;
      }

      if (_this.activity) {
        _this.activity.tick(req.pattern);
      }

      if (!_this.lockfile.getLocked(req.pattern, true)) {
        _this.newPatterns.push(req.pattern);
      }

      // propagate `visibility` option
      const parentRequest = req.parentRequest;

      if (parentRequest && parentRequest.visibility) {
        req.visibility = parentRequest.visibility;
      }

      const request = new (_packageRequest || _load_packageRequest()).default(req, _this);
      yield request.find();
    })();
  }

  /**
   * TODO description
   */

  init(deps, isFlat) {
    var _this2 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      _this2.flat = isFlat;

      //
      const activity = _this2.activity = _this2.reporter.activity();

      //
      _this2.seedPatterns = deps.map(function (dep) {
        return dep.pattern;
      });

      //
      yield Promise.all(deps.map(function (req) {
        return _this2.find(req);
      }));

      activity.end();
      _this2.activity = null;
    })();
  }
}
exports.default = PackageResolver;