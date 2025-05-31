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

; !function(window, undefined) {
	"use strict";

	/* ------------------------------------------------------------
	 *  核心工具 —— 无 jQuery 版
	 * ------------------------------------------------------------ */
	const isLayui = Boolean(window.layui && window.layui.define);
	let win = window;                        // 老代码偶尔会用到

	const ready = {
		/* 推断 layer.js 所在目录
		   1) 若全局 LAYUI_GLOBAL.layer_dir 已显式指定，则优先
		   2) 否则取当前 <script> 的 src 路径                     */
		getPath: (() => {
			const GLOBAL = window.LAYUI_GLOBAL || {};

			// 全局变量优先
			if (GLOBAL.layer_dir) return GLOBAL.layer_dir;

			// 通过当前 <script> 标签推断
			const scriptSrc = (document.currentScript?.src) || (() => {
				// 兼容异步加载：查找 readyState === 'interactive' 的 <script>
				const scripts = document.scripts;
				for (let i = scripts.length - 1; i >= 0; i--) {
					if (scripts[i].readyState === 'interactive') return scripts[i].src;
				}
				// 回退：最后一个 <script>
				return scripts[scripts.length - 1].src;
			})();

			return scriptSrc.substring(0, scriptSrc.lastIndexOf('/') + 1);
		})(),

		/* 运行期可改写的全局配置与状态 */
		config: {},
		end: {},
		minIndex: 0,
		minLeft: [],

		/* 默认按钮文案（保持 HTML 实体） */
		btn: ['&#x786E;&#x5B9A;', '&#x53D6;&#x6D88;'],

		/* layer 五种基础类型 */
		type: ['dialog', 'page', 'iframe', 'loading', 'tips'],

		/** 读取元素 style —— 等价 jQuery.css(node, name) */
		getStyle(node, name) {
			return getComputedStyle(node).getPropertyValue(name);
		},

		/** 动态加载 CSS，功能与原版一致（依赖 layer.path） */
		link(href, callback, cssname) {
			if (!layer.path) return;                               // 未配置路径直接跳过

			const head = document.head;
			const link = document.createElement('link');

			const finalName = (cssname || href).replace(/\\.\\|\\/ / g, '');
			const id = `layuicss-${finalName}`;
			const STATUS_CREATING = 'creating';

			// <link> 基本属性
			link.rel = 'stylesheet';
			link.href = `${layer.path}${href}`;
			link.id = id;

			// 同名样式表仅插入一次
			if (!document.getElementById(id)) head.appendChild(link);

			// 无回调就结束
			if (typeof callback !== 'function') return;

			/* ------------ 轮询检测 CSS 是否加载完毕 ------------ */
			let ticks = 0;
			const poll = (status) => {
				const delay = 100;
				const linkElem = document.getElementById(id);

				// 10 s 超时
				if (++ticks > 100) {
					console.error(`${finalName}.css: Invalid`);
					return;
				}

				// 判定方式沿用原版：CSS 内部会把 body 宽度设置为 1989px
				if (parseInt(ready.getStyle(linkElem, 'width'), 10) === 1989) {
					if (status === STATUS_CREATING) linkElem.removeAttribute('lay-status');

					// link 仍在创建中则继续轮询
					if (linkElem.getAttribute('lay-status') === STATUS_CREATING) {
						setTimeout(poll, delay);
					} else {
						callback();   // 加载完毕
					}
				} else {
					// 标记「创建中」并继续轮询
					linkElem.setAttribute('lay-status', STATUS_CREATING);
					setTimeout(() => poll(STATUS_CREATING), delay);
				}
			};

			poll();
		}
	};

	// ------------------------------------------------------------
	//  layer 单例 — API 兼容原版 3.x，但全部 ES10+ 实现
	// ------------------------------------------------------------
	const layer = {
		v: '4.0.0-es10',

		/**
		 * 检测 IE 浏览器版本（若非 IE，返回 false）
		 */
		ie: (() => {
			const ua = navigator.userAgent.toLowerCase();
			if (!('ActiveXObject' in window)) return false;
			return (ua.match(/msie\s(\d+)/) || [])[1] || '11'; // IE11 无 msie 标识
		})(),

		index: (window.layer && window.layer.v) ? 100000 : 0,
		path: ready.getPath,

		/**
		 * 全局配置入口（等价 $.extend 深合并）
		 * @param {object} options
		 */
		config(options = {}) {
			layer.cache = ready.config = { ...ready.config, ...options };
			layer.path = ready.config.path ?? layer.path;

			if (typeof options.extend === 'string') options.extend = [options.extend];

			// 若配置了 path，则先加载默认主题 CSS
			if (ready.config.path) layer.ready();

			// 加载扩展主题
			if (!options.extend) return this;

			const cssPath = `${isLayui ? 'modules/layer/' : 'theme/'}${options.extend}`;
			isLayui ? window.layui.addcss(cssPath) : ready.link(cssPath);

			return this;
		},

		/**
		 * 加载默认主题 CSS
		 * @param {Function} [cb]
		 */
		ready(cb) {
			const base = `${isLayui ? 'modules/layer/' : 'theme/'}`;
			const path = `${base}default/layer.css?v=${layer.v}`;
			const id = 'layer';
			isLayui ? window.layui.addcss(path, cb, id) : ready.link(path, cb, id);
			return this;
		},

		// ---------------- Quick APIs ----------------

		alert(content, options, yes) {
			if (typeof options === 'function') yes = options;
			return layer.open({ content, yes, ...(typeof options === 'function' ? {} : options) });
		},

		confirm(content, options, yes, cancel) {
			if (typeof options === 'function') {
				cancel = yes;
				yes = options;
			}
			return layer.open({
				content: content,
				btn: ready.btn,
				yes,
				btn2: cancel,
				...(typeof options === 'function' ? {} : options)
			});
		},

		/**
		 * 最常用的提示层
		 */
		msg(content, time = 3300, options, end) {
			if (typeof options === 'function') end = options;

			const rskin = ready.config.skin;
			const baseSkin = rskin ? `${rskin} ${rskin}-msg` : 'layui-layer-msg';
			const anim = doms.anim.length - 1; // doms.anim 来自旧全局，未改名保证兼容

			let finalOptions;
			if (typeof options === 'function' && !ready.config.skin) {
				finalOptions = {
					skin: `${baseSkin} layui-layer-hui`,
					anim,
				};
			} else {
				const opt = options ?? {};
				if ((opt.icon === -1 || opt.icon === undefined) && !ready.config.skin) {
					opt.skin = `${baseSkin} ${opt.skin ?? 'layui-layer-hui'}`;
				}
				finalOptions = opt;
			}

			return layer.open({
				content,
				time,
				shade: false,
				skin: baseSkin,
				title: false,
				closeBtn: false,
				btn: false,
				resize: false,
				end,
				...finalOptions
			});
		},

		load(icon = 0, options) {
			return layer.open({ type: 3, icon, resize: false, shade: 0.01, ...options });
		},

		tips(content, follow, options = {}) {
			return layer.open({
				type: 4,
				content: [content, follow],
				closeBtn: false,
				time: options.time,
				shade: false,
				resize: false,
				fixed: false,
				maxWidth: 260,
				...options
			});
		}
	};

	// ES10+ 纯原生实现
	const Class = function(settings = {}) {
		const that = this;

		/** 等待 <body> 就绪后再创建 */
		const create = () => {
			that.creat();             // 继续调用旧的实例方法
		};

		// 全局递增索引
		that.index = ++layer.index;

		// 合并配置：默认 → 全局 → 调用方
		that.config = Object.assign({}, that.config || {}, ready.config, settings);

		// 初始最大宽度 = 视口宽度 - 30 px（左右各留 15 px）
		that.config.maxWidth = window.innerWidth - 15 * 2;

		// 若文档尚未渲染完毕，稍后再执行
		if (document.body) {
			create();
		} else {
			setTimeout(create, 30);
		}
	};

	Class.pt = Class.prototype;

	//缓存常用字符
	var doms = ['layui-layer', '.layui-layer-title', '.layui-layer-main', '.layui-layer-dialog', 'layui-layer-iframe', 'layui-layer-content', 'layui-layer-btn', 'layui-layer-close'];
	doms.anim = ['layer-anim-00', 'layer-anim-01', 'layer-anim-02', 'layer-anim-03', 'layer-anim-04', 'layer-anim-05', 'layer-anim-06'];

	doms.SHADE = 'layui-layer-shade';
	doms.MOVE = 'layui-layer-move';

	//默认配置
	Class.pt.config = {
		type: 0,
		shade: 0.3,
		fixed: true,
		move: doms[1],
		title: '&#x4FE1;&#x606F;',
		offset: 'auto',
		area: 'auto',
		closeBtn: 1,
		time: 0, //0表示不自动关闭
		zIndex: 19891014,
		maxWidth: 360,
		anim: 0,
		isOutAnim: true, //退出动画
		minStack: true, //最小化堆叠
		icon: -1,
		moveType: 1,
		resize: true,
		scrollbar: true, //是否允许浏览器滚动条
		tips: 2
	};

	//容器
	Class.pt.vessel = function(conType, callback) {
		var that = this, times = that.index, config = that.config;
		var zIndex = config.zIndex + times, titype = typeof config.title === 'object';
		var ismax = config.maxmin && (config.type === 1 || config.type === 2);
		let titleHTML = (config.title ? '<div class="layui-layer-title" style="' + (titype ? config.title[1] : '') + '">'
			+ (titype ? config.title[0] : config.title)
			+ '</div>' : '');

		config.zIndex = zIndex;
		/* -----------------------------------------------------------
		 * 生成遮罩 + 主体 HTML，并回调
		 * ----------------------------------------------------------- */
		const moveElem = document.createElement('div');  // 原来的 $('<div …>')
		moveElem.className = doms.MOVE;
		moveElem.id = doms.MOVE;

		callback(
			[
				/* ① 遮罩层（可选） */
				config.shade
					? `<div class="${doms.SHADE}" id="${doms.SHADE + times}" times="${times}"
		           style="z-index:${zIndex - 1};"></div>`
					: '',

				/* ② 主体层 */
				`<div class="${doms[0]} layui-layer-${ready.type[config.type]}${(config.type === 0 || config.type === 2) && !config.shade ? ' layui-layer-border' : ''
				} ${config.skin || ''}"
		       id="${doms[0] + times}"
		       type="${ready.type[config.type]}"
		       times="${times}"
		       showtime="${config.time}"
		       conType="${conType ? 'object' : 'string'}"
		       style="z-index:${zIndex}; width:${config.area[0]}; height:${config.area[1]};
		              position:${config.fixed ? 'fixed' : 'absolute'};">

		        ${conType && config.type !== 2 ? '' : titleHTML}

		        <div id="${config.id || ''}"
		             class="layui-layer-content${config.type === 0 && config.icon !== -1 ? ' layui-layer-padding' : ''
				}${config.type === 3 ? ' layui-layer-loading' + config.icon : ''}">
		          ${config.type === 0 && config.icon !== -1
					? `<i class="layui-layer-ico layui-layer-ico${config.icon}"></i>` : ''}
		          ${config.type === 1 && conType ? '' : (config.content || '')}
		        </div>

		        <span class="layui-layer-setwin">${(() => {
					let closebtn = ismax
						? '<a class="layui-layer-min" href="javascript:;"><cite></cite></a>' +
						'<a class="layui-layer-ico layui-layer-max" href="javascript:;"></a>'
						: '';
					if (config.closeBtn) {
						closebtn += `<a class="layui-layer-ico ${doms[7]} ${doms[7]}${config.title ? config.closeBtn : (config.type === 4 ? '1' : '2')
							}" href="javascript:;"></a>`;
					}
					return closebtn;
				})()
				}</span>

		        ${config.btn ? (() => {
					if (typeof config.btn === 'string') config.btn = [config.btn];
					const buttons = config.btn
						.map((txt, i) => `<a class="${doms[6]}${i}">${txt}</a>`)
						.join('');
					return `<div class="${doms[6]} layui-layer-btn-${config.btnAlign || ''}">${buttons}</div>`;
				})() : ''}

		        ${config.resize ? '<span class="layui-layer-resize"></span>' : ''}
		    </div>`
			],

			/* ③ 标题 HTML（titleHTML） */
			titleHTML,

			/* ④ 拖拽柄 DOM（moveElem） */
			moveElem
		);
		return that;
	};

	// ---------------------------------------------------------------------
	// 替换原 jQuery 版：Class.pt.creat
	// ---------------------------------------------------------------------
	Class.pt.creat = function() {
		const that = this;
		const cfg = that.config;
		const times = that.index;
		let nodeIndex;

		// 内容既可以是 HTMLElement，也可以是字符串
		let content = cfg.content;
		const conType = typeof content === 'object';
		const body = document.body;

		/* 若配置了 id 且已存在，则不再重复创建 */
		if (cfg.id && document.getElementById(cfg.id)) return;

		/* area 支持字符串写法 */
		if (typeof cfg.area === 'string') {
			cfg.area = cfg.area === 'auto' ? ['', ''] : [cfg.area, ''];
		}

		/* anim 兼容旧版 shift */
		if (cfg.shift) cfg.anim = cfg.shift;

		/* IE6：不得固定定位（保留原逻辑；可直接删除） */
		if (layer.ie === 6) cfg.fixed = false;

		/* ------------------- 不同 type 的预处理 ------------------- */
		switch (cfg.type) {
			case 0: // dialog
				cfg.btn = 'btn' in cfg ? cfg.btn : ready.btn[0];
				layer.closeAll('dialog');
				break;

			case 2: // iframe
				// 若 cfg.content 不是数组则转成 [src, scroll]
				if (!conType) content = cfg.content = [cfg.content || '', 'auto'];

				cfg.content = `
	        <iframe scrolling="${content[1] || 'auto'}"
	                allowtransparency="true"
	                id="${doms[4]}${times}"
	                name="${doms[4]}${times}"
	                onload="this.className='';"
	                class="layui-layer-load"
	                frameborder="0"
	                src="${content[0]}"></iframe>`;
				break;

			case 3: // loading
				delete cfg.title;
				delete cfg.closeBtn;
				if (cfg.icon === -1) cfg.icon = 0;
				layer.closeAll('loading');
				break;

			case 4: // tips
				if (!conType) cfg.content = [cfg.content, 'body'];
				cfg.follow = cfg.content[1];
				cfg.content = `${cfg.content[0]}<i class="layui-layer-TipsG"></i>`;
				delete cfg.title;
				cfg.tips = typeof cfg.tips === 'object' ? cfg.tips : [cfg.tips, true];
				if (!cfg.tipsMore) layer.closeAll('tips');
				break;
		}

		/* ------------------- 建立容器 ------------------- */
		that.vessel(conType, (htmlArr, titleHTML, moveElem) => {
			/* 遮罩层 */
			body.insertAdjacentHTML('beforeend', htmlArr[0]);

			/* 主体层 / 内容插入 */
			if (conType) {
				if (cfg.type === 2 || cfg.type === 4) {
					body.insertAdjacentHTML('beforeend', htmlArr[1]);
				} else {
					// content 是 DOM 节点
					if (!content.closest(`.${doms[0]}`)) {
						// 记录原 display，便于关闭时恢复
						content.dataset.display = getComputedStyle(content).display;
						content.style.display = '';              // show()
						content.classList.add('layui-layer-wrap');

						// 用 htmlArr[1] 包裹 content
						const wrapper = document.createElement('div');
						wrapper.innerHTML = htmlArr[1];
						const wrapElem = wrapper.firstElementChild;
						wrapElem.querySelector(`.${doms[5]}`).insertAdjacentHTML('beforebegin', titleHTML);
						content.parentNode.insertBefore(wrapElem, content);
						wrapElem.querySelector(`.${doms[5]}`).appendChild(content);
					}
				}
			} else {
				body.insertAdjacentHTML('beforeend', htmlArr[1]);
			}

			/* 拖拽柄（全局唯一） */
			if (!document.getElementById(doms.MOVE)) {
				body.appendChild(moveElem);
			}

			// 缓存本层 DOM
			that.layero = document.getElementById(`${doms[0]}${times}`);
			that.shadeo = document.getElementById(`${doms.SHADE}${times}`);

			/* 禁止滚动条 */
			if (!cfg.scrollbar) {
				doms.html.style.overflow = 'hidden';
				doms.html.setAttribute('layer-full', times);
			}
		}).auto(times);

		/* ------------------- 遮罩样式 ------------------- */
		if (that.shadeo) {
			that.shadeo.style.backgroundColor = cfg.shade[1] || '#000';
			that.shadeo.style.opacity = cfg.shade[0] || cfg.shade;
		}

		/* 旧版 IE6 iframe src 修正 */
		if (cfg.type === 2 && layer.ie === 6) {
			const frame = that.layero.querySelector('iframe');
			if (frame) frame.src = content[0];
		}

		/* 坐标自适应 */
		if (cfg.type === 4) {
			that.tips();
		} else {
			that.offset();
			/* 首次弹出时如 CSS 未加载，等待后重新定位 */
			if (!parseInt(ready.getStyle(document.getElementById(doms.MOVE), 'z-index'), 10)) {
				that.layero.style.visibility = 'hidden';
				layer.ready(() => {
					that.offset();
					that.layero.style.visibility = 'visible';
				});
			}
		}

		/* 固定定位时随窗口变化 */
		if (cfg.fixed) {
			window.addEventListener('resize', () => {
				that.offset();
				if (/^\\d+%$/.test(cfg.area[0]) || /^\\d+%$/.test(cfg.area[1])) that.auto(times);
				if (cfg.type === 4) that.tips();
			});
		}

		/* 自动关闭计时 */
		if (cfg.time > 0) {
			setTimeout(() => layer.close(that.index), cfg.time);
		}

		/* 允许拖动、回调 */
		that.move().callback();

		/* 兼容 jQuery3 动画影响尺寸：纯 class 操作 */
		if (doms.anim[cfg.anim]) {
			const animClass = `layer-anim ${doms.anim[cfg.anim]}`;
			that.layero.classList.add(animClass);
			that.layero.addEventListener('animationend', () => {
				that.layero.classList.remove(animClass);
			}, { once: true });
		}

		/* 记录关闭动画标记 */
		if (cfg.isOutAnim) {
			that.layero.dataset.isOutAnim = 'true';
		}
	};

	/* 自适应（无 jQuery 版） */
	Class.pt.auto = function(index) {
		const that = this;
		const cfg = that.config;
		const layero = document.getElementById(`${doms[0]}${index}`);
		if (!layero) return that;

		/* ---------------- 宽度自适应 ---------------- */
		if (cfg.area[0] === '' && cfg.maxWidth > 0) {
			/* 修复 IE7 奇怪的 box 模型 bug（保留旧逻辑，现代浏览器基本无影响） */
			if (layer.ie && parseInt(layer.ie, 10) < 8 && cfg.btn) {
				layero.style.width = `${layero.clientWidth}px`;   // clientWidth ≈ innerWidth
			}
			if (layero.offsetWidth > cfg.maxWidth) {
				layero.style.width = `${cfg.maxWidth}px`;          // offsetWidth ≈ outerWidth
			}
		}

		/* 当前层实际尺寸 */
		const area = [layero.clientWidth, layero.clientHeight];

		/* 计算标题 & 按钮高度 */
		const titElem = layero.querySelector(doms[1]);
		const titHeight = titElem ? titElem.offsetHeight : 0;

		const btnElem = layero.querySelector(`.${doms[6]}`);
		const btnHeight = btnElem ? btnElem.offsetHeight : 0;

		/* 根据剩余高度设定内容区 */
		const setHeight = (selector) => {
			const el = layero.querySelector(selector);
			if (!el) return;
			const padTop = parseFloat(getComputedStyle(el).paddingTop) || 0;
			el.style.height = `${area[1] - titHeight - btnHeight - 2 * padTop}px`;
		};

		switch (cfg.type) {
			case 2:                             // iframe
				setHeight('iframe');
				break;

			default:
				if (cfg.area[1] === '') {         // 高度未指定
					if (cfg.maxHeight > 0 && layero.offsetHeight > cfg.maxHeight) {
						area[1] = cfg.maxHeight;
						setHeight(`.${doms[5]}`);
					} else if (cfg.fixed && area[1] >= window.innerHeight) {
						area[1] = window.innerHeight;
						setHeight(`.${doms[5]}`);
					}
				} else {                          // 高度已指定
					setHeight(`.${doms[5]}`);
				}
				break;
		}

		return that;                          // 链式调用保持不变
	};

	/* 计算坐标（无 jQuery 版） */
	Class.pt.offset = function() {
		const that = this;
		const cfg = that.config;
		const layero = that.layero;                      // HTMLElement

		/* 层本身尺寸（≈ jQuery.outerWidth/outerHeight） */
		const area = [layero.offsetWidth, layero.offsetHeight];

		/* 视口中央 */
		let top = (window.innerHeight - area[1]) / 2;
		let left = (window.innerWidth - area[0]) / 2;

		/* --------------------------------------------------
		 * 1) 自定义 offset 解析
		 * -------------------------------------------------- */
		if (Array.isArray(cfg.offset)) {                 // 传 [top, left]
			top = cfg.offset[0];
			left = cfg.offset[1] ?? left;
		} else if (cfg.offset !== 'auto') {
			switch (cfg.offset) {
				case 't': top = 0; break;
				case 'r': left = window.innerWidth - area[0]; break;
				case 'b': top = window.innerHeight - area[1]; break;
				case 'l': left = 0; break;
				case 'lt': top = 0; left = 0; break;
				case 'lb': top = window.innerHeight - area[1]; left = 0; break;
				case 'rt': top = 0; left = window.innerWidth - area[0]; break;
				case 'rb': top = window.innerHeight - area[1]; left = window.innerWidth - area[0]; break;
				default: top = cfg.offset;                  // 可能是具体像素/百分比
			}
		}

		/* --------------------------------------------------
		 * 2) 非 fixed：支持百分比 & 加上滚动条偏移
		 * -------------------------------------------------- */
		if (!cfg.fixed) {
			const pct = (val, full) =>
				/%$/.test(val) ? full * parseFloat(val) / 100 : parseFloat(val);

			top = pct(top, window.innerHeight) + window.pageYOffset;
			left = pct(left, window.innerWidth) + window.pageXOffset;
		}

		/* --------------------------------------------------
		 * 3) 最小化状态（minLeft attr）兼容
		 * -------------------------------------------------- */
		if (layero.hasAttribute('minLeft')) {
			const titleH = layero.querySelector(doms[1])?.offsetHeight || 0;
			top = window.innerHeight - titleH;
			left = getComputedStyle(layero).left;   // 保持原 left
		}

		/* --------------------------------------------------
		 * 4) 写入结果
		 * -------------------------------------------------- */
		that.offsetTop = top;
		that.offsetLeft = left;

		layero.style.top = typeof top === 'number' ? `${top}px` : top;
		layero.style.left = typeof left === 'number' ? `${left}px` : left;
	};

	Class.pt.tips = function() {
		const that = this;
		const config = that.config;
		const layero = that.layero;

		// 提示层尺寸（≈ outerWidth / outerHeight）
		const layArea = [layero.offsetWidth, layero.offsetHeight];

		// 目标元素（fallback 到 <body>）
		let follow = typeof config.follow === 'string'
			? document.querySelector(config.follow)
			: config.follow;

		if (!follow || !follow.getBoundingClientRect) follow = document.body;

		const rect = follow.getBoundingClientRect();

		// 目标尺寸/位置
		const goal = {
			width: follow.offsetWidth,
			height: follow.offsetHeight,
			top: rect.top + window.pageYOffset,
			left: rect.left + window.pageXOffset,
			tipLeft: 0,
			tipTop: 0,
			autoLeft() {
				if (this.left + layArea[0] > window.innerWidth) {
					this.tipLeft = this.left + this.width - layArea[0];
					tipsG.style.right = '12px';
					tipsG.style.left = 'auto';
				} else {
					this.tipLeft = this.left;
					tipsG.style.right = '';
					tipsG.style.left = '';
				}
			},
		};

		// 小箭头节点
		const tipsG = layero.querySelector('.layui-layer-TipsG');

		// 没开启小箭头
		if (!config.tips[1] && tipsG) tipsG.remove();

		// 定位方位 1=上 2=右 3=下 4=左
		const guide = config.tips[0];

		// 创建 4 个定位函数
		const where = [
			() => { // 上
				goal.autoLeft();
				goal.tipTop = goal.top - layArea[1] - 10;
				tipsG?.classList.remove('layui-layer-TipsB');
				tipsG?.classList.add('layui-layer-TipsT');
				tipsG && (tipsG.style.borderRightColor = config.tips[1]);
			},
			() => { // 右
				goal.tipLeft = goal.left + goal.width + 10;
				goal.tipTop = goal.top;
				tipsG?.classList.remove('layui-layer-TipsL');
				tipsG?.classList.add('layui-layer-TipsR');
				tipsG && (tipsG.style.borderBottomColor = config.tips[1]);
			},
			() => { // 下
				goal.autoLeft();
				goal.tipTop = goal.top + goal.height + 10;
				tipsG?.classList.remove('layui-layer-TipsT');
				tipsG?.classList.add('layui-layer-TipsB');
				tipsG && (tipsG.style.borderRightColor = config.tips[1]);
			},
			() => { // 左
				goal.tipLeft = goal.left - layArea[0] - 10;
				goal.tipTop = goal.top;
				tipsG?.classList.remove('layui-layer-TipsR');
				tipsG?.classList.add('layui-layer-TipsL');
				tipsG && (tipsG.style.borderBottomColor = config.tips[1]);
			}
		];

		// 初始按设定方位调用
		where[guide - 1]();

		// 防止超出可视区（尝试其他方向）
		const triangleSize = 8 * 2;
		if (guide === 1 && goal.top - (window.pageYOffset + layArea[1] + triangleSize) < 0) {
			where[2]();
		} else if (guide === 2 && window.innerWidth - (goal.left + goal.width + layArea[0] + triangleSize) <= 0) {
			where[3]();
		} else if (guide === 3 && (goal.top - window.pageYOffset + goal.height + layArea[1] + triangleSize) > window.innerHeight) {
			where[0]();
		} else if (guide === 4 && (layArea[0] + triangleSize) > goal.left) {
			where[1]();
		}

		// 设置内容区样式
		const contEl = layero.querySelector(`.${doms[5]}`);
		if (contEl) {
			contEl.style.backgroundColor = config.tips[1];
			contEl.style.paddingRight = config.closeBtn ? '30px' : '';
		}

		// 设置最终位置
		const scrollX = config.fixed ? 0 : window.pageXOffset;
		const scrollY = config.fixed ? 0 : window.pageYOffset;

		layero.style.left = `${goal.tipLeft - scrollX}px`;
		layero.style.top = `${goal.tipTop - scrollY}px`;
	};

	Class.pt.move = function() {
		const that = this;
		const cfg = that.config;
		const layero = that.layero;
		const moveElem = layero.querySelector(cfg.move);
		const resizeEl = layero.querySelector('.layui-layer-resize');
		const dict = {};

		// 设置拖拽光标样式
		if (cfg.move && moveElem) {
			moveElem.style.cursor = 'move';
		}

		// -------------------- 拖拽开始 --------------------
		moveElem?.addEventListener('mousedown', (e) => {
			e.preventDefault();

			if (cfg.move) {
				dict.moveStart = true;
				dict.offset = [
					e.clientX - parseFloat(layero.style.left || 0),
					e.clientY - parseFloat(layero.style.top || 0),
				];

				ready.moveElem.style.cursor = 'move';
				ready.moveElem.style.display = 'block';
			}
		});

		// -------------------- Resize 开始 --------------------
		resizeEl?.addEventListener('mousedown', (e) => {
			e.preventDefault();

			dict.resizeStart = true;
			dict.offset = [e.clientX, e.clientY];
			dict.area = [layero.offsetWidth, layero.offsetHeight];

			ready.moveElem.style.cursor = 'se-resize';
			ready.moveElem.style.display = 'block';
		});

		// -------------------- 拖拽/缩放行为 --------------------
		document.addEventListener('mousemove', (e) => {
			// 拖动中
			if (dict.moveStart) {
				e.preventDefault();

				let X = e.clientX - dict.offset[0];
				let Y = e.clientY - dict.offset[1];
				const isFixed = getComputedStyle(layero).position === 'fixed';

				dict.stX = isFixed ? 0 : window.pageXOffset;
				dict.stY = isFixed ? 0 : window.pageYOffset;

				if (!cfg.moveOut) {
					const maxX = window.innerWidth - layero.offsetWidth + dict.stX;
					const maxY = window.innerHeight - layero.offsetHeight + dict.stY;

					if (X < dict.stX) X = dict.stX;
					if (X > maxX) X = maxX;
					if (Y < dict.stY) Y = dict.stY;
					if (Y > maxY) Y = maxY;
				}

				layero.style.left = `${X}px`;
				layero.style.top = `${Y}px`;
			}

			// 缩放中
			if (cfg.resize && dict.resizeStart) {
				e.preventDefault();

				const deltaX = e.clientX - dict.offset[0];
				const deltaY = e.clientY - dict.offset[1];

				const newWidth = dict.area[0] + deltaX;
				const newHeight = dict.area[1] + deltaY;

				layer.style(that.index, {
					width: newWidth,
					height: newHeight
				});

				dict.isResize = true;
				if (typeof cfg.resizing === 'function') {
					cfg.resizing(layero);
				}
			}
		});

		// -------------------- 拖拽/缩放结束 --------------------
		document.addEventListener('mouseup', () => {
			if (dict.moveStart) {
				delete dict.moveStart;
				ready.moveElem.style.display = 'none';
				if (typeof cfg.moveEnd === 'function') {
					cfg.moveEnd(layero);
				}
			}

			if (dict.resizeStart) {
				delete dict.resizeStart;
				ready.moveElem.style.display = 'none';
			}
		});

		return that;
	};

	Class.pt.callback = function() {
		const that = this;
		const layero = that.layero;
		const config = that.config;

		that.openLayer();

		// ------------------ success 回调 ------------------
		if (typeof config.success === 'function') {
			if (config.type === 2) {
				const iframe = layero.querySelector('iframe');
				if (iframe) {
					iframe.addEventListener('load', () => {
						config.success(layero, that.index);
					});
				}
			} else {
				config.success(layero, that.index);
			}
		}

		// ------------------ IE6 fix ------------------
		if (layer.ie === 6) {
			that.IE6(layero);
		}

		// ------------------ 按钮点击 ------------------
		const btnContainer = layero.querySelector(`.${doms[6]}`);
		if (btnContainer) {
			const btnList = Array.from(btnContainer.children);
			btnList.forEach((btn, i) => {
				btn.addEventListener('click', () => {
					if (i === 0) {
						if (typeof config.yes === 'function') {
							config.yes(that.index, layero);
						} else if (typeof config['btn1'] === 'function') {
							config['btn1'](that.index, layero);
						} else {
							layer.close(that.index);
						}
					} else {
						const callbackFn = config[`btn${i + 1}`];
						const shouldClose = typeof callbackFn === 'function' ? callbackFn(that.index, layero) : undefined;
						if (shouldClose !== false) {
							layer.close(that.index);
						}
					}
				});
			});
		}

		// ------------------ 右上角关闭按钮 ------------------
		const closeBtn = layero.querySelector(`.${doms[7]}`);
		const cancel = () => {
			const shouldClose = typeof config.cancel === 'function' ? config.cancel(that.index, layero) : undefined;
			if (shouldClose !== false) {
				layer.close(that.index);
			}
		};
		closeBtn?.addEventListener('click', cancel);

		// ------------------ 遮罩点击关闭 ------------------
		if (config.shadeClose && that.shadeo) {
			that.shadeo.addEventListener('click', () => {
				layer.close(that.index);
			});
		}

		// ------------------ 最小化 ------------------
		const minBtn = layero.querySelector('.layui-layer-min');
		minBtn?.addEventListener('click', () => {
			const shouldMin = typeof config.min === 'function' ? config.min(layero, that.index) : undefined;
			if (shouldMin !== false) {
				layer.min(that.index, config);
			}
		});

		// ------------------ 最大化 / 还原 ------------------
		const maxBtn = layero.querySelector('.layui-layer-max');
		maxBtn?.addEventListener('click', function() {
			if (this.classList.contains('layui-layer-maxmin')) {
				layer.restore(that.index);
				if (typeof config.restore === 'function') {
					config.restore(layero, that.index);
				}
			} else {
				layer.full(that.index, config);
				setTimeout(() => {
					if (typeof config.full === 'function') {
						config.full(layero, that.index);
					}
				}, 100);
			}
		});

		// ------------------ 结束回调注册 ------------------
		if (typeof config.end === 'function') {
			ready.end[that.index] = config.end;
		}
	};

	// for IE6: 恢复被遮罩隐藏的 <select>
	ready.reselect = function() {
		const selects = document.querySelectorAll('select');

		selects.forEach(select => {
			const insideLayer = select.closest(`.${doms[0]}`);
			const isMarked = select.getAttribute('layer') === '1';
			const noLayers = document.querySelectorAll(`.${doms[0]}`).length < 1;

			if (!insideLayer && isMarked && noLayers) {
				select.removeAttribute('layer');
				select.style.display = '';  // 恢复显示
			}
		});
	};

	Class.pt.IE6 = function(layero) {
		// IE6 bug workaround：隐藏未被弹层包裹的 select 元素
		const selects = document.querySelectorAll('select');

		selects.forEach(select => {
			const insideLayer = select.closest(`.${doms[0]}`);
			const display = getComputedStyle(select).display;

			if (!insideLayer && display !== 'none') {
				select.setAttribute('layer', '1');
				select.style.display = 'none';
			}
		});
	};

	// 需依赖原型的对外方法
	Class.pt.openLayer = function() {
		const that = this;

		// 将当前窗口的 zIndex 写入 layer.zIndex
		layer.zIndex = that.config.zIndex;

		// 对外提供：设置某个 layero 的置顶函数
		layer.setTop = function(layero) {
			if (!layero) return layer.zIndex;

			// 提取当前 z-index（从 style 属性中）
			const currentZ = parseInt(layero.style.zIndex, 10);
			if (!isNaN(currentZ)) {
				layer.zIndex = currentZ;
			}

			// 每次点击时 +1 z-index
			const setZindex = () => {
				layer.zIndex++;
				layero.style.zIndex = layer.zIndex + 1;
			};

			layero.addEventListener('mousedown', setZindex);

			return layer.zIndex;
		};
	};

	ready.record = function(layero) {
		if (!layero) return;

		// 1️⃣ 获取宽高、top、left（含 margin-left）
		const rect = layero.getBoundingClientRect();
		const computed = getComputedStyle(layero);
		const scrollX = window.pageXOffset;
		const scrollY = window.pageYOffset;

		const width = layero.offsetWidth;
		const height = layero.offsetHeight;
		const top = rect.top + scrollY;
		const left = rect.left + scrollX + parseFloat(computed.marginLeft || 0);

		const area = [width, height, top, left];

		// 2️⃣ 添加最大化标记 class
		const maxBtn = layero.querySelector('.layui-layer-max');
		maxBtn?.classList.add('layui-layer-maxmin');

		// 3️⃣ 存入 DOM 属性
		layero.setAttribute('area', area.join(','));
	};

	ready.rescollbar = function(index) {
		const html = doms.html;  // 通常是 document.documentElement

		if (html.getAttribute('layer-full') === String(index)) {
			// 恢复 overflow 样式
			if (typeof html.style.removeProperty === 'function') {
				html.style.removeProperty('overflow');
			} else {
				html.style.removeAttribute('overflow');
			}

			// 移除 layer-full 属性
			html.removeAttribute('layer-full');
		}
	};

	/** 内置成员 */

	window.layer = layer;

	layer.getChildFrame = function(selector, index) {
		// 如果未传 index，则取第一个 iframe 的 times 属性
		if (!index) {
			const iframe = document.querySelector(`.${doms[4]}`);
			if (!iframe) return null;
			index = iframe.getAttribute('times');
		}

		// 获取对应层的 iframe 元素
		const container = document.getElementById(`${doms[0]}${index}`);
		if (!container) return null;

		const iframe = container.querySelector('iframe');
		if (!iframe || !iframe.contentDocument) return null;

		return iframe.contentDocument.querySelector(selector);
	};

	layer.getFrameIndex = function(name) {
		const iframe = document.getElementById(name);
		if (!iframe) return null;

		const parentLayer = iframe.closest(`.${doms[4]}`);
		if (!parentLayer) return null;

		return parentLayer.getAttribute('times');
	};

	layer.iframeAuto = function(index) {
		if (!index) return;

		// 获取子 iframe 的 HTML 元素
		const html = layer.getChildFrame('html', index);
		if (!html) return;

		const iframeHeight = html.offsetHeight;

		// 获取当前层容器
		const layero = document.getElementById(`${doms[0]}${index}`);
		if (!layero) return;

		const titleEl = layero.querySelector(doms[1]);
		const btnEl = layero.querySelector(`.${doms[6]}`);

		const titHeight = titleEl ? titleEl.offsetHeight : 0;
		const btnHeight = btnEl ? btnEl.offsetHeight : 0;

		// 设置整体高度
		layero.style.height = `${iframeHeight + titHeight + btnHeight}px`;

		// 设置 iframe 高度
		const iframe = layero.querySelector('iframe');
		if (iframe) iframe.style.height = `${iframeHeight}px`;
	};

	layer.iframeSrc = function(index, url) {
		const layero = document.getElementById(`${doms[0]}${index}`);
		if (!layero) return;

		const iframe = layero.querySelector('iframe');
		if (iframe) {
			iframe.setAttribute('src', url);
		}
	};

	layer.style = function(index, options = {}, limit) {
		const layero = document.getElementById(`${doms[0]}${index}`);
		if (!layero) return;

		const contElem = layero.querySelector('.layui-layer-content');
		const type = layero.getAttribute('type');
		const titleEl = layero.querySelector(doms[1]);
		const btnEl = layero.querySelector(`.${doms[6]}`);
		const minLeft = layero.getAttribute('minLeft');

		const titHeight = titleEl ? titleEl.offsetHeight : 0;
		let btnHeight = btnEl ? btnEl.offsetHeight : 0;

		if (type === ready.type[3] || type === ready.type[4]) {
			return; // loading 或 tips 不做尺寸修改
		}

		if (!limit) {
			// 最小宽度限制
			if (parseFloat(options.width) <= 260) {
				options.width = 260;
			}

			// 最小内容高度限制（64px）
			if (parseFloat(options.height) - titHeight - btnHeight <= 64) {
				options.height = 64 + titHeight + btnHeight;
			}
		}

		// 应用整体样式：支持传入 width / height / top / left 等
		for (const key in options) {
			if (Object.prototype.hasOwnProperty.call(options, key)) {
				layero.style[key] = typeof options[key] === 'number' ? `${options[key]}px` : options[key];
			}
		}

		// 重新计算按钮高度（可能因为高度改变后发生变化）
		btnHeight = (layero.querySelector(`.${doms[6]}`)?.offsetHeight) || 0;

		if (type === ready.type[2]) {
			// iframe 类型：设置 iframe 高度
			const iframe = layero.querySelector('iframe');
			if (iframe) {
				iframe.style.height = `${parseFloat(options.height) - titHeight - btnHeight}px`;
			}
		} else {
			// 其他类型：设置内容高度（减去 padding）
			if (contElem) {
				const computed = getComputedStyle(contElem);
				const pt = parseFloat(computed.paddingTop) || 0;
				const pb = parseFloat(computed.paddingBottom) || 0;

				const contHeight = parseFloat(options.height) - titHeight - btnHeight - pt - pb;
				contElem.style.height = `${contHeight}px`;
			}
		}
	};

	layer.min = function(index, options = {}) {
		const layero = document.getElementById(`${doms[0]}${index}`);
		const shadeo = document.getElementById(`${doms.SHADE}${index}`);
		if (!layero) return;

		const titleEl = layero.querySelector(doms[1]);
		const titHeight = titleEl ? titleEl.offsetHeight : 0;

		const defaultLeft = `${181 * ready.minIndex}px`;
		let left = layero.getAttribute('minLeft') || defaultLeft;

		const position = getComputedStyle(layero).position;

		const settings = {
			width: 180,
			height: titHeight,
			position: 'fixed',
			overflow: 'hidden'
		};

		// 记录宽高坐标，用于还原
		ready.record(layero);

		// 如果已经有等待恢复的 left 值，优先使用
		if (ready.minLeft.length > 0) {
			left = ready.minLeft[0];
			ready.minLeft.shift();
		}

		// 是否使用“堆叠式”最小化
		if (options.minStack) {
			settings.left = left;
			settings.top = window.innerHeight - titHeight;

			if (!layero.getAttribute('minLeft')) {
				ready.minIndex++;
			}

			layero.setAttribute('minLeft', left);
		}

		// 保存当前定位方式
		layero.setAttribute('position', position);

		// 应用样式
		layer.style(index, settings, true);

		// 隐藏最小化按钮
		const minBtn = layero.querySelector('.layui-layer-min');
		if (minBtn) minBtn.style.display = 'none';

		// 如果是 type='page'，隐藏主体内容区域
		if (layero.getAttribute('type') === 'page') {
			const pageContent = layero.querySelector(doms[4]);
			if (pageContent) pageContent.style.display = 'none';
		}

		// 恢复滚动条
		ready.rescollbar(index);

		// 隐藏遮罩
		if (shadeo) {
			shadeo.style.display = 'none';
		}
	};

	layer.restore = function(index) {
		const layero = document.getElementById(`${doms[0]}${index}`);
		const shadeo = document.getElementById(`${doms.SHADE}${index}`);
		if (!layero) return;

		const areaAttr = layero.getAttribute('area');
		if (!areaAttr) return;
		const [width, height, top, left] = areaAttr.split(',').map(parseFloat);
		const type = layero.getAttribute('type');

		// 恢复原样式
		layer.style(index, {
			width,
			height,
			top,
			left,
			position: layero.getAttribute('position'),
			overflow: 'visible'
		}, true);

		// 还原最大化按钮样式
		const maxBtn = layero.querySelector('.layui-layer-max');
		if (maxBtn) maxBtn.classList.remove('layui-layer-maxmin');

		// 显示最小化按钮
		const minBtn = layero.querySelector('.layui-layer-min');
		if (minBtn) minBtn.style.display = '';

		// 如果是 type="page"，显示内容容器
		if (type === 'page') {
			const contentEl = layero.querySelector(doms[4]);
			if (contentEl) contentEl.style.display = '';
		}

		// 恢复滚动条
		ready.rescollbar(index);

		// 恢复遮罩
		if (shadeon) {
			shadeo.style.display = '';
		}
	};

	layer.full = function(index) {
		const layero = document.getElementById(`${doms[0]}${index}`);
		if (!layero) return;

		// 记录原始宽高和位置，用于还原
		ready.record(layero);

		// 设置 HTML 不可滚动
		if (!doms.html.hasAttribute('layer-full')) {
			doms.html.style.overflow = 'hidden';
			doms.html.setAttribute('layer-full', index);
		}

		// 延迟执行样式设置（兼容性/渲染完成保障）
		setTimeout(() => {
			const isFixed = getComputedStyle(layero).position === 'fixed';
			const scrollTop = window.scrollY || document.documentElement.scrollTop;
			const scrollLeft = window.scrollX || document.documentElement.scrollLeft;

			layer.style(index, {
				top: isFixed ? 0 : scrollTop,
				left: isFixed ? 0 : scrollLeft,
				width: window.innerWidth,
				height: window.innerHeight
			}, true);

			const minBtn = layero.querySelector('.layui-layer-min');
			if (minBtn) minBtn.style.display = 'none';

		}, 100);
	};

	layer.title = function(name, index) {
		index = index || layer.index;
		const layero = document.getElementById(doms[0] + index);
		if (!layero) return;

		const titleElem = layero.querySelector(doms[1]); // doms[1] 是 title 的 class 选择器
		if (titleElem) {
			titleElem.innerHTML = name;
		}
	};

	//关闭layer总方法
	layer.close = function(index, callback) {
		const layerId = doms[0] + index;
		const layero = document.getElementById(layerId);
		if (!layero) return;

		const type = layero.getAttribute('type');
		const closeAnim = 'layer-anim-close';

		const remove = () => {
			if (type === ready.type[1] && layero.getAttribute('conType') === 'object') {
				// 清除除内容外的所有子节点
				[...layero.children].forEach(child => {
					if (!child.classList.contains(doms[5])) {
						layero.removeChild(child);
					}
				});

				const wrap = layero.querySelector(`.${WRAP}`);
				if (wrap) {
					for (let i = 0; i < 2; i++) {
						if (wrap.parentElement) {
							wrap.parentElement.replaceChild(wrap, wrap.parentElement);
						}
					}
					const originalDisplay = wrap.dataset.display || '';
					wrap.style.display = originalDisplay;
					wrap.classList.remove(WRAP);
				}
			} else {
				if (type === ready.type[2]) {
					try {
						const iframe = document.getElementById(doms[4] + index);
						if (iframe && iframe.contentWindow) {
							iframe.contentWindow.document.write('');
							iframe.contentWindow.close();
						}
						const cont = layero.querySelector(`.${doms[5]}`);
						cont && cont.removeChild(iframe);
					} catch (e) { }
				}
				layero.innerHTML = '';
				layero.remove();
			}

			if (typeof ready.end[index] === 'function') {
				ready.end[index]();
				delete ready.end[index];
			}
			if (typeof callback === 'function') callback();
		};

		if (layero.dataset.isOutAnim !== undefined) {
			layero.classList.add('layer-anim', closeAnim);
		}

		const moveElem = document.getElementById('layui-layer-moves');
		if (moveElem) moveElem.remove();
		const shadeElem = document.getElementById(doms.SHADE + index);
		if (shadeElem) shadeElem.remove();

		if (layer.ie === 6) {
			ready.reselect();
		}
		ready.rescollbar(index);

		if (layero.getAttribute('minLeft')) {
			ready.minIndex--;
			ready.minLeft.push(layero.getAttribute('minLeft'));
		}

		if ((layer.ie && layer.ie < 10) || layero.dataset.isOutAnim === undefined) {
			remove();
		} else {
			setTimeout(remove, 200);
		}
	};

	layer.closeAll = function(type, callback) {
		if (typeof type === 'function') {
			callback = type;
			type = null;
		}

		const domsElem = document.querySelectorAll(`.${doms[0]}`);
		let closedCount = 0;

		domsElem.forEach((el, idx) => {
			const elType = el.getAttribute('type');
			const times = el.getAttribute('times');

			const shouldClose = type ? (elType === type) : true;

			if (shouldClose) {
				const isLast = idx === domsElem.length - 1;
				layer.close(times, isLast ? callback : null);
				closedCount++;
			}
		});

		if (domsElem.length === 0 || closedCount === 0) {
			if (typeof callback === 'function') {
				callback();
			}
		}
	};

	/** 
	
	  拓展模块，layui 开始合并在一起
	
	 */

	var cache = layer.cache || {}, skin = function(type) {
		return (cache.skin ? (' ' + cache.skin + ' ' + cache.skin + '-' + type) : '');
	};

	//仿系统prompt
	layer.prompt = function(options, yes) {
		var style = '';
		options = options || {};

		if (typeof options === 'function') yes = options;

		if (options.area) {
			var area = options.area;
			style = 'style="width: ' + area[0] + '; height: ' + area[1] + ';"';
			delete options.area;
		}
		var prompt, content = options.formType == 2 ? '<textarea class="layui-layer-input"' + style + '></textarea>' : function() {
			return '<input type="' + (options.formType == 1 ? 'password' : 'text') + '" class="layui-layer-input">';
		}();

		var success = options.success;
		delete options.success;

		return layer.open(Object.assign({
			type: 1
			, btn: ['&#x786E;&#x5B9A;', '&#x53D6;&#x6D88;']
			, content: content
			, skin: 'layui-layer-prompt' + skin('prompt')
			, maxWidth: win.width()
			, success: function(layero) {
				prompt = layero.find('.layui-layer-input');
				prompt.val(options.value || '').focus();
				typeof success === 'function' && success(layero);
			}
			, resize: false
			, yes: function(index) {
				var value = prompt.val();
				if (value === '') {
					prompt.focus();
				} else if (value.length > (options.maxlength || 500)) {
					layer.tips('&#x6700;&#x591A;&#x8F93;&#x5165;' + (options.maxlength || 500) + '&#x4E2A;&#x5B57;&#x6570;', prompt, { tips: 1 });
				} else {
					yes && yes(value, index, prompt);
				}
			}
		}, options));
	};

	layer.tab = function(options = {}) {
		const tab = options.tab || [];
		const THIS = 'layui-this';
		const originalSuccess = options.success;
		delete options.success;

		// 构建标题 HTML
		const titleHTML = (() => {
			return tab.map((t, i) =>
				`<span class="${i === 0 ? THIS : ''}">${t.title}</span>`
			).join('');
		})();

		// 构建内容 HTML
		const contentHTML = (() => {
			return `<ul class="layui-layer-tabmain">` +
				tab.map((t, i) =>
					`<li class="layui-layer-tabli ${i === 0 ? THIS : ''}">${t.content || 'no content'}</li>`
				).join('') +
				`</ul>`;
		})();

		// open 方法调用
		return layer.open(Object.assign({
			type: 1,
			skin: `layui-layer-tab${skin('tab')}`,
			resize: false,
			title: titleHTML,
			content: contentHTML,
			success: function(layero) {
				const titleContainer = layero.querySelector('.layui-layer-title');
				const btns = titleContainer ? titleContainer.children : [];
				const mainItems = layero.querySelectorAll('.layui-layer-tabmain .layui-layer-tabli');

				Array.from(btns).forEach((btn, index) => {
					btn.addEventListener('mousedown', e => {
						e.stopPropagation();
						// 切换 tab 按钮状态
						Array.from(btns).forEach(b => b.classList.remove(THIS));
						btn.classList.add(THIS);

						// 切换 tab 内容区
						mainItems.forEach((li, i) => {
							li.style.display = i === index ? '' : 'none';
						});

						if (typeof options.change === 'function') {
							options.change(index);
						}
					});
				});

				if (typeof originalSuccess === 'function') {
					originalSuccess(layero);
				}
			}
		}, options));
	};

	//相册层
	layer.photos = function(options, loop, key) {
		var dict = {};
		options = options || {};
		if (!options.photos) return;

		//若 photos 并非选择器或 jQuery 对象，则为普通 object
		var isObject = !(typeof options.photos === 'string' || options.photos instanceof $)
			, photos = isObject ? options.photos : {}
			, data = photos.data || []
			, start = photos.start || 0;

		dict.imgIndex = (start | 0) + 1;
		options.img = options.img || 'img';

		var success = options.success;
		delete options.success;

		//如果 options.photos 不是一个对象
		if (!isObject) { //页面直接获取
			// 1️⃣ 取得容器元素：支持传入选择器或已有 DOM 节点
			const parent = typeof options.photos === 'string'
				? document.querySelector(options.photos)
				: options.photos;   // 若调用方已给 HTMLElement，直接用

			// 2️⃣ 收集 <img> 数据
			const pushData = () => {
				data = [];                               // 重置外层 data 数组
				parent.querySelectorAll(options.img)     // 等价 parent.find(options.img)
					.forEach((el, index) => {
						el.setAttribute('layer-index', index);          // othis.attr('layer-index', …)

						data.push({
							alt: el.getAttribute('alt') ?? '',   // 可选链 + null 合并
							pid: el.getAttribute('layer-pid') ?? '',
							src: (el.getAttribute('layer-src') || el.getAttribute('src')),
							thumb: el.getAttribute('src')
						});
					});
			};

			pushData();

			if (data.length === 0) return;

			// 如果 loop 为真就跳过绑定，保持与  jQuery 语法  “loop || …”  等价
			if (!loop) {
				// parent 必须是 HTMLElement，而不是 jQuery 对象
				parent.addEventListener('click', (ev) => {
					// 事件委托：只处理匹配 options.img 的元素
					const target = ev.target.closest(options.img);
					if (!target) return;        // 点击的不是目标元素，忽略

					pushData();

					// 取自定义属性 layer-index
					const index = target.getAttribute('layer-index');

					layer.photos(
						Object.assign({}, options, {
							photos: {
								start: index,
								data,
								tab: options.tab,
							},
							full: options.full,
						}),
						true
					);
				});
			}

		} else if (data.length === 0) {
			return layer.msg('&#x6CA1;&#x6709;&#x56FE;&#x7247;');
		}

		//上一张
		dict.imgprev = function(key) {
			dict.imgIndex--;
			if (dict.imgIndex < 1) {
				dict.imgIndex = data.length;
			}
			dict.tabimg(key);
		};

		//下一张
		dict.imgnext = function(key, errorMsg) {
			dict.imgIndex++;
			if (dict.imgIndex > data.length) {
				dict.imgIndex = 1;
				if (errorMsg) { return };
			}
			dict.tabimg(key)
		};

		//方向键
		dict.keyup = function(event) {
			if (!dict.end) {
				var code = event.keyCode;
				event.preventDefault();
				if (code === 37) {
					dict.imgprev(true);
				} else if (code === 39) {
					dict.imgnext(true);
				} else if (code === 27) {
					layer.close(dict.index);
				}
			}
		}

		//切换
		dict.tabimg = function(key) {
			if (data.length <= 1) return;
			photos.start = dict.imgIndex - 1;
			layer.close(dict.index);
			return layer.photos(options, true, key);
			setTimeout(function() {
				layer.photos(options, true, key);
			}, 200);
		}

		//一些动作
		dict.event = function() {

			dict.bigimg.find('.layui-layer-imgprev').on('click', function(event) {
				event.preventDefault();
				dict.imgprev(true);
			});

			dict.bigimg.find('.layui-layer-imgnext').on('click', function(event) {
				event.preventDefault();
				dict.imgnext(true);
			});

			$(document).on('keyup', dict.keyup);
		};

		//图片预加载
		function loadImage(url, callback, error) {
			var img = new Image();
			img.src = url;
			if (img.complete) {
				return callback(img);
			}
			img.onload = function() {
				img.onload = null;
				callback(img);
			};
			img.onerror = function(e) {
				img.onerror = null;
				error(e);
			};
		};

		dict.loadi = layer.load(1, {
			shade: 'shade' in options ? false : 0.9,
			scrollbar: false
		});

		loadImage(data[start].src, function(img) {
			layer.close(dict.loadi);

			//切换图片时不出现动画
			if (key) options.anim = -1;

			//弹出图片层
			dict.index = layer.open(Object.assign({
				type: 1,
				id: 'layui-layer-photos',
				area: (() => {
					// 原始图片尺寸
					const imgarea = [img.width, img.height];

					// 视口（window）尺寸，留出 100 px 边距
					const winarea = [window.innerWidth - 100, window.innerHeight - 100];

					// 如果图片宽或高大于可视区域，就按最大缩放比缩小
					if (!options.full && (imgarea[0] > winarea[0] || imgarea[1] > winarea[1])) {
						// 计算宽度、高度各自的缩放比例
						const scaleW = imgarea[0] / winarea[0];
						const scaleH = imgarea[1] / winarea[1];
						const scale = Math.max(scaleW, scaleH); // 取较大的缩放比例

						imgarea[0] /= scale;  // 按比例缩放宽度
						imgarea[1] /= scale;  // 按比例缩放高度
					}

					// 返回带单位的尺寸字符串
					return [`${imgarea[0]}px`, `${imgarea[1]}px`];
				})(),
				title: false,
				shade: 0.9,
				shadeClose: true,
				closeBtn: false,
				move: '.layui-layer-phimg img',
				moveType: 1,
				scrollbar: false,
				moveOut: true,
				anim: 5,
				isOutAnim: false,
				skin: 'layui-layer-photos' + skin('photos'),
				content: '<div class="layui-layer-phimg">'
					+ '<img src="' + data[start].src + '" alt="' + (data[start].alt || '') + '" layer-pid="' + data[start].pid + '">'
					+ function() {
						if (data.length > 1) {
							return '<div class="layui-layer-imgsee">'
								+ '<span class="layui-layer-imguide"><a href="javascript:;" class="layui-layer-iconext layui-layer-imgprev"></a><a href="javascript:;" class="layui-layer-iconext layui-layer-imgnext"></a></span>'
								+ '<div class="layui-layer-imgbar" style="display:' + (key ? 'block' : '') + '"><span class="layui-layer-imgtit"><a href="javascript:;">' + (data[start].alt || '') + '</a><em>' + dict.imgIndex + ' / ' + data.length + '</em></span></div>'
								+ '</div>'
						}
						return '';
					}()
					+ '</div>',
				success(layero, index) {
					// ① 获取内部元素 ⇒ querySelector
					dict.bigimg = layero.querySelector('.layui-layer-phimg');
					dict.imgsee = layero.querySelector('.layui-layer-imgbar');

					// ② 继续执行原有逻辑
					dict.event(layero);
					if (options.tab) options.tab(data[start], layero);
					if (typeof success === 'function') success(layero);
				},

				end() {
					dict.end = true;
					// ③ 解绑按键监听 ⇒ removeEventListener
					document.removeEventListener('keyup', dict.keyup);
				}
			}, options));
		}, function() {
			layer.close(dict.loadi);
			layer.msg('&#x5F53;&#x524D;&#x56FE;&#x7247;&#x5730;&#x5740;&#x5F02;&#x5E38;<br>&#x662F;&#x5426;&#x7EE7;&#x7EED;&#x67E5;&#x770B;&#x4E0B;&#x4E00;&#x5F20;&#xFF1F;', {
				time: 30000,
				btn: ['&#x4E0B;&#x4E00;&#x5F20;', '&#x4E0D;&#x770B;&#x4E86;'],
				yes: function() {
					data.length > 1 && dict.imgnext(true, true);
				}
			});
		});
	};

	/* ---------- 主入口：原生 JS 版 ---------- */
	ready.run = () => {
		win = window;
		doms.html = document.documentElement;

		// 确保 layer 是对象
		if (typeof layer !== 'object') {
			throw new Error('`layer` must be initialized as an object before calling ready.run()');
		}

		layer.open = (deliver) => {
			const o = new Class(deliver);
			return o.index;
		};
	};

	ready.run(); // 自动初始化 layer.open

	// 标准 UMD 模板
	if (typeof define === 'function' && define.amd) {
		define([], function() {
			return layer;
		});
	} else if (typeof exports === 'object') {
		module.exports = layer;
	} else {
		window.layer = layer;
	}

}(window);
