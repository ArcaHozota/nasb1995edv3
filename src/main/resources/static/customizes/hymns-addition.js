let pageNum = $("#pageNumContainer").val();
$(document).ready(() => {
    let $toCollection = $("#toCollection");
    $toCollection.css('color', '#006b3c');
    $toCollection.addClass('animate__animated animate__flipInY');
});
$("#toHymnPages").on("click", (e) => {
    e.preventDefault();
    let url = '/hymns/to-pages?pageNum=' + pageNum;
    checkPermissionAndTransfer(url);
});
$("#nameJpInput").on("change", (e) => {
    checkHymnName(e.currentTarget, null);
});
$("#nameKrInput").on("change", (e) => {
    checkHymnName2(e.currentTarget, null);
});
$("#infoStorageBtn").on("click", () => {
    let inputArrays = ["#nameJpInput", "#nameKrInput", "#linkInput", "#serifInput"];
    for (const array of inputArrays) {
        $(array).removeClass('is-valid is-invalid');
        $(array).next("span").removeClass('valid-feedback invalid-feedback');
        $(array).next("span").text(emptyString);
    }
    let listArray = projectInputContextGet(inputArrays);
    if (listArray.includes(emptyString)) {
        projectNullInputBoxDiscern(inputArrays);
    } else if ($("#inputForm").find("*").hasClass('is-invalid')) {
        layer.msg(inputWarning);
    } else {
        let postData = JSON.stringify({
            'nameJp': $("#nameJpInput").val().trim(),
            'nameKr': $("#nameKrInput").val().trim(),
            'link': $("#linkInput").val(),
            'serif': $("#serifInput").val()
        });
        projectAjaxModify('/hymns/info-storage', POST, postData, hymnsPostSuccessFunction);
    }
});
$("#nameJpEdit").on("change", (e) => {
    checkHymnName(e.currentTarget, $("#idContainer").val());
});
$("#nameKrEdit").on("change", (e) => {
    checkHymnName2(e.currentTarget, $("#idContainer").val());
});
$("#infoUpdateBtn").on("click", () => {
    let inputArrays = ["#nameJpEdit", "#nameKrEdit", "#linkEdit", "#serifEdit"];
    for (const array of inputArrays) {
        $(array).removeClass('is-valid is-invalid');
        $(array).next("span").removeClass('valid-feedback invalid-feedback');
        $(array).next("span").text(emptyString);
    }
    let listArray = projectInputContextGet(inputArrays);
    if (listArray.includes(emptyString)) {
        projectNullInputBoxDiscern(inputArrays);
    } else if ($("#editForm").find("*").hasClass('is-invalid')) {
        layer.msg(inputWarning);
    } else {
        let putData = JSON.stringify({
            'id': $("#idContainer").val(),
            'nameJp': $("#nameJpEdit").val().trim(),
            'nameKr': $("#nameKrEdit").val().trim(),
            'link': $("#linkEdit").val(),
            'serif': $("#serifEdit").val(),
            'updatedTime': $("#datestampContainer").val()
        });
        projectAjaxModify('/hymns/info-update', PUT, putData, hymnsPutSuccessFunction);
    }
});
$("#resetBtn").on("click", () => {
    formReset("#inputForm");
});
$("#restoreBtn").on("click", () => {
    formReset("#editForm");
});

function hymnsPostSuccessFunction(response) {
    localStorage.setItem('redirectMessage', inputString);
    window.location.replace('/hymns/to-pages?pageNum=' + response);
}

function hymnsPutSuccessFunction(response) {
    let message = response.replace(/^"|"$/g, emptyString);
    localStorage.setItem('redirectMessage', message);
    window.location.replace('/hymns/to-pages?pageNum=' + pageNum);
}

function checkHymnName(hymnName, idVal) {
    let nameVal = $(hymnName).val().trim();
    if (nameVal === emptyString) {
        showValidationMsg(hymnName, responseFailure, showVadMsgError);
    } else {
        $.ajax({
            url: '/hymns/check-duplicated',
            data: {
                'id': idVal,
                'nameJp': nameVal
            },
            success: (response) => {
                showValidationMsg(hymnName, responseSuccess, response);
            },
            error: (xhr) => {
                showValidationMsg(hymnName, responseFailure, xhr.responseText);
            }
        });
    }
}

function checkHymnName2(hymnName, idVal) {
    let nameVal = $(hymnName).val().trim();
    if (nameVal === emptyString) {
        showValidationMsg(hymnName, responseFailure, showVadMsgError);
    } else {
        $.ajax({
            url: '/hymns/check-duplicated2',
            data: {
                'id': idVal,
                'nameKr': nameVal
            },
            success: (response) => {
                showValidationMsg(hymnName, responseSuccess, response);
            },
            error: (xhr) => {
                showValidationMsg(hymnName, responseFailure, xhr.responseText);
            }
        });
    }
}
