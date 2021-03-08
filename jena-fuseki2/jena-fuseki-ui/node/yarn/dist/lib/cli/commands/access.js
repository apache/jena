'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.setFlags = exports.run = undefined;

var _buildSubCommands2;

function _load_buildSubCommands() {
  return _buildSubCommands2 = _interopRequireDefault(require('./_build-sub-commands.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _buildSubCommands = (0, (_buildSubCommands2 || _load_buildSubCommands()).default)('access', {
  public() {
    return Promise.reject(new Error('TODO'));
  },

  restricted() {
    return Promise.reject(new Error('TODO'));
  },

  grant() {
    return Promise.reject(new Error('TODO'));
  },

  revoke() {
    return Promise.reject(new Error('TODO'));
  },

  lsPackages() {
    return Promise.reject(new Error('TODO'));
  },

  lsCollaborators() {
    return Promise.reject(new Error('TODO'));
  },

  edit() {
    return Promise.reject(new Error('TODO'));
  }
}, ['access public [<package>]', 'access restricted [<package>]', 'access grant <read-only|read-write> <scope:team> [<package>]', 'access revoke <scope:team> [<package>]', 'access ls-packages [<user>|<scope>|<scope:team>]', 'access ls-collaborators [<package> [<user>]]', 'access edit [<package>]']);

const run = _buildSubCommands.run;
const setFlags = _buildSubCommands.setFlags;
exports.run = run;
exports.setFlags = setFlags;