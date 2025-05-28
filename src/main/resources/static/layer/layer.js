/*
 * layer.js - Pure ES10+ version without jQuery or legacy browser support
 */

const layer = {
  v: '3.5.1',
  index: 0,
  path: '',
  cache: {},

  config(options = {}) {
    this.cache = { ...this.cache, ...options };
    this.path = options.path || this.path;
    if (this.path) this.ready();
    return this;
  },

  ready(callback) {
    const cssname = 'layer';
    const path = `${this.path}theme/default/layer.css?v=${this.v}`;
    this.loadCSS(path, callback, cssname);
    return this;
  },

  loadCSS(href, callback, id) {
    if (!this.path) return;
    const link = document.createElement('link');
    link.rel = 'stylesheet';
    link.href = href;
    link.id = id || `layuicss-${href.replace(/\.|\//g, '')}`;
    if (!document.getElementById(link.id)) {
      document.head.appendChild(link);
    }
    if (typeof callback === 'function') {
      link.onload = callback;
      link.onerror = () => console.error(`${href}: load failed`);
    }
  },

  open(config = {}) {
    const zIndex = (config.zIndex || 19891014) + ++this.index;
    const layerElem = document.createElement('div');
    layerElem.className = `layer-popup layui-layer-${config.type || 'dialog'} ${config.skin || ''}`.trim();
    layerElem.style.cssText = `
      z-index: ${zIndex};
      position: fixed;
      left: 50%;
      top: 20%;
      transform: translateX(-50%);
      min-width: 300px;
      background-color: #fff;
      border: 1px solid #ccc;
      padding: 15px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    `;

    layerElem.innerHTML = `
      ${config.title ? `<div class="layui-layer-title" style="font-weight:bold;margin-bottom:10px;">${config.title}</div>` : ''}
      <div class="layui-layer-content">${config.content || ''}</div>
      ${Array.isArray(config.btn) ? '<div class="layui-layer-btn" style="margin-top:15px;text-align:right;">' + config.btn.map((b, i) => `<button data-btn-index="${i}" style="margin-left:10px;">${b}</button>`).join('') + '</div>' : ''}
    `;

    document.body.appendChild(layerElem);

    if (Array.isArray(config.btn)) {
      layerElem.querySelectorAll('button[data-btn-index]').forEach(btn => {
        btn.addEventListener('click', () => {
          const index = Number(btn.dataset.btnIndex);
          const callbackName = index === 0 ? 'yes' : `btn${index + 1}`;
          if (typeof config[callbackName] === 'function') {
            const result = config[callbackName](this.index, layerElem);
            if (result !== false) this.close(this.index);
          } else {
            this.close(this.index);
          }
        });
      });
    }

    if (typeof config.success === 'function') config.success(layerElem, this.index);
    if (config.time > 0) {
      setTimeout(() => this.close(this.index), config.time);
    }
    return this.index;
  },

  close(index) {
    const elems = document.querySelectorAll(`.layer-popup`);
    elems.forEach(el => el.remove());
  },

  msg(content, time = 3300, options = {}, end = null) {
    const isFunc = typeof options === 'function';
    if (isFunc) end = options;
    const rskin = this.cache.skin || '';
    let skin = (rskin ? `${rskin} ${rskin}-msg` : 'layui-layer-msg');

    if (isFunc && !rskin) {
      skin += ' layui-layer-hui';
    } else if (!rskin && (!options.icon || options.icon === -1)) {
      options.skin = `${skin} ${options.skin || 'layui-layer-hui'}`;
    }

    return this.open({
      content,
      time,
      shade: false,
      skin: options.skin || skin,
      title: false,
      closeBtn: false,
      btn: false,
      resize: false,
      end
    });
  }
};

if (typeof window !== 'undefined') {
  window.layer = layer;
  layer.path = (document.currentScript?.src || '').split('/').slice(0, -1).join('/') + '/';
  layer.ready();
}