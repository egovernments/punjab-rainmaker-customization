$(document).ready(() => {
    $("#linkAccount").on('click', linkAccount)
    if (getAuthToken() !== null) {
        let params = (new URL(document.location)).searchParams;
        if (params.get("unlink") !== null)
        {
            unlinkAccount();
        } else {
            setTimeout(loginToBND, 10);
        }
    } else {
        $("#msg").html("<strong>You need to be logged in to access this page</strong>")
        $("#form").hide()
    }
});

function unlinkAccount() {
    axios({
        url: location.pathname.replace("/open/", "/protected/") + "/unlinkAccount",
        method: 'POST',
        json: true, // important
        headers: {
            'Accept': 'application/json'
        },
        data: {
            RequestInfo: {
                "authToken": getAuthToken(),
            }
        }
    }).then((response) => {
        window.location.assign(window.location.href.replace("unlink", ""))
    });
}

function linkAccount() {
    username = $("#username").val();
    password = $("#password").val()

    axios({
        url: location.pathname.replace("/open/", "/protected/") + "/linkAccount",
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
        let res = response.data;
        if (res && res.code && res.code == "SUCCESS" && res.redirect) {
            var urlInfo = decodeURI(window.location.href).split('?');
            var path = urlInfo[0];
            $("#msg").html("The application has been opened in new window. If you want to unlink your account click <a href='" + path + "?unlink'>here</a>")
            window.open(res.redirect,"bnd", "", true)
        } else {
            $("#msg").text(res.message).css("color", "red");
        }
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
                    $('#msg').text("Please enter your eSewa Health application Username & Password to link your account")
                    $('#inputEmail').val(response.data.username || "")
                    $("#form").show()
                    break
                case "INVALID_CREDENTIALS":
                    $('#msg').text(response.data.message)
                    $('#inputEmail').val(response.data.username || "")
                    $("#form").show()
                    break
                case "SUCCESS":
                    $("#msg").html("The application has been opened in new window. If you want to unlink your account click <a href='" + location.href + "?unlink'>here</a>")
                    window.open(response.data.redirect,"bnd", "", true)
                    break;
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
    $("#loadMe").modal("hide")
    $('#loadMe').on('shown.bs.modal', function (e) {
        $("#loadMe").modal('hide');
    })
}