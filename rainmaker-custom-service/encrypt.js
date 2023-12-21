const ENCRYPTION_KEY = process.env.ENCRYPTION_KEY
const ENCRYPTION_IV = process.env.ENCRYPTION_IV
const JWT_KEY = process.env.JWT_KEY
const JWT_EXPIRY = process.env.JWT_EXPIRY || '5min'
const DEBUG_MODE = Boolean(process.env.DEBUG_MODE) || false;

function log(val) {
    if (DEBUG_MODE) {
        console.log(val)
    }
}

log ("EK=" + ENCRYPTION_KEY)
log ("EI=" + ENCRYPTION_IV)

var encrypt = function (pass) {
    // Include Library
    var crypto = require('crypto');
    
    // Declare Key and Block
    var key = ENCRYPTION_KEY;
    var iv = ENCRYPTION_IV;
    
    // Calc Cipher
    var cipher = crypto.createCipheriv('aes256', key, iv)
    var crypted = cipher.update(pass, 'utf8', 'base64')
    crypted += cipher.final('base64');
    
    // Return Cipher Text
    return crypted;
}


var jwt = require("jsonwebtoken");

jwt_sign = (data) => jwt.sign(data, JWT_KEY, { expiresIn: JWT_EXPIRY });

module.exports = {
    jwt_sign,
    encrypt
}