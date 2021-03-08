'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.execCommand = exports.execFromManifest = undefined;

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let execFromManifest = exports.execFromManifest = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, commandName, pkg, cwd) {
    if (!pkg.scripts) {
      return;
    }

    const cmd = pkg.scripts[commandName];
    if (cmd) {
      yield execCommand(commandName, config, cmd, cwd);
    }
  });

  return function execFromManifest(_x3, _x4, _x5, _x6) {
    return _ref2.apply(this, arguments);
  };
})();

let execCommand = exports.execCommand = (() => {
  var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (stage, config, cmd, cwd) {
    const reporter = config.reporter;

    try {
      reporter.command(cmd);
      yield (0, (_executeLifecycleScript || _load_executeLifecycleScript()).default)(stage, config, cwd, cmd);
      return (_promise || _load_promise()).default.resolve();
    } catch (err) {
      if (err instanceof (_errors || _load_errors()).SpawnError) {
        throw new (_errors || _load_errors()).MessageError(reporter.lang('commandFailed', err.EXIT_CODE));
      } else {
        throw err;
      }
    }
  });

  return function execCommand(_x7, _x8, _x9, _x10) {
    return _ref3.apply(this, arguments);
  };
})();

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _executeLifecycleScript;

function _load_executeLifecycleScript() {
  return _executeLifecycleScript = _interopRequireDefault(require('../../util/execute-lifecycle-script.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

exports.default = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, commandName) {
    const pkg = yield config.readManifest(config.cwd);
    yield execFromManifest(config, commandName, pkg, config.cwd);
  });

  return function (_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();