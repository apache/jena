'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _stringify;

function _load_stringify() {
  return _stringify = _interopRequireDefault(require('babel-runtime/core-js/json/stringify'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _keys;

function _load_keys() {
  return _keys = _interopRequireDefault(require('babel-runtime/core-js/object/keys'));
}

var _index;

function _load_index() {
  return _index = _interopRequireDefault(require('./util/normalize-manifest/index.js'));
}

var _errors;

function _load_errors() {
  return _errors = require('./errors.js');
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('./util/fs.js'));
}

var _constants;

function _load_constants() {
  return _constants = _interopRequireWildcard(require('./constants.js'));
}

var _packageConstraintResolver;

function _load_packageConstraintResolver() {
  return _packageConstraintResolver = _interopRequireDefault(require('./package-constraint-resolver.js'));
}

var _requestManager;

function _load_requestManager() {
  return _requestManager = _interopRequireDefault(require('./util/request-manager.js'));
}

var _index2;

function _load_index2() {
  return _index2 = require('./registries/index.js');
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('./util/map.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const detectIndent = require('detect-indent');

const invariant = require('invariant');
const path = require('path');
const url = require('url');

function sortObject(object) {
  const sortedObject = {};
  (0, (_keys || _load_keys()).default)(object).sort().forEach(item => {
    sortedObject[item] = object[item];
  });
  return sortedObject;
}

class Config {
  constructor(reporter) {
    this.constraintResolver = new (_packageConstraintResolver || _load_packageConstraintResolver()).default(this, reporter);
    this.requestManager = new (_requestManager || _load_requestManager()).default(reporter);
    this.reporter = reporter;
    this._init({});
  }

  //


  //


  //


  //


  //


  //


  //


  //


  //


  //


  //


  //


  //


  //


  /**
   * Execute a promise produced by factory if it doesn't exist in our cache with
   * the associated key.
   */

  getCache(key, factory) {
    const cached = this.cache[key];
    if (cached) {
      return cached;
    }

    return this.cache[key] = factory().catch(err => {
      this.cache[key] = null;
      throw err;
    });
  }

  /**
   * Get a config option from our yarn config.
   */

  getOption(key) {
    return this.registries.yarn.getOption(key);
  }

  /**
   * Reduce a list of versions to a single one based on an input range.
   */

  resolveConstraints(versions, range) {
    return this.constraintResolver.reduce(versions, range);
  }

  /**
   * Initialise config. Fetch registry options, find package roots.
   */

  init() {
    var _arguments = arguments,
        _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      let opts = _arguments.length > 0 && _arguments[0] !== undefined ? _arguments[0] : {};

      _this._init(opts);

      yield (_fs || _load_fs()).mkdirp(_this.globalFolder);
      yield (_fs || _load_fs()).mkdirp(_this.cacheFolder);
      yield (_fs || _load_fs()).mkdirp(_this.tempFolder);

      yield (_fs || _load_fs()).mkdirp(_this.linkFolder);
      _this.linkedModules = yield (_fs || _load_fs()).readdir(_this.linkFolder);

      for (const key of (0, (_keys || _load_keys()).default)((_index2 || _load_index2()).registries)) {
        const Registry = (_index2 || _load_index2()).registries[key];

        // instantiate registry
        const registry = new Registry(_this.cwd, _this.registries, _this.requestManager);
        yield registry.init();

        _this.registries[key] = registry;
        _this.registryFolders.push(registry.folder);
        _this.rootModuleFolders.push(path.join(_this.cwd, registry.folder));
      }

      _this.requestManager.setOptions({
        userAgent: String(_this.getOption('user-agent')),
        httpProxy: String(_this.getOption('proxy') || ''),
        httpsProxy: String(_this.getOption('https-proxy') || ''),
        strictSSL: Boolean(_this.getOption('strict-ssl')),
        cafile: String(opts.cafile || _this.getOption('cafile') || '')
      });
    })();
  }

  _init(opts) {
    this.rootModuleFolders = [];
    this.registryFolders = [];
    this.linkedModules = [];

    this.registries = (0, (_map || _load_map()).default)();
    this.cache = (0, (_map || _load_map()).default)();
    this.cwd = opts.cwd || this.cwd || process.cwd();

    this.looseSemver = opts.looseSemver == undefined ? true : opts.looseSemver;

    this.preferOffline = !!opts.preferOffline;
    this.modulesFolder = opts.modulesFolder;
    this.globalFolder = opts.globalFolder || (_constants || _load_constants()).GLOBAL_MODULE_DIRECTORY;
    this.cacheFolder = opts.cacheFolder || (_constants || _load_constants()).MODULE_CACHE_DIRECTORY;
    this.linkFolder = opts.linkFolder || (_constants || _load_constants()).LINK_REGISTRY_DIRECTORY;
    this.tempFolder = opts.tempFolder || path.join(this.cacheFolder, '.tmp');
    this.offline = !!opts.offline;
    this.ignorePlatform = !!opts.ignorePlatform;

    this.requestManager.setOptions({
      offline: !!opts.offline && !opts.preferOffline,
      captureHar: !!opts.captureHar
    });

    if (this.modulesFolder) {
      this.rootModuleFolders.push(this.modulesFolder);
    }
  }

  /**
   * Generate an absolute module path.
   */

  generateHardModulePath(pkg, ignoreLocation) {
    invariant(this.cacheFolder, 'No package root');
    invariant(pkg, 'Undefined package');
    invariant(pkg.name, 'No name field in package');
    invariant(pkg.uid, 'No uid field in package');
    if (pkg.location && !ignoreLocation) {
      return pkg.location;
    }

    let name = pkg.name;
    let uid = pkg.uid;
    if (pkg.registry) {
      name = `${ pkg.registry }-${ name }`;
      uid = pkg.version || uid;
    }

    return path.join(this.cacheFolder, `${ name }-${ uid }`);
  }

  /**
   * Generate an absolute temporary filename location based on the input filename.
   */

  getTemp(filename) {
    invariant(this.tempFolder, 'No temp folder');
    return path.join(this.tempFolder, filename);
  }

  /**
   * Remote packages may be cached in a file system to be available for offline installation
   * Second time the same package needs to be installed it will be loaded from there
   */

  getOfflineMirrorPath(tarUrl) {
    const registry = this.registries.npm;
    if (registry == null) {
      return null;
    }

    //
    const mirrorPath = registry.config['yarn-offline-mirror'];
    if (mirrorPath == null) {
      return null;
    }

    //
    if (tarUrl == null) {
      return mirrorPath;
    }

    //

    var _url$parse = url.parse(tarUrl);

    const pathname = _url$parse.pathname;

    if (pathname == null) {
      return mirrorPath;
    } else {
      return path.join(mirrorPath, path.basename(pathname));
    }
  }

  /**
   * Checker whether the folder input is a valid module folder. We output a yarn metadata
   * file when we've successfully setup a folder so use this as a marker.
   */

  isValidModuleDest(dest) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      if (!(yield (_fs || _load_fs()).exists(dest))) {
        return false;
      }

      if (!(yield (_fs || _load_fs()).exists(path.join(dest, (_constants || _load_constants()).METADATA_FILENAME)))) {
        return false;
      }

      return true;
    })();
  }

  /**
   * Read package metadata and normalized package info.
   */

  readPackageMetadata(dir) {
    var _this2 = this;

    return this.getCache(`metadata-${ dir }`, (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const metadata = yield (_fs || _load_fs()).readJson(path.join(dir, (_constants || _load_constants()).METADATA_FILENAME));
      const pkg = yield _this2.readManifest(dir, metadata.registry);

      return {
        package: pkg,
        hash: metadata.hash,
        remote: metadata.remote,
        registry: metadata.registry
      };
    }));
  }

  /**
   * Read normalized package info.
   */

  readManifest(dir, priorityRegistry) {
    var _this3 = this;

    let isRoot = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : false;

    return this.getCache(`manifest-${ dir }`, (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const metadataLoc = path.join(dir, (_constants || _load_constants()).METADATA_FILENAME);
      if (!priorityRegistry && (yield (_fs || _load_fs()).exists(metadataLoc))) {
        var _ref3 = yield (_fs || _load_fs()).readJson(metadataLoc);

        priorityRegistry = _ref3.registry;
      }

      if (priorityRegistry) {
        const file = yield _this3.tryManifest(dir, priorityRegistry, isRoot);
        if (file) {
          return file;
        }
      }

      for (const registry of (0, (_keys || _load_keys()).default)((_index2 || _load_index2()).registries)) {
        if (priorityRegistry === registry) {
          continue;
        }

        const file = yield _this3.tryManifest(dir, registry, isRoot);
        if (file) {
          return file;
        }
      }

      throw new (_errors || _load_errors()).MessageError(`Couldn't find a package.json (or bower.json) file in ${ dir }`);
    }));
  }

  /**
   * Read the root manifest.
   */

  readRootManifest() {
    return this.readManifest(this.cwd, 'npm', true);
  }

  /**
   * Try and find package info with the input directory and registry.
   */

  tryManifest(dir, registry, isRoot) {
    var _this4 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const filename = (_index2 || _load_index2()).registries[registry].filename;

      const loc = path.join(dir, filename);
      if (yield (_fs || _load_fs()).exists(loc)) {
        const data = yield (_fs || _load_fs()).readJson(loc);
        data._registry = registry;
        data._loc = loc;
        return (0, (_index || _load_index()).default)(data, dir, _this4, isRoot);
      } else {
        return null;
      }
    })();
  }

  /**
   * Description
   */

  getFolder(pkg) {
    let registryName = pkg._registry;
    if (!registryName) {
      const ref = pkg._reference;
      invariant(ref, 'expected reference');
      registryName = ref.registry;
    }
    return this.registries[registryName].folder;
  }

  /**
   * Get root manifests.
   */

  getRootManifests() {
    var _this5 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const manifests = {};
      for (const registryName of (_index2 || _load_index2()).registryNames) {
        const registry = (_index2 || _load_index2()).registries[registryName];
        const jsonLoc = path.join(_this5.cwd, registry.filename);

        let object = {};
        let exists = false;
        let indent;
        if (yield (_fs || _load_fs()).exists(jsonLoc)) {
          exists = true;

          const info = yield (_fs || _load_fs()).readJsonAndFile(jsonLoc);
          object = info.object;
          indent = detectIndent(info.content).indent || undefined;
        }
        manifests[registryName] = { loc: jsonLoc, object: object, exists: exists, indent: indent };
      }
      return manifests;
    })();
  }

  /**
   * Save root manifests.
   */

  saveRootManifests(manifests) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      for (const registryName of (_index2 || _load_index2()).registryNames) {
        var _manifests$registryNa = manifests[registryName];
        const loc = _manifests$registryNa.loc;
        const object = _manifests$registryNa.object;
        const exists = _manifests$registryNa.exists;
        const indent = _manifests$registryNa.indent;

        if (!exists && !(0, (_keys || _load_keys()).default)(object).length) {
          continue;
        }

        for (const field of (_constants || _load_constants()).DEPENDENCY_TYPES) {
          if (object[field]) {
            object[field] = sortObject(object[field]);
          }
        }

        yield (_fs || _load_fs()).writeFile(loc, (0, (_stringify || _load_stringify()).default)(object, null, indent || (_constants || _load_constants()).DEFAULT_INDENT) + '\n');
      }
    })();
  }
}
exports.default = Config;