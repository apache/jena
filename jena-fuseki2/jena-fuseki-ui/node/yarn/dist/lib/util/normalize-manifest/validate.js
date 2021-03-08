'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

exports.isValidPackageName = isValidPackageName;

exports.default = function (info, isRoot, reporter, warn) {
  if (isRoot) {
    for (const key in (_typos || _load_typos()).default) {
      if (key in info) {
        warn(reporter.lang('manifestPotentialType', key, (_typos || _load_typos()).default[key]));
      }
    }
  }

  // validate name
  const name = info.name;

  if (typeof name === 'string') {
    if (isRoot && isBuiltinModule(name)) {
      warn(reporter.lang('manifestBuiltinModule', name));
    }

    // cannot start with a dot
    if (name[0] === '.') {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('manifestNameDot'));
    }

    // cannot contain the following characters
    if (!isValidPackageName(name)) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('manifestNameIllegalChars'));
    }

    // cannot equal node_modules or favicon.ico
    const lower = name.toLowerCase();
    if (lower === 'node_modules' || lower === 'favicon.ico') {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('manifestNameBlacklisted'));
    }
  }

  // validate license
  if (isRoot && !info.private) {
    if (typeof info.license === 'string') {
      const license = info.license.replace(/\*$/g, '');
      if (!(0, (_util || _load_util()).isValidLicense)(license)) {
        warn(reporter.lang('manifestLicenseInvalid'));
      }
    } else {
      warn(reporter.lang('manifestLicenseNone'));
    }
  }

  // validate strings
  for (const key of strings) {
    const val = info[key];
    if (val && typeof val !== 'string') {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('manifestStringExpected', key));
    }
  }

  cleanDependencies(info, isRoot, reporter, warn);
};

exports.cleanDependencies = cleanDependencies;

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _util;

function _load_util() {
  return _util = require('./util.js');
}

var _typos;

function _load_typos() {
  return _typos = _interopRequireDefault(require('./typos.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const isBuiltinModule = require('is-builtin-module');

const strings = ['name', 'version'];

const dependencyKeys = [
// npm registry will include optionalDependencies in dependencies and we'll want to dedupe them from the
// other fields first
'optionalDependencies',

// it's seemingly common to include a dependency in dependencies and devDependencies of the same name but
// different ranges, this can cause a lot of issues with our determinism and the behaviour of npm is
// currently unspecified.
'dependencies', 'devDependencies'];

function isValidName(name) {
  return !name.match(/[\/@\s\+%:]/) && encodeURIComponent(name) === name;
}

function isValidScopedName(name) {
  if (name[0] !== '@') {
    return false;
  }

  const parts = name.slice(1).split('/');
  return parts.length === 2 && isValidName(parts[0]) && isValidName(parts[1]);
}

function isValidPackageName(name) {
  return isValidName(name) || isValidScopedName(name);
}

function cleanDependencies(info, isRoot, reporter, warn) {
  // get dependency objects
  const depTypes = [];
  for (const type of dependencyKeys) {
    const deps = info[type];
    if (!deps || typeof deps !== 'object') {
      continue;
    }
    depTypes.push([type, deps]);
  }

  // check root dependencies for builtin module names
  if (isRoot) {
    for (const _ref of depTypes) {
      var _ref2 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref, 2);

      const type = _ref2[0];
      const deps = _ref2[1];

      for (const name in deps) {
        if (isBuiltinModule(name)) {
          warn(reporter.lang('manifestDependencyBuiltin', name, type));
        }
      }
    }
  }

  // ensure that dependencies don't have ones that can collide
  for (const _ref3 of depTypes) {
    var _ref4 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref3, 2);

    const type = _ref4[0];
    const deps = _ref4[1];

    for (const name in deps) {
      const version = deps[name];

      // check collisions
      for (const _ref5 of depTypes) {
        var _ref6 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref5, 2);

        const type2 = _ref6[0];
        const deps2 = _ref6[1];

        const version2 = deps2[name];
        if (deps === deps2 || !version2 || version2 === '*') {
          continue;
        }

        if (version !== version2 && isRoot) {
          // only throw a warning when at the root
          warn(reporter.lang('manifestDependencyCollision', type, name, version, type2, version2));
        }

        delete deps2[name];
      }
    }
  }
}