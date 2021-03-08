'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _baseReporter;

function _load_baseReporter() {
  return _baseReporter = _interopRequireDefault(require('../base-reporter.js'));
}

var _progressBar;

function _load_progressBar() {
  return _progressBar = _interopRequireDefault(require('./progress-bar.js'));
}

var _spinnerProgress;

function _load_spinnerProgress() {
  return _spinnerProgress = _interopRequireDefault(require('./spinner-progress.js'));
}

var _util;

function _load_util() {
  return _util = require('./util.js');
}

var _misc;

function _load_misc() {
  return _misc = require('../../util/misc.js');
}

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var _require = require('util');

const inspect = _require.inspect;

const readline = require('readline');
const repeat = require('repeating');
const chalk = require('chalk');
const read = require('read');

function sortTrees() {
  let trees = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : [];

  return trees.sort(function (tree1, tree2) {
    return tree1.name.localeCompare(tree2.name);
  });
}

class ConsoleReporter extends (_baseReporter || _load_baseReporter()).default {
  constructor(opts) {
    super(opts);
    this._lastCategorySize = 0;

    this.format = chalk;
  }

  _prependEmoji(msg, emoji) {
    if (this.emoji && emoji && this.isTTY) {
      msg = `${ emoji }  ${ msg }`;
    }
    return msg;
  }

  _logCategory(category, color, msg) {
    this._lastCategorySize = category.length;
    this._log(`${ this.format[color](category) } ${ msg }`);
  }

  table(head, body) {
    //
    head = head.map(field => this.format.underline(field));

    //
    const rows = [head].concat(body);

    // get column widths
    const cols = [];
    for (let i = 0; i < head.length; i++) {
      const widths = rows.map(row => this.format.stripColor(row[i]).length);
      cols[i] = Math.max(...widths);
    }

    //
    const builtRows = rows.map(row => {
      for (let i = 0; i < row.length; i++) {
        const field = row[i];
        const padding = cols[i] - this.format.stripColor(field).length;

        row[i] = field + repeat(' ', padding);
      }
      return row.join(' ');
    });

    this.log(builtRows.join('\n'));
  }

  step(current, total, msg, emoji) {
    msg = this._prependEmoji(msg, emoji);

    if (msg.endsWith('?')) {
      msg = `${ (0, (_misc || _load_misc()).removeSuffix)(msg, '?') }...?`;
    } else {
      msg += '...';
    }

    this.log(`${ this.format.grey(`[${ current }/${ total }]`) } ${ msg }`);
  }

  inspect(value) {
    if (typeof value !== 'number' && typeof value !== 'string') {
      value = inspect(value, {
        breakLength: 0,
        colors: true,
        depth: null,
        maxArrayLength: null
      });
    }

    this.log('' + value);
  }

  list(key, items) {
    const gutterWidth = (this._lastCategorySize || 2) - 1;
    for (const item of items) {
      this._log(`${ repeat(' ', gutterWidth) }- ${ item }`);
    }
  }

  header(command, pkg) {
    this.log(this.format.bold(`${ pkg.name } ${ command } v${ pkg.version }`));
  }

  footer(showPeakMemory) {
    const totalTime = (this.getTotalTime() / 1000).toFixed(2);
    let msg = `Done in ${ totalTime }s.`;
    if (showPeakMemory) {
      const peakMemory = (this.peakMemory / 1024 / 1024).toFixed(2);
      msg += ` Peak memory usage ${ peakMemory }MB.`;
    }
    this.log(this._prependEmoji(msg, '✨'));
  }

  log(msg) {
    this._lastCategorySize = 0;
    this._log(msg);
  }

  _log(msg) {
    (0, (_util || _load_util()).clearLine)(this.stdout);
    this.stdout.write(`${ msg }\n`);
  }

  success(msg) {
    this._logCategory('success', 'green', msg);
  }

  error(msg) {
    (0, (_util || _load_util()).clearLine)(this.stderr);
    this.stderr.write(`${ this.format.red('error') } ${ msg }\n`);
  }

  info(msg) {
    this._logCategory('info', 'blue', msg);
  }

  command(command) {
    this.log(this.format.grey(`$ ${ command }`));
  }

  warn(msg) {
    (0, (_util || _load_util()).clearLine)(this.stderr);
    this.stderr.write(`${ this.format.yellow('warning') } ${ msg }\n`);
  }

  question(question) {
    let options = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};

    if (!process.stdout.isTTY) {
      return Promise.reject(new Error("Can't answer a question unless a user TTY"));
    }

