var express = require('express'),
    slash = require('express-slash');
var bodyParser = require('body-parser')
var url = require("url");
var request = require('request-promise');
const {
    asyncMiddleware
} = require('./utils/asyncMiddleware');

var app = express();
var mustache = require('mustache-express')
const Cryptr = require('cryptr');
var {
    encrypt,
    jwt_sign
} = require('./encrypt')

app.use(require('morgan')('dev'));

app.engine('html', mustache())
app.set('view engine', 'html')
app.set('views', __dirname + '/templates')
app.disable('view cache');

// https://codepen.io/graphicfreedom/pen/evaBXm

const DEBUG_MODE = Boolean(process.env.DEBUG_MODE) || false;
const PT_DEMAND_HOST = process.env.PT_DEMAND_HOST

const PT_ZERO_ASSESSMENTYEAR = process.env.PT_ZERO_ASSESSMENTYEAR || "2013-14";
const PT_ZERO_TENANTS = (process.env.PT_ZERO_TENANTS || "pb.testing").split(",");

//<PT Integration variables>
const PT_INTEGRATION_ASSESSMENTYEAR =process.env.PT_INTEGRATION_ASSESSMENTYEAR || "2013-14"
const PT_INTEGRATION_TENANTS = (process.env.PT_INTEGRATION_TENANTS || "pb.testing").split(",");

const PT_INTEGRATION_HOST = process.env.PT_INTEGRATION_HOST 
const PT_CALCULATOR_V2_HOST = process.env.PT_CALCULATOR_V2_HOST

//</PT Integration variables>

const PT_ENABLE_FC_CALC = Boolean(process.env.PT_ENABLE_FC_CALC || false);
const EGOV_MDMS_HOST = process.env.EGOV_MDMS_HOST
const EGOV_BND_LOGIN_URL = process.env.EGOV_BND_LOGIN_URL
const EGOV_BND_REDIRECT_URL = process.env.EGOV_BND_REDIRECT_URL
// const EGOV_BND_API_KEY = process.env.EGOV_BND_API_KEY
const EGOV_BND_ENCRYPTION_KEY = process.env.EGOV_BND_ENCRYPTION_KEY || "Vol0otuji0X03wSuZGI3zySUzxj7bReQ"

function log(val) {
    if (DEBUG_MODE) {
        console.log(val)
    }
}

log("ENCKEY=" + EGOV_BND_ENCRYPTION_KEY)

const cryptr = new Cryptr(EGOV_BND_ENCRYPTION_KEY);


function getUserUUID(data) {
    return data.RequestInfo.userInfo.uuid;
}

function isCitizen(data) {
    return data.RequestInfo.userInfo.roles.filter(role => role.code.toUpperCase() == "CITIZEN").length > 0 
}

function getUserID(data) {
    return data.RequestInfo.userInfo.id;
}

function getIntegrateYearDemand(demands){
    for(demand of demands["Demands"]){
        let demandYear = new Date(demand["taxPeriodFrom"]).getFullYear();
        let integrationYear = PT_INTEGRATION_ASSESSMENTYEAR.split("-")[0];
        let demandStatus = demand["status"]

        log("getIntegrateYearDemand> Demand year: "+ demandYear+" Integrated Year: "+integrationYear+" Demand Status: "+demandStatus);

        if((demandYear == integrationYear) && (demandStatus == "ACTIVE")){
            demands["Demands"] = [demand];
            break;
        }
        
    }

    log("Response from getIntegrateYearDemand: "+ JSON.stringify(demands));

    return demands;
}

function isValidDemand(demands){
    let count = 0;
    for(demand of demands["Demands"]){
        demandYear = new Date(demand["taxPeriodFrom"]).getFullYear()
        integrationYear = PT_INTEGRATION_ASSESSMENTYEAR.split("-")[0]
        demandStatus = demand["status"]
        if((demandYear == integrationYear) && (demandStatus == "ACTIVE")){
            count++;
        }
    }

    return count == 1;
}

function isReceiptGenerated(demand){
    for (demandDetail of demand["Demands"][0]["demandDetails"]) 
        {
            if(demandDetail.collectionAmount || 0 > 0){
                return true;
            }
        }
    return false;
}

