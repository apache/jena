'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.Install = undefined;

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _assign;

function _load_assign() {
  return _assign = _interopRequireDefault(require('babel-runtime/core-js/object/assign'));
}

var _keys;

function _load_keys() {
  return _keys = _interopRequireDefault(require('babel-runtime/core-js/object/keys'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let run = exports.run = (() => {
  var _ref10 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    let lockfile;
    if (flags.lockfile === false) {
      lockfile = new (_wrapper || _load_wrapper()).default();
    } else {
      lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd, reporter);
    }

    if (args.length) {
      const exampleArgs = args.slice();
      if (flags.saveDev) {
        exampleArgs.push('--dev');
      }
      if (flags.savePeer) {
        exampleArgs.push('--peer');
      }
      if (flags.saveOptional) {
        exampleArgs.push('--optional');
      }
      if (flags.saveExact) {
        exampleArgs.push('--exact');
      }
      if (flags.saveTilde) {
        exampleArgs.push('--tilde');
      }
      let command = 'add';
      if (flags.global) {
        command = 'global add';
      }
      throw new (_errors || _load_errors()).MessageError(reporter.lang('installCommandRenamed', `yarn ${ command } ${ exampleArgs.join(' ') }`));
    }

    yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'preinstall');

    const install = new Install(flags, config, reporter, lockfile);
    yield install.init();

    // npm behaviour, seems kinda funky but yay compatibility
    yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'install');
    yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'postinstall');
    if (!flags.production) {
      yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(config, 'prepublish');
    }
  });

  return function run(_x14, _x15, _x16, _x17) {
    return _ref10.apply(this, arguments);
  };
})();

exports._setFlags = _setFlags;
exports.setFlags = setFlags;

var _index;

function _load_index() {
  return _index = _interopRequireDefault(require('../../util/normalize-manifest/index.js'));
}

var _index2;

function _load_index2() {
  return _index2 = require('../../registries/index.js');
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('../../lockfile/wrapper.js'));
}

var _executeLifecycleScript;

function _load_executeLifecycleScript() {
  return _executeLifecycleScript = _interopRequireDefault(require('./_execute-lifecycle-script.js'));
}

var _stringify;

function _load_stringify() {
  return _stringify = _interopRequireDefault(require('../../lockfile/stringify.js'));
}

var _packageReference;

function _load_packageReference() {
  return _packageReference = _interopRequireWildcard(require('../../package-reference.js'));
}

var _packageFetcher;

function _load_packageFetcher() {
  return _packageFetcher = _interopRequireDefault(require('../../package-fetcher.js'));
}

var _packageInstallScripts;

function _load_packageInstallScripts() {
  return _packageInstallScripts = _interopRequireDefault(require('../../package-install-scripts.js'));
}

var _packageCompatibility;

function _load_packageCompatibility() {
  return _packageCompatibility = _interopRequireDefault(require('../../package-compatibility.js'));
}

var _packageResolver;

function _load_packageResolver() {
  return _packageResolver = _interopRequireDefault(require('../../package-resolver.js'));
}

var _packageLinker;

function _load_packageLinker() {
  return _packageLinker = _interopRequireDefault(require('../../package-linker.js'));
}

var _packageRequest;

function _load_packageRequest() {
  return _packageRequest = _interopRequireDefault(require('../../package-request.js'));
}

var _clean;

function _load_clean() {
  return _clean = require('./clean.js');
}

var _constants;

