'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.run = exports.buildTree = exports.noArguments = exports.requireLockfile = undefined;

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _asyncToGenerator2;

function _load_asyncToGenerator() {
  return _asyncToGenerator2 = _interopRequireDefault(require('babel-runtime/helpers/asyncToGenerator'));
}

let buildTree = exports.buildTree = (() => {
  var _ref = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (resolver, linker, patterns, onlyFresh, ignoreHoisted) {
    const treesByKey = {};
    const trees = [];
    const hoisted = yield linker.getFlatHoistedTree(patterns);

    const hoistedByKey = {};
    for (const _ref2 of hoisted) {
      var _ref3 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref2, 2);

      const key = _ref3[0];
      const info = _ref3[1];

      hoistedByKey[key] = info;
    }

    // build initial trees
    for (const _ref4 of hoisted) {
      var _ref5 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref4, 2);

      const info = _ref5[1];

      const ref = info.pkg._reference;
      invariant(ref, 'expected reference');

      if (onlyFresh) {
        let isFresh = false;
        for (const pattern of ref.patterns) {
          if (resolver.isNewPattern(pattern)) {
            isFresh = true;
            break;
          }
        }
        if (!isFresh) {
          continue;
        }
      }

      const hint = null;
      let color = 'bold';

      if (info.originalKey !== info.key) {
        // was hoisted
        color = null;
      }

      const children = [];
      treesByKey[info.key] = {
        name: `${ info.pkg.name }@${ info.pkg.version }`,
        children,
        hint,
        color
      };

      // add in dummy children for hoisted dependencies
      invariant(ref, 'expected reference');
      if (!ignoreHoisted) {
        for (const pattern of resolver.dedupePatterns(ref.dependencies)) {
          const pkg = resolver.getStrictResolvedPattern(pattern);

          if (!hoistedByKey[`${ info.key }#${ pkg.name }`]) {
            children.push({
              name: pattern,
              color: 'dim',
              shadow: true
            });
          }
        }
      }
    }

    // add children
    for (const _ref6 of hoisted) {
      var _ref7 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref6, 2);

      const info = _ref7[1];

      const tree = treesByKey[info.key];
      if (!tree) {
        continue;
      }

      const keyParts = info.key.split('#');
      if (keyParts.length === 1) {
        trees.push(tree);
        continue;
      }

      const parentKey = keyParts.slice(0, -1).join('#');
      const parent = treesByKey[parentKey];
      if (parent) {
        parent.children.push(tree);
      }
    }

    return { trees, count: buildCount(trees) };
  });

  return function buildTree(_x, _x2, _x3, _x4, _x5) {
    return _ref.apply(this, arguments);
  };
})();

let run = exports.run = (() => {
  var _ref8 = (0, (_asyncToGenerator2 || _load_asyncToGenerator()).default)(function* (config, reporter, flags, args) {
    const lockfile = yield (_wrapper || _load_wrapper()).default.fromDirectory(config.cwd, reporter);
    const install = new (_install || _load_install()).Install(flags, config, reporter, lockfile);

    var _ref9 = yield install.fetchRequestFromCwd();

    var _ref10 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref9, 2);

    const depRequests = _ref10[0];
    const patterns = _ref10[1];

    yield install.resolver.init(depRequests, install.flags.flat);

    var _ref11 = yield buildTree(install.resolver, install.linker, patterns);

    const trees = _ref11.trees;

    reporter.tree('ls', trees);
  });

  return function run(_x6, _x7, _x8, _x9) {
    return _ref8.apply(this, arguments);
  };
})();

var _install;

function _load_install() {
  return _install = require('./install.js');
}

var _wrapper;

function _load_wrapper() {
  return _wrapper = _interopRequireDefault(require('../../lockfile/wrapper.js'));
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');

const requireLockfile = exports.requireLockfile = true;
const noArguments = exports.noArguments = true;

function buildCount(trees) {
  if (!trees || !trees.length) {
    return 0;
  }

  let count = 0;

  for (const tree of trees) {
    if (tree.shadow) {
      continue;
    }

    count++;
    count += buildCount(tree.children);
  }

  return count;
}