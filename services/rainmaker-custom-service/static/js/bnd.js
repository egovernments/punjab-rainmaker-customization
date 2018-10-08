$(document).ready(() => {
    $("#updatePassword").on('click', updatePassword)
    if (getAuthToken() !== null) {
        setTimeout(loginToBND, 10);
    } else {
        $(".form-signin *").hide()
        $("#noaccess").html("<strong>You need to be logged in to access this page</strong>")
        $("#noaccess").show()
    }
});

function updatePassword() {
    username = $("#username").val();
    password = $("#password").val()

    axios({
        url: location.pathname.replace("/open/", "/protected/") + "/update",
        method: 'POST',
        json: true, // important
        headers: {
            'Accept': 'application/json'
        },
        data: {
            username: username,
            password: password,
            RequestInfo: {
                "authToken": getAuthToken(),
            }

        }
    }).then((response) => {
        window.location.reload();
    });
}

function getAuthToken() {
    var access_token = localStorage.getItem("token");
    return access_token;
}

function loginToBND() {
    showLoader()
    try {
        axios({
            url: location.href.replace("/open/", "/protected/"),
            method: 'POST',
            json: true, // important
            headers: {
                'Accept': 'application/json'
            },
            data: {
                RequestInfo: {
                    "authToken": getAuthToken()
                }
            }
        }).then((response) => {
            hideLoader()
            switch (response.data.code) {
                case "NO_CREDENTIAL_MAPPING":
                case "INVALID_CREDENTIALS":
                    $('#msg').text(response.data.message)
                    $('#inputEmail').val(response.data.username || "")
                    $("#form").show()
                    break
                case "SUCCESS":
                    $('#msg').text("Redirecting to login URL");
                    document.location.href = response.data.redirect;
                default:
                    $('#msg').text(response.data.message || "Some error occcured while processing your request")
                    break
            }
        }).catch((err) => {
            hideLoader();
        });
    } catch (ex) {
        hideLoader()
    }
}

function showLoader() {
    $("#loadMe").modal({
        backdrop: "static", //remove ability to close modal with click
        keyboard: false, //remove option to close with keyboard
        show: true //Display loader!
    });
}

function hideLoader() {
    setTimeout(() => $("#loadMe").modal("hide"), 20)
    setTimeout(() => $("#loadMe").modal("hide"), 100)
}