'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _validate;

function _load_validate() {
  return _validate = _interopRequireDefault(require('./validate.js'));
}

var _fix;

function _load_fix() {
  return _fix = _interopRequireDefault(require('./fix.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const path = require('path');

exports.default = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (info, moduleLoc, config, isRoot) {
    yield (0, (_fix || _load_fix()).default)(info, moduleLoc, config.reporter, config.looseSemver);

    // create human readable name
    const name = info.name;
    const version = info.version;

    let human;
    if (typeof name === 'string') {
      human = name;
    }
    if (human && typeof version === 'string' && version) {
      human += `@${ version }`;
    }
    if (isRoot && info._loc) {
      human = path.relative(config.cwd, info._loc);
    }

    function warn(msg) {
      if (human) {
        msg = `${ human }: ${ msg }`;
      }
      config.reporter.warn(msg);
    }

    try {
      (0, (_validate || _load_validate()).default)(info, isRoot, config.reporter, warn);
    } catch (err) {
      if (human) {
        err.message = `${ human }: ${ err.message }`;
      }
      throw err;
    }

    return info;
  });

  return function (_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();