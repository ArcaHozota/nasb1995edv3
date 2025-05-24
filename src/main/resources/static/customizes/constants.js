const responseSuccess = 'SUCCESS';
const responseFailure = 'FAILURE';
const emptyString = '';
const inputWarning = '入力情報不正';
const inputString = '追加済み';
const delimiter = ' / ';
const delayApology = 'すみませんが、当機能はまだ実装されていません';
const showVadMsgError = '名称を空になってはいけません。';
const GET = 'GET';
const POST = 'POST';
const PUT = 'PUT';
const DELETE = 'DELETE';
const trimQuote = (str) => str.replace(/^"|"$/g, emptyString);

function buildPageInfos(response) {
    const pageInfos = document.getElementById("pageInfos");
    pageInfos.innerHTML = emptyString;
    pageNum = response.pageNum;
    totalPages = response.totalPages;
    totalRecords = response.totalRecords;
    pageInfos.textContent = `${totalPages}ページ中の${pageNum}ページ、${totalRecords}件のレコードが見つかりました。`;
}

function buildPageNavi(result) {
    const pageNavi = document.getElementById("pageNavi");
    pageNavi.innerHTML = emptyString;
    const ul = document.createElement('ul');
    ul.classList.add('pagination');
    const createPageItem = (label, disabled, clickHandler) => {
        const li = document.createElement('li');
        li.className = 'page-item';
        const a = document.createElement('a');
        a.className = 'page-link';
        a.href = '#';
        a.innerHTML = label;
        if (disabled) {
            li.classList.add('disabled');
        } else if (clickHandler) {
            li.addEventListener('click', clickHandler);
        }
        li.appendChild(a);
        return li;
    };
    ul.appendChild(createPageItem("最初へ", !result.hasPrevPage, () => toSelectedPg(1, keyword)));
    ul.appendChild(createPageItem("&laquo;", !result.hasPrevPage, () => toSelectedPg(pageNum - 1, keyword)));
    result.navigateNos.forEach(item => {
        const li = document.createElement('li');
        li.className = 'page-item';
        const a = document.createElement('a');
        a.className = 'page-link';
        a.href = '#';
        a.textContent = item;
        if (pageNum === item) {
            li.classList.add('active');
        }
        li.appendChild(a);
        li.addEventListener('click', () => toSelectedPg(item, keyword));
        ul.appendChild(li);
    });
    ul.appendChild(createPageItem("&raquo;", !result.hasNextPage, () => toSelectedPg(pageNum + 1, keyword)));
    ul.appendChild(createPageItem("最後へ", !result.hasNextPage, () => toSelectedPg(totalPages, keyword)));
    const nav = document.createElement('nav');
    nav.appendChild(ul);
    pageNavi.appendChild(nav);
}
