// === DOM Element References ===
const torokuMsgContainer = document.getElementById("torokuMsgContainer");
const booksKanriMainmenu = document.getElementById("booksKanriMainmenu");
const hymnsKanriMainmenu = document.getElementById("hymnsKanriMainmenu");
const randomKanriMainmenu = document.getElementById("randomKanriMainmenu");

document.addEventListener("DOMContentLoaded", () => {
    const loginMsg = torokuMsgContainer?.value?.trim();
    if (loginMsg !== emptyString) {
        layer.msg(loginMsg);
    }
    const message = localStorage.getItem('redirectMessage');
    if (message) {
        layer.msg(message);
        localStorage.removeItem('redirectMessage');
    }
});

booksKanriMainmenu.addEventListener("click", () => {
    layer.msg(delayApology);
    // const url = '/books/initial';
    // checkPermissionAndTransfer(url);
});

hymnsKanriMainmenu.addEventListener("click", () => {
    const url = '/hymns/to-pages?pageNum=1';
    checkPermissionAndTransfer(url);
});

randomKanriMainmenu.addEventListener("click", () => {
    const url = '/hymns/to-random-five';
    checkPermissionAndTransfer(url);
});