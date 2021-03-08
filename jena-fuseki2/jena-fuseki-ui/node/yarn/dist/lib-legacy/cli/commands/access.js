'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.setFlags = exports.run = undefined;

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _buildSubCommands2;

function _load_buildSubCommands() {
  return _buildSubCommands2 = _interopRequireDefault(require('./_build-sub-commands.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _buildSubCommands = (0, (_buildSubCommands2 || _load_buildSubCommands()).default)('access', {
  public: function _public() {
    return (_promise || _load_promise()).default.reject(new Error('TODO'));
  },
  restricted: function restricted() {
    return (_promise || _load_promise()).default.reject(new Error('TODO'));
  },
  grant: function grant() {
    return (_promise || _load_promise()).default.reject(new Error('TODO'));
  },
  revoke: function revoke() {
    return (_promise || _load_promise()).default.reject(new Error('TODO'));
  },
  lsPackages: function lsPackages() {
    return (_promise || _load_promise()).default.reject(new Error('TODO'));
  },
  lsCollaborators: function lsCollaborators() {
    return (_promise || _load_promise()).default.reject(new Error('TODO'));
  },
  edit: function edit() {
    return (_promise || _load_promise()).default.reject(new Error('TODO'));
  }
}, ['access public [<package>]', 'access restricted [<package>]', 'access grant <read-only|read-write> <scope:team> [<package>]', 'access revoke <scope:team> [<package>]', 'access ls-packages [<user>|<scope>|<scope:team>]', 'access ls-collaborators [<package> [<user>]]', 'access edit [<package>]']);

const run = _buildSubCommands.run;
const setFlags = _buildSubCommands.setFlags;
exports.run = run;
exports.setFlags = setFlags;