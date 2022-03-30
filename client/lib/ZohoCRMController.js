var ZohoCRM = {
    init: function () {
        return new Promise((resolve, reject) => {
            ZOHO.embeddedApp.on('PageLoad', function (data) {
                ZohoCRM.setKeys().then(function () {
                    resolve();
                });
            })
            ZOHO.embeddedApp.init();
        });
    },
    save: function (data) {
        return new Promise((resolve, reject) => {
            if (data != undefined) {
                let promiseArray = [];
                $.each(data, function (key, val) {
                    if (val != undefined) {
                        var parameterMap = { "apiname": key, "value": val };
                        promiseArray.push(saveCRMData(parameterMap));
                    }
                });
                Promise.all(promiseArray).then(function () {
                    resolve();
                });
            }
        });
    },
    get: function (key) {
        return new Promise((resolve, reject) => {
            //        var data = { apiKeys: [key] };

            ZOHO.CRM.API.getOrgVariable(key).then(function (response) {

                if (typeof response == "string") {
                    response = JSON.parse(response);
                }
                var successResp = response.Success;
                if (typeof successResp == "string") {
                    successResp = JSON.parse(successResp);
                }
                var contentObj = successResp.Content;

                resolve(contentObj);
            }).catch(function (err) {
                reject(err);
            });
        });
    },
    delete: function (key) {
        return new Promise((resolve, reject) => {
        });
    },
    getUserData: function () {
        return new Promise((resolve, reject) => {
            ZOHO.CRM.CONFIG.getCurrentUser().then(function (data) {
                let promiseArray = [];
                var default_leftservice_id, default_rightservice_id = "";
                var crmUserName = data.users[0].full_name;
                var crmId = data.users[0].zuid;
                var email = data.users[0].email;
                ZOHO.CRM.CONFIG.getOrgInfo().then(function (dataOrg) {
                    var userData = {
                        "companyId": dataOrg.org[0].zgid,
                        "name": crmUserName,
                        "email": email,
                        "planName": "standard"
                    };
                    promiseArray.push(getLeftCRMData(leftServiceIdKey, userData));
                    promiseArray.push(getRightCRMData(rightServiceIdKey, userData));


                    Promise.all(promiseArray).then(function () {
                        var leftServiceId = userData["left_service_id"];
                        var rightServiceId = userData["right_service_id"];
                        var remoteId = leftServiceId + "_" + rightServiceId + "_" + dataOrg.org[0].zgid;
                        userData.companyId = remoteId;

                        resolve(userData);
                    });
                }).catch(function (err) {
                    reject(err);
                });
            }).catch(function (err) {
                reject(err);
            });
        });
    },
    setKeys : function(){
        return new Promise((resolve, reject) => {
            osyncIdKey = nameSpace + "__oSyncId";
            hashKey = nameSpace + "__hash";
            leftServiceIdKey = nameSpace + "__leftServiceId";
            rightServiceIdKey = nameSpace + "__rightServiceId";
            resolve();
        });
    }
};
function saveCRMData(parameterMap) {
    return new Promise((resolve, reject) => {
        ZOHO.CRM.CONNECTOR.invokeAPI("crm.set", parameterMap).then(function (data) {
            resolve(data);
        }).catch(function (err) {
            console.log("saveCRMData >>>>>> ERRROR>>>>>>> saved_after_err", err);
        });
    });
}


function getLeftCRMData(dataKey, userData) {
    return new Promise((resolve, reject) => {
        ZOHO.CRM.API.getOrgVariable(dataKey).then(function (data) {
            userData["left_service_id"] = data.Success.Content;
            resolve(data);
        });
    });
}

function getRightCRMData(dataKey, userData) {
    return new Promise((resolve, reject) => {
        ZOHO.CRM.API.getOrgVariable(dataKey).then(function (data) {
            userData["right_service_id"] = data.Success.Content;
            resolve(data);
        });
    });
}

function getRandomId() {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < 8; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}