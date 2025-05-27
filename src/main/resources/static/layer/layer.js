/*
 * layer.js - Modernized ES10+ version without jQuery or IE6-9 support
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
    if (typeof callback !== 'function') return;

    let attempts = 0;
    const poll = () => {
      attempts++;
      const testElem = document.getElementById(link.id);
      if (parseInt(getComputedStyle(testElem).width) === 1989) {
        callback();
      } else if (attempts < 100) {
        setTimeout(poll, 100);
      } else {
        console.error(`${link.href}: load failed`);
      }
    };
    poll();
  },

  open(config = {}) {
    const zIndex = (config.zIndex || 19891014) + ++this.index;
    const layerElem = document.createElement('div');
    layerElem.className = `layer-popup layui-layer-${config.type || 'dialog'}`;
    layerElem.style.zIndex = zIndex;
    layerElem.style.position = 'fixed';
    layerElem.style.left = '50%';
    layerElem.style.top = '20%';
    layerElem.style.transform = 'translateX(-50%)';
    layerElem.style.minWidth = '300px';
    layerElem.style.backgroundColor = '#fff';
    layerElem.style.border = '1px solid #ccc';
    layerElem.style.padding = '15px';
    layerElem.style.boxShadow = '0 2px 10px rgba(0,0,0,0.1)';

    layerElem.innerHTML = `
      ${config.title ? `<div class="layui-layer-title" style="font-weight:bold;margin-bottom:10px;">${config.title}</div>` : ''}
      <div class="layui-layer-content">${config.content || ''}</div>
      ${config.btn ? '<div class="layui-layer-btn" style="margin-top:15px;text-align:right;">' + config.btn.map((b, i) => `<button data-btn-index="${i}" style="margin-left:10px;">${b}</button>`).join('') + '</div>' : ''}
    `;

    document.body.appendChild(layerElem);

    if (config.btn && Array.isArray(config.btn)) {
      layerElem.querySelectorAll('button[data-btn-index]').forEach(btn => {
        btn.addEventListener('click', () => {
          const index = parseInt(btn.dataset.btnIndex);
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
    if (!elems.length) return;
    elems.forEach(el => {
      el.remove();
    });
  },

  alert(content, yes) {
    return this.open({
      content,
      title: 'Alert',
      btn: ['OK'],
      yes
    });
  },

  confirm(content, yes, cancel) {
    return this.open({
      content,
      title: 'Confirm',
      btn: ['OK', 'Cancel'],
      yes,
      btn2: cancel
    });
  }
};

if (typeof window !== 'undefined') {
  window.layer = layer;
  layer.path = (document.currentScript && document.currentScript.src || '').split('/').slice(0, -1).join('/') + '/';
  layer.ready();
}