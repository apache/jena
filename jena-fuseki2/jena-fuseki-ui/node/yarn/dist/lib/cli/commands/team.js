'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.setFlags = exports.run = undefined;

var _extends2;

function _load_extends() {
  return _extends2 = _interopRequireDefault(require('babel-runtime/helpers/extends'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

var _toArray2;

function _load_toArray() {
  return _toArray2 = _interopRequireDefault(require('babel-runtime/helpers/toArray'));
}

var _buildSubCommands2;

function _load_buildSubCommands() {
  return _buildSubCommands2 = _interopRequireDefault(require('./_build-sub-commands.js'));
}

var _login;

function _load_login() {
  return _login = require('./login.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function explodeScopeTeam(arg, requireTeam, reporter) {
  var _arg$split = arg.split(':');

  var _arg$split2 = (0, (_toArray2 || _load_toArray()).default)(_arg$split);

  const scope = _arg$split2[0];
  const team = _arg$split2[1];

  const parts = _arg$split2.slice(2);

  if (parts.length) {
    return false;
  }

  if (requireTeam && !team) {
    return false;
  }

  return {
    scope: scope || '',
    team: team || '',
    user: ''
  };
}

function wrapRequired(callback, requireTeam) {
  return (() => {
    var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
      if (!args.length) {
        return false;
      }

      const parts = explodeScopeTeam(args[0], requireTeam, reporter);
      if (!parts) {
        return false;
      }

      reporter.step(1, 3, reporter.lang('loggingIn'));
      const revoke = yield (0, (_login || _load_login()).getToken)(config, reporter);

      const res = yield callback(parts, config, reporter, flags, args);
      if (!res) {
        return res;
      }

      reporter.step(3, 3, reporter.lang('revokingToken'));
      yield revoke();
      return true;
    });

    return function (_x, _x2, _x3, _x4) {
      return _ref.apply(this, arguments);
    };
  })();
}

function wrapRequiredTeam(callback) {
  let requireTeam = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : true;

  return wrapRequired(function (parts, config, reporter, flags, args) {
    if (args.length === 1) {
      return callback(parts, config, reporter, flags, args);
    } else {
      return false;
    }
  }, requireTeam);
}

function wrapRequiredUser(callback) {
  return wrapRequired(function (parts, config, reporter, flags, args) {
    if (args.length === 2) {
      return callback((0, (_extends2 || _load_extends()).default)({
        user: args[1]
      }, parts), config, reporter, flags, args);
    } else {
      return false;
    }
  }, true);
}

var _buildSubCommands = (0, (_buildSubCommands2 || _load_buildSubCommands()).default)('team', {
  create: wrapRequiredTeam((() => {
    var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (parts, config, reporter, flags, args) {
      reporter.step(2, 3, reporter.lang('teamCreating'));
      reporter.inspect((yield config.registries.npm.request(`team/${ parts.scope }`, {
        method: 'PUT',
        body: {
          team: parts.team
        }
      })));
      return true;
    });

    return function (_x6, _x7, _x8, _x9, _x10) {
      return _ref2.apply(this, arguments);
    };
  })()),

  destroy: wrapRequiredTeam((() => {
    var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (parts, config, reporter, flags, args) {
      reporter.step(2, 3, reporter.lang('teamRemoving'));
      reporter.inspect((yield config.registries.npm.request(`team/${ parts.scope }/${ parts.team }`, {
        method: 'DELETE'
      })));
      return true;
    });

    return function (_x11, _x12, _x13, _x14, _x15) {
      return _ref3.apply(this, arguments);
    };
  })()),

  add: wrapRequiredUser((() => {
    var _ref4 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (parts, config, reporter, flags, args) {
      reporter.step(2, 3, reporter.lang('teamAddingUser'));
      reporter.inspect((yield config.registries.npm.request(`team/${ parts.scope }/${ parts.team }/user`, {
        method: 'PUT',
        body: {
          user: parts.user
        }
      })));
      return true;
    });

    return function (_x16, _x17, _x18, _x19, _x20) {
      return _ref4.apply(this, arguments);
    };
  })()),

  rm: wrapRequiredUser((() => {
    var _ref5 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (parts, config, reporter, flags, args) {
      reporter.step(2, 3, reporter.lang('teamRemovingUser'));
      reporter.inspect((yield config.registries.npm.request(`team/${ parts.scope }/${ parts.team }/user`, {
        method: 'DELETE',
        body: {
          user: parts.user
        }
      })));
      return true;
    });

    return function (_x21, _x22, _x23, _x24, _x25) {
      return _ref5.apply(this, arguments);
    };
  })()),

  ls: wrapRequiredTeam((() => {
    var _ref6 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (parts, config, reporter, flags, args) {
      reporter.step(2, 3, reporter.lang('teamListing'));
      const uriParams = '?format=cli';
      if (parts.team) {
        reporter.inspect((yield config.registries.npm.request(`team/${ parts.scope }/${ parts.team }/user${ uriParams }`)));
      } else {
        reporter.inspect((yield config.registries.npm.request(`org/${ parts.scope }/team${ uriParams }`)));
      }
      return true;
    });

    return function (_x26, _x27, _x28, _x29, _x30) {
      return _ref6.apply(this, arguments);
    };
  })(), false)
}, ['create <scope:team>', 'destroy <scope:team>', 'add <scope:team> <user>', 'rm <scope:team> <user>', 'ls <scope>|<scope:team>']);

const run = _buildSubCommands.run;
const setFlags = _buildSubCommands.setFlags;
exports.run = run;
exports.setFlags = setFlags;