$(document).ready(() => {
	let loginMsg = $("#torokuMsgContainer").val().trim();
	if (loginMsg !== emptyString) {
		layer.msg(loginMsg);
	}
	let message = localStorage.getItem('redirectMessage');
	if (message) {
		layer.msg(message);
		localStorage.removeItem('redirectMessage');
	}
});
$("#booksKanriMainmenu").on("click", () => {
	layer.msg(delayApology);
	// let url = '/books/initial';
	// checkPermissionAndTransfer(url);
});
$("#hymnsKanriMainmenu").on("click", () => {
	let url = '/hymns/to-pages?pageNum=1';
	checkPermissionAndTransfer(url);
});
$("#randomKanriMainmenu").on("click", () => {
	let url = '/hymns/to-random-five';
	checkPermissionAndTransfer(url);
});