    return new Promise((resolve, reject) => {
      read({
        prompt: `${ this.format.grey('question') } ${ question }: `,
        silent: !!options.password,
        output: this.stdout,
        input: this.stdin
      }, (err, answer) => {
        if (err) {
          if (err.message === 'canceled') {
            process.exit(1);
          } else {
            reject(err);
          }
        } else {
          if (!answer && options.required) {
            this.error(this.lang('answerRequired'));
            resolve(this.question(question, options));
          } else {
            resolve(answer);
          }
        }
      });
    });
  }

  tree(key, trees) {
    trees = sortTrees(trees);

    const stdout = this.stdout;

    const output = (_ref, level, end) => {
      let name = _ref.name;
      let children = _ref.children;
      let hint = _ref.hint;
      let color = _ref.color;

      children = sortTrees(children);

      let indent = end ? '└' : '├';

      if (level) {
        indent = repeat('│  ', level) + indent;
      }

      let suffix = '';
      if (hint) {
        suffix += ` (${ this.format.grey(hint) })`;
      }
      if (color) {
        name = this.format[color](name);
      }
      stdout.write(`${ indent }─ ${ name }${ suffix }\n`);

      if (children && children.length) {
        for (let i = 0; i < children.length; i++) {
          const tree = children[i];
          output(tree, level + 1, i === children.length - 1);
        }
      }
    };

    for (let i = 0; i < trees.length; i++) {
      const tree = trees[i];
      output(tree, 0, i === trees.length - 1);
    }
  }

  activitySet(total, workers) {
    if (!this.isTTY) {
      return super.activitySet(total, workers);
    }

    const spinners = [];

    for (let i = 1; i < workers; i++) {
      this.log('');
    }

    for (let i = 0; i < workers; i++) {
      const spinner = new (_spinnerProgress || _load_spinnerProgress()).default(this.stderr, i);
      spinner.start();

      let prefix = null;
      let current = 0;
      const updatePrefix = () => {
        spinner.setPrefix(`${ this.format.grey(`[${ current === 0 ? '-' : current }/${ total }]`) } `);
      };
      const clear = () => {
        prefix = null;
        current = 0;
        updatePrefix();
        spinner.setText('waiting...');
      };
      clear();

      spinners.unshift({
        clear,

        setPrefix(_current, _prefix) {
          current = _current;
          prefix = _prefix;
          spinner.setText(prefix);
          updatePrefix();
        },

        tick(msg) {
          if (prefix) {
            msg = `${ prefix }: ${ msg }`;
          }
          spinner.setText(msg);
        },

        end() {
          spinner.stop();
        }
      });
    }

    return {
      spinners,
      end: () => {
        for (const spinner of spinners) {
          spinner.end();
        }
        readline.moveCursor(this.stdout, 0, -workers + 1);
      }
    };
  }

  activity() {
    if (!this.isTTY) {
      return {
        tick() {},
        end() {}
      };
    }

    const spinner = new (_spinnerProgress || _load_spinnerProgress()).default(this.stderr);
    spinner.start();

    return {
      tick(name) {
        spinner.setText(name);
      },

      end() {
        spinner.stop();
      }
    };
  }

  select(header, question, options) {
    if (!this.isTTY) {
      return Promise.reject(new Error("Can't answer a question unless a user TTY"));
    }

    const rl = readline.createInterface({
      input: this.stdin,
      output: this.stdout,
      terminal: true
    });

    const questions = options.map(opt => opt.name);
    const answers = options.map(opt => opt.value);

    function toIndex(input) {
      const index = answers.indexOf(input);

      if (index >= 0) {
        return index;
      } else {
        return +input;
      }
    }

    return new Promise(resolve => {
      this.info(header);

      for (let i = 0; i < questions.length; i++) {
        this.log(`  ${ this.format.dim(`${ i + 1 })`) } ${ questions[i] }`);
      }

      const ask = () => {
        rl.question(`${ question }: `, input => {
          let index = toIndex(input);

          if (isNaN(index)) {
            this.log('Not a number');
            ask();
            return;
          }

          if (index <= 0 || index > options.length) {
            this.log('Outside answer range');
            ask();
            return;
          }

          // get index
          index--;
          rl.close();
          resolve(answers[index]);
        });
      };

      ask();
    });
  }

  progress(count) {
    if (count <= 0) {
      return function () {
        // noop
      };
    }

    if (!this.isTTY) {
      return function () {
        // TODO what should the behaviour here be? we could buffer progress messages maybe
      };
    }

    const bar = new (_progressBar || _load_progressBar()).default(count, this.stderr);

    bar.render();

    return function () {
      bar.tick();
    };
  }
}
exports.default = ConsoleReporter;