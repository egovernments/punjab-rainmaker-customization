var express = require('express'),
    slash = require('express-slash');
var bodyParser = require('body-parser')

var request = require('request-promise');
const {
    asyncMiddleware
} = require('./utils/asyncMiddleware');

var app = express();
var mustache = require('mustache-express')
app.engine('html', mustache())
app.set('view engine', 'html')
app.set('views', __dirname + '/templates')
app.disable('view cache');

const PT_DEBUG_MODE = Boolean(process.env.PT_DEBUG_MODE) || false;
const PT_DEMAND_HOST = process.env.PT_DEMAND_HOST
const EGOV_MDMS_HOST = process.env.EGOV_MDMS_HOST
const EGOV_BND_LOGIN_URL = process.env.EGOV_BND_LOGIN_URL
const EGOV_BND_REDIRECT_URL = process.env.EGOV_BND_REDIRECT_URL
const EGOV_BND_API_KEY = process.env.EGOV_BND_API_KEY

async function getFireCessConfig(tenantId) {
    let fireCessConfig = await request.post({
        url: EGOV_MDMS_HOST + "egov-mdms-service/v1/_search?tenantId=" + tenantId,
        body: {
            RequestInfo: {
                "apiId": "Rainmaker-custom-service",
                "ver": ".01",
                "ts": "",
                "action": "_search",
                "did": "1",
                "key": "",
                "msgId": "20170310130900|en_IN",
                "authToken": null
            },
            "MdmsCriteria": {
                "tenantId": tenantId,
                "moduleDetails": [{
                    "moduleName": "PropertyTax",
                    "masterDetails": [{
                        "name": "FireCess"
                    }]
                }]
            }
        },
        json: true
    })

    console.log("Got firecess config", tenantId, JSON.stringify(fireCessConfig, null, 2))
    return fireCessConfig["MdmsRes"]["PropertyTax"]["FireCess"][0];
}

const TAX_TYPE = {
    PT_TAX: false,
    PT_TIME_REBATE: true,
    PT_UNIT_USAGE_EXEMPTION: true,
    PT_TIME_PENALTY: false,
    PT_CANCER_CESS: false,
    PT_ADHOC_PENALTY: false,
    PT_ADHOC_REBATE: true,
    PT_DECIMAL_CEILING_CREDIT: false,
    PT_DECIMAL_CEILING_DEBIT: true,
    PT_FIRE_CESS: false,
    PT_OWNER_EXEMPTION: true,
    PT_TIME_INTEREST: false
}

if (PT_DEMAND_HOST === undefined) {
    throw Error("PT_DEMAND_HOST environment variable needs to be configured to run this")
}

function round(num, digits) {
    return parseFloat(parseFloat(num).toFixed(digits))
}

// parse application/x-www-form-urlencoded
app.use(bodyParser.urlencoded({
    extended: true
}))

// parse application/json
app.use(bodyParser.json())

// Because you're the type of developer who cares about this sort of thing!
app.enable('strict routing');

// Create the router using the same routing options as the app.
var router = express.Router({
    caseSensitive: app.get('case sensitive routing'),
    strict: app.get('strict routing')
});

router.use("/static", express.static("static"))

// Add the `slash()` middleware after your app's `router`, optionally specify
// an HTTP status code to use when redirecting (defaults to 301).
app.use('/customization', router);
// app.use(slash());
app.use(express.json());

var promise = require('bluebird');

var options = {
    // Initialization Options
    promiseLib: promise
};

var pgp = require('pg-promise')(options);

const connectionString = {
    host: process.env.DB_HOST || 'localhost',
    port: 5432,
    database: process.env.DB_NAME || 'postgres',
    user: process.env.DB_USER || 'postgres',
    password: process.env.DB_PASSWORD || 'postgres'
};

// var connectionString = 'postgres://localhost:5432/egov_prod_db';
var db = pgp(connectionString);
var path = require('path');

query = `SELECT servicerequestid as complaint_no, servicecode as complaint_type, eg_user.name as citizen_name, eg_user.mobilenumber as citizen_mobile, address, landmark, description as details
FROM eg_pgr_service INNER JOIN eg_user ON eg_pgr_service.accountid = cast(eg_user.id as varchar)
WHERE eg_pgr_service.status = 'assigned' AND servicerequestid IN (select DISTINCT businesskey from eg_pgr_action where status = 'assigned' AND "when" 
IN (select max("when") from eg_pgr_action where assignee NOTNULL group by businesskey) AND assignee = $1);`

router.get('/open/reports/*', function (req, res) {
    res.render('apicall.html');
});

router.get('/open/bndlogin', function (req, res) {
    res.render('bndlogin.html');
});

router.post('/protected/bndlogin/update', asyncMiddleware(async function (req, res) {
    let username = req.body.username
    let password = req.body.password

    data = await db.any("select value from custom_eg_user_metatdata where user_id = $1 and key = $2", ['123456', 'BND_CREDENTIALS']);
    value = {
        username,
        password
    }

    if (data.length == 0) {
        await db.any("insert into custom_eg_user_metatdata(key, user_id, value) values ($1, $2, $3:json)",
            ['BND_CREDENTIALS', '123456', value])
    } else {
        await db.any("update custom_eg_user_metatdata set value = $3:json where key = $1 and user_id = $2",
            ['BND_CREDENTIALS', '123456', value])
    }
}));

router.post('/protected/bndlogin', asyncMiddleware(async function (req, res) {
    try {
        data = await db.any("select value from custom_eg_user_metatdata where user_id = $1 and key = $2", ['123456', 'BND_CREDENTIALS']);

        if (data.length == 0) {
            res.json({
                "code": "NO_CREDENTIAL_MAPPING",
                "message": "Your B&D Integration Credentials are missing"
            })
            return
        }

        let username = data[0]["value"]["username"]
        let password = data[0]["value"]["password"]

        let response = await request.post(EGOV_BND_LOGIN_URL, {
            form: {
                username: username,
                password: password
            },
            headers: {
                "x-api-key": EGOV_BND_API_KEY
            }
        })

        let login_response = JSON.parse(response);

        if (login_response.response == "0") {
            if (login_response.reason == "INVALID_CREDENTIALS") {
                res.json({
                    code: "INVALID_CREDENTIALS",
                    username: username,
                    message: login_response.sys_message +". Kindly update your credentials"
                })
            } else {
                res.json({
                    code: "INTEGRATION_ERROR",
                    username: username,
                    message: login_response.sys_message
                })
            }
            return;
        }

        res.json({
            code: "SUCCESS",
            redirect: EGOV_BND_REDIRECT_URL + JSON.stringify(["data"])
        })
    } catch (ex) {
        console.log("Exception occured while login", ex)
        res.json({
            "code": "ERROR",
            "message": "Failed to login to BND - " + ex.toString()
        })
    }

}));

router.post('/protected/reports/lmereport', function (req, res) {
    console.log(req.body)
    let userId = String(req.body.RequestInfo.userInfo.id);

    console.log("User id is", userId);
    db.any(query, userId).then(function (data) {
        console.log(data)
    })

    var excel = require('excel4node');

    var workbook = new excel.Workbook();

    var worksheet = workbook.addWorksheet('Sheet 1');

    worksheet.cell(1, 1).string('content for display');

    workbook.write('report.xlsx', res);
});

// data = require('./sampleRequest')

function getFireCessPercentage(propertyDetails) {
    // let propertyDetails = request["CalculationCriteria"][0]["propertyDetails"][0]

    let propertyUsageCategoryMajor = propertyDetails["usageCategoryMajor"]
    let units = propertyDetails["units"]
    let propertyAttributes = propertyDetails["additionalDetails"]
    let unitSet = new Set()

    for (unit of units) {
        unitSet.add(unit["usageCategoryMajor"])
    }
    let firecess_category_major = 0;
    let firecess_building_height = 0;
    let firecess_inflammable = 0;

    if (propertyUsageCategoryMajor == "RESIDENTIAL" || (unitSet.size == 1 && unitSet.has("RESIDENTIAL"))) {
        // There is no category major firecess applicable as it i
        firecess_category_major = 0
    } else {
        firecess_category_major = 5.0
    }

    if (propertyAttributes &&
        propertyAttributes.heightAbove36Feet &&
        propertyAttributes.heightAbove36Feet.toString() == "true") {
        // height is above 36 feet
        firecess_building_height = 2.0
    }

    if (propertyAttributes &&
        propertyAttributes.inflammable &&
        propertyAttributes.inflammable.toString() == "true") {
        // height is above 36 feet
        firecess_inflammable = 10.0
    }

    return {
        firecess_inflammable,
        firecess_building_height,
        firecess_category_major,
        firecess: firecess_category_major + firecess_building_height + firecess_inflammable
    }
}

