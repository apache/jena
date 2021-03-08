'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.getToken = undefined;

var _promise;

function _load_promise() {
  return _promise = _interopRequireDefault(require('babel-runtime/core-js/promise'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let getCredentials = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter) {
    var _config$registries$ya = config.registries.yarn.config;
    let username = _config$registries$ya.username;
    let email = _config$registries$ya.email;


    if (username) {
      reporter.info(`${ reporter.lang('npmUsername') }: ${ username }`);
    } else {
      username = yield reporter.question(reporter.lang('npmUsername'));
      if (!username) {
        return null;
      }
    }

    if (email) {
      reporter.info(`${ reporter.lang('npmUsername') }: ${ email }`);
    } else {
      email = yield reporter.question(reporter.lang('npmEmail'));
      if (!email) {
        return null;
      }
    }

    yield config.registries.yarn.saveHomeConfig({ username: username, email: email });

    return { username: username, email: email };
  });

  return function getCredentials(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let getToken = exports.getToken = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter) {
    let name = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : '';

    const auth = config.registries.npm.getAuth(name);
    if (auth) {
      config.registries.npm.setToken(auth);
      return function revoke() {
        reporter.info(reporter.lang('notRevokingConfigToken'));
        return (_promise || _load_promise()).default.resolve();
      };
    }

    const env = process.env.YARN_AUTH_TOKEN || process.env.NPM_AUTH_TOKEN;
    if (env) {
      config.registries.npm.setToken(`Bearer ${ env }`);
      return function revoke() {
        reporter.info(reporter.lang('notRevokingEnvToken'));
        return (_promise || _load_promise()).default.resolve();
      };
    }

    //
    const creds = yield getCredentials(config, reporter);
    if (!creds) {
      reporter.warn(reporter.lang('loginAsPublic'));
      return function revoke() {
        reporter.info(reporter.lang('noTokenToRevoke'));
        return (_promise || _load_promise()).default.resolve();
      };
    }

    const username = creds.username;
    const email = creds.email;

    const password = yield reporter.question(reporter.lang('npmPassword'), { password: true, required: true });

    //
    const userobj = {
      _id: `org.couchdb.user:${ username }`,
      name: username,
      password: password,
      email: email,
      type: 'user',
      roles: [],
      date: new Date().toISOString()
    };

    //
    const res = yield config.registries.npm.request(`-/user/org.couchdb.user:${ encodeURIComponent(username) }`, {
      method: 'PUT',
      body: userobj,
      auth: { username: username, password: password, email: email }
    });

    if (res && res.ok) {
      reporter.success(reporter.lang('loggedIn'));

      const token = res.token;
      config.registries.npm.setToken(`Bearer ${ token }`);

      return (() => {
        var _ref3 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* () {
          reporter.success(reporter.lang('revokedToken'));
          yield config.registries.npm.request(`-/user/token/${ token }`, {
            method: 'DELETE'
          });
        });

        function revoke() {
          return _ref3.apply(this, arguments);
        }

        return revoke;
      })();
    } else {
      throw new (_errors || _load_errors()).MessageError(reporter.lang('incorrectCredentials'));
    }
  });

  return function getToken(_x4, _x5) {
    return _ref2.apply(this, arguments);
  };
})();

let run = exports.run = (() => {
  var _ref4 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    yield getCredentials(config, reporter);
  });

  return function run(_x6, _x7, _x8, _x9) {
    return _ref4.apply(this, arguments);
  };
})();

var _errors;

function _load_errors() {
  return _errors = require('../../errors.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }