let requests = require("request")

requests.post("http://13.126.198.70/eDistServices/ModuleCommon/serAuth.asmx/serAuthExternalUser",
    {
        form: {
            username: "eomcbrn",
            password: "esewa@123"
        },
        headers: {
            "x-api-key": "Tw0AxM3kyV3mb8SM"
        }
    }
,function(a,b,c) {
    console.log(a,c);
})