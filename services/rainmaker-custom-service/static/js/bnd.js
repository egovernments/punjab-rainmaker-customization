$(document).ready(() => {
    $("#updatePassword").on('click', updatePassword)
    /*let strUserInfo = localStorage.getItem("user-info");
    if (strUserInfo !== null && strUserInfo !== "") 
    {
        var userInfo = JSON.parse(strUserInfo);
        var isBNDEmployee = false;
        for (role of userInfo.roles) {
            if (role.code == "BNDEMPLOYEE")
                {
                    isBNDEmployee = true
                    break;
                }
        }
        if (!isBNDEmployee)
            document.getElementById('msg').innerText = "You don't have access to Birth and Death application";
        else { */
    loginToBND();
    //}
    //}


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
            password: password
        }
    }).then((response) => {});
}

function loginToBND() {
    var access_token = localStorage.getItem("token");
    showLoader()
    axios({
        url: location.href.replace("/open/", "/protected/"),
        method: 'POST',
        json: true, // important
        headers: {
            'Accept': 'application/json'
        },
        data: {
            RequestInfo: {
                "authToken": access_token
            }
        }
    }).then((response) => {
        hideLoader()
        switch (response.data.code) {
            case "NO_CREDENTIAL_MAPPING":
            case "LOGIN_PASSWORD_ERROR":
                $('#msg').text(response.data.message)
                $('#inputEmail').val(response.data.username || "")
                $("#updateCredential").show()
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
}