function calculateNewFireCess(taxHeads, firecess_percent, taxField, taxHeadCodeField) {

    let applicablePropertyTax = 0
    for (taxHead of taxHeads) {
        if (taxHead[taxHeadCodeField] == "PT_TAX") {
            applicablePropertyTax += taxHead[taxField]
        } else if (taxHead[taxHeadCodeField] == "PT_UNIT_USAGE_EXEMPTION") {
            applicablePropertyTax -= taxHead[taxField]
        } else if (taxHead[taxHeadCodeField] == "PT_OWNER_EXEMPTION") {
            applicablePropertyTax -= taxHead[taxField]
        }
    }

    return round(applicablePropertyTax * (firecess_percent / 100), 2);
}

async function findDemandForConsumerCode(consumerCode, tenantId, service, RequestInfo) {
    let demandSearchResponse = await request.post({
        url: PT_DEMAND_HOST + "/billing-service/demand/_search?tenantId=" + tenantId +
            "&consumerCode=" + consumerCode + "&businessService=" + service,
        body: {
            RequestInfo
        },
        json: true
    })

    return demandSearchResponse;
}

async function updateDemand(demands, RequestInfo) {
    let demandUpdateResponse = await request.post({
        url: PT_DEMAND_HOST + "/billing-service/demand/_update",
        body: {
            RequestInfo,
            "Demands": demands
        },
        json: true
    })

    return demandUpdateResponse;
}

function _estimateTaxProcessor(request, response) {
    let index = 0;
    for (let calc of request["CalculationCriteria"]) {
        let fireCessPercentage = getFireCessPercentage(calc["property"]["propertyDetails"][0])

        let updateFirecessAmount = calculateNewFireCess(response["Calculation"][0]["taxHeadEstimates"], fireCessPercentage.firecess, "estimateAmount", "taxHeadCode")
        let taxes = getUpdateTaxSummary(response["Calculation"][index], updateFirecessAmount, "taxHeadCode", "estimateAmount")

        response["Calculation"][index]["totalAmount"] = taxes.totalAmount
        response["Calculation"][index]["taxAmount"] = round(taxes.taxAmount, 2)
        response["Calculation"][index]["rebate"] = taxes.rebate

        index++
    }

    return response;
}

