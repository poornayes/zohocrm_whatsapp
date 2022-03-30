var urlParams = new URLSearchParams(window.location.search);
var integId = "";
var hash = "";
var selectedIds = "";
var resource = "";

var PipeDriveSMS = {
    service: "PipeDriveSMS",
    checkIsAlreadyInstalled: function (rightServiceName,leftId,rightId) {
        return new Promise((resolve, reject) => {
            var companyId = urlParams.get("companyId");
            var leftServiceId = urlParams.get("leftServiceId");
            var rightServiceId = urlParams.get("rightServiceId");
            if (companyId != null || companyId != "") {
                $("#installationFailed").hide();
                $("#completeInstallationLoadingDiv").show();

                var url = osyncUrl + '/api/v1/omessage/' + leftServiceId + '/integrate/' + rightServiceId + "?selectedIds=" + selectedIds + "&resource=" + resource + "&companyId=" + companyId;

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
                        console.log("response >>>>>>>>>", response);
                        integId = response.data.integId;
                        hash = response.data.hash;
                        var extensionData = {
                            "hash": hash,
                            "integId": integId
                        }
                        resolve(extensionData);
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        $("#completeInstallationLoadingDiv").hide();
                        $("#installationFailed").show();
                        reject();
                    },
                    complete: function (jqXHR, textStatus, errorThrown) {
                    }
                });
            }
        });
    },
    init: function () {
        return new Promise((resolve, reject) => {
            console.log("pipedrive initiated");
            resolve();
        });
    }, save: function (data) {
        return new Promise((resolve, reject) => {
            if (data != undefined) {
                resolve();
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
                alert("Please select validate phone");
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
                        ZohoCRM.getSavedNumbers();
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        $("#completeInstallationLoadingDiv").hide();
                        $("#installationFailed").show();
                    },
                    complete: function (jqXHR, textStatus, errorThrown) {
                        var domain = getDomain();
                        console.log("domain>>>>", domain);
                        $('#installationCompletedPipeDriveSuccessfully').html('successfully configured, Please go to your<a href="https://' + domain + '.pipedrive.com"> pipedrive </a>page to access Twilio App');
                        $("#installationCompletedPipeDriveSuccessfully").show();
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
            resolve();
        });
    },
    delete: function (key) {
        return new Promise((resolve, reject) => {
            resolve();
        });
    },
    getUserData: function () {
        return new Promise((resolve, reject) => {
            resolve();
        });
    },
    getAssociatedObjectId: function (thisObj) {
        return new Promise((resolve, reject) => {
            console.log("getAssociatedObjectId>>>>>>>selectedIds>>>>>>", urlParams.get("selectedIds"));
            resolve(urlParams.get("selectedIds"));
        });
    },
    getAssociatedObjectType: function (thisObj) {
        return new Promise((resolve, reject) => {
            resolve(urlParams.get("resource"));
        });
    },
    getAssociatedIntegId: function (thisObj,rightServiceName) {
        return new Promise((resolve, reject) => {
            resolve(urlParams.get("integId"));
        });
    },
    getAssociatedHash: function (thisObj,rightServiceName) {
        return new Promise((resolve, reject) => {
            resolve(urlParams.get("hash"));
        });
    },
    fetchContact: function (thisObj) {
        return new Promise((resolve, reject) => {
            resolve("+" + $.trim(urlParams.get("phone")));
            if (urlParams.get("resource") != "organization") {
                $("#toPhoneNumber").attr("disabled", "disabled");
            }
        });
    },
    doAfterSuccessfulEnable: function (dataObj) {
        console.log("Pipedrive>>>>>>dataObj>>>>", dataObj);
        $("#leftButtonLoadingDiv").show();
        pipedriveAuthorize(dataObj);
    },
    doCompleteInstallationProcess: function (integId) {
        $("#installationFailed").hide();
        $("#configurationDiv").show();
        $("#completeInstallationLoadingDiv").show();
        var url = osyncUrl + '/api/v1/omessage/completeinstallation?integId=' + integId;
        $.ajax({
            url: url,
            type: "POST",
            crossDomain: true,
            datatype: 'json',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Osync-Authorization': hash
            },
            success: function (response, textStatus, jqXHR) {
                $("#completeInstallationLoadingDiv").hide();
                $("#completeInstallation").hide();
                SMSHandler.getNumbers(integId, hash);
                $("#installationFailed,#authorizeDiv,#enableDiv").hide();
                $("#configurationDiv,#emptyPhoneMessage").show();
                $("#completeInstallationLoadingDiv").show();
                $("#installationCompletedSuccessfully").show();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                $("#completeInstallationLoadingDiv").hide();
                $("#installationFailed").show();
            },
            complete: function (jqXHR, textStatus, errorThrown) {
            }
        });
    }
}
function getDomain() {
    var url = 'https://api.pipedrive.com/v1/users/me';
    $.ajax({
        url: url,
        type: "POST",
        crossDomain: true,
        datatype: 'json',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Osync-Authorization': hash
        },
        success: function (response, textStatus, jqXHR) {
            console.log("domain response>>>>>>>>", response);
        },
        error: function (jqXHR, textStatus, errorThrown) {

        },
        complete: function (jqXHR, textStatus, errorThrown) {

        }
    });
}

function pipedriveAuthorize(dataObj) {
    var leftServiceId = dataObj.leftServiceId;
    var code = dataObj.code;
    var osyncUrl = dataObj.osyncUrl;
    var osyncId = dataObj.osyncId;
    var integId = dataObj.integId;
    var url = osyncUrl + '/api/v1/redirect'
    var scope = osyncId + '::' + leftServiceId + '::' + integId + '::true';

    var userdata = {
        'state': scope,
        'code': code,
        'redirect_uri': 'https://api-osync.oapps.xyz/app/omessage/authorize/index.html?leftServiceId=e6179720-ccee-45df-bebe-ba811740b9d0&rightServiceId=b283ff18-8107-4a33-b35e-21f2b58bbb75&serviceName=pd'
    }
    $.ajax({
        url: url,
        type: "GET",
        crossDomain: true,
        datatype: 'json',
        data: userdata,
        headers: {
            'Accept': 'text/html',
            'Content-Type': 'text/html',
            'Osync-Authorization': hash
        },
        success: function (response, textStatus, jqXHR) {
            console.log("pipedriveauthorize>>>>>>>>response>>>>>", response);
            $("#leftButtonLoadingDiv").hide();
            showAuthButton(leftServiceId);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            $("#installationFailed").show();
        },
        complete: function (jqXHR, textStatus, errorThrown) {
        }
    });
}
