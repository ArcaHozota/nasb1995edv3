// new-layer.js - ES10+ モダン書き換え版
const ready = {
  getPath: (() => {
    const jsPath = document.currentScript ? document.currentScript.src :
      (() => {
        const js = document.scripts;
        const last = js.length - 1;
        for (let i = last; i > 0; i--) {
          if (js[i].readyState === 'interactive') {
            return js[i].src;
          }
        }
        return js[last].src;
      })();
    const GLOBAL = window.LAYUI_GLOBAL || {};
    return GLOBAL.layer_dir || jsPath.substring(0, jsPath.lastIndexOf('/') + 1);
  })(),
  config: {},
  end: {},
  minIndex: 0,
  minLeft: [],
  btn: ['確定', 'キャンセル'],
  type: ['dialog', 'page', 'iframe', 'loading', 'tips'],
  getStyle: (node, name) => getComputedStyle(node).getPropertyValue(name),
  link: (href, fn, cssname) => {
    if (!layer.path) return;
    const head = document.head;
    const link = document.createElement('link');
    if (typeof fn === 'string') cssname = fn;
    const app = (cssname || href).replace(/\.|\//g, '');
    const id = `layuicss-${app}`;
    link.rel = 'stylesheet';
    link.href = layer.path + href;
    link.id = id;
    if (!document.getElementById(id)) head.appendChild(link);
    if (typeof fn !== 'function') return;
    let timeout = 0;
    (function poll() {
      const delay = 100;
      const getLinkElem = document.getElementById(id);
      if (++timeout > 100) {
        return console.error(`${app}.css: Invalid`);
      }
      if (parseInt(ready.getStyle(getLinkElem, 'width')) === 1989) {
        fn();
      } else {
        setTimeout(poll, delay);
      }
    })();
  }
};

const layer = {
  v: '3.5.1',
  index: 0,
  path: ready.getPath,
  config(options = {}) {
    layer.cache = ready.config = Object.assign({}, ready.config, options);
    layer.path = ready.config.path || layer.path;
    if (layer.path) layer.ready();
    if (!options.extend) return this;
    ready.link('theme/' + options.extend);
    return this;
  },
  ready(callback) {
    const cssname = 'layer';
    const path = `theme/default/layer.css?v=${layer.v}`;
    ready.link(path, callback, cssname);
    return this;
  },
  confirm(content, options, yes, cancel) {
    if (typeof options === 'function') {
      cancel = yes;
      yes = options;
    }
    return layer.open(Object.assign({
      content,
      btn: ready.btn,
      yes,
      btn2: cancel
    }, typeof options === 'function' ? {} : options));
  },
  msg(content, time = 3300, options, end) {
    const rskin = ready.config.skin;
    const skin = (rskin ? `${rskin} ${rskin}-msg` : '') || 'layui-layer-msg';
    const anim = 0; // doms.anim未定義のため仮
    if (typeof options === 'function') end = options;
    return layer.open(Object.assign({
      content,
      time,
      shade: false,
      skin,
      title: false,
      closeBtn: false,
      btn: false,
      resize: false,
      end
    }, (typeof options === 'function' && !ready.config.skin) ? {
      skin: `${skin} layui-layer-hui`,
      anim
    } : (() => {
      options = options || {};
      if (options.icon === -1 || (options.icon === undefined && !ready.config.skin)) {
        options.skin = `${skin} ${options.skin || 'layui-layer-hui'}`;
      }
      return options;
    })()));
  },
  // openや他のメソッドも必要に応じて追加
};

window.layer = layer; // グローバルに公開
