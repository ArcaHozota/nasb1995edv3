$(document).ready(() => {
    // initialStudent();
});
$("#toStudentsPages").on("click", (e) => {
    e.preventDefault();
    layer.msg(delayApology);
});
$("#accountEdit").on("change", () => {
    checkStudentName(this, $("#idContainer").val());
});
$("#infoUpdateBtn").on("click", () => {
    let inputArrays = ["#accountEdit", "#nameEdit", "#passwordEdit", "#birthdayEdit", "#emailEdit"];
    for (const array of inputArrays) {
        $(array).removeClass("is-valid is-invalid");
        $(array).next("span").removeClass("valid-feedback invalid-feedback");
        $(array).next("span").text(emptyString);
    }
    let listArray = projectInputContextGet(inputArrays);
    if (listArray.includes(emptyString)) {
        projectNullInputBoxDiscern(inputArrays);
    } else if ($("#editForm").find('*').hasClass('is-invalid')) {
        layer.msg(inputWarning);
    } else {
        let editId = $("#idContainer").val();
        let putData = JSON.stringify({
            'id': editId,
            'loginAccount': $("#accountEdit").val().trim(),
            'username': $("#nameEdit").val().trim(),
            'password': $("#passwordEdit").val(),
            'email': $("#emailEdit").val(),
            'dateOfBirth': $("#birthdayEdit").val()
        });
        projectAjaxModify('/students/info-update', PUT, putData, studentsPutSuccessFunction);
    }
});
$("#restoreBtn").on("click", () => {
    formReset("#editForm");
    // initialStudent();
});

function studentsPutSuccessFunction(response) {
    let message = trimQuote(response);
    localStorage.setItem('redirectMessage', message);
    window.location.replace('/category/to-mainmenu');
}

function checkStudentName(studentName, idVal) {
    let nameVal = $(studentName).val().trim();
    if (nameVal === emptyString) {
        showValidationMsg(studentName, responseFailure, "名称を空になってはいけません。");
    } else {
        $.ajax({
            url: '/students/check-duplicated',
            data: {
                'id': idVal,
                'loginAccount': nameVal
            },
            success: (xhr) => {
                showValidationMsg(studentName, responseSuccess, xhr.responseText);
            },
            error: (xhr) => {
                showValidationMsg(studentName, responseFailure, xhr.responseText);
            }
        });
    }
}

function initialStudent() {
    let editId = $("#idContainer").val();
    $.ajax({
        url: '/students/initial',
        data: 'editId=' + editId,
        success: (response) => {
            $("#accountEdit").val(response.loginAccount);
            $("#nameEdit").val(response.username);
            $("#passwordEdit").val(response.password);
            $("#birthdayEdit").val(toDateInputValue(response.dateOfBirth));
            $("#emailEdit").val(response.email);
        },
        error: (xhr) => {
            let message = trimQuote(xhr.responseText);
            layer.msg(message);
        }
    });
}

/**
 * 文字列・数値・Date 何が来ても HTML date 用の YYYY-MM-DD に整形
 */
function toDateInputValue(src) {
    if (!src) return emptyString;
    // すでに YYYY-MM-DD ならそのまま
    if (/^\d{4}-\d{2}-\d{2}$/.test(src)) return src;
    // ISO 8601 やタイムスタンプを Date に変換
    const d = new Date(src);
    // タイムゾーン影響を避けつつローカル日付を取り出す
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}
