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
    let $pageInfos = $("#pageInfos");
    $pageInfos.empty();
    pageNum = response.pageNum;
    totalPages = response.totalPages;
    totalRecords = response.totalRecords;
    $pageInfos.append(totalPages + "ページ中の" + pageNum + "ページ、" + totalRecords + "件のレコードが見つかりました。");
}

function buildPageNavi(result) {
    $("#pageNavi").empty();
    let ul = $("<ul></ul>").addClass('pagination');
    let firstPageLi = $("<li class='page-item'></li>").append(
        $("<a class='page-link'></a>").append("最初へ").attr("href", "#"));
    let prevPageLi = $("<li class='page-item'></li>").append(
        $("<a class='page-link'></a>").append("&laquo;").attr("href", "#"));
    if (!result.hasPrevPage) {
        firstPageLi.addClass('disabled');
        prevPageLi.addClass('disabled');
    } else {
        firstPageLi.click(() => {
            toSelectedPg(1, keyword);
        });
        prevPageLi.click(() => {
            toSelectedPg(pageNum - 1, keyword);
        });
    }
    let nextPageLi = $("<li class='page-item'></li>").append(
        $("<a class='page-link'></a>").append("&raquo;").attr("href", "#"));
    let lastPageLi = $("<li class='page-item'></li>").append(
        $("<a class='page-link'></a>").append("最後へ").attr("href", "#"));
    if (!result.hasNextPage) {
        nextPageLi.addClass('disabled');
        lastPageLi.addClass('disabled');
    } else {
        lastPageLi.addClass('success');
        nextPageLi.click(() => {
            toSelectedPg(pageNum + 1, keyword);
        });
        lastPageLi.click(() => {
            toSelectedPg(totalPages, keyword);
        });
    }
    ul.append(firstPageLi).append(prevPageLi);
    $.each(result.navigateNos, (_, item) => {
        let numsLi = $("<li class='page-item'></li>").append(
            $("<a class='page-link'></a>").append(item).attr("href", "#"));
        if (pageNum === item) {
            numsLi.attr("href", "#").addClass("active");
        }
        numsLi.click(() => {
            toSelectedPg(item, keyword);
        });
        ul.append(numsLi);
    });
    ul.append(nextPageLi).append(lastPageLi);
    $("<nav></nav>").append(ul).appendTo("#pageNavi");
}