function getUpdateTaxSummary(calculation, newTaxAmount, taxHeadCodeField, taxAmountField) {
    let ceilingTaxHead = null;
    let firecessTaxHead = null;

    let taxAmount = 0,
        penalty = 0,
        rebate = 0,
        exemption = 0
    let taxHeads = calculation["taxHeadEstimates"]
    for (taxHead of taxHeads) {
        if (taxHead[taxHeadCodeField] == "PT_FIRE_CESS") {
            let existingTaxAmount = taxHead[taxAmountField]
            taxHead[taxAmountField] = newTaxAmount
            firecessTaxHead = taxHead
            taxAmount += newTaxAmount
            if (PT_DEBUG_MODE) {
                taxHead.oldEstimateAmount = existingTaxAmount
            }
        } else {
            switch (taxHead[taxHeadCodeField]) {
                case "PT_DECIMAL_CEILING_CREDIT":
                case "PT_DECIMAL_CEILING_DEBIT":
                    ceilingTaxHead = taxHead
                    break
                case "PT_ADVANCE_CARRYFORWARD":
                    exemption += taxHead[taxAmountField]
                    break
                default:
                    switch (taxHead.category) {
                        case "PENALTY":
                            penalty += taxHead[taxAmountField]
                            break
                        case "TAX":
                            taxAmount += taxHead[taxAmountField]
                            break
                        case "REBATE":
                            rebate += taxHead[taxAmountField]
                            break
                        case "EXEMPTION":
                            exemption += taxHead[taxAmountField]
                            break
                        default:
                            console.log("Going to default for taxHead", taxHead)
                            taxAmount += taxHead[taxAmountField]
                    }

            }
        }
    }

    taxAmount = round(taxAmount, 2)
    penalty = round(penalty, 2)
    exemption = round(exemption, 2)
    rebate = round(rebate, 2)

    let totalAmount = taxAmount + penalty - rebate - exemption

    totalAmount = round(totalAmount, 2)
    let fractionAmount = totalAmount - Math.trunc(totalAmount)
    let newCeilingTax = false

    if (ceilingTaxHead == null && fractionAmount == 0) {

    } else {
        let ceilingDelta = 0.0;

        if (ceilingTaxHead == null) {
            ceilingTaxHead = {
                taxHeadCode: "",
                estimateAmount: 0,
                category: null
            }
            newCeilingTax = true
            taxHeads.push(ceilingTaxHead)
        }

        if (fractionAmount < 0.5) {
            ceilingDelta = parseFloat(fractionAmount.toFixed(2))
            totalAmount = Math.trunc(totalAmount)
            ceilingTaxHead[taxHeadCodeField] = "PT_DECIMAL_CEILING_DEBIT"
            ceilingTaxHead[taxAmountField] = ceilingDelta
            rebate += ceilingDelta
        } else {
            ceilingDelta = parseFloat((1 - fractionAmount).toFixed(2))

            totalAmount = Math.trunc(totalAmount) + 1
            ceilingTaxHead[taxHeadCodeField] = "PT_DECIMAL_CEILING_CREDIT"
            ceilingTaxHead[taxAmountField] = ceilingDelta
            taxAmount += ceilingDelta
        }
    }

    console.log({
        taxAmount,
        penalty,
        rebate,
        exemption,
        totalAmount,
        fractionAmount,
        ceilingTaxHead
    })

    return {
        taxHeads,
        rebate,
        totalAmount,
        taxAmount,
        newCeilingTax,
        ceilingTaxHead,
        firecessTaxHead
    }
}

async function _createAndUpdateTaxProcessor(request, response) {
    let index = 0
    for (reqProperty of request["Properties"]) {

        let resProperty = response["Properties"][index]
        let propertyId = resProperty["propertyId"]

        let assessmentNumber = resProperty["propertyDetails"][0]["assessmentNumber"]

        let consumerCode = propertyId + ":" + assessmentNumber
        let service = "PT"
        let tenantId = reqProperty["tenantId"]

        let demandSearchResponse = await findDemandForConsumerCode(consumerCode, tenantId, service, request["RequestInfo"])
        console.log("Existing demand is ", JSON.stringify(demandSearchResponse["Demands"], null, 2))

        let fireCessPercentage = getFireCessPercentage(reqProperty["propertyDetails"][0])

        if (PT_DEBUG_MODE) {
            demandSearchResponse["Demands"][0]["firecess"] = fireCessPercentage
        }
        let calc = response["Properties"][index]["propertyDetails"][0]["calculation"]
        let updateFirecessTax = calculateNewFireCess(calc["taxHeadEstimates"], fireCessPercentage.firecess, "estimateAmount", "taxHeadCode")

        let taxes = getUpdateTaxSummary(calc,
            updateFirecessTax, "taxHeadCode", "estimateAmount")

        if (taxes.newCeilingTax) {
            let firstDemand = demandSearchResponse["Demands"][0]["demandDetails"][0]
            let newDemand = {
                id: null,
                demandId: firstDemand["demandId"],
                taxHeadMasterCode: taxes.ceilingTaxHead.taxHeadCode,
                taxAmount: taxes.ceilingTaxHead.estimateAmount,
                tenantId: firstDemand["tenantId"],
                collectionAmount: 0
            }
            demandSearchResponse["Demands"][0]["demandDetails"].push(newDemand)
        }

        for (demandDetail of demandSearchResponse["Demands"][0]["demandDetails"]) {
            if (demandDetail.taxHeadMasterCode == "PT_FIRE_CESS") {
                demandDetail.taxAmount = taxes.firecessTaxHead.estimateAmount
            }
            if (demandDetail.taxHeadMasterCode == "PT_DECIMAL_CEILING_DEBIT" || demandDetail.taxHeadMasterCode == "PT_DECIMAL_CEILING_DEBIT") {
                demandDetail.taxHeadMasterCode = taxes.ceilingTaxHead.taxHeadCode
                demandDetail.taxAmount = taxes.ceilingTaxHead.estimateAmount
            }
        }

        console.log("Updating demand to ", JSON.stringify(demandSearchResponse["Demands"], null, 2))
        let demandUpdateResponse = await updateDemand(demandSearchResponse["Demands"], request["RequestInfo"])

        // let updateTaxHeads = []

        // for (taxHead of demandSearchResponse["Demands"]) {
        //     updateTaxHeads.push({
        //         taxHeadCode: taxHead.taxHeadMasterCode,
        //         estimateAmount: taxHead.taxtAmount,
        //         category: taxHead.category
        //     })
        // }

        calc["totalAmount"] = taxes.totalAmount
        calc["taxAmount"] = taxes.taxAmount
        calc["rebate"] = taxes.rebate
        // calc["taxHeadEstimates"] = updateTaxHeads
        index++
    }

    return response
}