async function getFireCessConfig(tenantId) {
    let fireCessConfig = await request.post({
        url: url.resolve(EGOV_MDMS_HOST, "/egov-mdms-service/v1/_search?tenantId=" + tenantId),
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

if (PT_CALCULATOR_V2_HOST === undefined) {
    throw Error("PT_CALCULATOR_V2_HOST environment variable needs to be configured to run this")
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

router.post('/protected/bndlogin/unlinkAccount', asyncMiddleware(async function (req, res) {
    let uuid = getUserUUID(req.body);

    data = await db.any("DELETE FROM custom_eg_user_metatdata where user_id = $1 and key = $2", [uuid, 'BND_CREDENTIALS']);
    res.status(200).send({})
}));

router.post('/protected/bndlogin/linkAccount', asyncMiddleware(async function (req, res) {
    let username = req.body.username
    let password = req.body.password
    let uuid = getUserUUID(req.body);
    let payload = {
        Username: username,
        Password: encrypt(password)
    }

    log("inside linkAccount")
    log(payload)

    let response = await request.post(EGOV_BND_LOGIN_URL, {
        json: payload
    })

    log(response)

    if (response.sys_message && response.sys_message == 'Invalid User and Password') {
        res.status(200).send({
            code: "INVALID_CREDENTIALS",
            message: "Invalid User and Password"
        })
        return
    } else if (response.sys_message && response.sys_message == 'INTERNAL APPLICATION ERROR') {
        res.status(200).send({
            code: "ERROR",
            message: "Something went wrong"
        })
        return
    }

    loginID = response["data"][0]["loginID"];
    data = await db.any("select value from custom_eg_user_metatdata where user_id = $1 and key = $2", [uuid, 'BND_CREDENTIALS']);
    value = {
        username,
        GUID: cryptr.encrypt(loginID)
    }

    if (data.length == 0) {
        await db.any("insert into custom_eg_user_metatdata(key, user_id, value) values ($1, $2, $3:json)",
            ['BND_CREDENTIALS', uuid, value])
    } else {
        await db.any("update custom_eg_user_metatdata set value = $3:json where key = $1 and user_id = $2",
            ['BND_CREDENTIALS', uuid, value])
    }
    res.json({
        code: "SUCCESS",
        redirect: EGOV_BND_REDIRECT_URL + jwt_sign({
            loginID: encrypt(loginID)
        })
    })
}));

router.post('/protected/bndlogin', asyncMiddleware(async function (req, res) {
    try {
        let uuid = getUserUUID(req.body);
        data = await db.any("select value from custom_eg_user_metatdata where user_id = $1 and key = $2", [uuid, 'BND_CREDENTIALS']);

        if (data.length == 0) {
            res.json({
                "code": "NO_CREDENTIAL_MAPPING",
                "message": "Please update your Birth and Death credentials"
            })
            return
        }

        let loginID = cryptr.decrypt(data[0]["value"]["GUID"])

        res.json({
            code: "SUCCESS",
            redirect: EGOV_BND_REDIRECT_URL + jwt_sign({
                loginID: encrypt(loginID)
            })
        })

    } catch (ex) {
        console.log("Exception occured while login", ex)
        res.json({
            "code": "ERROR",
            "message": "Failed to login to BND - " + ex.toString()
        })
    }

}));

// function getFireCessPercentage(propertyDetails, fireCessConfig) {
//     // let propertyDetails = request["CalculationCriteria"][0]["propertyDetails"][0]

//     let propertyUsageCategoryMajor = propertyDetails["usageCategoryMajor"]
//     let units = propertyDetails["units"]
//     let propertyAttributes = propertyDetails["additionalDetails"]
//     let unitSet = new Set()

//     for (unit of units) {
//         unitSet.add(unit["usageCategoryMajor"])
//     }
//     let firecess_category_major = 0;
//     let firecess_building_height = 0;
//     let firecess_inflammable = 0;

//     if (propertyUsageCategoryMajor == "RESIDENTIAL" || (unitSet.size == 1 && unitSet.has("RESIDENTIAL"))) {
//         // There is no category major firecess applicable as it i
//         firecess_category_major = 0
//     } else {
//         firecess_category_major = fireCessConfig.dynamicRates.firecess_category_major;
//     }

//     if (propertyAttributes &&
//         propertyAttributes.heightAbove36Feet &&
//         propertyAttributes.heightAbove36Feet.toString() == "true") {
//         // height is above 36 feet
//         firecess_building_height = fireCessConfig.dynamicRates.firecess_building_height;
//     }

//     if (propertyAttributes &&
//         propertyAttributes.inflammable &&
//         propertyAttributes.inflammable.toString() == "true") {
//         // height is above 36 feet
//         firecess_inflammable = fireCessConfig.dynamicRates.firecess_inflammable;
//     }

//     return {
//         firecess_inflammable,
//         firecess_building_height,
//         firecess_category_major,
//         firecess: firecess_category_major + firecess_building_height + firecess_inflammable
//     }
// }

// function calculateNewFireCess(taxHeads, firecess_percent, taxField, taxHeadCodeField) {

//     let applicablePropertyTax = 0
//     for (taxHead of taxHeads) {
//         if (taxHead[taxHeadCodeField] == "PT_TAX") {
//             applicablePropertyTax += taxHead[taxField]
//         } else if (taxHead[taxHeadCodeField] == "PT_UNIT_USAGE_EXEMPTION") {
//             applicablePropertyTax += taxHead[taxField]
//         } else if (taxHead[taxHeadCodeField] == "PT_OWNER_EXEMPTION") {
//             applicablePropertyTax += taxHead[taxField]
//         }
//     }

//     return round(applicablePropertyTax * (firecess_percent / 100), 2);
// }

async function findDemandForConsumerCode(consumerCode, tenantId, service, RequestInfo) {

    log("Got Request to Find Demand for Comsumer code: "+consumerCode+" tenantid : "+tenantId+" Service : "+service);

    let demandSearchResponse = await request.post({
        url: url.resolve(PT_DEMAND_HOST, "/billing-service/demand/_search?tenantId=" + tenantId +
            "&consumerCode=" + consumerCode + "&businessService=" + service),
        body: {
            RequestInfo
        },
        json: true
    })

    log("Got response from demand search : "+ JSON.stringify(demandSearchResponse));

    return demandSearchResponse;
}



async function updateDemand(demands, RequestInfo) {
    let demandUpdateResponse = await request.post({
        url: url.resolve(PT_DEMAND_HOST, "/billing-service/demand/_update"),
        body: {
            RequestInfo,
            "Demands": demands
        },
        json: true
    })

    return demandUpdateResponse;
}

async function getOldRequestBody(requestBody) {
    log("Translate Call for Property: "+ JSON.stringify(requestBody));
    let CalculationCriteria = await request.post({
        url: url.resolve(PT_CALCULATOR_V2_HOST, "/pt-calculator-v2/propertytax/v2/_translate"),
        body: requestBody,
        json: true
    })

    log("Response from Translate API: " +JSON.stringify(CalculationCriteria));

    return CalculationCriteria;
}

async function findEstimate(requestBody){

    log("Got Request to find estimate : "+ JSON.stringify(requestBody))

    let estimateRes = await request.post({
        url: url.resolve(PT_CALCULATOR_V2_HOST, "/pt-calculator-v2/propertytax/v2/_estimate"),
        body: requestBody,
        json: true
    })

    log("Got response from estimate : "+ JSON.stringify(estimateRes));

    return estimateRes;
}
// function _estimateTaxProcessor(request, response, fireCessConfig) {
//     response = _estimateZeroTaxProcessor(request, response);

//     let index = 0;
//     for (let calc of request["CalculationCriteria"]) {
//         let fireCessPercentage = getFireCessPercentage(calc["property"]["propertyDetails"][0], fireCessConfig)

//         let updateFirecessAmount = calculateNewFireCess(response["Calculation"][0]["taxHeadEstimates"], fireCessPercentage.firecess, "estimateAmount", "taxHeadCode")
//         let taxes = getUpdateTaxSummary(response["Calculation"][index], updateFirecessAmount, "taxHeadCode", "estimateAmount")

//         response["Calculation"][index]["totalAmount"] = taxes.totalAmount
//         response["Calculation"][index]["taxAmount"] = round(taxes.taxAmount, 2)
//         response["Calculation"][index]["rebate"] = taxes.rebate

//         index++
//     }

//     return response;
// }


async function _estimateIntegrationTaxProcessor(req1, res1) {

    log("Calling PMIDC estimate API: "+ JSON.stringify(res1) )

    let estimate = await request.post({
        url: url.resolve(PT_INTEGRATION_HOST, "/apt_estimate_pt_2013/api_estimate_pt_2013"),
        body: {request:req1, response:res1},
        json: true
    })

    log("Got response from PMIDC estimate API: " + JSON.stringify(estimate))

    return estimate;
}



function _estimateZeroTaxProcessor(request, response) {
    let index = 0;

    for (let calc of response["Calculation"]) {
        let assessmentYear = request["CalculationCriteria"][index]["assessmentYear"]
        let tenantId = request["CalculationCriteria"][index]["tenantId"]
        let newTotal = 0;

        if (isCitizen(request) && assessmentYear == PT_ZERO_ASSESSMENTYEAR) {
            
            data =  
                {
                    "ResponseInfo":null,
                    "Errors":[
                        {
                            "code":"CitizenOnlineNotAllowed",
                            "message":"Sorry but online assessment for " + PT_ZERO_ASSESSMENTYEAR + " is not allowed. Please make the payment at the counter",
                            "description": "Sorry but online assessment for " + PT_ZERO_ASSESSMENTYEAR + " is not allowed. Please make the payment at the counter",
                            "params":null
                        }
                    ]
                }
            return data;
            
        }

        if (!(assessmentYear == PT_ZERO_ASSESSMENTYEAR && PT_ZERO_TENANTS.indexOf(tenantId) >= 0))
            continue
    
        let taxHeads = calc["taxHeadEstimates"];

        for (taxHead of taxHeads) {
            if (taxHead.taxHeadCode != "PT_ADHOC_PENALTY" && taxHead.taxHeadCode != 'PT_ADVANCE_CARRYFORWARD') {
                taxHead.estimateAmount = 0
            } else if (taxHead.taxHeadCode == 'PT_ADVANCE_CARRYFORWARD') {
                newTotal += taxHead.estimateAmount
            } else {
                newTotal += taxHead.estimateAmount
            }
        }

        calc["totalAmount"] = newTotal
        calc["taxAmount"] = 0
        calc["rebate"] = 0
        calc["penalty"] = newTotal
        calc["exemption"] = 0

        index++
    }

    return response;
}


// function getUpdateTaxSummary(calculation, newTaxAmount, taxHeadCodeField, taxAmountField) {
//     let ceilingTaxHead = null;
//     let firecessTaxHead = null;

//     let taxAmount = 0,
//         penalty = 0,
//         rebate = 0,
//         exemption = 0
//     let taxHeads = calculation["taxHeadEstimates"]
//     for (taxHead of taxHeads) {
//         if (taxHead[taxHeadCodeField] == "PT_FIRE_CESS") {
//             let existingTaxAmount = taxHead[taxAmountField]
//             taxHead[taxAmountField] = newTaxAmount
//             firecessTaxHead = taxHead
//             taxAmount += newTaxAmount
//             if (DEBUG_MODE) {
//                 taxHead.oldEstimateAmount = existingTaxAmount
//             }
//         } else {
//             switch (taxHead[taxHeadCodeField]) {
//                 case "PT_DECIMAL_CEILING_CREDIT":
//                 case "PT_DECIMAL_CEILING_DEBIT":
//                 case "PT_ROUNDOFF":
//                     ceilingTaxHead = taxHead
//                     break
//                 case "PT_ADVANCE_CARRYFORWARD":
//                     exemption += taxHead[taxAmountField]
//                     break
//                 default:
//                     switch (taxHead.category) {
//                         case "PENALTY":
//                             penalty += taxHead[taxAmountField]
//                             break
//                         case "TAX":
//                             taxAmount += taxHead[taxAmountField]
//                             break
//                         case "REBATE":
//                             rebate += taxHead[taxAmountField]
//                             break
//                         case "EXEMPTION":
//                             exemption += taxHead[taxAmountField]
//                             break
//                         default:
//                             console.log("Going to default for taxHead", taxHead)
//                             taxAmount += taxHead[taxAmountField]
//                     }

//             }
//         }
//     }

//     taxAmount = round(taxAmount, 2)
//     penalty = round(penalty, 2)
//     exemption = round(exemption, 2)
//     rebate = round(rebate, 2)

//     let totalAmount = taxAmount + penalty - rebate - exemption

//     totalAmount = round(totalAmount, 2)
//     let fractionAmount = totalAmount - Math.trunc(totalAmount)
//     let newCeilingTax = false

//     if (ceilingTaxHead == null && fractionAmount == 0) {

//     } else {
//         let ceilingDelta = 0.0;

//         if (ceilingTaxHead == null) {
//             ceilingTaxHead = {
//                 taxHeadCode: "",
//                 estimateAmount: 0,
//                 category: null
//             }
//             newCeilingTax = true
//             taxHeads.push(ceilingTaxHead)
//         }

//         if (fractionAmount < 0.5) {
//             ceilingDelta = parseFloat(fractionAmount.toFixed(2))
//             totalAmount = Math.trunc(totalAmount)
//             ceilingTaxHead[taxHeadCodeField] = "PT_ROUNDOFF"
//             ceilingTaxHead[taxAmountField] = -ceilingDelta
//             rebate += ceilingDelta
//         } else {
//             ceilingDelta = parseFloat((1 - fractionAmount).toFixed(2))

//             totalAmount = Math.trunc(totalAmount) + 1
//             ceilingTaxHead[taxHeadCodeField] = "PT_ROUNDOFF"
//             ceilingTaxHead[taxAmountField] = ceilingDelta
//             taxAmount += ceilingDelta
//         }
//     }

//     return {
//         taxHeads,
//         rebate,
//         totalAmount,
//         taxAmount,
//         newCeilingTax,
//         ceilingTaxHead,
//         firecessTaxHead
//     }
// }

async function _createAndUpdateZeroTaxProcessor(request, response) {
    let index = 0
    for (reqProperty of request["Properties"]) {

        let resProperty = response["Properties"][index]
        let propertyId = resProperty["propertyId"]

        let assessmentNumber = resProperty["propertyDetails"][0]["assessmentNumber"]

        let assessmentYear = resProperty["propertyDetails"][0]["financialYear"]
        let tenantId = reqProperty["tenantId"]

        if (isCitizen(request) && assessmentYear === PT_ZERO_ASSESSMENTYEAR) {
            data =  
                {
                    "ResponseInfo":null,
                    "Errors":[
                        {
                            "code":"CitizenOnlineNotAllowed",
                            "message":"Sorry but online assessment for " + PT_ZERO_ASSESSMENTYEAR + " is not allowed. Please make the payment at the counter",
                            "description": "Sorry but online assessment for " + PT_ZERO_ASSESSMENTYEAR + " is not allowed. Please make the payment at the counter",
                            "params":null
                        }
                    ]
                };
            return data;
        }

        if (!(assessmentYear == PT_ZERO_ASSESSMENTYEAR && PT_ZERO_TENANTS.indexOf(tenantId) >= 0))
            continue

        request_info = request["RequestInfo"] || request["requestInfo"]

        let consumerCode = propertyId + ":" + assessmentNumber
        let service = "PT"
        let calc = response["Properties"][index]["propertyDetails"][0]["calculation"]

        let newTotal = 0;

        let demandSearchResponse = await findDemandForConsumerCode(consumerCode, tenantId, service, request["RequestInfo"])

        for (demandDetail of demandSearchResponse["Demands"][0]["demandDetails"]) {
            if (demandDetail.taxHeadMasterCode != "PT_ADHOC_PENALTY" && demandDetail.taxHeadMasterCode != 'PT_ADVANCE_CARRYFORWARD') {
                demandDetail.taxAmount = 0
            } else if (demandDetail.taxHeadMasterCode == 'PT_ADVANCE_CARRYFORWARD') {
                newTotal += demandDetail.taxAmount
            } else {
                newTotal += demandDetail.taxAmount
            }
        }

        let taxHeads = calc["taxHeadEstimates"];

        for (taxHead of taxHeads) {
            if (taxHead.taxHeadCode != "PT_ADHOC_PENALTY" && taxHead.taxHeadCode == 'PT_ADVANCE_CARRYFORWARD') {
                taxHead.estimateAmount = 0
            }
        }
        let demandUpdateResponse = await updateDemand(demandSearchResponse["Demands"], request["RequestInfo"])

        calc["taxAmount"] = 0;
        calc["exemption"] = 0;
        calc["totalAmount"] = newTotal;
        calc["rebate"] = 0
        calc["penanlty"] = newTotal
        index++
    }

    return response;
}

async function _createAndUpdateIntegrationTaxProcessor(req, response){

        console.log("REQ : ", JSON.stringify(req))
        console.log("RESPONSE : ", JSON.stringify(response))

        let reqProperty = req["Assessment"];

        let propertyId = reqProperty["propertyId"]

        let assessmentYear = reqProperty["financialYear"]
        let tenantId = reqProperty["tenantId"]

        if (!(assessmentYear == PT_INTEGRATION_ASSESSMENTYEAR && PT_INTEGRATION_TENANTS.indexOf(tenantId) >= 0))
            return response;
            
        let oldRequestbody = await getOldRequestBody(req)

        oldRequestbody["CalculationCriteria"][0]["assessmentYear"] = assessmentYear;
            // assessmentYear field was there in old request body but not present in new request body Without this field we will get null pointer exception.

        log("Got Assessment CREATE/ UPDATE request for tenantid: "+tenantId+" and finanancial year: "+assessmentYear)
        log("Assessment CREATE/ UPDATE Request body: "+ JSON.stringify(reqProperty) )

        let estimateResponseBody = await findEstimate(req)
        console.log("After Getting Response from entimate sending it to PMIDC TO get the Estimate Response")
        let estimateResponse = await _estimateIntegrationTaxProcessor(oldRequestbody, estimateResponseBody)


        
        request_info = req["RequestInfo"] || req["requestInfo"]

        let consumerCode = propertyId
        let service = "PT"
        let calc = estimateResponse["Calculation"][0]
        let taxHeads = calc["taxHeadEstimates"];
        let createTaxHeadsArray = {};
        let TaxHeadsType= {};  //Collecting All the Tax head coming from PMIDC
        
        for(taxHead of taxHeads){
            createTaxHeadsArray[taxHead.taxHeadCode] = taxHead.estimateAmount;
            TaxHeadsType[taxHead.taxHeadCode] = taxHead.estimateAmount;
            //print(texthead)
            console.log(taxHead.taxHeadCode , ": ", taxHead.estimateAmount)
        }

        let demandSearchResponse = await findDemandForConsumerCode(consumerCode, tenantId, service, req["RequestInfo"])

        if( !isValidDemand(demandSearchResponse)){
            //Throw Error
            data =  
            {
                "ResponseInfo":null,
                "Errors":[
                    {
                        "code":"MultipleActiveDemandForOneFinancialYear",
                        "message":"There are multiple demand for property id : "+ propertyId +" for financial year" + PT_INTEGRATION_ASSESSMENTYEAR,
                        "description": "There multiple demand  for property id : "+ propertyId +" for financial year" + PT_INTEGRATION_ASSESSMENTYEAR,
                        "params":null
                    }
                ]
            };
        return data; 
        }

        demandSearchResponse = getIntegrateYearDemand(demandSearchResponse)

        log("Search Demand response For Integrated Year: " + JSON.stringify(demandSearchResponse))

        if(isReceiptGenerated(demandSearchResponse)){
            //Throw Error
            data =  
                {
                    "ResponseInfo":null,
                    "Errors":[
                        {
                            "code":"MultiplePaymentNotAllowed",
                            "message":"There already exists a recipt for property id : "+ propertyId +" for financial year" + PT_INTEGRATION_ASSESSMENTYEAR,
                            "description": "There already exists a recipt for property id : "+ propertyId +" for financial year" + PT_INTEGRATION_ASSESSMENTYEAR,
                            "params":null
                        }
                    ]
                };
            return data;
        }


        let newTotal = 0;
        
        for (demandDetail of demandSearchResponse["Demands"][0]["demandDetails"]) 
        {
            if(demandDetail.taxAmount){
                demandDetail.taxAmount = 0
            }
            delete TaxHeadsType[demandDetail.taxHeadMasterCode]//Deleting tax head codes present in both TaxHeadsType and demandDetail
            console.log("Deleted taxhead from TaxHeadsType: ", demandDetail.taxHeadMasterCode);
        }
        
        console.log("After Deleting the taxhead present in demand: ", JSON.stringify(TaxHeadsType));
        console.log("demandSearchResponse AFTER MAKING EVERYTHING ZERO : ", JSON.stringify(demandSearchResponse))

        if(!(Object.keys(TaxHeadsType).length === 0)){
            for(taxHead in TaxHeadsType){
                taxHeadObj={};
                taxHeadObj["taxHeadMasterCode"] = taxHead;
                taxHeadObj["taxAmount"] = 0;
                taxHeadObj["collectionAmount"] = 0;
                demandSearchResponse["Demands"][0]["demandDetails"].push(taxHeadObj); // Adding those obj to demand details for which taxhead there was no entry in it
                console.log("Pushing obj", taxHeadObj);
            }
        }

        console.log("After Adding All the tax head demandDetail: ", demandSearchResponse["Demands"][0]["demandDetails"])

        for (demandDetail of demandSearchResponse["Demands"][0]["demandDetails"]) 
        {
            if(createTaxHeadsArray[demandDetail.taxHeadMasterCode])
            {
                demandDetail.taxAmount = createTaxHeadsArray[demandDetail.taxHeadMasterCode]; 
                createTaxHeadsArray[demandDetail.taxHeadMasterCode] =0; // Incase of Multiple PT_ROUNDOFF
                newTotal += demandDetail.taxAmount;
            }
        }

        console.log("demandSearchResponse AFTER SETTING THE VALUE: ", JSON.stringify(demandSearchResponse))

        let demandUpdateResponse = await updateDemand(demandSearchResponse["Demands"], req["RequestInfo"])

        console.log("demandUpdateResponse : ", JSON.stringify(demandUpdateResponse))

        calc["taxAmount"] = 0;
        calc["exemption"] = 0;
        calc["totalAmount"] = newTotal;
        calc["rebate"] = 0;
        calc["penalty"] = 0;
        
        log("Demand Updated with Details : " + demandUpdateResponse )
    
    return response;

}

// async function _createAndUpdateTaxProcessor(request, response, fireCessConfig) {

//     let index = 0
//     for (reqProperty of request["Properties"]) {

//         let resProperty = response["Properties"][index]
//         let propertyId = resProperty["propertyId"]

//         let assessmentNumber = resProperty["propertyDetails"][0]["assessmentNumber"]

//         let consumerCode = propertyId + ":" + assessmentNumber
//         let service = "PT"
//         let tenantId = reqProperty["tenantId"]

//         let demandSearchResponse = await findDemandForConsumerCode(consumerCode, tenantId, service, request["RequestInfo"])

//         let fireCessPercentage = getFireCessPercentage(reqProperty["propertyDetails"][0], fireCessConfig)

//         if (DEBUG_MODE) {
//             demandSearchResponse["Demands"][0]["firecess"] = fireCessPercentage
//         }
//         let calc = response["Properties"][index]["propertyDetails"][0]["calculation"]
//         let updateFirecessTax = calculateNewFireCess(calc["taxHeadEstimates"], fireCessPercentage.firecess, "estimateAmount", "taxHeadCode")

//         let taxes = getUpdateTaxSummary(calc,
//             updateFirecessTax, "taxHeadCode", "estimateAmount")

//         if (taxes.newCeilingTax) {
//             let firstDemand = demandSearchResponse["Demands"][0]["demandDetails"][0]
//             let newDemand = {
//                 id: null,
//                 demandId: firstDemand["demandId"],
//                 taxHeadMasterCode: taxes.ceilingTaxHead.taxHeadCode,
//                 taxAmount: taxes.ceilingTaxHead.estimateAmount,
//                 tenantId: firstDemand["tenantId"],
//                 collectionAmount: 0
//             }
//             demandSearchResponse["Demands"][0]["demandDetails"].push(newDemand)
//         }

//         for (demandDetail of demandSearchResponse["Demands"][0]["demandDetails"]) {
//             if (demandDetail.taxHeadMasterCode == "PT_FIRE_CESS") {
//                 demandDetail.taxAmount = taxes.firecessTaxHead.estimateAmount
//             }
//             if (demandDetail.taxHeadMasterCode == "PT_ROUNDOFF" && demandDetail.adjustedAmount == 0.0) {
//                 demandDetail.taxHeadMasterCode = taxes.ceilingTaxHead.taxHeadCode
//                 demandDetail.taxAmount = taxes.ceilingTaxHead.estimateAmount
//             }
//         }

//         let demandUpdateResponse = await updateDemand(demandSearchResponse["Demands"], request["RequestInfo"])

//         // let updateTaxHeads = []

//         // for (taxHead of demandSearchResponse["Demands"]) {
//         //     updateTaxHeads.push({
//         //         taxHeadCode: taxHead.taxHeadMasterCode,
//         //         estimateAmount: taxHead.taxtAmount,
//         //         category: taxHead.category
//         //     })
//         // }

//         calc["totalAmount"] = taxes.totalAmount
//         calc["taxAmount"] = taxes.taxAmount
//         calc["rebate"] = taxes.rebate
//         // calc["taxHeadEstimates"] = updateTaxHeads
//         index++
//     }

//     return response
// }

async function _createAndUpdateRequestHandler(req, res) {
    let {
        request,
        response
    } = getRequestResponse(req)


        let assessmentYear = request["Assessment"]["financialYear"]
        let tenantId = request["Assessment"]["tenantId"] 

        if (assessmentYear == PT_ZERO_ASSESSMENTYEAR && PT_ZERO_TENANTS.indexOf(tenantId) >= 0){
            response = await _createAndUpdateZeroTaxProcessor(request, response)
        }else if(assessmentYear == PT_INTEGRATION_ASSESSMENTYEAR && PT_INTEGRATION_TENANTS.indexOf(tenantId) >= 0){
            log("Got Request for Assessment Create and Update")

            response = await _createAndUpdateIntegrationTaxProcessor(request, response)
        }
    
    // if (!PT_ENABLE_FC_CALC)
    if ("Errors" in response)
        res.status(400).json(response)
    else
        res.json(response);

    // firecess logic is enabled, so execute it
    // let tenantId = request["Properties"][0]["tenantId"]

    // let fireCessConfig = await getFireCessConfig(tenantId)
    // if (fireCessConfig && fireCessConfig.dynamicFirecess && fireCessConfig.dynamicFirecess == true) {
    //     let updatedResponse = await _createAndUpdateTaxProcessor(request, response, fireCessConfig)
    //     res.json(updatedResponse);
    // } else {
    //     res.json(response)
    // }
}

function getRequestResponse(req) {
    let request = null,
        response = null

    if (typeof req.body.request === "string") {
        request = JSON.parse(req.body.request)
        if (req.body.response)
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

router.post('/protected/punjab-pt/assessment/_create',  asyncMiddleware(_createAndUpdateRequestHandler))

router.post('/protected/punjab-pt/assessment/_update', asyncMiddleware(_createAndUpdateRequestHandler))

router.post('/open/punjab-pt/payu/confirm', asyncMiddleware((async function (req, res) {
    let return_data = req.body;
    original_callback = req.query.original_callback;
    delete req.query['original_callback'];
    let txnid = req.query.eg_pg_txnid
    delete req.query['eg_pg_txnid'];

    new_query_params = Object.assign({}, return_data, req.query);
    redirect_url = url.format(
        {
            pathname: original_callback,
            query: new_query_params
        }
    )
    //ensuring the first query param is eg_pg_txnid
    redirect_url = redirect_url.replace('?', '?'+ 'eg_pg_txnid=' + txnid +'&')
    res.redirect(redirect_url);
})))

router.post('/protected/punjab-pt/pre-hook/pg-service/transaction/v1/_create', asyncMiddleware((async function (req, res) {
    let {
        request
    } = getRequestResponse(req)

    if (request['Transaction']['tenantId'] == 'pb.jalandhar' || request['Transaction']['tenantId'] == 'pb.testing') {
        let original_callback = request['Transaction']['callbackUrl'];
        request['Transaction']['gateway'] = 'PAYU'
        url_callback = url.parse(original_callback)

        url_callback.query = url_callback.query || {};

        url_callback.query['original_callback'] = url_callback.path;

        url_callback.path = '/customization/open/punjab-pt/payu/confirm';
        url_callback.pathname = '/customization/open/punjab-pt/payu/confirm';

        request['Transaction']['callbackUrl'] = url.format(url_callback);
    }

    res.json(request);
})));

router.post('/protected/punjab-pt/pt-calculator-v2/_estimate', asyncMiddleware(async function (req, res) {

    let {
        request,
        response
    } = getRequestResponse(req)


    // let oldRequestbody = getOldRequestBody(request) 

    // oldRequestbody["CalculationCriteria"][0]["assessmentYear"] = oldRequestbody["CalculationCriteria"][0]["property"]["propertyDetails"][0]["financialYear"]
    // // assessmentYear field was there in old request body but not present in new request body so we are adding this field.

    // request = oldRequestbody;

    let tenantId = request["Assessment"]["tenantId"]
    let assessmentYear = request["Assessment"]["financialYear"]


    let oldRequestbody = await getOldRequestBody(request); 
    oldRequestbody["CalculationCriteria"][0]["assessmentYear"] =  assessmentYear;
    // assessmentYear field was there in old request body but not present in new request body Without this field we will get null pointer exception.


    if (assessmentYear == PT_ZERO_ASSESSMENTYEAR && PT_ZERO_TENANTS.indexOf(tenantId) >= 0){
        response = _estimateZeroTaxProcessor(request, response)
    }
    else if (assessmentYear == PT_INTEGRATION_ASSESSMENTYEAR){
            
        if(PT_INTEGRATION_TENANTS.indexOf(tenantId) >= 0){
                log(":: Estimate request Received ::");

                log("Got request for tenantid: "+tenantId+" and finanancial year: "+assessmentYear)
                log("Request body: "+ JSON.stringify(request));
                
                response = await _estimateIntegrationTaxProcessor(oldRequestbody, response)
        } else if(isCitizen(request)){
            data =  
            {
                "ResponseInfo":null,
                "Errors":[
                    {
                        "code":"CitizenOnlineNotAllowed",
                        "message":"Sorry but online assessment for " + PT_INTEGRATION_ASSESSMENTYEAR + " is not allowed. Please make the payment at the counter",
                        "description": "Sorry but online assessment for " + PT_INTEGRATION_ASSESSMENTYEAR + " is not allowed. Please make the payment at the counter",
                        "params":null
                    }
                ]
            }
        response = data;
        }

    }


    // if (!PT_ENABLE_FC_CALC)
    if ("Errors" in response)
        res.status(400).json(response)
    else
        res.json(response);

    // let tenantId = request["CalculationCriteria"][0]["tenantId"]

    // let fireCessConfig = await getFireCessConfig(tenantId)

    // if (fireCessConfig && fireCessConfig.dynamicFirecess && fireCessConfig.dynamicFirecess == true) {
    //     let updatedResponse = _estimateTaxProcessor(request, response, fireCessConfig)
    //     res.json(updatedResponse);
    // } else {
    //     res.json(response);
    // }
}))

app.listen(8000, () => {
    console.log("Listening on port 8000")
});

// TODO:
// Add total amount to calculations                "totalAmount": 460,
//  "taxAmount": 510,
