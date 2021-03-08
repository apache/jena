'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _assign;

function _load_assign() {
  return _assign = _interopRequireDefault(require('babel-runtime/core-js/object/assign'));
}

exports.testEngine = testEngine;

var _packageReference;

function _load_packageReference() {
  return _packageReference = _interopRequireWildcard(require('./package-reference.js'));
}

var _errors;

function _load_errors() {
  return _errors = require('./errors.js');
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('./util/map.js'));
}

var _misc;

function _load_misc() {
  return _misc = require('./util/misc.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');
const semver = require('semver');

const VERSIONS = (0, (_assign || _load_assign()).default)({}, process.versions, {
  yarn: require('../package.json').version
});

function isValid(items, actual) {
  let isNotWhitelist = true;
  let isBlacklist = false;

  for (const item of items) {
    // blacklist
    if (item[0] === '!') {
      isBlacklist = true;

      if (actual === item.slice(1)) {
        return false;
      }
      // whitelist
    } else {
      isNotWhitelist = false;

      if (item === actual) {
        return true;
      }
    }
  }

  // npm allows blacklists and whitelists to be mixed. Blacklists with
  // whitelisted items should be treated as whitelists.
  return isBlacklist && isNotWhitelist;
}

const aliases = (0, (_map || _load_map()).default)({
  iojs: 'node' });

const ignore = ['npm', // we'll never satisfy this for obvious reasons
'teleport', // a module bundler used by some modules
'rhino'];

function testEngine(name, range, versions, looseSemver) {
  const actual = versions[name];
  if (!actual) {
    return false;
  }

  if (!semver.valid(actual, looseSemver)) {
    return false;
  }

  if (semver.satisfies(actual, range, looseSemver)) {
    return true;
  }

  if (name === 'node' && semver.gt(actual, '1.0.0', looseSemver)) {
    // WARNING: this is a massive hack and is super gross but necessary for compatibility
    // some modules have the `engines.node` field set to a caret version below semver major v1
    // eg. ^0.12.0. this is problematic as we enforce engines checks and node is now on version >=1
    // to allow this pattern we transform the node version to fake ones in the minor range 10-13
    const major = semver.major(actual, looseSemver);
    const fakes = [`0.10.${ major }`, `0.11.${ major }`, `0.12.${ major }`, `0.13.${ major }`];
    for (const actualFake of fakes) {
      if (semver.satisfies(actualFake, range, looseSemver)) {
        return true;
      }
    }
  }

  // incompatible version
  return false;
}

class PackageCompatibility {
  constructor(config, resolver, ignoreEngines) {
    this.reporter = config.reporter;
    this.resolver = resolver;
    this.config = config;
    this.ignoreEngines = ignoreEngines;
  }

  static isValidArch(archs) {
    return isValid(archs, process.arch);
  }

  static isValidPlatform(platforms) {
    return isValid(platforms, process.platform);
  }

  check(info) {
    let didIgnore = false;
    let didError = false;
    const reporter = this.reporter;
    const human = `${ info.name }@${ info.version }`;

    const pushError = msg => {
      const ref = info._reference;
      invariant(ref, 'expected package reference');

      if (ref.optional) {
        ref.addVisibility((_packageReference || _load_packageReference()).ENVIRONMENT_IGNORE);

        reporter.warn(`${ human }: ${ msg }`);
        if (!didIgnore) {
          reporter.info(reporter.lang('optionalCompatibilityExcluded', human));
          didIgnore = true;
        }
      } else {
        reporter.error(`${ human }: ${ msg }`);
        didError = true;
      }
    };

    if (!this.config.ignorePlatform && Array.isArray(info.os)) {
      if (!PackageCompatibility.isValidPlatform(info.os)) {
        pushError(this.reporter.lang('incompatibleOS', process.platform));
      }
    }

    if (!this.config.ignorePlatform && Array.isArray(info.cpu)) {
      if (!PackageCompatibility.isValidArch(info.cpu)) {
        pushError(this.reporter.lang('incompatibleCPU', process.arch));
      }
    }

    if (!this.ignoreEngines && typeof info.engines === 'object') {
      for (const entry of (0, (_misc || _load_misc()).entries)(info.engines)) {
        let name = entry[0];
        const range = entry[1];

        if (aliases[name]) {
          name = aliases[name];
        }

        if (VERSIONS[name]) {
          if (!testEngine(name, range, VERSIONS, this.config.looseSemver)) {
            pushError(this.reporter.lang('incompatibleEngine', name, range));
          }
        } else if (ignore.indexOf(name) < 0) {
          this.reporter.warn(`${ human }: ${ this.reporter.lang('invalidEngine', name) }`);
        }
      }
    }

    if (didError) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('foundIncompatible'));
    }
  }

  init() {
    const infos = this.resolver.getManifests();
    for (const info of infos) {
      this.check(info);
    }
    return (_promise || _load_promise()).default.resolve();
  }
}
exports.default = PackageCompatibility;