'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends2;

function _load_extends() {
  return _extends2 = _interopRequireDefault(require('babel-runtime/helpers/extends'));
}

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let makeEnv = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (stage, cwd, config) {
    const env = Object.assign({}, process.env);

    env.npm_lifecycle_event = stage;
    env.npm_node_execpath = env.NODE || process.execPath;
    env.npm_execpath = path.join(__dirname, '..', '..', 'bin', 'yarn.js');

    // add npm_package_*
    const manifest = yield config.readManifest(cwd);
    const queue = [['', manifest]];
    while (queue.length) {
      var _queue$pop = queue.pop();

      var _queue$pop2 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_queue$pop, 2);

      const key = _queue$pop2[0];
      const val = _queue$pop2[1];

      if (key[0] === '_') {
        continue;
      }

      if (typeof val === 'object') {
        for (const subKey in val) {
          const completeKey = [key, subKey].filter(function (part) {
            return !!part;
          }).join('_');
          queue.push([completeKey, val[subKey]]);
        }
      } else if (IGNORE_MANIFEST_KEYS.indexOf(key) < 0) {
        let cleanVal = String(val);
        if (cleanVal.indexOf('\n') >= 0) {
          cleanVal = JSON.stringify(cleanVal);
        }
        env[`npm_package_${ key }`] = cleanVal;
      }
    }

    // add npm_config_*
    const keys = new Set([...Object.keys(config.registries.yarn.config), ...Object.keys(config.registries.npm.config)]);
    for (const key of keys) {
      if (key.match(/:_/)) {
        continue;
      }

      let val = config.getOption(key);

      if (!val) {
        val = '';
      } else if (typeof val === 'number') {
        val = '' + val;
      } else if (typeof val !== 'string') {
        val = JSON.stringify(val);
      }

      if (val.indexOf('\n') >= 0) {
        val = JSON.stringify(val);
      }

      const cleanKey = key.replace(/^_+/, '');
      const envKey = `npm_config_${ cleanKey }`.replace(/[^a-zA-Z0-9_]/g, '_');
      env[envKey] = val;
    }

    return env;
  });

  return function makeEnv(_x, _x2, _x3) {
    return _ref.apply(this, arguments);
  };
})();

var _constants;

function _load_constants() {
  return _constants = _interopRequireWildcard(require('../constants.js'));
}

var _child;

function _load_child() {
  return _child = _interopRequireWildcard(require('./child.js'));
}

var _index;

function _load_index() {
  return _index = require('../resolvers/index.js');
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const path = require('path');

const IGNORE_MANIFEST_KEYS = ['readme'];

exports.default = (() => {
  var _ref2 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (stage, config, cwd, cmd, spinner) {
    // if we don't have a spinner then pipe everything to the terminal
    const stdio = spinner ? undefined : 'inherit';

    const env = yield makeEnv(stage, cwd, config);

    // split up the path
    const pathParts = (env[(_constants || _load_constants()).ENV_PATH_KEY] || '').split(path.delimiter);

    // add node-gyp
    pathParts.unshift(path.join(__dirname, '..', '..', 'bin', 'node-gyp-bin'));

    // add .bin folders to PATH
    for (const registry of Object.keys((_index || _load_index()).registries)) {
      const binFolder = path.join(config.registries[registry].folder, '.bin');
      pathParts.unshift(path.join(config.linkFolder, binFolder));
      pathParts.unshift(path.join(cwd, binFolder));
    }

    // join path back together
    env[(_constants || _load_constants()).ENV_PATH_KEY] = pathParts.join(path.delimiter);

    // get shell
    const conf = { windowsVerbatimArguments: false };
    let sh = 'sh';
    let shFlag = '-c';
    if (process.platform === 'win32') {
      // cmd or command.com
      sh = process.env.comspec || 'cmd';

      // d - Ignore registry AutoRun commands
      // s - Strip " quote characters from command.
      // c - Run Command and then terminate
      shFlag = '/d /s /c';

      // handle quotes properly in windows environments - https://github.com/nodejs/node/issues/5060
      conf.windowsVerbatimArguments = true;
    }

    const stdout = yield (_child || _load_child()).spawn(sh, [shFlag, cmd], (0, (_extends2 || _load_extends()).default)({ cwd, env, stdio }, conf), function (data) {
      if (spinner) {
        const line = data.toString() // turn buffer into string
        .trim() // trim whitespace
        .split('\n') // split into lines
        .pop() // use only the last line
        .replace(/\t/g, ' '); // change tabs to spaces as they can interfere with the console

        if (line) {
          spinner.tick(line);
        }
      }
    });

    return { cwd, command: cmd, stdout };
  });

  return function (_x4, _x5, _x6, _x7, _x8) {
    return _ref2.apply(this, arguments);
  };
})();