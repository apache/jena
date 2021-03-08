'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = undefined;

var _assign;

function _load_assign() {
  return _assign = _interopRequireDefault(require('babel-runtime/core-js/object/assign'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let run = exports.run = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    const manifests = yield config.getRootManifests();

    let gitUrl;
    const author = {
      name: config.getOption('init-author-name'),
      email: config.getOption('init-author-email'),
      url: config.getOption('init-author-url')
    };
    if (yield (_fs || _load_fs()).exists(path.join(config.cwd, '.git'))) {
      // get git origin of the cwd
      try {
        gitUrl = yield (_child || _load_child()).spawn('git', ['config', 'remote.origin.url'], { cwd: config.cwd });
      } catch (ex) {}
      // Ignore - Git repo may not have an origin URL yet (eg. if it only exists locally)


      // get author default based on git config
      author.name = author.name || (yield (_child || _load_child()).spawn('git', ['config', 'user.name']));
      author.email = author.email || (yield (_child || _load_child()).spawn('git', ['config', 'user.email']));
    }

    const keys = [{
      key: 'name',
      question: 'name',
      default: path.basename(config.cwd)
    }, {
      key: 'version',
      question: 'version',
      default: String(config.getOption('init-version'))
    }, {
      key: 'description',
      question: 'description',
      default: ''
    }, {
      key: 'main',
      question: 'entry point',
      default: 'index.js'
    }, {
      key: 'repository.url',
      question: 'git repository',
      default: gitUrl
    }, {
      key: 'author',
      question: 'author',
      default: (0, (_util || _load_util()).stringifyPerson)(author)
    }, {
      key: 'license',
      question: 'license',
      default: String(config.getOption('init-license'))
    }];

    // get answers
    const pkg = {};
    for (const entry of keys) {
      const yes = flags.yes;
      const manifestKey = entry.key;
      let question = entry.question;
      let def = entry.default;


      for (const registryName of (_index || _load_index()).registryNames) {
        const object = manifests[registryName].object;

        const val = objectPath.get(object, manifestKey);
        if (val) {
          def = val;
          break;
        }
      }

      if (def) {
        question += ` (${ def })`;
      }

      let answer;

      if (yes) {
        answer = def;
      } else {
        answer = (yield reporter.question(question)) || def;
      }

      if (answer) {
        objectPath.set(pkg, manifestKey, answer);
      }
    }

    // if we have a git url then set the type
    if (pkg.repository && pkg.repository.url) {
      pkg.repository.type = 'git';
    }

    // save answers
    const targetManifests = [];
    for (const registryName of (_index || _load_index()).registryNames) {
      const info = manifests[registryName];
      if (info.exists) {
        targetManifests.push(info);
      }
    }
    if (!targetManifests.length) {
      targetManifests.push(manifests.npm);
    }
    for (const targetManifest of targetManifests) {
      (0, (_assign || _load_assign()).default)(targetManifest.object, pkg);
      reporter.success(`Saved ${ path.basename(targetManifest.loc) }`);
    }

    yield config.saveRootManifests(manifests);
  });

  return function run(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

exports.setFlags = setFlags;

var _util;

function _load_util() {
  return _util = require('../../util/normalize-manifest/util.js');
}

var _index;

function _load_index() {
  return _index = require('../../registries/index.js');
}

var _child;

function _load_child() {
  return _child = _interopRequireWildcard(require('../../util/child.js'));
}

var _fs;

function _load_fs() {
  return _fs = _interopRequireWildcard(require('../../util/fs.js'));
}

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const objectPath = require('object-path');
const path = require('path');

function setFlags(commander) {
  commander.option('-y, --yes', 'use default options');
}