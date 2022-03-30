var osyncUrl = "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";
var module = "";
var entityId = "";
$(document).ready(function () {
    ZOHO.embeddedApp.on('PageLoad', function (data) {
        module = data.Entity;
        selectedEntities = data.EntityId;
        $.each(selectedEntities, function (i, entityId) {
            populateData(entityId);
        });
    })
    ZOHO.embeddedApp.init();

    userObj = {
        "zoho.currenttime": new Date().toLocaleString(),
        "zoho.currentUser": ZOHO.CRM.CONFIG.getCurrentUser()
    };
    logList.push(JSON.stringify(userObj));
})


function populateData(entityId) {
    ZOHO.CRM.API.getRecord({ Entity: module, RecordID: entityId })
        .then(function (data) {
            data = data.data;
            var phone = data[0]["Phone"];
            var mobile = data[0]["Mobile"];
            if (phone != undefined && phone != "") {
                verifyMyPhone(phone, "phone").then(function () {
                    $("#loadingMobileCardDiv").addClass("d-flex").show();

                    if (mobile != undefined && mobile != "") {
                        setTimeout(function () {
                            verifyMyPhone(mobile, "mobile");
                        }, 5000);
                    } else {
                        $("#loadingMobileCardDiv").removeClass("d-flex").hide();
                        $("#mobileCard").append("No data");
                    }
                });
            } else if (mobile != undefined && mobile != "") {
                verifyMyPhone(mobile, "mobile");
                if (phone == undefined || phone == "") {
                    $("#loadingPhoneCardDiv").removeClass("d-flex").hide();
                    $("#phoneCard").append("No data");
                }
            } else {
                if (mobile == undefined || mobile == "") {
                    $("#loadingMobileCardDiv").removeClass("d-flex").hide();
                    $("#mobileCard").append("No data");
                }
                if (phone == undefined || phone == "") {
                    $("#loadingPhoneCardDiv").removeClass("d-flex").hide();
                    $("#phoneCard").append("No data");
                }
            }

        })
}

function verifyMyPhone(number, fieldCard) {
    return new Promise((resolve, reject) => {
        ZOHO.CRM.API.getOrgVariable("phoneverifierforzohocrm__integId").then(function (data) {
            //console.log(data.Success.Content);
            var integId = data.Success.Content;
            ZOHO.CRM.API.getOrgVariable("phoneverifierforzohocrm__hash").then(function (data) {
                var hash = data.Success.Content;
                var url = osyncUrl + "/api/v1/" + integId + "/verifyNumber?number=" + number;
                $.ajax({
                    url: url,
                    type: "GET",
                    crossDomain: true,
                    headers: {
                        "Osync-Authorization": hash,
                        "Content-Type": "application/json"
                    },
                    success: function (response, textStatus, jqXHR) {
                        var data = response.data;
                        var dataObj = JSON.parse(data);

                        var respArray = [];
                        $.each(dataObj, function (key, value) {
                            var keyValue = key.charAt(0).toUpperCase() + key.slice(1);
                            keyValue = keyValue.replaceAll("_"," ");
                            var repArg = {
                                "key": keyValue,
                                "value": value
                            }
                            respArray.push(repArg);
                        });

                        var cardDiv = "verifiedListView";

                        var verifiedPage = $("#" + cardDiv).html();
                        var text = Mustache.render(verifiedPage, respArray);

                        if (fieldCard === "phone") {
                            $("#phoneCard").html(text).show();
                            $("#phoneNumber").text(number);
                            if (dataObj.valid) {
                                $("#phoneCardStamp").append(validStamp());
                            } else {
                                $("#phoneCardStamp").append(invalidStamp());
                            }
                            $("#phoneCardColumn").show();
                            $("#loadingPhoneCardDiv").removeClass("d-flex").hide();
                        } else if (fieldCard === "mobile") {
                            $("#mobileCard").html(text).show();
                            $("#mobNumber").text(number);
                            if (dataObj.valid) {
                                $("#mobileCardStamp").append(validStamp());
                            } else {
                                $("#mobileCardStamp").append(invalidStamp());
                            }
                            $("#mobileCardColumn").show();
                            $("#loadingMobileCardDiv").removeClass("d-flex").hide();
                        }
                        resolve();
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.log("Error ::::: ", jqXHR);
                    },
                    complete: function () {
                    }
                });
            });
        });
    });
}

function validStamp() {
    return '<span class="stamp is-approved" style="">Valid</span>';
}

function invalidStamp() {
    return '<span class="stamp is-nope">Invalid</span>';
}