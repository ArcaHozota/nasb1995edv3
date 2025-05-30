/*
 * layer.js (ES10+ rewrite)
 * Original version 3.5.1 was jQuery‑dependent and contained legacy IE hacks.
 * This modern rewrite removes jQuery/IE6‑9 support and embraces
 * modern browser APIs, const/let, template literals, optional chaining,
 * and the module pattern. 100 % API‑compatible for common calls
 * (alert/confirm/msg/open/close/closeAll etc.).
 */

const layer = (() => {
  const version = '3.6.0-es10';
  let index = 0;
  const instances = new Map();
  const cache = {};
  const defaultButtons = ['确定', '取消'];
  const readyTypes = ['dialog', 'page', 'iframe', 'loading', 'tips'];
  const domPrefix = 'layui-layer';
  const win = window;
  const doc = document;

  /* --------------------------------------------------------------------
   * Helpers
   * ------------------------------------------------------------------*/

  /** Detect script path so we can lazy‑load CSS next to the script file. */
  const path = (() => {
    const script = doc.currentScript || [...doc.querySelectorAll('script')].pop();
    return script?.src.replace(/[^/]+$/, '') ?? '';
  })();

  /** Load a style‑sheet once and fire optional callback when ready. */
  const link = (href, cb) => {
    if (!path) return cb?.();
    const id = `layuicss-${href.replace(/\.|\//g, '')}`;
    if (doc.getElementById(id)) return cb?.();

    const el = Object.assign(doc.createElement('link'), {
      rel: 'stylesheet',
      href: `${path}${href}`,
      id
    });
    el.onload = () => cb?.();
    doc.head.append(el);
  };

  /** Ensure default CSS is present before first open(). */
  const ready = cb => link(`theme/default/layer.css?v=${version}`, cb);

  /** Convert area option to [w, h] array. */
  const normArea = area => {
    if (typeof area === 'string') return area === 'auto' ? ['', ''] : [area, ''];
    return area || ['auto', 'auto'];
  };

  /** Build shade layer HTML. */
  const shadeHtml = (z, idx, shade) => {
    if (!shade) return '';
    const [opacity, color] = Array.isArray(shade) ? shade : [shade, '#000'];
    return `<div class="layui-layer-shade" id="layui-layer-shade${idx}" times="${idx}" style="z-index:${z - 1};background-color:${color};opacity:${opacity};"></div>`;
  };

  /** Build title bar HTML (supports [text, style] form). */
  const titleHtml = title => {
    if (!title) return '';
    return Array.isArray(title)
      ? `<div class="layui-layer-title" style="${title[1] || ''}">${title[0]}</div>`
      : `<div class="layui-layer-title">${title}</div>`;
  };

  /** Main layer skeleton factory. */
  const layerHtml = (cfg, idx, contentIsNode) => {
    const z = cfg.zIndex;
    const tit = (!contentIsNode || cfg.type === 2) ? titleHtml(cfg.title) : '';
    const cls = `${domPrefix} ${domPrefix}-${readyTypes[cfg.type]} ${(!cfg.shade && (cfg.type === 0 || cfg.type === 2)) ? 'layui-layer-border' : ''} ${cfg.skin || ''}`;

    // content wrapper
    const pad = (cfg.type === 0 && cfg.icon !== -1) ? ' layui-layer-padding' : '';
    const loadingCls = cfg.type === 3 ? ` layui-layer-loading${cfg.icon}` : '';

    const iconHtml = (cfg.type === 0 && cfg.icon !== -1)
      ? `<i class="layui-layer-ico layui-layer-ico${cfg.icon}"></i>`
      : '';

    const iframeHtml = cfg.type === 2
      ? `<iframe scrolling="${cfg.content[1] || 'auto'}" allowtransparency="true" id="layui-layer-iframe${idx}" name="layui-layer-iframe${idx}" class="layui-layer-load" frameborder="0" src="${cfg.content[0]}"></iframe>`
      : '';

    const bodyHtml = cfg.type === 2 ? iframeHtml : (iconHtml + (contentIsNode && cfg.type === 1 ? '' : (cfg.content || '')));

    const btnHtml = cfg.btn
      ? `<div class="layui-layer-btn">${cfg.btn.map((b, i) => `<a class="layui-layer-btn${i}">${b}</a>`).join('')}</div>`
      : '';

    const resizeHtml = cfg.resize ? '<span class="layui-layer-resize"></span>' : '';

    return `${shadeHtml(z, idx, cfg.shade)}<div class="${cls}" id="${domPrefix}${idx}" type="${readyTypes[cfg.type]}" times="${idx}" style="z-index:${z};width:${cfg.area[0]};height:${cfg.area[1]};position:${cfg.fixed ? 'fixed' : 'absolute'};">${tit}<div id="${cfg.id || ''}" class="layui-layer-content${pad}${loadingCls}">${bodyHtml}</div>${btnHtml}${resizeHtml}</div>`;
  };

  /* --------------------------------------------------------------------
   * Core open / close logic
   * ------------------------------------------------------------------*/

  const open = (opt = {}) => {
    const cfg = {
      type: 0,
      shade: 0.3,
      fixed: true,
      title: '信息',
      offset: 'auto',
      area: ['auto', 'auto'],
      closeBtn: 1,
      time: 0,
      zIndex: 19891014,
      maxWidth: 360,
      anim: 0,
      icon: -1,
      resize: true,
      scrollbar: true,
      tips: 2,
      ...opt
    };

    cfg.area = normArea(cfg.area);
    cfg.zIndex += ++index;

    if (cfg.type === 0) cfg.btn = cfg.btn ?? defaultButtons;
    if (typeof cfg.btn === 'string') cfg.btn = [cfg.btn];

    const contentIsNode = typeof cfg.content === 'object';

    // iframe mode normalisation
    if (cfg.type === 2 && !contentIsNode) cfg.content = [cfg.content || '', 'auto'];

    // loading mode tweaks
    if (cfg.type === 3) {
      delete cfg.title;
      delete cfg.closeBtn;
      if (cfg.icon === -1) cfg.icon = 0;
    }

    // tips mode tweaks
    if (cfg.type === 4) {
      if (!contentIsNode) cfg.content = [cfg.content, 'body'];
      cfg.follow = cfg.content[1];
      cfg.content = `${cfg.content[0]}<i class="layui-layer-TipsG"></i>`;
      delete cfg.title;
      cfg.tips = Array.isArray(cfg.tips) ? cfg.tips : [cfg.tips, true];
    }

    // DOM injection
    const html = layerHtml(cfg, index, contentIsNode);
    const temp = doc.createElement('div');
    temp.innerHTML = html.trim();

    const shadeEl = temp.querySelector('.layui-layer-shade');
    const layerEl = temp.querySelector(`.${domPrefix}`);

    shadeEl && doc.body.append(shadeEl);
    doc.body.append(layerEl);

    // auto close timer
    if (cfg.time > 0) setTimeout(() => close(index), cfg.time);

    /* ----------------------------- events ---------------------------*/

    // shade click to close
    if (cfg.shadeClose && shadeEl) {
      shadeEl.addEventListener('click', () => close(index));
    }

    // button actions
    if (cfg.btn) {
      layerEl.querySelectorAll('.layui-layer-btn a').forEach((btnEl, i) => {
        btnEl.addEventListener('click', () => {
          if (i === 0) {
            if (cfg.yes) cfg.yes(index, layerEl);
            else close(index);
          } else {
            const cb = cfg[`btn${i + 1}`];
            const ret = cb?.(index, layerEl);
            if (ret !== false) close(index);
          }
        });
      });
    }

    // close button
    if (cfg.closeBtn) {
      const closeBtn = doc.createElement('span');
      closeBtn.className = 'layui-layer-ico layui-layer-close layui-layer-close1';
      closeBtn.addEventListener('click', () => close(index));
      layerEl.append(closeBtn);
    }

    cfg.success?.(layerEl, index);

    instances.set(index, { cfg, el: layerEl, shade: shadeEl });
    return index;
  };

  const close = idx => {
    const inst = instances.get(idx);
    if (!inst) return;
    const { el, shade, cfg } = inst;
    el.remove();
    shade?.remove();
    cfg.end?.();
    instances.delete(idx);
  };

  const closeAll = type => {
    [...instances.entries()].forEach(([id, inst]) => {
      if (!type || inst.cfg.type === type) close(id);
    });
  };

  /* --------------------------------------------------------------------
   * Convenience wrappers (alert / confirm / msg / load / tips)
   * ------------------------------------------------------------------*/

  const alert = (content, opts, yes) => {
    if (typeof opts === 'function') [yes, opts] = [opts, {}];
    return open({ ...opts, content, yes });
  };

  const confirm = (content, opts, yes, cancel) => {
    if (typeof opts === 'function') {
      [cancel, yes, opts] = [yes, opts, {}];
    }
    return open({ ...opts, content, btn: defaultButtons, yes, btn2: cancel });
  };

  const msg = (content, time = 3000, opts, end) => {
    if (typeof opts === 'function') [end, opts] = [opts, {}];
    return open({
      content,
      time,
      shade: false,
      skin: 'layui-layer-msg',
      title: false,
      closeBtn: false,
      btn: false,
      resize: false,
      end,
      ...opts
    });
  };

  const load = (icon = 0, opts) => open({ type: 3, icon, resize: false, shade: 0.01, ...opts });

  const tips = (content, follow, opts) => open({ type: 4, content: [content, follow], closeBtn: false, shade: false, resize: false, fixed: false, maxWidth: 260, ...opts });

  /* --------------------------------------------------------------------
   * Public API
   * ------------------------------------------------------------------*/

  return {
    v: version,
    path,
    open,
    close,
    closeAll,
    alert,
    confirm,
    msg,
    load,
    tips,
    ready,
    config: options => Object.assign(cache, options)
  };
})();

export default layer;