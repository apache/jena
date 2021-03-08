'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.HoistManifest = undefined;

var _slicedToArray2;

function _load_slicedToArray() {
  return _slicedToArray2 = _interopRequireDefault(require('babel-runtime/helpers/slicedToArray'));
}

var _map;

function _load_map() {
  return _map = _interopRequireDefault(require('babel-runtime/core-js/map'));
}

var _misc;

function _load_misc() {
  return _misc = require('./util/misc.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

const invariant = require('invariant');
const path = require('path');

let historyCounter = 0;

class HoistManifest {
  constructor(key, parts, pkg, loc) {
    this.loc = loc;
    this.pkg = pkg;

    this.key = key;
    this.parts = parts;
    this.originalKey = key;
    this.previousKeys = [];

    this.history = [];
    this.addHistory(`Start position = ${ key }`);
  }

  addHistory(msg) {
    this.history.push(`${ ++historyCounter }: ${ msg }`);
  }
}

exports.HoistManifest = HoistManifest;
class PackageHoister {
  constructor(config, resolver, ignoreOptional) {
    this.ignoreOptional = ignoreOptional;
    this.resolver = resolver;
    this.config = config;

    this.taintedKeys = new (_map || _load_map()).default();
    this.levelQueue = [];
    this.tree = new (_map || _load_map()).default();
  }

  /**
   * Taint this key and prevent any modules from being hoisted to it.
   */

  taintKey(key, info) {
    const existingTaint = this.taintedKeys.get(key);
    if (existingTaint && existingTaint.loc !== info.loc) {
      return false;
    } else {
      this.taintedKeys.set(key, info);
      return true;
    }
  }

  /**
   * Implode an array of ancestry parts into a key.
   */

  implodeKey(parts) {
    return parts.join('#');
  }

  /**
   * Seed the hoister with patterns taken from the included resolver.
   */

  seed(patterns) {
    for (const pattern of this.resolver.dedupePatterns(patterns)) {
      this._seed(pattern);
    }

    while (true) {
      let queue = this.levelQueue;
      if (!queue.length) {
        return;
      }

      this.levelQueue = [];

      // sort queue to get determinism between runs
      queue = queue.sort((_ref, _ref2) => {
        var _ref4 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref, 1);

        let aPattern = _ref4[0];

        var _ref3 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref2, 1);

        let bPattern = _ref3[0];

        return (0, (_misc || _load_misc()).sortAlpha)(aPattern, bPattern);
      });

      //
      const infos = [];
      for (const _ref5 of queue) {
        var _ref6 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref5, 2);

        const pattern = _ref6[0];
        const parents = _ref6[1];

        const info = this._seed(pattern, parents);
        if (info) {
          infos.push(info);
        }
      }

      //
      for (const info of infos) {
        this.hoist(info);
      }
    }
  }

  /**
   * Seed the hoister with a specific pattern.
   */

  _seed(pattern, parent) {
    let parentParts = [];

    if (parent) {
      if (!this.tree.get(parent.key)) {
        return null;
      }
      parentParts = parent.parts;
    }

    //
    const pkg = this.resolver.getStrictResolvedPattern(pattern);
    const ref = pkg._reference;
    invariant(ref, 'expected reference');

    //
    const loc = this.config.generateHardModulePath(ref);
    const parts = parentParts.concat(pkg.name);
    const key = this.implodeKey(parts);
    const info = new HoistManifest(key, parts, pkg, loc);

    //
    this.tree.set(key, info);
    this.taintKey(key, info);

    //
    for (const depPattern of ref.dependencies) {
      this.levelQueue.push([depPattern, info]);
    }

    return info;
  }

  /**
   * Find the highest position we can hoist this module to.
   */

  getNewParts(key, info, parts) {
    let stepUp = false;

    const fullKey = this.implodeKey(parts);
    const stack = []; // stack of removed parts
    const name = parts.pop();

    //
    for (let i = parts.length - 1; i >= 0; i--) {
      const checkParts = parts.slice(0, i).concat(name);
      const checkKey = this.implodeKey(checkParts);
      info.addHistory(`Looked at ${ checkKey } for a match`);

      const existing = this.tree.get(checkKey);
      if (existing) {
        if (existing.loc === info.loc) {
          existing.addHistory(`Deduped ${ fullKey } to this item`);
          return { parts: checkParts, duplicate: true };
        } else {
          // everything above will be shadowed and this is a conflict
          info.addHistory(`Found a collision at ${ checkKey }`);
          break;
        }
      }

      const existingTaint = this.taintedKeys.get(checkKey);
      if (existingTaint && existingTaint.loc !== info.loc) {
        info.addHistory(`Broken by ${ checkKey }`);
        break;
      }
    }

    // remove redundant parts that wont collide
    while (parts.length) {
      const checkParts = parts.concat(name);
      const checkKey = this.implodeKey(checkParts);

      //
      const existing = this.tree.get(checkKey);
      if (existing) {
        stepUp = true;
        break;
      }

      // check if we're trying to hoist ourselves to a previously unflattened module key,
      // this will result in a conflict and we'll need to move ourselves up
      if (key !== checkKey && this.taintedKeys.has(checkKey)) {
        stepUp = true;
        break;
      }

      //
      stack.push(parts.pop());
    }

    //
    parts.push(name);

    //
    const isValidPosition = parts => {
      const key = this.implodeKey(parts);
      const existing = this.tree.get(key);
      if (existing && existing.loc === info.loc) {
        return true;
      }

      // ensure there's no taint or the taint is us
      const existingTaint = this.taintedKeys.get(key);
      if (existingTaint && existingTaint.loc !== info.loc) {
        return false;
      }

      return true;
    };

    // we need to special case when we attempt to hoist to the top level as the `existing` logic
    // wont be hit in the above `while` loop and we could conflict
    if (!isValidPosition(parts)) {
      stepUp = true;
    }

    // sometimes we need to step up to a parent module to install ourselves
    while (stepUp && stack.length) {
      info.addHistory(`Stepping up from ${ this.implodeKey(parts) }`);

      parts.pop(); // remove `name`
      parts.push(stack.pop(), name);

      if (isValidPosition(parts)) {
        info.addHistory(`Found valid position ${ this.implodeKey(parts) }`);
        stepUp = false;
      }
    }

    return { parts: parts, duplicate: false };
  }

  /**
   * Hoist all seeded patterns to their highest positions.
   */

  hoist(info) {
    const key = info.key;
    const rawParts = info.parts;

    // remove this item from the `tree` map so we can ignore it

    this.tree.delete(key);

    //

    var _getNewParts = this.getNewParts(key, info, rawParts.slice());

    const parts = _getNewParts.parts;
    const duplicate = _getNewParts.duplicate;

    const newKey = this.implodeKey(parts);
    const oldKey = key;
    if (duplicate) {
      info.addHistory(`Satisfied from above by ${ newKey }`);
      this.declareRename(info, rawParts, parts);
      return;
    }

    // update to the new key
    if (oldKey === newKey) {
      info.addHistory("Didn't hoist - conflicts above");
      this.setKey(info, oldKey, parts);
      return;
    }

    //
    this.declareRename(info, rawParts, parts);
    this.setKey(info, newKey, parts);
  }

  /**
   * Declare that a module has been hoisted and update our internal references.
   */

  declareRename(info, oldParts, newParts) {
    // go down the tree from our new position reserving our name
    this.taintParents(info, oldParts.slice(0, -1), newParts.length - 1);
  }

  /**
   * Crawl upwards through a list of ancestry parts and taint a package name.
   */

  taintParents(info, processParts, start) {
    for (let i = start; i < processParts.length; i++) {
      const parts = processParts.slice(0, i).concat(info.pkg.name);
      const key = this.implodeKey(parts);

      if (this.taintKey(key, info)) {
        info.addHistory(`Tainted ${ key } to prevent collisions`);
      }
    }
  }

  /**
   * Update the key of a module and update our references.
   */

  setKey(info, newKey, parts) {
    const oldKey = info.key;

    info.key = newKey;
    info.parts = parts;
    this.tree.set(newKey, info);

    if (oldKey === newKey) {
      return;
    }

    info.previousKeys.push(newKey);
    info.addHistory(`New position = ${ newKey }`);
  }

  /**
   * Produce a flattened list of module locations and manifests.
   */

  init() {
    const flatTree = [];

    //
    for (const _ref7 of this.tree.entries()) {
      var _ref8 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref7, 2);

      const key = _ref8[0];
      const info = _ref8[1];

      // decompress the location and push it to the flat tree. this path could be made
      // up of modules from different registries so we need to handle this specially
      const parts = [];
      const keyParts = key.split('#');
      for (let i = 0; i < keyParts.length; i++) {
        const key = keyParts.slice(0, i + 1).join('#');
        const hoisted = this.tree.get(key);
        invariant(hoisted, 'expected hoisted manifest');
        parts.push(this.config.getFolder(hoisted.pkg));
        parts.push(keyParts[i]);
      }

      if (this.config.modulesFolder) {
        // remove the first part which will be the folder name and replace it with a
        // hardcoded modules folder
        parts.shift();
        parts.unshift(this.config.modulesFolder);
      } else {
        // first part will be the registry-specific module folder
        parts.unshift(this.config.cwd);
      }

      const loc = parts.join(path.sep);
      flatTree.push([loc, info]);
    }

    // remove ignored modules from the tree
    const visibleFlatTree = [];
    for (const _ref9 of flatTree) {
      var _ref10 = (0, (_slicedToArray2 || _load_slicedToArray()).default)(_ref9, 2);

      const loc = _ref10[0];
      const info = _ref10[1];

      const ref = info.pkg._reference;
      invariant(ref, 'expected reference');

      let ignored = ref.ignore;
      if (ref.optional && this.ignoreOptional) {
        ignored = true;
      }
      if (ignored) {
        info.addHistory('Deleted as this module was ignored');
      } else {
        visibleFlatTree.push([loc, info]);
      }
    }
    return visibleFlatTree;
  }
}
exports.default = PackageHoister;