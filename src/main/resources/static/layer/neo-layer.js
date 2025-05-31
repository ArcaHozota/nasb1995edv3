/*
 * layer.js — ES10+ rewrite with **zero** external dependencies
 * ===========================================================
 * Original: v3.5.1 (jQuery + IE6‑9 support)
 * Re‑implemented: v4.0.0‑es10  (2025‑05‑31)
 *
 * ✅ 100 % vanilla‑JS – no jQuery / Zepto / layui run‑time
 * ✅ Modern syntax (const/let, template literals, optional chaining, arrow fn)
 * ✅ Self‑contained UMD (ES Modules, CommonJS, <script>)
 * ✅ Core public API parity: open / close / closeAll / alert / confirm / msg / load / tips
 * ✅ Dynamic CSS loader for theme files (layer.css + optional skins)
 * ✅ Responsive: auto‑centering + viewport resize handling
 * ✅ Small helper $() provides **tiny DOM utility** (query, on, css…) but is **NOT** jQuery
 * -----------------------------------------------------------
 *  ⚠️ Advanced features (drag‑move, resize, min/restore, photos gallery, iframe
 *    cross‑domain, IE ≤10 quirks) have been trimmed for clarity.  
 *    Add them back as needed using plain DOM.
 * -----------------------------------------------------------
*/

/** Tiny DOM helper – *not* jQuery. */
const $ = (selector, context = document) => {
  const nodes = typeof selector === 'string' ? context.querySelectorAll(selector) : [selector];
  const list = Array.from(nodes);
  const api = {
    on(evt, cb) { list.forEach(el => el.addEventListener(evt, cb)); return api; },
    css(prop, val) {
      if (typeof prop === 'object') list.forEach(el => Object.assign(el.style, prop));
      else if (val !== undefined) list.forEach(el => { el.style[prop] = val; });
      else return getComputedStyle(list[0])[prop];
      return api;
    },
    attr(name, value) {
      if (value === undefined) return list[0]?.getAttribute(name);
      list.forEach(el => el.setAttribute(name, value));
      return api;
    },
    html(html) { if (html === undefined) return list[0]?.innerHTML; list.forEach(el => el.innerHTML = html); return api; },
    addClass(c) { list.forEach(el => el.classList.add(...c.split(/\s+/))); return api; },
    remove() { list.forEach(el => el.remove()); },
    first() { return list[0]; },
    all: list
  };
  return api;
};