async function _createAndUpdateRequestHandler(req, res) {
    let {
        request,
        response
    } = getRequestResponse(req)

    let tenantId = request["Properties"][0]["tenantId"]

    let fireCessConfig = await getFireCessConfig(tenantId)
    console.log("Got fireCessConfig as", JSON.stringify(fireCessConfig, null, 2), tenantId)
    if (fireCessConfig && fireCessConfig.dynamicFirecess && fireCessConfig.dynamicFirecess == true) {
        console.log("----------------- inside _createAndUpdateRequestHandler --------------")
        console.log("Existing Request is", JSON.stringify(request, null, 2))
        console.log("Existing Response is", JSON.stringify(response, null, 2))
        let updatedResponse = await _createAndUpdateTaxProcessor(request, response)
        console.log("Updated Response is", JSON.stringify(updatedResponse, null, 2))
        res.json(updatedResponse);
        console.log("----------------- finished _createAndUpdateRequestHandler --------------")
    } else {
        console.log("Dynamic firecess not applicable for -", tenantId)
        res.json(response)
    }
}

function getRequestResponse(req) {
    let request, response

    if (typeof req.body.request === "string") {
        request = JSON.parse(req.body.request)
        response = JSON.parse(req.body.response)
    } else {
        request = req.body.request
        response = req.body.response
    }
    return {
        request,
        response
    }
}

router.post('/protected/punjab-pt/property/_create', asyncMiddleware(_createAndUpdateRequestHandler))

router.post('/protected/punjab-pt/property/_update', asyncMiddleware(_createAndUpdateRequestHandler))

router.post('/protected/punjab-pt/pt-calculator-v2/_estimate', asyncMiddleware(async function (req, res) {

    let {
        request,
        response
    } = getRequestResponse(req)

    let tenantId = request["CalculationCriteria"][0]["tenantId"]

    let fireCessConfig = await getFireCessConfig(tenantId)

    if (fireCessConfig && fireCessConfig.dynamicFirecess && fireCessConfig.dynamicFirecess == true) {
        console.log("----------------- inside _estimate --------------")
        console.log("Existing Request is", JSON.stringify(request, null, 2))
        console.log("Existing Response is", JSON.stringify(response, null, 2))

        let updatedResponse = _estimateTaxProcessor(request, response)
        console.log("Updated Response is", JSON.stringify(updatedResponse, null, 2))
        res.json(updatedResponse);
        console.log("----------------- finished _estimate --------------")
    } else {
        console.log("Dynamic firecess not applicable for -", tenantId)
        res.json(response);
    }
}))

app.listen(8000, () => {
    console.log("Listening on port 8000")
});

// TODO:
// Add total amount to calculations                "totalAmount": 460,
//  "taxAmount": 510,