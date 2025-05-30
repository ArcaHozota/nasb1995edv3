/*!
 * layer - 通用 Web 弹出层组件
 * MIT Licensed 
 */

; (function (window, undefined) {
  "use strict";

  // DOM 工具函数
  const $ = {
    create: (tag, attrs) => {
      const el = document.createElement(tag);
      if (attrs) Object.entries(attrs).forEach(([k, v]) => el.setAttribute(k, v));
      return el;
    },
    id: id => document.getElementById(id),
    qs: selector => document.querySelector(selector),
    qsa: selector => Array.from(document.querySelectorAll(selector)),
    addClass: (el, className) => el && el.classList.add(className),
    removeClass: (el, className) => el && el.classList.remove(className),
    hasClass: (el, className) => el && el.classList.contains(className),
    css: (el, styles) => {
      if (!el) return;
      if (typeof styles === 'string') return getComputedStyle(el)[styles];
      Object.entries(styles).forEach(([prop, value]) => {
        el.style[prop] = value;
      });
    },
    append: (parent, child) => parent.appendChild(child),
    prepend: (parent, child) => parent.insertBefore(child, parent.firstChild),
    remove: el => el && el.parentNode.removeChild(el),
    hide: el => $.css(el, { display: 'none' }),
    show: (el, display = 'block') => $.css(el, { display }),
    on: (el, event, handler) => el.addEventListener(event, handler),
    off: (el, event, handler) => el.removeEventListener(event, handler),
    attr: (el, attr, value) => {
      if (value !== undefined) return el.setAttribute(attr, value);
      return el.getAttribute(attr);
    },
    data: (el, key, value) => {
      if (value !== undefined) return el.dataset[key] = value;
      return el.dataset[key];
    },
    each: (arr, fn) => arr.forEach(fn),
    extend: (target, ...sources) => Object.assign(target, ...sources),
    width: el => el.offsetWidth,
    height: el => el.offsetHeight,
    outerWidth: el => el.offsetWidth,
    outerHeight: el => el.offsetHeight,
    position: el => ({
      top: el.offsetTop,
      left: el.offsetLeft
    }),
    offset: el => {
      const rect = el.getBoundingClientRect();
      return {
        top: rect.top + window.pageYOffset,
        left: rect.left + window.pageXOffset
      };
    },
    find: (el, selector) => Array.from(el.querySelectorAll(selector)),
    siblings: el => Array.from(el.parentNode.children).filter(child => child !== el),
    wrap: (inner, outer) => {
      const parent = inner.parentNode;
      parent.insertBefore(outer, inner);
      outer.appendChild(inner);
    },
    unwrap: el => {
      const parent = el.parentNode;
      const grandParent = parent.parentNode;
      grandParent.insertBefore(el, parent);
      grandParent.removeChild(parent);
    },
    html: (el, content) => {
      if (content === undefined) return el.innerHTML;
      el.innerHTML = content;
    },
    text: (el, content) => {
      if (content === undefined) return el.textContent;
      el.textContent = content;
    },
    val: (el, value) => {
      if (value === undefined) return el.value;
      el.value = value;
    },
    isArray: arr => Array.isArray(arr),
    isFunction: fn => typeof fn === 'function',
    isObject: obj => obj !== null && typeof obj === 'object',
    trim: str => str.trim(),
    inArray: (item, arr) => arr.includes(item),
    proxy: (fn, context) => fn.bind(context)
  };

  const isLayui = window.layui && window.layui.define;
  const doc = document;
  const body = doc.body;
  const html = doc.documentElement;
  let ready = {
    getPath: function () {
      let jsPath = '';
      if (doc.currentScript) {
        jsPath = doc.currentScript.src;
      } else {
        const scripts = doc.getElementsByTagName('script');
        for (let i = scripts.length - 1; i >= 0; i--) {
          if (scripts[i].readyState === 'interactive') {
            jsPath = scripts[i].src;
            break;
          }
        }
        if (!jsPath && scripts.length > 0) {
          jsPath = scripts[scripts.length - 1].src;
        }
      }
      const GLOBAL = window.LAYUI_GLOBAL || {};
      return GLOBAL.layer_dir || jsPath.substring(0, jsPath.lastIndexOf('/') + 1);
    }(),
    config: {},
    end: {},
    minIndex: 0,
    minLeft: [],
    btn: ['确定', '取消'],
    type: ['dialog', 'page', 'iframe', 'loading', 'tips'],
    getStyle: function (node, name) {
      const style = node.currentStyle || getComputedStyle(node, null);
      return style.getPropertyValue ? style.getPropertyValue(name) : style.getAttribute(name);
    },
    link: function (href, fn, cssname) {
      if (!layer.path) return;

      const head = doc.getElementsByTagName('head')[0];
      const link = $.create('link', {
        rel: 'stylesheet',
        href: layer.path + href,
        id: cssname ? `layuicss-${cssname.replace(/\.|\//g, '')}` : undefined
      });

      if (!$.id(link.id)) {
        head.appendChild(link);
      }

      if (!$.isFunction(fn)) return;

      const STAUTS_NAME = 'creating';
      let timeout = 0;
      const poll = (status) => {
        const delay = 100;
        const linkElem = $.id(link.id);
        if (++timeout > 10 * 1000 / delay) {
          console.error(`${cssname || href}.css: Invalid`);
          return;
        }
        if (parseInt($.getStyle(linkElem, 'width')) === 1989) {
          if (status === STAUTS_NAME) linkElem.removeAttribute('lay-status');
          linkElem.getAttribute('lay-status') === STAUTS_NAME ? setTimeout(() => poll(STAUTS_NAME), delay) : fn();
        } else {
          linkElem.setAttribute('lay-status', STAUTS_NAME);
          setTimeout(() => poll(STAUTS_NAME), delay);
        }
      };
      poll();
    }
  };

  const layer = {
    v: '3.5.1',
    ie: (() => {
      const agent = navigator.userAgent.toLowerCase();
      return (!!window.ActiveXObject || "ActiveXObject" in window) ? 
        (agent.match(/msie\s(\d+)/) || [])[1] || '11' : false;
    })(),
    index: (window.layer && window.layer.v) ? 100000 : 0,
    path: ready.getPath,
    config: function (options, fn) {
      options = options || {};
      layer.cache = ready.config = $.extend({}, ready.config, options);
      layer.path = ready.config.path || layer.path;
      if (typeof options.extend === 'string') options.extend = [options.extend];
      if (ready.config.path) layer.ready();
      if (!options.extend) return this;
      isLayui ? 
        layui.addcss(`modules/layer/${options.extend}`) : 
        ready.link(`theme/${options.extend}`);
      return this;
    },
    ready: function (callback) {
      const cssname = 'layer';
      const path = (isLayui ? 'modules/layer/' : 'theme/') + `default/layer.css?v=${layer.v}`;
      isLayui ? 
        layui.addcss(path, callback, cssname) : 
        ready.link(path, callback, cssname);
      return this;
    },
    alert: function (content, options, yes) {
      const type = $.isFunction(options);
      if (type) yes = options;
      return layer.open($.extend({
        content: content,
        yes: yes
      }, type ? {} : options));
    },
    confirm: function (content, options, yes, cancel) {
      const type = $.isFunction(options);
      if (type) {
        cancel = yes;
        yes = options;
      }
      return layer.open($.extend({
        content: content,
        btn: ready.btn,
        yes: yes,
        btn2: cancel
      }, type ? {} : options));
    },
    msg: function (content, time = 3000, options, end) {
      const type = $.isFunction(options);
      const rskin = ready.config.skin;
      let skin = (rskin ? `${rskin} ${rskin}-msg` : '') || 'layui-layer-msg';
      const anim = doms.anim.length - 1;
      if (type) end = options;
      return layer.open($.extend({
        content: content,
        time: time,
        shade: false,
        skin: skin,
        title: false,
        closeBtn: false,
        btn: false,
        resize: false,
        end: end
      }, (type && !ready.config.skin) ? {
        skin: `${skin} layui-layer-hui`,
        anim: anim
      } : (() => {
        options = options || {};
        if (options.icon === -1 || (options.icon === undefined && !ready.config.skin)) {
          options.skin = `${skin} ${options.skin || 'layui-layer-hui'}`;
        }
        return options;
      })()));
    },
    load: function (icon, options) {
      return layer.open($.extend({
        type: 3,
        icon: icon || 0,
        resize: false,
        shade: 0.01
      }, options));
    },
    tips: function (content, follow, options) {
      return layer.open($.extend({
        type: 4,
        content: [content, follow],
        closeBtn: false,
        time: 0,
        shade: false,
        resize: false,
        fixed: false,
        maxWidth: 260
      }, options));
    }
  };

  class LayerClass {
    constructor(settings) {
      this.index = ++layer.index;
      this.config = $.extend({}, this.constructor.defaultConfig, ready.config, settings);
      this.config.maxWidth = window.innerWidth - 30;
      body ? this.creat() : setTimeout(() => this.creat(), 30);
    }

    static defaultConfig = {
      type: 0,
      shade: 0.3,
      fixed: true,
      move: '.layui-layer-title',
      title: '信息',
      offset: 'auto',
      area: 'auto',
      closeBtn: 1,
      time: 0,
      zIndex: 19891014,
      maxWidth: 360,
      anim: 0,
      isOutAnim: true,
      minStack: true,
      icon: -1,
      moveType: 1,
      resize: true,
      scrollbar: true,
      tips: 2
    };

    vessel(conType, callback) {
      const config = this.config;
      const times = this.index;
      const zIndex = config.zIndex + times;
      const titype = $.isObject(config.title);
      const ismax = config.maxmin && (config.type === 1 || config.type === 2);
      
      const titleHTML = config.title ? 
        `<div class="layui-layer-title" style="${titype ? config.title[1] : ''}">${titype ? config.title[0] : config.title}</div>` : '';

      config.zIndex = zIndex;
      
      const html = [
        config.shade ? 
          `<div class="${doms.SHADE}" id="${doms.SHADE}${times}" times="${times}" style="z-index:${zIndex - 1}"></div>` : '',
        `<div class="${doms[0]} layui-layer-${ready.type[config.type]} ${(config.type == 0 || config.type == 2) && !config.shade ? ' layui-layer-border' : ''} ${config.skin || ''}" id="${doms[0]}${times}" type="${ready.type[config.type]}" times="${times}" showtime="${config.time}" conType="${conType ? 'object' : 'string'}" style="z-index:${zIndex}; width:${config.area[0]};height:${config.area[1]};position:${config.fixed ? 'fixed' : 'absolute'};">` +
          (conType && config.type != 2 ? '' : titleHTML) +
          `<div id="${config.id || ''}" class="layui-layer-content${config.type == 0 && config.icon !== -1 ? ' layui-layer-padding' : ''}${config.type == 3 ? ` layui-layer-loading${config.icon}` : ''}">` +
          (config.type == 0 && config.icon !== -1 ? `<i class="layui-layer-ico layui-layer-ico${config.icon}"></i>` : '') +
          (config.type == 1 && conType ? '' : (config.content || '')) +
          '</div>' +
          `<span class="layui-layer-setwin">` +
            (ismax ? '<a class="layui-layer-min" href="javascript:;"><cite></cite></a><a class="layui-layer-ico layui-layer-max" href="javascript:;"></a>' : '') +
            (config.closeBtn ? `<a class="layui-layer-ico ${doms[7]} ${doms[7]}${config.title ? config.closeBtn : (config.type == 4 ? '1' : '2')}" href="javascript:;"></a>` : '') +
          '</span>' +
          (config.btn ? (() => {
            const buttons = $.isArray(config.btn) ? config.btn : [config.btn];
            return `<div class="${doms[6]} layui-layer-btn-${config.btnAlign || ''}">` +
              buttons.map((btn, i) => `<a class="${doms[6]}${i}">${btn}</a>`).join('') +
            '</div>';
          })() : '') +
          (config.resize ? '<span class="layui-layer-resize"></span>' : '') +
        '</div>'
      ];

      callback(html, titleHTML, $.create('div', { class: doms.MOVE, id: doms.MOVE }));
      return this;
    }

    creat() {
      const config = this.config;
      const times = this.index;
      const content = config.content;
      const conType = $.isObject(content);

      if (config.id && $.id(config.id)) return;

      if (typeof config.area === 'string') {
        config.area = config.area === 'auto' ? ['', ''] : [config.area, ''];
      }

      if (config.shift) config.anim = config.shift;
      if (layer.ie == 6) config.fixed = false;

      switch (config.type) {
        case 0:
          config.btn = 'btn' in config ? config.btn : ready.btn[0];
          layer.closeAll('dialog');
          break;
        case 2:
          const iframeContent = conType ? content : [content || '', 'auto'];
          config.content = `<iframe scrolling="${iframeContent[1] || 'auto'}" allowtransparency="true" id="${doms[4]}${times}" name="${doms[4]}${times}" onload="this.className='';" class="layui-layer-load" frameborder="0" src="${iframeContent[0]}"></iframe>`;
          break;
        case 3:
          case 4:
          // 处理逻辑...
          break;
      }

      this.vessel(conType, (html, titleHTML, moveElem) => {
        html.forEach(item => {
          if (item) $.append(body, $.create('div', { html: item }));
        });

        if (conType) {
          if (config.type === 2 || config.type === 4) {
            $.append(body, $.create('div', { html: html[1] }));
          } else {
            const wrap = $.create('div', { html: html[1] });
            $.wrap(content, wrap);
            const container = $.id(`${doms[0]}${times}`);
            $.prepend($.find(container, `.${doms[5]}`)[0], titleHTML);
          }
        } else {
          $.append(body, $.create('div', { html: html[1] }));
        }

        if (!$.id(doms.MOVE)) $.append(body, moveElem);

        this.layero = $.id(`${doms[0]}${times}`);
        this.shadeo = $.id(`${doms.SHADE}${times}`);

        if (!config.scrollbar) {
          $.css(html, 'overflow', 'hidden');
          $.attr(html, 'layer-full', times);
        }
      });

      // 其他初始化逻辑...
    }

    // 其他方法实现...
  }

  // 缓存常用DOM字符串
  const doms = [
    'layui-layer',
    '.layui-layer-title',
    '.layui-layer-main',
    '.layui-layer-dialog',
    'layui-layer-iframe',
    'layui-layer-content',
    'layui-layer-btn',
    'layui-layer-close'
  ];
  doms.anim = [
    'layer-anim-00', 'layer-anim-01', 'layer-anim-02', 
    'layer-anim-03', 'layer-anim-04', 'layer-anim-05', 'layer-anim-06'
  ];
  doms.SHADE = 'layui-layer-shade';
  doms.MOVE = 'layui-layer-move';

  // 暴露全局API
  LayerClass.pt = LayerClass.prototype;
  window.layer = layer;

  // 初始化
  ready.run = function () {
    layer.open = function (deliver) {
      return new LayerClass(deliver).index;
    };
  };

  // 加载方式
  if (isLayui) {
    layer.ready();
    layui.define('jquery', exports => {
      layer.path = layui.cache.dir;
      ready.run();
      window.layer = layer;
      exports('layer', layer);
    });
  } else if (typeof define === 'function' && define.amd) {
    define([], () => {
      ready.run();
      return layer;
    });
  } else {
    layer.ready();
    ready.run();
  }

})(window);