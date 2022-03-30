var urlParams = new URLSearchParams(window.location.search);
var integId = "";
var hash = "";
var selectedIds = "";
var resource = "";

var Hubspot = {
    service: "Hubspot",
    checkIsAlreadyInstalled: function (rightServiceName,leftId,rightId) {
        return new Promise((resolve, reject) => {
            var companyId = urlParams.get("portalId");
            var leftServiceId = urlParams.get("leftServiceId");
            var rightServiceId = urlParams.get("rightServiceId");
            if (portalId != null || portalId != "") {
                $("#installationFailed").hide();
                $("#completeInstallationLoadingDiv").show();

                var url = osyncUrl + '/api/v1/omessage/' + leftServiceId + '/integrate/' + rightServiceId + "?portalId=" + portalId;

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
            console.log("not installed");
        });
    },
    init: function () {
        return new Promise((resolve, reject) => {

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
                var url = url = osyncUrl + '/api/v1/omessage/' + integId + '/savePhone';
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
                    }
                });
            }
        });
    },
    getSavedNumbers: function () {
        var url = url = osyncUrl + '/api/v1/omessage/' + integId + '/savedNumbers';
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
            resolve(urlParams.get("selectedIds"));
        });
    },
    getAssociatedObjectType: function (thisObj) {
        return new Promise((resolve, reject) => {
            resolve(urlParams.get("resource"));
        });
    },
    getAssociatedIntegId: function (thisObj, rightServiceName) {
        return new Promise((resolve, reject) => {
            resolve(urlParams.get("integId"));
        });
    },
    getAssociatedHash: function (thisObj, rightServiceName) {
        return new Promise((resolve, reject) => {
            resolve(urlParams.get("hash"));
        });
    },
    fetchContact: function (thisObj) {
        return new Promise((resolve, reject) => {
            resolve("+" + $.trim(urlParams.get("phone")));
        });
    },
    doAfterSuccessfulEnable: function (dataObj) {

        $("#leftButtonLoadingDiv").show();

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