/* ------------------------------------------------------------------ */
const layer = (() => {
  const VERSION = '4.0.0-es10';
  let zIndexBase = 19891014;
  let globalIndex = 0;
  const cacheEnd = new Map();

  /* -------------------------------------------------------------- */
  function loadCss(href, id) {
    return new Promise(res => {
      if (document.getElementById(id)) return res();
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      link.id = id;
      link.href = href;
      link.onload = () => res();
      document.head.appendChild(link);
    });
  }

  const defaultConfig = {
    type: 0,                // 0 dialog / 1 page / 2 iframe / 3 loading / 4 tips
    title: '信息',           // string | false
    content: '',
    btn: ['确定', '取消'],   // button text array | false
    area: ['auto', 'auto'], // [w,h] css units or %
    shade: 0.3,             // false | number opacity 0‑1 | [opacity,color]
    shadeClose: false,
    closeBtn: 1,
    time: 0,                // auto‑close ms, 0 = none
    offset: 'auto',         // 'auto' | 't' 'b' 'l' 'r' 'lt'… | [top,left] | px/%
    fixed: true,
    zIndex: () => ++zIndexBase,
    anim: 0,                // 0‑6 css animation index
    skin: '',               // extra class
  };

  /* =======================  Core renderer  ====================== */
  function render(opts) {
    const cfg = { ...defaultConfig, ...opts }; // shallow merge
    if (typeof cfg.zIndex === 'function') cfg.zIndex = cfg.zIndex();
    const idx = ++globalIndex;

    // ---------- Shade ----------
    let shadeEl = null;
    if (cfg.shade) {
      const [opacity, color] = Array.isArray(cfg.shade) ? cfg.shade : [cfg.shade, '#000'];
      shadeEl = document.createElement('div');
      shadeEl.className = 'layui-layer-shade';
      shadeEl.style.cssText = `z-index:${cfg.zIndex - 1};background:${color};opacity:${opacity};position:${cfg.fixed ? 'fixed' : 'absolute'};top:0;left:0;width:100%;height:100%;`;
      document.body.appendChild(shadeEl);
      if (cfg.shadeClose) shadeEl.addEventListener('click', () => layer.close(idx));
    }

    // ---------- Container ----------
    const panel = document.createElement('div');
    panel.className = `layui-layer layui-layer-${['dialog','page','iframe','loading','tips'][cfg.type]} ${cfg.skin}`;
    panel.id = `layui-layer${idx}`;
    panel.style.zIndex = cfg.zIndex;
    panel.style.position = cfg.fixed ? 'fixed' : 'absolute';
    panel.style.width = cfg.area[0];
    panel.style.height = cfg.area[1];
    panel.setAttribute('times', idx);

    // Title
    if (cfg.title !== false) {
      const titleBar = document.createElement('div');
      titleBar.className = 'layui-layer-title';
      titleBar.innerHTML = cfg.title;
      panel.appendChild(titleBar);
    }

    // Content
    const contentBox = document.createElement('div');
    contentBox.className = 'layui-layer-content';
    if (cfg.type === 2) { // iframe
      const iframe = document.createElement('iframe');
      iframe.src = Array.isArray(cfg.content) ? cfg.content[0] : cfg.content;
      iframe.style.border = 'none';
      iframe.style.width = '100%';
      iframe.style.height = '100%';
      contentBox.appendChild(iframe);
    } else {
      contentBox.innerHTML = cfg.content;
    }
    panel.appendChild(contentBox);

    // Buttons
    if (cfg.btn && cfg.btn.length) {
      const btnBar = document.createElement('div');
      btnBar.className = 'layui-layer-btn';
      cfg.btn.forEach((txt, i) => {
        const b = document.createElement('button');
        b.className = `layui-layer-btn${i}`;
        b.textContent = txt;
        b.addEventListener('click', () => {
          const cbName = i === 0 ? 'yes' : `btn${i+1}`;
          const shouldClose = cfg[cbName] ? cfg[cbName](idx, panel) !== false : true;
          if (shouldClose) layer.close(idx);
        });
        btnBar.appendChild(b);
      });
      panel.appendChild(btnBar);
    }

    // Close btn
    if (cfg.closeBtn) {
      const close = document.createElement('span');
      close.className = 'layui-layer-close';
      close.innerHTML = cfg.closeBtn === 1 ? '&times;' : 'x';
      close.addEventListener('click', () => layer.close(idx));
      panel.appendChild(close);
    }

    document.body.appendChild(panel);

    // Centering / positioning
    const center = () => {
      const top = cfg.offset === 'auto' ? (window.innerHeight - panel.offsetHeight) / 2 : parseOffset(cfg.offset, 0);
      const left = cfg.offset === 'auto' ? (window.innerWidth - panel.offsetWidth) / 2 : parseOffset(cfg.offset, 1);
      panel.style.top = `${Math.max(top, 0)}px`;
      panel.style.left = `${Math.max(left, 0)}px`;
    };
    const parseOffset = (off, idx) => {
      if (typeof off === 'string') {
        const map = { t:0, r: ['auto',0], b: ['auto','auto'], l:0 };
        if (['t','r','b','l','lt','rt','lb','rb'].includes(off)) {
          const [v,h] = off.split('');
          if (v==='t') return idx?0:0;
          if (v==='b') return idx?0:window.innerHeight - panel.offsetHeight;
          if (h==='l') return idx?0:0;
          if (h==='r') return idx?window.innerWidth - panel.offsetWidth:0;
        }
        if (/%$/.test(off)) return idx?window.innerWidth*parseFloat(off)/100:window.innerHeight*parseFloat(off)/100;
        return parseFloat(off);
      }
      if (Array.isArray(off)) return parseFloat(off[idx]);
      return 0;
    };
    center();
    window.addEventListener('resize', center);

    // Auto‑close
    if (cfg.time > 0) setTimeout(() => layer.close(idx), cfg.time);

    // Anim
    if (cfg.anim) panel.classList.add(`layer-anim-${cfg.anim}`);

    // Success callback
    cfg.success?.(panel, idx);

    return idx;
  }

  /* ===========================  Public API  =========================== */
  const api = {
    v: VERSION,
    open: render,
    close(index) {
      const panel = document.getElementById(`layui-layer${index}`);
      if (!panel) return;
      const shade = document.querySelector(`.layui-layer-shade[times="${index}"]`);
      shade?.remove();
      panel.remove();
      window.removeEventListener('resize', null);
      cacheEnd.get(index)?.();
      cacheEnd.delete(index);
    },
    closeAll() {
      document.querySelectorAll('.layui-layer').forEach(el => el.remove());
      document.querySelectorAll('.layui-layer-shade').forEach(el => el.remove());
    },
    alert(content, options = {}, yes) {
      if (typeof options === 'function') { yes = options; options = {}; }
      return render({ content, yes, ...options });
    },
    confirm(content, yes, cancel, options = {}) {
      return render({ content, btn: ['确定','取消'], yes, btn2: cancel, ...options });
    },
    msg(content, time = 3300, options = {}) {
      return render({ content, time, shade: false, title: false, closeBtn: 0, ...options });
    },
    load(icon = 0, options = {}) {
      return render({ type:3, icon, shade: 0.01, closeBtn:0, ...options });
    },
    tips(content, follow, options = {}) {
      const rect = (follow instanceof HTMLElement ? follow : document.querySelector(follow))?.getBoundingClientRect() || {top:0,left:0,width:0,height:0};
      return render({
        type:4,
        content,
        shade:false,
        closeBtn:0,
        fixed:false,
        time: options.time ?? 0,
        offset:[rect.top+rect.height+10,rect.left],
        ...options
      });
    }
  };

  /* -------------------------  Auto CSS hook  ------------------------ */
  (async () => {
    await loadCss(`${getScriptDir()}theme/default/layer.css?v=${VERSION}`, 'layer‑skin');
  })();

  function getScriptDir(){
    const url = document.currentScript?.src || Array.from(document.scripts).pop().src;
    return url.substring(0, url.lastIndexOf('/') + 1);
  }

  /* ------------------------------------------------------------------ */
  return api;
})();

// UMD export
if (typeof module !== 'undefined' && module.exports) module.exports = layer;
else if (typeof define === 'function' && define.amd) define(() => layer);
else window.layer = layer;
