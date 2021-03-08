'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.setFlags = exports.run = exports.mutate = undefined;

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let mutate = exports.mutate = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (args, config, reporter, buildMessages, mutator) {
    if (args.length !== 2 && args.length !== 1) {
      return false;
    }

    const username = args.shift();
    const name = yield (0, (_tag || _load_tag()).getName)(args, config);
    if (!(0, (_validate || _load_validate()).isValidPackageName)(name)) {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('invalidPackageName'));
    }

    const msgs = buildMessages(username, name);
    reporter.step(1, 3, reporter.lang('loggingIn'));
    const revoke = yield (0, (_login || _load_login()).getToken)(config, reporter, name);

    reporter.step(2, 3, msgs.info);
    const user = yield config.registries.npm.request(`-/user/org.couchdb.user:${ username }`);
    let error = false;
    if (user) {
      // get package
      const pkg = yield config.registries.npm.request((_npmRegistry || _load_npmRegistry()).default.escapeName(name));
      if (pkg) {
        pkg.maintainers = pkg.maintainers || [];
        error = mutator({ name: user.name, email: user.email }, pkg);
      } else {
        error = true;
        reporter.error(reporter.lang('unknownPackage', name));
      }

      // update package
      if (pkg && !error) {
        const res = yield config.registries.npm.request(`${ (_npmRegistry || _load_npmRegistry()).default.escapeName(name) }/-rev/${ pkg._rev }`, {
          method: 'PUT',
          body: {
            _id: pkg._id,
            _rev: pkg._rev,
            maintainers: pkg.maintainers
          }
        });

        if (res != null && res.success) {
          reporter.success(msgs.success);
        } else {
          error = true;
          reporter.error(msgs.error);
        }
      }
    } else {
      error = true;
      reporter.error(reporter.lang('unknownUser', username));
    }

    reporter.step(3, 3, reporter.lang('revokingToken'));
    yield revoke();

    if (error) {
      throw new Error();
    } else {
      return true;
    }
  });

  return function mutate(_x, _x2, _x3, _x4, _x5) {
    return _ref.apply(this, arguments);
  };
})();

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

var _buildSubCommands2;

function _load_buildSubCommands() {
  return _buildSubCommands2 = _interopRequireDefault(require('./_build-sub-commands.js'));
}

var _validate;

function _load_validate() {
  return _validate = require('../../util/normalize-manifest/validate.js');
}

var _tag;

function _load_tag() {
  return _tag = require('./tag.js');
}

var _login;

function _load_login() {
  return _login = require('./login.js');
}

var _npmRegistry;

function _load_npmRegistry() {
  return _npmRegistry = _interopRequireDefault(require('../../registries/npm-registry.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _buildSubCommands = (0, (_buildSubCommands2 || _load_buildSubCommands()).default)('owner', {
  add(config, reporter, flags, args) {
    return mutate(args, config, reporter, (username, name) => ({
      info: reporter.lang('ownerAdding', username, name),
      success: reporter.lang('ownerAdded'),
      error: reporter.lang('ownerAddingFailed')
    }), (user, pkg) => {
      for (const owner of pkg.maintainers) {
        if (owner.name === user) {
          reporter.error(reporter.lang('ownerAlready'));
          return true;
        }
      }

      pkg.maintainers.push(user);

      return false;
    });
  },

  rm(config, reporter, flags, args) {
    return mutate(args, config, reporter, (username, name) => ({
      info: reporter.lang('ownerRemoving', username, name),
      success: reporter.lang('ownerRemoved'),
      error: reporter.lang('ownerRemoveError')
    }), (user, pkg) => {
      let found = false;

      pkg.maintainers = pkg.maintainers.filter(o => {
        const match = o.name === user.name;
        found = found || match;
        return !match;
      });

      if (!found) {
        reporter.error(reporter.lang('userNotAnOwner', user.name));
      }

      return found;
    });
  },

  ls(config, reporter, flags, args) {
    return (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
      if (args.length > 1) {
        return false;
      }

      const name = yield (0, (_tag || _load_tag()).getName)(args, config);

      reporter.step(1, 3, reporter.lang('loggingIn'));
      const revoke = yield (0, (_login || _load_login()).getToken)(config, reporter, name);

      reporter.step(2, 3, reporter.lang('ownerGetting', name));
      const pkg = yield config.registries.npm.request(name);
      if (pkg) {
        const owners = pkg.maintainers;
        if (!owners || !owners.length) {
          reporter.warn(reporter.lang('ownerNone'));
        } else {
          for (const owner of owners) {
            reporter.info(`${ owner.name } <${ owner.email }>`);
          }
        }
      } else {
        reporter.error(reporter.lang('ownerGettingFailed'));
      }

      reporter.step(3, 3, reporter.lang('revokingToken'));
      yield revoke();

      if (pkg) {
        return true;
      } else {
        throw new Error();
      }
    })();
  }
}, ['add <user> [[<@scope>/]<pkg>]', 'rm <user> [[<@scope>/]<pkg>]', 'ls [<@scope>/]<pkg>']);

const run = _buildSubCommands.run;
const setFlags = _buildSubCommands.setFlags;
exports.run = run;
exports.setFlags = setFlags;