function _load_constants() {
  return _constants = _interopRequireWildcard(require('../../constants.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

var _crypto;

function _load_crypto() {
  return _crypto = _interopRequireWildcard(require('../../util/crypto.js'));
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('../../util/map.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');
const emoji = require('node-emoji');
const path = require('path');

function normalizeFlags(config, rawFlags) {
  const flags = {
    // install
    har: !!rawFlags.har,
    ignorePlatform: !!rawFlags.ignorePlatform,
    ignoreEngines: !!rawFlags.ignoreEngines,
    ignoreScripts: !!rawFlags.ignoreScripts,
    ignoreOptional: !!rawFlags.ignoreOptional,
    force: !!rawFlags.force,
    flat: !!rawFlags.flat,
    production: !!rawFlags.production,
    lockfile: rawFlags.lockfile !== false,
    pureLockfile: !!rawFlags.pureLockfile,
    skipIntegrity: !!rawFlags.skipIntegrity,

    // add
    peer: !!rawFlags.peer,
    dev: !!rawFlags.dev,
    optional: !!rawFlags.optional,
    exact: !!rawFlags.exact,
    tilde: !!rawFlags.tilde
  };

  if (config.getOption('ignore-scripts')) {
    flags.ignoreScripts = true;
  }

  if (config.getOption('ignore-platform')) {
    flags.ignorePlatform = true;
  }

  if (config.getOption('ignore-engines')) {
    flags.ignoreEngines = true;
  }

  if (config.getOption('ignore-optional')) {
    flags.ignoreOptional = true;
  }

  if (config.getOption('force')) {
    flags.force = true;
  }

  if (config.getOption('production')) {
    flags.production = true;
  }

  return flags;
}

class Install {
  constructor(flags, config, reporter, lockfile) {
    this.rootManifestRegistries = [];
    this.rootPatternsToOrigin = (0, (_map || _load_map()).default)();
    this.resolutions = (0, (_map || _load_map()).default)();
    this.lockfile = lockfile;
    this.reporter = reporter;
    this.config = config;
    this.flags = normalizeFlags(config, flags);

    this.resolver = new (_packageResolver || _load_packageResolver()).default(config, lockfile);
    this.fetcher = new (_packageFetcher || _load_packageFetcher()).default(config, this.resolver);
    this.compatibility = new (_packageCompatibility || _load_packageCompatibility()).default(config, this.resolver, this.flags.ignoreEngines);
    this.linker = new (_packageLinker || _load_packageLinker()).default(config, this.resolver, this.flags.ignoreOptional);
    this.scripts = new (_packageInstallScripts || _load_packageInstallScripts()).default(config, this.resolver, this.flags.force);
  }

  /**
   * Create a list of dependency requests from the current directories manifests.
   */

  fetchRequestFromCwd() {
    var _arguments = arguments,
        _this = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      let excludePatterns = _arguments.length > 0 && _arguments[0] !== undefined ? _arguments[0] : [];

      const patterns = [];
      const deps = [];
      const manifest = {};

      // exclude package names that are in install args
      const excludeNames = [];
      for (const pattern of excludePatterns) {
        // can't extract a package name from this
        if ((_packageRequest || _load_packageRequest()).default.getExoticResolver(pattern)) {
          continue;
        }

        // extract the name
        const parts = (_packageRequest || _load_packageRequest()).default.normalizePattern(pattern);
        excludeNames.push(parts.name);
      }

      for (const registry of (0, (_keys || _load_keys()).default)((_index2 || _load_index2()).registries)) {
        const filename = (_index2 || _load_index2()).registries[registry].filename;

        const loc = path.join(_this.config.cwd, filename);
        if (!(yield (_fs || _load_fs()).exists(loc))) {
          continue;
        }

        _this.rootManifestRegistries.push(registry);
        const json = yield (_fs || _load_fs()).readJson(loc);
        yield (0, (_index || _load_index()).default)(json, _this.config.cwd, _this.config, true);

        (0, (_assign || _load_assign()).default)(_this.resolutions, json.resolutions);
        (0, (_assign || _load_assign()).default)(manifest, json);

        const pushDeps = function pushDeps(depType, _ref) {
          let hint = _ref.hint;
          let visibility = _ref.visibility;
          let optional = _ref.optional;

          const depMap = json[depType];
          for (const name in depMap) {
            if (excludeNames.indexOf(name) >= 0) {
              continue;
            }

            let pattern = name;
            if (!_this.lockfile.getLocked(pattern, true)) {
              // when we use --save we save the dependency to the lockfile with just the name rather than the
              // version combo
              pattern += '@' + depMap[name];
            }

            _this.rootPatternsToOrigin[pattern] = depType;
            patterns.push(pattern);
            deps.push({ pattern: pattern, registry: registry, visibility: visibility, hint: hint, optional: optional });
          }
        };

        pushDeps('dependencies', { hint: null, visibility: (_packageReference || _load_packageReference()).USED, optional: false });

        const devVisibility = _this.flags.production ? (_packageReference || _load_packageReference()).ENVIRONMENT_IGNORE : (_packageReference || _load_packageReference()).USED;
        pushDeps('devDependencies', { hint: 'dev', visibility: devVisibility, optional: false });

        pushDeps('optionalDependencies', { hint: 'optional', visibility: (_packageReference || _load_packageReference()).USED, optional: true });

        break;
      }

      // inherit root flat flag
      if (manifest.flat) {
        _this.flags.flat = true;
      }

      return [deps, patterns, manifest];
    })();
  }

  /**
   * TODO description
   */

  prepare(patterns, requests, match) {
    var _this2 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      if (!_this2.flags.skipIntegrity && !_this2.flags.force && match.matches) {
        _this2.reporter.success(_this2.reporter.lang('upToDate'));
        return { patterns: patterns, requests: requests, skip: true };
      }

      if (!patterns.length && !match.expected) {
        _this2.reporter.success(_this2.reporter.lang('nothingToInstall'));
        yield _this2.createEmptyManifestFolders();
        return { patterns: patterns, requests: requests, skip: true };
      }

      return { patterns: patterns, requests: requests, skip: false };
    })();
  }

  /**
   * Produce empty folders for all used root manifests.
   */

  createEmptyManifestFolders() {
    var _this3 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      if (_this3.config.modulesFolder) {
        // already created
        return;
      }

      for (const registryName of _this3.rootManifestRegistries) {
        const folder = _this3.config.registries[registryName].folder;

        yield (_fs || _load_fs()).mkdirp(path.join(_this3.config.cwd, folder));
      }
    })();
  }

  /**
   * TODO description
   */

  init() {
    var _this4 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      var _ref2 = yield _this4.fetchRequestFromCwd();

      var _ref3 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref2, 2);

      let depRequests = _ref3[0];
      let rawPatterns = _ref3[1];

      const match = yield _this4.matchesIntegrityHash(rawPatterns);

      const prepared = yield _this4.prepare(rawPatterns, depRequests, match);
      rawPatterns = prepared.patterns;
      depRequests = prepared.requests;
      if (prepared.skip) {
        return rawPatterns;
      }

      // remove integrity hash to make this operation atomic
      yield (_fs || _load_fs()).unlink(match.loc);

      // warn if we have a shrinkwrap
      if (yield (_fs || _load_fs()).exists(path.join(_this4.config.cwd, 'npm-shrinkwrap.json'))) {
        _this4.reporter.error(_this4.reporter.lang('shrinkwrapWarning'));
      }

      //
      let patterns = rawPatterns;
      const steps = [];

      steps.push((() => {
        var _ref4 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (curr, total) {
          _this4.reporter.step(curr, total, _this4.reporter.lang('resolvingPackages'), emoji.get('mag'));
          yield _this4.resolver.init(depRequests, _this4.flags.flat);
          patterns = yield _this4.flatten(rawPatterns);
        });

        return function (_x2, _x3) {
          return _ref4.apply(this, arguments);
        };
      })());

      steps.push((() => {
        var _ref5 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (curr, total) {
          _this4.reporter.step(curr, total, _this4.reporter.lang('fetchingPackages'), emoji.get('truck'));
          yield _this4.fetcher.init();
          yield _this4.compatibility.init();
        });

        return function (_x4, _x5) {
          return _ref5.apply(this, arguments);
        };
      })());

      steps.push((() => {
        var _ref6 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (curr, total) {
          _this4.reporter.step(curr, total, _this4.reporter.lang('linkingDependencies'), emoji.get('link'));
          yield _this4.linker.init(patterns);
        });

        return function (_x6, _x7) {
          return _ref6.apply(this, arguments);
        };
      })());

      steps.push((() => {
        var _ref7 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (curr, total) {
          _this4.reporter.step(curr, total, _this4.flags.force ? _this4.reporter.lang('rebuildingPackages') : _this4.reporter.lang('buildingFreshPackages'), emoji.get('page_with_curl'));

          if (_this4.flags.ignoreScripts) {
            _this4.reporter.warn(_this4.reporter.lang('ignoredScripts'));
          } else {
            yield _this4.scripts.init(patterns);
          }
        });

        return function (_x8, _x9) {
          return _ref7.apply(this, arguments);
        };
      })());

      if (_this4.flags.har) {
        steps.push((() => {
          var _ref8 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (curr, total) {
            const formattedDate = new Date().toISOString().replace(/:/g, '-');
            const filename = `yarn-install_${ formattedDate }.har`;
            _this4.reporter.step(curr, total, _this4.reporter.lang('savingHar', filename), emoji.get('black_circle_for_record'));
            yield _this4.config.requestManager.saveHar(filename);
          });

          return function (_x10, _x11) {
            return _ref8.apply(this, arguments);
          };
        })());
      }

      if (yield _this4.shouldClean()) {
        steps.push((() => {
          var _ref9 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (curr, total) {
            _this4.reporter.step(curr, total, _this4.reporter.lang('cleaningModules'), emoji.get('recycle'));
            yield (0, (_clean || _load_clean()).clean)(_this4.config, _this4.reporter);
          });

          return function (_x12, _x13) {
            return _ref9.apply(this, arguments);
          };
        })());
      }

      let currentStep = 0;
      for (const step of steps) {
        yield step(++currentStep, steps.length);
      }

      // fin!
      yield _this4.saveLockfileAndIntegrity(rawPatterns);
      _this4.config.requestManager.clearCache();
      return patterns;
    })();
  }

  /**
   * Check if we should run the cleaning step.
   */

  shouldClean() {
    return (_fs || _load_fs()).exists(path.join(this.config.cwd, (_constants || _load_constants()).CLEAN_FILENAME));
  }

  /**
   * TODO
   */

  flatten(patterns) {
    var _this5 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      if (!_this5.flags.flat) {
        return patterns;
      }

      const flattenedPatterns = [];

      for (const name of _this5.resolver.getAllDependencyNamesByLevelOrder(patterns)) {
        const infos = _this5.resolver.getAllInfoForPackageName(name).filter(function (manifest) {
          const ref = manifest._reference;
          invariant(ref, 'expected package reference');
          return !ref.ignore;
        });

        if (infos.length === 0) {
          continue;
        }

        if (infos.length === 1) {
          // single version of this package
          // take out a single pattern as multiple patterns may have resolved to this package
          flattenedPatterns.push(_this5.resolver.patternsByPackage[name][0]);
          continue;
        }

        const options = infos.map(function (info) {
          const ref = info._reference;
          invariant(ref, 'expected reference');
          return {
            // TODO `and is required by {PARENT}`,
            name: _this5.reporter.lang('manualVersionResolutionOption', ref.patterns.join(', '), info.version),

            value: info.version
          };
        });
        const versions = infos.map(function (info) {
          return info.version;
        });
        let version;

        const resolutionVersion = _this5.resolutions[name];
        if (resolutionVersion && versions.indexOf(resolutionVersion) >= 0) {
          // use json `resolution` version
          version = resolutionVersion;
        } else {
          version = yield _this5.reporter.select(_this5.reporter.lang('manualVersionResolution', name), _this5.reporter.lang('answer'), options);
          _this5.resolutions[name] = version;
        }

        flattenedPatterns.push(_this5.resolver.collapseAllVersionsOfPackage(name, version));
      }

      // save resolutions to their appropriate root manifest
      if ((0, (_keys || _load_keys()).default)(_this5.resolutions).length) {
        const manifests = yield _this5.config.getRootManifests();

        for (const name in _this5.resolutions) {
          const version = _this5.resolutions[name];

          const patterns = _this5.resolver.patternsByPackage[name];
          if (!patterns) {
            continue;
          }

          let manifest;
          for (const pattern of patterns) {
            manifest = _this5.resolver.getResolvedPattern(pattern);
            if (manifest) {
              break;
            }
          }
          invariant(manifest, 'expected manifest');

          const ref = manifest._reference;
          invariant(ref, 'expected reference');

          const object = manifests[ref.registry].object;
          object.resolutions = object.resolutions || {};
          object.resolutions[name] = version;
        }

        yield _this5.config.saveRootManifests(manifests);
      }

      return flattenedPatterns;
    })();
  }

  /**
   * Save updated integrity and lockfiles.
   */

  saveLockfileAndIntegrity(patterns) {
    var _this6 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // stringify current lockfile
      const lockSource = (0, (_stringify || _load_stringify()).default)(_this6.lockfile.getLockfile(_this6.resolver.patterns)) + '\n';

      // write integrity hash
      yield _this6.writeIntegrityHash(lockSource, patterns);

      // --no-lockfile or --pure-lockfile flag
      if (_this6.flags.lockfile === false || _this6.flags.pureLockfile) {
        return;
      }

      // check if the loaded lockfile has all the included patterns
      let inSync = true;
      for (const pattern of patterns) {
        if (!_this6.lockfile.getLocked(pattern)) {
          inSync = false;
          break;
        }
      }
      // check if loaded lockfile has patterns we don't have, eg. uninstall
      for (const pattern in _this6.lockfile.cache) {
        if (patterns.indexOf(pattern) === -1) {
          inSync = false;
          break;
        }
      }
      // don't write new lockfile if in sync
      if (inSync) {
        return;
      }

      // build lockfile location
      const loc = path.join(_this6.config.cwd, (_constants || _load_constants()).LOCKFILE_FILENAME);

      // write lockfile
      yield (_fs || _load_fs()).writeFile(loc, lockSource);

      _this6._logSuccessSaveLockfile();
    })();
  }

  _logSuccessSaveLockfile() {
    this.reporter.success(this.reporter.lang('savedLockfile'));
  }

  /**
   * Check if the integrity hash of this installation matches one on disk.
   */

  matchesIntegrityHash(patterns) {
    var _this7 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const loc = yield _this7.getIntegrityHashLocation();
      if (!(yield (_fs || _load_fs()).exists(loc))) {
        return {
          actual: '',
          expected: '',
          loc: loc,
          matches: false
        };
      }

      const actual = _this7.generateIntegrityHash(_this7.lockfile.source, patterns);
      const expected = (yield (_fs || _load_fs()).readFile(loc)).trim();

      return {
        actual: actual,
        expected: expected,
        loc: loc,
        matches: actual === expected
      };
    })();
  }

  /**
   * Get the location of an existing integrity hash. If none exists then return the location where we should
   * write a new one.
   */

  getIntegrityHashLocation() {
    var _this8 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      // build up possible folders
      const possibleFolders = [];
      if (_this8.config.modulesFolder) {
        possibleFolders.push(_this8.config.modulesFolder);
      }

      // get a list of registry names to check existence in
      let checkRegistryNames = _this8.resolver.usedRegistries;
      if (!checkRegistryNames.length) {
        // we haven't used any registries yet
        checkRegistryNames = (_index2 || _load_index2()).registryNames;
      }

      // ensure we only write to a registry folder that was used
      for (const name of checkRegistryNames) {
        const loc = path.join(_this8.config.cwd, _this8.config.registries[name].folder);
        possibleFolders.push(loc);
      }

      // if we already have an integrity hash in one of these folders then use it's location otherwise use the
      // first folder
      const possibles = possibleFolders.map(function (folder) {
        return path.join(folder, (_constants || _load_constants()).INTEGRITY_FILENAME);
      });
      let loc = possibles[0];
      for (const possibleLoc of possibles) {
        if (yield (_fs || _load_fs()).exists(possibleLoc)) {
          loc = possibleLoc;
          break;
        }
      }
      return loc;
    })();
  }
  /**
   * Write the integrity hash of the current install to disk.
   */

  writeIntegrityHash(lockSource, patterns) {
    var _this9 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const loc = yield _this9.getIntegrityHashLocation();
      invariant(loc, 'expected integrity hash location');
      yield (_fs || _load_fs()).writeFile(loc, _this9.generateIntegrityHash(lockSource, patterns));
    })();
  }

  /**
   * Generate integrity hash of input lockfile.
   */

  generateIntegrityHash(lockfile, patterns) {
    const opts = [lockfile];

    opts.push(`patterns:${ patterns.join(',') }`);

    if (this.flags.flat) {
      opts.push('flat');
    }

    if (this.flags.production) {
      opts.push('production');
    }

    const linkedModules = this.config.linkedModules;
    if (linkedModules.length) {
      opts.push(`linked:${ linkedModules.join(',') }`);
    }

    const mirror = this.config.getOfflineMirrorPath();
    if (mirror != null) {
      opts.push(`mirror:${ mirror }`);
    }

    return (_crypto || _load_crypto()).hash(opts.join('-'), 'sha256');
  }

  /**
   * Load the dependency graph of the current install. Only does package resolving and wont write to the cwd.
   */

  hydrate(fetch) {
    var _this10 = this;

    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      const request = yield _this10.fetchRequestFromCwd();

      var _request = (0, (_slicedToArray2 || _load_slicedToArray()).default)(request, 2);

      const depRequests = _request[0];
      const rawPatterns = _request[1];


      yield _this10.resolver.init(depRequests, _this10.flags.flat);
      yield _this10.flatten(rawPatterns);

      if (fetch) {
        // fetch packages, should hit cache most of the time
        yield _this10.fetcher.init();

        // expand minimal manifests
        for (const manifest of _this10.resolver.getManifests()) {
          const ref = manifest._reference;
          invariant(ref, 'expected reference');

          const loc = _this10.config.generateHardModulePath(ref);
          const newPkg = yield _this10.config.readManifest(loc);
          yield _this10.resolver.updateManifest(ref, newPkg);
        }
      }

      return request;
    })();
  }
}

exports.Install = Install;
function _setFlags(commander) {
  commander.option('--har', 'save HAR output of network traffic');
  commander.option('--ignore-platform', 'ignore platform checks');
  commander.option('--ignore-engines', 'ignore engines check');
  commander.option('--ignore-scripts', '');
  commander.option('--ignore-optional', '');
  commander.option('--force', 'ignore all caches');
  commander.option('--flat', 'only allow one version of a package');
  commander.option('--prod, --production', '');
  commander.option('--no-lockfile', "don't read or generate a lockfile");
  commander.option('--pure-lockfile', "don't generate a lockfile");
}

function setFlags(commander) {
  commander.usage('install [flags]');
  _setFlags(commander);

  commander.option('-g, --global', 'DEPRECATED');
  commander.option('-S, --save', 'DEPRECATED - save package to your `dependencies`');
  commander.option('-D, --save-dev', 'DEPRECATED - save package to your `devDependencies`');
  commander.option('-P, --save-peer', 'DEPRECATED - save package to your `peerDependencies`');
  commander.option('-O, --save-optional', 'DEPRECATED - save package to your `optionalDependencies`');
  commander.option('-E, --save-exact', 'DEPRECATED');
  commander.option('-T, --save-tilde', 'DEPRECATED');
}