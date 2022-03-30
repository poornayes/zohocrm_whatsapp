var module, selEntityId, phone, mobile = "";
var extensionData = "";
var ZohoWhatsappCRM = {
    service: "Zoho CRM",
    checkIsAlreadyInstalled: function (rightServiceName, leftId, rightId) {
        return new Promise((resolve, reject) => {
            apiVariableName = "whatsapp_".trim();
            ZohoWhatsappCRM.get(apiVariableName + "integId").then(function (integId) {
                if (integId != undefined && integId != "" && integId != "null") {
                    ZohoWhatsappCRM.get(apiVariableName + "hash").then(function (hash) {
                        if (hash != undefined && hash != "" && hash != "null") {
                            extensionData = {
                                "hash": hash,
                                "integId": integId
                            }

                            var url = osyncUrl + "/api/v1/omessage?leftSid=" + leftId + "&rightSid=" + rightId + "&integId=" + integId;
                            $.ajax({
                                url: url,
                                type: "GET",
                                crossDomain: true,
                                datatype: 'json',
                                headers: {
                                    'Accept': 'application/json',
                                    'Content-Type': 'application/json',
                                    "Osync-Authorization": hash
                                },
                                success: function (response, textStatus, jqXHR) {
                                
                                    if (typeof data === "string") {
                                        response = JSON.parse(data);
                                    }

                                    if (response.leftService == "false" || response.rightService == "false") {
                                        callAuthorize(extensionData);
                                        reject();

                                    } else {
                                        $("#selectWhatsApp").show();
                                        resolve(extensionData);
                                    }

                                },
                                error: function (jqXHR, textStatus, errorThrown) {
                                    
                                }
                            });
                        } else {
                            getInstallationDetails(leftId, rightId);
                        }
                    }).catch(function (err) {
                        $("#loadingSpinner").hide();
                        $("#forceAuthAction").show();
                        console.log(apiVariableName + "__hash Catch", err);
                        reject();
                    });
                } else {
                    getInstallationDetails(leftId, rightId);
                }
            }).catch(function (err) {
                $("#loadingSpinner").hide();
                $("#forceAuthAction").show();
                console.log(apiVariableName + "__integId Catch", err);
                reject();
            });
        });
    },


    init: function () {
        return new Promise((resolve, reject) => {
            ZOHO.embeddedApp.on('PageLoad', function (data) {
                //console.log("data ::: ", data);
                module = data.Entity;
                selEntityId = data.EntityId;
                resolve();
                //fetchContact(selEntityId);
            })
            ZOHO.embeddedApp.init().then(function () {
                console.log("ZCRM Whatsapp Initiation");
            })

        });
    },

    save: function (data) {
        return new Promise((resolve, reject) => {
            if (data != undefined) {
                let promiseArray = [];
                promiseArray.push(saveCRMData(data));
                Promise.all(promiseArray).then(function () {
                    resolve();
                });
            }
        });
    },
    savePhone: function (thisObj) {
        return new Promise((resolve, reject) => {
            var phoneNumber = $("#twilioPhoneNumbers").val();
            var isAcknowledgementConfigured = $("#acknowledgmentInput").is(":checked") ? true : false;
            var isAutomatedMessageConfigured = $("#automateMessage").is(":checked") ? true : false;
            var acknowledgementTemplate = $("#acknowledgementText").val();
            var friendlyName = $("#friendlyName").val();
            if (phoneNumber == undefined || phoneNumber.indexOf("Select") != -1) {
                alert("Please select valid phone");
                return false;
            }

            var payload = {
                "phoneNumber": phoneNumber,
                "acknowledgementEnabled": isAcknowledgementConfigured,
                "acknowledgement": btoa(acknowledgementTemplate),
                "smsAutomator": isAutomatedMessageConfigured,
                "friendlyName": friendlyName
            }


            if (thisObj != undefined) {
                var url = osyncUrl + '/api/v1/omessage/' + integId + '/savePhone';
                $.ajax({
                    url: url,
                    type: "POST",
                    crossDomain: true,
                    datatype: 'json',
                    data: JSON.stringify(payload),
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                        'Osync-Authorization': hash
                    },
                    success: function (response, textStatus, jqXHR) {
                        $('#addPhoneNumberWidget').modal('hide');
                        ZohoWhatsappCRM.getSavedNumbers();
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        $("#completeInstallationLoadingDiv").hide();
                        $("#installationFailed").show();
                    },
                    complete: function (jqXHR, textStatus, errorThrown) {
                    }
                });
            }
        });
    },
    getSavedNumbers: function () {
        var url = osyncUrl + '/api/v1/omessage/' + integId + '/savedNumbers';
        $.ajax({
            url: url,
            type: "GET",
            crossDomain: true,
            datatype: 'json',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Osync-Authorization': hash
            },
            success: function (response, textStatus, jqXHR) {
                var data = response.data;

                if (typeof data === "string") {
                    data = JSON.parse(data);
                }

                var template = $("#savedPhoneRow").html();
                var text = Mustache.render(template, data);
                $("#newMessageConfigDiv").html(text).show();
                $("#emptyPhoneMessage").hide();

                $("#newMessageConfigDiv").removeClass("d-flex justify-content-center").addClass("ml-5");

            },
            error: function (jqXHR, textStatus, errorThrown) {
                $("#completeInstallationLoadingDiv").hide();
                $("#installationFailed").show();
            },
            complete: function (jqXHR, textStatus, errorThrown) {
                providerObj.doCompleteInstallationProcess(integId);
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

                    Promise.all(promiseArray).then(function () {
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
    getAssociatedObjectId: function (thisObj) {
        return new Promise((resolve, reject) => {
            resolve(selEntityId);
        });
    },
    getAssociatedObjectType: function (thisObj) {
        return new Promise((resolve, reject) => {
            resolve(module);
        });
    },
    getAssociatedIntegId: function (thisObj, rightServiceName) {
        return new Promise((resolve, reject) => {
            apiVariableName = "whatsapp_".trim();

            ZohoWhatsappCRM.get(apiVariableName + "integId").then(function (integId) {
                if (integId != undefined && integId != "" && integId != "null") {
                    resolve(integId);
                } else {
                    $("#loadingSpinner").hide();
                    $("#forceAuthAction").show();
                    console.log("getAssociatedIntegId Catch");
                    reject();
                }
            }).catch(function () {
                $("#loadingSpinner").hide();
                $("#forceAuthAction").show();
                console.log("getAssociatedIntegId Catch", err);
                reject();
            });
        });
    },
    getAssociatedHash: function (thisObj, rightServiceName) {
        return new Promise((resolve, reject) => {

            apiVariableName = "whatsapp_".trim();

            ZohoWhatsappCRM.get(apiVariableName + "hash").then(function (hash) {
                if (hash != undefined && hash != "" && hash != "null") {
                    resolve(hash);
                }
            });
        });
    },

    fetchContact: function (thisObj) {
        return new Promise((resolve, reject) => {
            ZOHO.CRM.API.getRecord({ Entity: module, RecordID: selEntityId })
                .then(function (data) {
                    data = data.data;
                    phone = data[0]["Phone"];
                    mobile = data[0]["Mobile"];
                    if (phone != null && phone != "") { resolve(phone); }
                    if (mobile != null && mobile != "") { resolve(mobile); }
                    resolve();
                }).catch(function (err) {
                    reject(err);
                });

        });
    },
    doCompleteInstallationProcess: function (integId) {
        $("#installationFailed,#authorizeDiv,#enableDiv").hide();
        $("#configurationDiv,#emptyPhoneMessage").show();
        $("#afterPhone").show();
        $("#completeInstallationLoadingDiv").show();
        rightServiceName = urlParams.get("rightServiceName");

        $("#selectWhatsApp").show();

        var url = osyncUrl + '/api/v1/omessage/' + integId + '/numbers';
        $.ajax({
            url: url,
            type: "GET",
            crossDomain: true,
            datatype: 'json',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Osync-Authorization': hash
            },
            success: function (response, textStatus, jqXHR) {

                var data = response.data;
                if (data != null) {
                    if (typeof data === "string") {
                        data = JSON.parse(data);
                    }
                    var phoneList = [];
                    $.each(data.phone, function (index, value) {
                        var phoneNumber = {
                            "phone": value
                        };
                        phoneList.push(phoneNumber);
                    });
                    $("#twilioPhoneNumbers").empty();
                    var template = $("#phone_template").html();
                    var text = Mustache.render(template, phoneList);
                    $("#twilioPhoneNumbers").html(text).show();
                    $("#twilioPhoneNumbers").select2();
                    $("#loadingSpinner").hide();
                    $("#loadSMSFormPage").show();
                } else {
                    $("#noPhoneNumbers").show();
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $("#completeInstallationLoadingDiv").hide();
                $("#installationFailed").show();
            },
            complete: function (jqXHR, textStatus, errorThrown) {
            }
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


function getRandomId() {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < 8; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}

function getInstallationDetails(leftId, rightId) {
    return new Promise((resolve, reject) => {
        providerObj.getUserData().then(function (zcrmUserData) {
            companyId = zcrmUserData.companyId;
            var url = osyncUrl + "/api/v1/omessage/checkinstallation?leftSid=" + leftId + "&rightSid=" + rightId + "&companyId=" + companyId;
            $.ajax({
                url: url,
                type: "GET",
                crossDomain: true,
                datatype: 'json',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                },
                success: function (response, textStatus, jqXHR) {
                    if (typeof data === "string") {
                        response = JSON.parse(data);
                    }
                    if (response.integId != null && response.hash != null) {

                        extensionData = {
                            "hash": response.hash,
                            "integId": response.integId
                        }

                        apiVariableName = "whatsapp_".trim();

                        var integIdValueMap = {
                            "apiname": apiVariableName + "integId",
                            "value": response.integId
                        };
                        var hashValueMap = {
                            "apiname": apiVariableName + "hash",
                            "value": response.hash
                        };
                        ZohoWhatsappCRM.save(hashValueMap);
                        ZohoWhatsappCRM.save(integIdValueMap);

                        callAuthorize(extensionData);
                        reject();
                    } else {
                        reject();
                    }

                },
                error: function (jqXHR, textStatus, errorThrown) {
                }
            });
        });
    });
}