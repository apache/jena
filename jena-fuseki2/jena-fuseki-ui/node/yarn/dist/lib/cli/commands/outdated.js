'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.requireLockfile = undefined;

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let run = exports.run = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    const requestedDependencies = args.length ? new Set(args) : null;

    const lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd);
    const install = new (_install || _load_install()).Install(flags, config, reporter, lockfile);

    const items = [];

    var _ref2 = yield install.fetchRequestFromCwd();

    var _ref3 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref2, 2);

    let patterns = _ref3[1];


    if (requestedDependencies) {
      patterns = patterns.filter(function (pattern) {
        return requestedDependencies.has((0, (_parsePackageName || _load_parsePackageName()).default)(pattern).name);
      });
    }

    yield Promise.all(patterns.map((() => {
      var _ref4 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (pattern) {
        const locked = lockfile.getLocked(pattern);
        if (!locked) {
          throw new (_errors || _load_errors()).MessageError(reporter.lang('lockfileOutdated'));
        }

        const normalized = (_packageRequest || _load_packageRequest()).default.normalizePattern(pattern);

        const current = locked.version;
        let name = locked.name;

        let latest = '';
        let wanted = '';

        if ((_packageRequest || _load_packageRequest()).default.getExoticResolver(pattern) || (_packageRequest || _load_packageRequest()).default.getExoticResolver(normalized.range)) {
          latest = wanted = 'exotic';
        } else {
          var _ref5 = yield config.registries[locked.registry].checkOutdated(config, name, normalized.range);

          latest = _ref5.latest;
          wanted = _ref5.wanted;
        }

        if (current === latest) {
          return;
        }

        if (current === wanted) {
          name = reporter.format.yellow(name);
        } else {
          name = reporter.format.red(name);
        }

        items.push({
          name,
          current,
          wanted,
          latest
        });
      });

      return function (_x5) {
        return _ref4.apply(this, arguments);
      };
    })()));

    if (items.length) {
      let body = items.map(function (info) {
        return [info.name, info.current, reporter.format.green(info.wanted), reporter.format.magenta(info.latest)];
      });

      body = body.sort(function (a, b) {
        return (0, (_misc || _load_misc()).sortAlpha)(a[0], b[0]);
      });

      reporter.table(['Package', 'Current', 'Wanted', 'Latest'], body);
    }

    return Promise.resolve();
  });

  return function run(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

exports.setFlags = setFlags;

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _misc;

function _load_misc() {
  return _misc = require('../../util/misc.js');
}

var _packageRequest;

function _load_packageRequest() {
  return _packageRequest = _interopRequireDefault(require('../../package-request.js'));
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('../../lockfile/wrapper.js'));
}

var _install;

function _load_install() {
  return _install = require('./install.js');
}

var _parsePackageName;

function _load_parsePackageName() {
  return _parsePackageName = _interopRequireDefault(require('../../util/parse-package-name.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const requireLockfile = exports.requireLockfile = true;

function setFlags(commander) {
  commander.usage('outdated [packages ...]');
}