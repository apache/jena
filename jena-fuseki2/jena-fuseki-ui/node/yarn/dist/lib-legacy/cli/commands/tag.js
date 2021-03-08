'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.examples = exports.setFlags = exports.run = exports.getName = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let getName = exports.getName = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (args, config) {
    let name = args.shift();

    if (!name) {
      const pkg = yield config.readRootManifest();
      name = pkg.name;
    }

    if (name) {
      if (!(0, (_validate || _load_validate()).isValidPackageName)(name)) {
        throw new (_errors || _load_errors()).MessageError(config.reporter.lang('invalidPackageName'));
      }

      return (_npmRegistry || _load_npmRegistry()).default.escapeName(name);
    } else {
      throw new (_errors || _load_errors()).MessageError(config.reporter.lang('unknownPackageName'));
    }
  });

  return function getName(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

var _packageRequest;

function _load_packageRequest() {
  return _packageRequest = _interopRequireDefault(require('../../package-request.js'));
}

var _buildSubCommands2;

function _load_buildSubCommands() {
  return _buildSubCommands2 = _interopRequireDefault(require('./_build-sub-commands.js'));
}

var _login;

function _load_login() {
  return _login = require('./login.js');
}

var _npmRegistry;

function _load_npmRegistry() {
  return _npmRegistry = _interopRequireDefault(require('../../registries/npm-registry.js'));
}

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _validate;

function _load_validate() {
  return _validate = require('../../util/normalize-manifest/validate.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _buildSubCommands = (0, (_buildSubCommands2 || _load_buildSubCommands()).default)('tag', {
  add: (() => {
    var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
      if (args.length !== 2) {
        return false;
      }

      var _PackageRequest$norma = (_packageRequest || _load_packageRequest()).default.normalizePattern(args.shift());

      const name = _PackageRequest$norma.name;
      const range = _PackageRequest$norma.range;
      const hasVersion = _PackageRequest$norma.hasVersion;

      if (!hasVersion) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('requiredVersionInRange'));
      }
      if (!(0, (_validate || _load_validate()).isValidPackageName)(name)) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('invalidPackageName'));
      }

      const tag = args.shift();

      reporter.step(1, 3, reporter.lang('loggingIn'));
      const revoke = yield (0, (_login || _load_login()).getToken)(config, reporter, name);

      reporter.step(2, 3, reporter.lang('creatingTag', tag, range));
      const result = yield config.registries.npm.request(`-/package/${ (_npmRegistry || _load_npmRegistry()).default.escapeName(name) }/dist-tags/${ encodeURI(tag) }`, {
        method: 'PUT',
        body: range
      });

      if (result != null && result.ok) {
        reporter.success(reporter.lang('createdTag'));
      } else {
        reporter.error(reporter.lang('createdTagFail'));
      }

      reporter.step(3, 3, reporter.lang('revokingToken'));
      yield revoke();

      if (result != null && result.ok) {
        return true;
      } else {
        throw new Error();
      }
    });

    function add(_x3, _x4, _x5, _x6) {
      return _ref2.apply(this, arguments);
    }

    return add;
  })(),
  rm: (() => {
    var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
      if (args.length !== 2) {
        return false;
      }

      const name = yield getName(args, config);
      const tag = args.shift();

      reporter.step(1, 3, reporter.lang('loggingIn'));
      const revoke = yield (0, (_login || _load_login()).getToken)(config, reporter, name);

      reporter.step(2, 3, reporter.lang('deletingTags'));
      const result = yield config.registries.npm.request(`-/package/${ name }/dist-tags/${ encodeURI(tag) }`, {
        method: 'DELETE'
      });

      if (result === false) {
        reporter.error(reporter.lang('deletedTagFail'));
      } else {
        reporter.success(reporter.lang('deletedTag'));
      }

      reporter.step(3, 3, reporter.lang('revokingToken'));
      yield revoke();

      if (result === false) {
        throw new Error();
      } else {
        return true;
      }
    });

    function rm(_x7, _x8, _x9, _x10) {
      return _ref3.apply(this, arguments);
    }

    return rm;
  })(),
  ls: (() => {
    var _ref4 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
      const name = yield getName(args, config);

      reporter.step(1, 3, reporter.lang('loggingIn'));
      const revoke = yield (0, (_login || _load_login()).getToken)(config, reporter, name);

      reporter.step(2, 3, reporter.lang('gettingTags'));
      const tags = yield config.registries.npm.request(`-/package/${ name }/dist-tags`);

      if (tags) {
        reporter.info(`Package ${ name }`);
        for (const name in tags) {
          reporter.info(`${ name }: ${ tags[name] }`);
        }
      }

      reporter.step(3, 3, reporter.lang('revokingToken'));
      yield revoke();

      if (!tags) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('packageNotFoundRegistry', name, 'npm'));
      }
    });

    function ls(_x11, _x12, _x13, _x14) {
      return _ref4.apply(this, arguments);
    }

    return ls;
  })()
}, ['add <pkg>@<version> [<tag>]', 'rm <pkg> <tag>', 'ls [<pkg>]']);

const run = _buildSubCommands.run;
const setFlags = _buildSubCommands.setFlags;
const examples = _buildSubCommands.examples;
exports.run = run;
exports.setFlags = setFlags;
exports.examples = examples;