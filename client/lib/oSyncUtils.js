$(document).ready(function () {
    //Authorize
    $("#syncEnable").click(function (event) {
        if ($("#termsCheckBox").is(':checked')) {
            oSyncId = oSyncDataObj.osyncId;
            if (oSyncId != null) { getLastStep(); }
            else { invokeAuthMethod(); }
        } else {
            alert("Please accept the Terms and Conditions to continue..");
        }
    });
    $(document).on('click', '#showFieldList', function () {
        makeBreadCrumbActive("fieldMapListBreadCrumb");
        showFieldPage();
        showBreadCrumb();
    });
    $(document).on('click', '#showModuleList', function () {
        var leftText = $("#leftButton").text().trim();
        var rightText = $("#rightButton").text().trim();
        if (leftText == "Revoke" && rightText == "Revoke") {
            makeBreadCrumbActive("entityListBreadCrumb");
            showModulePage();
        } else {
            displayFailureMessage("Please authorize both services to continue..");
        }
        showBreadCrumb();
    });
    $(document).on('click', '#saveFields', function () {
        //$(this).find("span").addClass("spinner-border spinner-border-sm");
        showCommonLoading("Verifying mandatory fields mapping..."); //Saving the mapped fields
        makeBreadCrumbActive("syncConfListBreadCrumb");
        showBreadCrumb();
        showConfPage();

    });

    $(document).on('click', '#fieldMapStartSync', function () {
        //$(this).find("span").addClass("spinner-border spinner-border-sm");
        showCommonLoading("Verifying mandatory fields mapping..."); //Saving the mapped fields
        makeBreadCrumbActive("syncReportBreadCrumb");
        showBreadCrumb();
        showConfPage();

    });

    $(document).on('click', '#startSync', function () {
        makeBreadCrumbActive("syncReportBreadCrumb");
        saveSyncConfiguration();
    });

    $(document).on('click', '#pauseSync', function () {
        showCommonLoading("Sync will be paused until you resume. Take a breath...");
        pauseSync();
        // $(this).find("#roleID").removeClass("spinner-border spinner-border-sm").addClass("mr-1");
        // $(this).attr("id", "resumeSync");
        // $(this).html('<svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-play" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M10.804 8L5 4.633v6.734L10.804 8zm.792-.696a.802.802 0 0 1 0 1.392l-6.363 3.692C4.713 12.69 4 12.345 4 11.692V4.308c0-.653.713-.998 1.233-.696l6.363 3.692z"/></svg>&nbsp;Resume');
        // $("#breadCrumbDiv").show();
        // $("#syncStatusDiv").html('<div class="ribbon-wrap right-edge fork lred"><span>Paused</span></div>');
    });
    $(document).on('click', '#forceSync', function () {
        showCommonLoading("Starting manual data sync. Check your Sync Report after some time...");
        //$(this).html("Forcing Sync");
        //$(this).removeClass("text-primary pt-3").addClass("text-success pt-3");
        forceSync();
    });

    $(document).on('click', '#resumeSync', function () {
        showCommonLoading("Hurray! Your sync will resume now...");
        resumeSync();
        // $(this).attr("id", "pauseSync");
        // $(this).html('<span id="roleID" class="" role="status" aria-hidden="true"><svg width="1.5em" height="1.5em" viewBox="0 0 16 16" class="bi bi-pause-fill" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path d="M5.5 3.5A1.5 1.5 0 0 1 7 5v6a1.5 1.5 0 0 1-3 0V5a1.5 1.5 0 0 1 1.5-1.5zm5 0A1.5 1.5 0 0 1 12 5v6a1.5 1.5 0 0 1-3 0V5a1.5 1.5 0 0 1 1.5-1.5z"></path></svg></span>Pause');
        // $("#breadCrumbDiv").hide();
        // $("#syncStatusDiv").html('<div class="ribbon-wrap right-edge fork lblue"><span>Running</span></div>');
    });

    $(document).on('click', '#stopSync', function () {
        showCommonLoading("Sync gonna stopped. Please hold on");
        stopSync();
        // $("#resumeSync").hide();
        // $("#pauseSync").hide();
        // $("#breadCrumbDiv").show();
        // $(this).attr("id", "editConf");
        // $(this).html("Edit");
        // $(this).removeClass("badge badge-danger float-right ml-2").addClass("badge badge-primary float-right");
        // $("#syncStatusDiv").html('<div class="ribbon-wrap right-edge fork lred"><span>Stopped</span></div>');
    });
    $(document).on('click', '#editConf', function () {
        invokeAuthMethod();
    });
    $(document).on('click', '#autoSuggest', function () {
        var matched = getMatchFields("", "");
    });

    $(document).on('change', 'select', function () {
        fieldMapDataChanged = true;
        $("select option").removeAttr('disabled');
        $("select").each(function (i, s) {
            var a = $(s).val();
            if (a != "") {
                $(this).parents("div.autoSuggestDummy").addClass("selectedFieldRow");
                $("select").not(s).find("option[value='" + $(s).val() + "']").attr('disabled', 'disabled').addClass("redColor");
            } else {
                $(this).parents("div.autoSuggestDummy").removeClass("selectedFieldRow");
            }
        });

        $(".dateSelectElement").change(function () {
            if ($(this).find('option:selected').attr('dataType') == 'text') {
                $(this).parents("div.autoSuggestDummy").find("div.oneWayFieldMapClass svg").remove();
                $(this).parents("div.autoSuggestDummy").find("div.oneWayFieldMapClass").append('<svg style="opacity:0.6" width="2.5em" height="2.5em" viewBox="0 0 16 16" class="bi bi-arrow-right" fill="currentColor" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M10.146 4.646a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1 0 .708l-3 3a.5.5 0 0 1-.708-.708L12.793 8l-2.647-2.646a.5.5 0 0 1 0-.708z"/><path fill-rule="evenodd" d="M2 8a.5.5 0 0 1 .5-.5H13a.5.5 0 0 1 0 1H2.5A.5.5 0 0 1 2 8z"/></svg>');
            }
            else {
                //                alert($(this).parents("div.autoSuggestDummy").find("input[type='text']").attr("format"));
                $(this).parents("div.autoSuggestDummy").find("div.oneWayFieldMapClass svg").remove();
            }
        });

    });

    $(document).on('click', 'span.directionSelector', function () {
        $("span.directionSelector").removeClass("badge-secondary").addClass("badge-light")
        $("svg.selectedSpan").removeClass("selectedSpan");
        $(this).find("svg").addClass("selectedSpan");
        $(this).removeClass("badge badge-light").addClass("badge badge-secondary");
    });


    $(document).on('click', '#addAnotherModule', function () {
        showModulePage();
    });

    $(document).on('click', '#_syncReport_', function () {
        fieldMapDataChanged = false;
        showCommonLoading("Loading Page.Please wait...")
        integrationId = $(this).attr("integrationId");
        oSyncDataObj.setIntegrationId(integrationId);

        populateOsyncDataObj(integrationId).then(function () {
            getLastStep();
        });
    });

    $(document).on('click', '#addModuleId', function () {
        sync_status = 0;
        showCommonLoading("Loading page.Please wait...");
        var addModuleURL = "/api/v1/integration/new?module=integ";
        var serviceMapJson = {
            osync_id: oSyncDataObj.osyncId,
            left_service_id: oSyncDataObj.leftServiceId,
            right_service_id: oSyncDataObj.rightServiceId
        }
        postInvoker(addModuleURL, serviceMapJson).then(function (data) {
            oSyncDataObj.integrationId = data.integId;
            if (data.left.auth.authorized === true && data.right.auth.authorized === true) {
                showModulePage();
            } else {
                invokeAuthMethod();
            }

        });
    });

    $(document).find('#revokeConfirmationBox').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget)
        var serviceId = button.data('name');
        var isleft = button.data('isleft');
        var actionType = button.data('action');
        var onclickAttr = button.data('onclick');
        var confirmationBox = $(this);
        if (actionType === "delete") {
            var integrationId = button.data('integrationid');
            confirmationBox.find("#confirmBoxRevokeBtn").text("Delete");
            confirmationBox.find("#confirmBoxRevokeBtn").attr("onclick", onclickAttr);

            confirmationBox.find("#confirmBoxRevokeBtn").attr("integrationId", integrationId);
            confirmationBox.find("#confirmBoxRevokeBtn").attr("actionType", actionType);

            confirmationBox.find("#generalTextMessage").text("Deleting this integration cannont be reverted back");
            confirmationBox.find("#strongConfirmationBoxMessage").text("Are you sure want to Delete?");
        }
        else if (actionType === "revoke") {
            // confirmationBox.find("#confirmBoxRevokeBtn").data({
            //     "serviceId": serviceId,
            //     "isleft": isleft,
            //     "actionType": actionType
            // });
            confirmationBox.find("#confirmBoxRevokeBtn").data("serviceId", serviceId);
            confirmationBox.find("#confirmBoxRevokeBtn").data("isleft", isleft);
            confirmationBox.find("#confirmBoxRevokeBtn").data("actionType", actionType);

            confirmationBox.find("#generalTextMessage").text("Revoking the authtoken will stop all the sync process for this extension.");
            confirmationBox.find("#strongConfirmationBoxMessage").text("Are you sure want to Revoke?");
        }
    });
    $(document).on('click', '#deleteIntegrationBtn', function () {

    });
    $('#termsCheckBox').change(function () {
        if ($("#termsCheckBox").is(':checked')) { $('#syncEnable').prop('disabled', false); }
        else { $('#syncEnable').prop('disabled', true); }
    });
    $(document).on('click', '#apiKeySave', function () {
        var domain_url = "";
        if ($("#domainUrlInputDiv").is(":visible")) {
            domain_url = $("#domainUrlInput").val();
        }
        var buttonText = $("#apiKeySave").text();
        var isLeft = $("#apiKeySave").attr("isleft");

        showCommonLoading("Authenticating and Saving API Details..");
        apiData = {
            "access_token": $("#apiKeyInput").val(),
            "state": oSyncDataObj.osyncId + "::" + $("#apiKeyModal").attr("serviceId") + "::" + oSyncDataObj.integrationId + "::" + $("#apiKeyModal").attr("isLeft"),
            "refresh_token": "",
            "api_domain": domain_url
        }
        apiKeySaveURL = "/api/v1/saveApiKey";
        postInvoker(apiKeySaveURL, apiData).then(function (data) {
            processPostResponse(apiData, "saveAPIKeyDetails");
            var response = data;
            if (typeof data === "string") {
                response = $.parseJSON(data);
            }

            if (buttonText === "Save and Authorize") {
                if (domain_url === "") {
                    return false;
                }
                $("#apiKeyModal").modal("hide");
                hideCommonLoading();
                var authUrl = $("button.btn-outline-primary[data-isleft='" + isLeft + "']").attr("url");
                //authUrl = domain_url + authUrl;

                var buttonName = isLeft ? "leftButton" : "rightButton";
                openNewWindow(buttonName, $("button.btn-outline-primary[data-isleft='" + isLeft + "']").attr("name"), authUrl);
            } else {
                if (response.data.userEmail == null) {
                    $("#apiKeyModal").modal("hide");
                    hideCommonLoading();
                    displayFailureMessage("Please Enter valid details")
                }
                else {
                    $("#apiKeyModal").modal("hide");
                    var serviceId = response.data.serviceId;
                    var emailId = response.data.userEmail;
                    showRevokeButton(serviceId, emailId);
                    hideCommonLoading();
                }
            }
            // https://dev82546.service-now.com/oauth_token.do?
            // client_id=2193753f7e832010d503f2be047af158
            // &client_secret=5W7@z0NkIQ
            // &redirect_uri=https://e894a449952e.ngrok.io/api/v1/redirect
            // &code=5bSSUdE7RrwlkA-I3hgoL5ikPj2ldWmBKNTZqOITNGIMsHQ3blKiHahNi1eqq8CsEo8l7FWTiOky9mp9ZLPrEQ
            // &state=0132ee4a-7600-4259-8ae1-97e2749a8441::erbb5b7b-bbc1-1605a-a04e-7belai6f1ab::f7c9e94c-2d22-44ca-a621-5a251c3ded5e::true
            // &grant_type=authorization_code
            // &accounts-server=https://accounts.zoho.com
            //$("a#serviceListBreadCrumb").parent("li").addClass("active");
        });
    });

});

function deleteInvoker(url) {
    url = preURL + url;
    return new Promise((resolve, reject) => {
        authHeader = oSyncDataObj.hash;
        $.ajax({
            url: url,
            type: "DELETE",
            crossDomain: true,
            datatype: 'json',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Osync-Authorization': authHeader
            },
            success: function (response, textStatus, jqXHR) {
                resolve(response);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
            }
        });
    });
}

function postInvoker(url, data) {
    url = preURL + url;
    return new Promise((resolve, reject) => {
        authHeader = oSyncDataObj.hash;
        $.ajax({
            url: url,
            type: "POST",
            data: JSON.stringify(data),
            crossDomain: true,
            context: data,
            datatype: 'json',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Osync-Authorization': authHeader
            },
            success: function (data, textStatus, jqXHR) {
                args = data;
                resolve(data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
            }
        });
    });
}

function processPostResponse(data, scriptId) {
    args = data;
    if (scriptId != "") {
        if (scriptId === "fieldMapPage") {
            showFieldMapPage();
        } else {
            if (scriptId === "serviceList") {
                if (sync_status == 0) {
                    saveLocalStorageData(data);
                }
            }
            populateDetails(scriptId);
        }
    } else {
        syncReport();
    }
}

function processGetResponse(data, scriptId) {
    args = data;
    if (scriptId == "getRecentStep") {
        sync_status = data.sync_status;
        findPage(data);
    } else if (scriptId === "saveOsyncValues") {
        getConfigurationData(data);
    } else if (scriptId === "fieldRetriver") {
        return data;
    }
    else if (scriptId != "") {
        populateDetails(scriptId);
    }
    else {
        //syncReport();
        overAllSyncReport(false);
    }
}

function getInvoker(url) {
    url = preURL + url;
    return new Promise((resolve, reject) => {
        authHeader = oSyncDataObj.hash;
        $.ajax({
            url: url,
            type: "GET",
            crossDomain: true,
            datatype: 'json',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                'Osync-Authorization': authHeader
            },
            success: function (data, textStatus, jqXHR) {
                args = data;
                resolve(data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                console.log(jqXHR);
            }
        });
    });
}

function invokeAPI_Delete(url, buttonName) {
    url = preURL + url;
    var isleft = false;
    if (buttonName === "leftButton") { isleft = true; }
    $.ajax({
        url: url,
        type: "DELETE",
        data: JSON.stringify(url),
        crossDomain: true,
        datatype: 'json',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Osync-Authorization': authHeader
        },
        success: function (data, textStatus, jqXHR) {
            var authUrl = $("#" + buttonName).attr("url");
            $("#" + buttonName).text('Authorize').removeClass("btn-outline-danger").addClass("btn-outline-primary");
            $("#" + buttonName).attr("onClick", "serviceAuth('" + buttonName + "')");
            $("#" + buttonName).attr("data-target", "");
            $("#" + buttonName).attr("data-isleft", isleft);
            $("#" + buttonName + "_UserEMail").hide();
            hideCommonLoading();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
        }
    });
}
function findPage(data) {
    $(".breadcrumb-item.active").removeClass("active");
    showBreadCrumb();
    if (data.authorization_page == false) {
        // getAuthPageData();
        invokeAuthMethod();
        $("a#serviceListBreadCrumb").attr("isAlreadyLoaded", "true");
        $("a#serviceListBreadCrumb").parent("li").addClass("active");
    } else if (data.module_page == false) {
        showModulePage();
        $("a#entityListBreadCrumb").attr("isAlreadyLoaded", "true");
        $("a#entityListBreadCrumb").parent("li").addClass("active");
    } else if (data.field_page == false) {
        showFieldMapPage();
        $("a#fieldMapListBreadCrumb").attr("isAlreadyLoaded", "true");
        $("a#fieldMapListBreadCrumb").parent("li").addClass("active");
    } else if (data.configuration_page == false) {
        showSettingsPage();
        $("a#syncConfListBreadCrumb").attr("isAlreadyLoaded", "true");
        $("a#syncConfListBreadCrumb").parent("li").addClass("active");
    } else {
        syncReport();
        $("a#syncReportBreadCrumb").attr("isAlreadyLoaded", "true");
        $("a#syncReportBreadCrumb").parent("li").addClass("active");
    }
}
function populateDetails(scriptId) {
    if ($("#" + scriptId).length != 0) {
        populateDetailsForScriptId(scriptId);
    } else {
        loadSpecificScript(scriptId).then(function () {
            populateDetailsForScriptId(scriptId);
        })
    }
}
function loadSpecificScript(scriptId) {
    return new Promise((resolve, reject) => {
        if (scriptId === "serviceList") {
            resolve(loadAuthenticationPageHtml());
        } else if (scriptId === "entityList") {
            resolve(loadModuleMapHtml());
        } else if (scriptId === "fieldMapList") {
            resolve(loadFieldMapHtml());
        } else if (scriptId === "syncConfiguration") {
            resolve(loadSyncConfHtml());
        } else if (scriptId === "syncReport") {
            resolve(loadReportHtml());
        } else if (scriptId === "overAllSyncReport") {
            resolve(loadOverAllSyncReportHtml());
        }
    });
}
function populateDetailsForScriptId(scriptId) {

    $("div[type='settings']").hide();
    var serviceList = document.getElementById(scriptId).innerHTML;

    if (scriptId === "syncReport") {
        if (args.integProps != null) {
            args = manipulateJson(args);
        }
    }
    var renderedHtml = Mustache.render(serviceList, args);
    var tempScriptId = scriptId + "_R";
    $("#" + tempScriptId).html(renderedHtml).show();
    $("li.breadcrumb-item a[source='" + tempScriptId + "']").attr("isalreadyloaded", "true");


    if (scriptId === "fieldMapList") {
        if (sync_status != 0) {
            fieldRetriver().then(function () {
                disableSelectedOption();
            });
        } else {
            autoMatch(fieldMapJsonVal.leftArgs.data, fieldMapJsonVal.rightArgs.data).then(function () {
                disableSelectedOption();
            });;
        }
    }
    if (scriptId === "entityList") {
        if (args.data.left_module_id != "null") {
            selectSavedFields(args);
        }
    }

    if (scriptId == "syncConfiguration") {
        if (sync_status != 0) {
            masterService = oSyncDataObj.masterService;
            $("#" + masterService).prop('checked', true);
        }
    }
    setTimeout(() => {
        $('select').select2({
            "width": "100%",
            "padding-top": "6px",
            "theme": "classic"
        });
        $('[data-toggle="tooltip"]').tooltip();
        hideCommonLoading();
    }, 500);
    // });
}

function saveLocalStorageData(serviceData) {
    oSyncId = serviceData.osync_id;
    leftServiceId = serviceData.left.service_id;
    rightServiceId = serviceData.right.service_id;
    leftServiceName = serviceData.left.service_display_name;
    rightServiceName = serviceData.right.service_display_name;
    authHeader = serviceData.hash;
    integrationId = serviceData.integ_id;

    oSyncDataObj.setIntegrationId(integrationId);
    oSyncDataObj.setLeftServiceId(leftServiceId);
    oSyncDataObj.setRightServiceId(rightServiceId);
    oSyncDataObj.setLeftServiceName(leftServiceName);
    oSyncDataObj.setRightServiceName(rightServiceName);
    oSyncDataObj.setLeftServiceImgName(serviceData.left.service_name);
    oSyncDataObj.setRightServiceImgName(serviceData.right.service_name);
    oSyncDataObj.setHash(authHeader);

    var dataToBeStored = {};

    dataToBeStored[osyncIdKey] = oSyncId;
    dataToBeStored[hashKey] = authHeader;
    dataToBeStored[leftServiceIdKey] = leftServiceId;
    dataToBeStored[rightServiceIdKey] = rightServiceId;

    providerObj.save(dataToBeStored).then(function () {
        assignOsyncValues();
    });

}
function initPage(thisObj) {
    $("div[type='settings']").hide();
    var source = $(thisObj).attr("source");
    var isAlreadyLoaded = $(thisObj).attr("isAlreadyLoaded");
    $("div#" + source).show();
    $(".breadcrumb-item.active").removeClass("active");
    if (isAlreadyLoaded !== "true") {
        $(thisObj).parent("li").addClass("active");
        if (source === "serviceList_R") {
            // getAuthPageData();
            invokeAuthMethod();
            $("a#serviceListBreadCrumb").parent("li").addClass("active");
        } else if (source === "entityList_R") {
            showModulePage();
            $("a#entityListBreadCrumb").parent("li").addClass("active");
        } else if (source === "fieldMapList_R") {
            showFieldMapPage();
            $("a#fieldMapListBreadCrumb").parent("li").addClass("active");
        } else if (source === "syncConfiguration_R") {
            showSettingsPage();
            $("a#syncConfListBreadCrumb").parent("li").addClass("active");
        }
        $(thisObj).attr("isAlreadyLoaded", "true");
    }
    if (source === "syncReport_R") {
        syncReport();
    }
}
function getKeyValue(key) {
    return new Promise((resolve, reject) => {
        providerObj.get(key).then(function (data) {
            if (key === osyncIdKey) {
                oSyncId = data;
            } else if (key === leftServiceIdKey) {
                leftServiceId = data;
            } else if (key === rightServiceIdKey) {
                rightServiceId = data;
            } else if (key === hashKey) {
                hash = data;
            }
            if (data != undefined && data != "") {
                resolve(data);
            } else {
                reject();
            }
        }).catch(function (err) {
            console.log("first time invoke "+ err);
        });
    });
}
function assignOsyncValues() {
    var keyAttr = [osyncIdKey, rightServiceIdKey, hashKey, leftServiceIdKey];
    return new Promise((resolve, reject) => {
        let promiseArray = [];
        $.each(keyAttr, function (i, key) {
            promiseArray.push(getKeyValue(key));
        });
        Promise.all(promiseArray)
            .then(() => {
                if (promiseArray.length < 4) {
                    hideCommonLoading();
                    $("#configPage").hide();
                    $("#landingPage").show();
                } else {
                    $("#configPage").show();
                    $("#landingPage").hide();
                    oSyncDataObj.setOsyncId(oSyncId);
                    oSyncDataObj.setLeftServiceId(leftServiceId);
                    oSyncDataObj.setRightServiceId(rightServiceId);
                    oSyncDataObj.setHash(hash);
                    loadOverAllReportData(oSyncId, leftServiceId, rightServiceId).then(function (data) {
                        resolve();
                    });
                }
            })
            .catch((e) => {
                console.log(" assignOsyncValues :::::::::: Promise ERRRRRRRO in getKeyValue" + e);
                //invokeAuthMethod();
                hideCommonLoading();
                $("#configPage").hide();
                $("#landingPage").show();
            });

    });
}

function loadOverAllReportData(oSyncId, leftServiceId, rightServiceId) {
    return new Promise((resolve, reject) => {
        var url = "/api/v1/account/" + oSyncId + "?module=integ&left_service_id=" + leftServiceId + "&right_service_id=" + rightServiceId;
        getInvoker(url).then(function (data) {
            processGetResponse(data, "saveOsyncValues");
            resolve();
        });
    });
}

function getLastStep() {
    integrationId = oSyncDataObj.integrationId;
    var url = "/api/v1/integration/" + integrationId + "/get-page?module=integ&osync_id=" + oSyncId;
    getInvoker(url).then(function (data) {
        processGetResponse(data, "getRecentStep");
    });
}

function getConfigurationData(data) {
    var lmId, rmId, iId, iStatus, mService, iWay = "";
    var serviceItem = {};
    var integrationData = data.integrations;
    var left_modules = data.left_modules;
    var right_modules = data.right_modules;
    var left_service = data.left_service;
    var right_service = data.right_service;
    var integItemArr = [];
    var sync_report = data.sync_report;
    var integ_status = data.integ_status;


    $.each(integrationData, function (i, row) {

        iId = row.integId;
        integrationId = iId;
        lmId = row.leftModuleId;
        rmId = row.rightModuleId;
        iStatus = row.syncStatus;
        iWay = row.direction;
        mService = row.masterService;

        integIdArr.push(iId);
        var integItem = {};
        integItem["integrationId"] = iId;
        integItem["leftModuleId"] = lmId;
        integItem["rightModuleId"] = rmId;
        integItem["masterService"] = mService;


        if (iStatus == "0") { integItem["status"] = "Unfinished" }
        else if (iStatus == "1") { integItem["status"] = "Running" }
        else if (iStatus == "2") { integItem["status"] = "Paused" }
        else if (iStatus == "3") { integItem["status"] = "Stopped" }

        if (iWay == "2") { integItem["direction"] = { "leftOnly": "true" } }
        else if (iWay == "3") { integItem["direction"] = { "bothWay": "true" } }
        else if (iWay == "1") { integItem["direction"] = { "rightOnly": "true" } }

        $.each(left_modules, function (lmi, lmrow) {
            moduleInfo[lmrow.moduleId] = lmrow.name;
            if (lmId == lmrow.moduleId) {
                integItem["leftModuleName"] = lmrow.name;
                oSyncDataObj.setLeftModuleName(lmrow.name);
            }
        });

        $.each(right_modules, function (rmi, rmrow) {
            moduleInfo[rmrow.moduleId] = rmrow.name;
            if (rmId == rmrow.moduleId) {
                integItem["rightModuleName"] = rmrow.name;
                oSyncDataObj.setRightModuleName(rmrow.name);
            }
        });
        if (sync_report != null) {
            $.grep(sync_report, function (value) {
                if (iId == value.integId) {
                    integItem["data_in_sync"] = value.dataInSync;
                }
            });
        }
        if (integ_status != null) {
            $.grep(integ_status, function (value) {
                if (iId == value.integId) {
                    var integ_status_val = value.status;
                    if (integ_status_val != "COMPLETE" && integ_status_val != "RUNNING") {
                        integItem["integ_error"] = true;
                    }
                }
            });
        }
        integItemArr.push(integItem);
    });

    $.each(left_service, function (lsi, lsrow) {
        serviceItem["leftServiceId"] = left_service.serviceId;
        serviceItem["leftServiceName"] = left_service.displayName;
        serviceItem["leftImgName"] = left_service.name;
    });
    $.each(right_service, function (rsi, rsrow) {
        serviceItem["rightServiceId"] = right_service.serviceId;
        serviceItem["rightServiceName"] = right_service.displayName;
        serviceItem["rightImgName"] = right_service.name;
    });
    integData["service"] = serviceItem;
    integData["integ"] = integItemArr;

    oSyncDataObj.setLeftModuleId(lmId);
    oSyncDataObj.setRightModuleId(rmId);

    oSyncDataObj.setModuleSyncDirection(iWay);
    oSyncDataObj.setIntegrationId(iId);

    oSyncDataObj.setLeftServiceId(left_service.serviceId);
    oSyncDataObj.setRightServiceId(right_service.serviceId);

    oSyncDataObj.setLeftServiceName(left_service.displayName);
    oSyncDataObj.setRightServiceName(right_service.displayName);

    oSyncDataObj.setLeftServiceImgName(left_service.name);
    oSyncDataObj.setRightServiceImgName(right_service.name);
}
function showCommonLoading(progressMessage) {
    // hideCommonLoading();
    $("#mainLoader").addClass("show").show();
    if (progressMessage != undefined && progressMessage != "") {
        //progressMessage += " Take a breath.."
        $("#progressMessage").attr("data-text", progressMessage).text(progressMessage);
    }
}
function hideCommonLoading(progressMessage) {
    $("#mainLoader").removeClass("show").hide();
}

function showHomePage(thisObj) {
    if (fieldMapDataChanged) {
        var yesContinue = confirm("Your unsaved configurations will be lost. Do you wish to continue?");
        if (!yesContinue) {
            return;
        } else {
            fieldMapDataChanged = false;
        }
    }
    oSyncId = oSyncDataObj.osyncId;
    if (oSyncId != null) {
        showCommonLoading("Generating OSync Overview.Please wait...");
        $("#navbarCollapse a.active").removeClass("active");
        $(thisObj).addClass("active");
        $("#mainParentDiv div[type='settings']").hide();
        hideBreadCrumb();
        //$("div#overAllSyncReport_R").show();
        overAllSyncReport(false);
    }
    else {
        $("#navbarCollapse a.active").removeClass("active");
        $(thisObj).addClass("active");
        showCommonLoading("Enable your first sync..");
        $("#configPage").hide();
        $("#landingPage").show();
        hideCommonLoading();
    }

}
function showContactOsyncPage(thisObj) {
    $("#landingPage").hide();
    $("#configPage").show();
    $("#navbarCollapse a.active").removeClass("active");
    $(thisObj).addClass("active");
    $("#mainParentDiv div[type='settings']").hide();
    hideBreadCrumb();
    $("div#contactOsync").show();

    $("iframe#zsfeedbackFrame").attr("src", "https://desk.zoho.com/support/fbw?formType=AdvancedWebForm&fbwId=21e8a3ca1a24a4e4668e9427d0b86bde0ad7e16f5bb4b7e7&xnQsjsdp=AszRgVAE*rXwZmTHHfGK-w$$&mode=showNewWidget&displayType=iframe");
}
function makeBreadCrumbActive(idToActive) {
    $("#navbarsExample10 li.active").removeClass("active");
    $("#navbarsExample10").find("#" + idToActive).parent("li").addClass("active");
}

function loadAllHtmlFilesAtaTime() {
    return new Promise((resolve, reject) => {
        let promiseArray = [];
        promiseArray.push(loadSyncConfHtml());
        promiseArray.push(loadReportHtml());
        promiseArray.push(loadOverAllSyncReportHtml());
        promiseArray.push(loadModuleMapHtml());
        promiseArray.push(loadFieldMapHtml());
        promiseArray.push(loadAuthenticationPageHtml());

        Promise.all(promiseArray).then(function () {
            resolve();
        });
    });
}
function loadSyncConfHtml() {
    return new Promise((resolve, reject) => {
        $("#syncConfiguration_R").load("./sync_conf.html", function () {
            resolve();
        });
    });
}

function loadReportHtml() {
    return new Promise((resolve, reject) => {
        $("#syncReport_R").load("./sync_report.html", function () {
            resolve();
        });
    });
}

function loadOverAllSyncReportHtml() {
    return new Promise((resolve, reject) => {
        $("#overAllSyncReport_R").load("./sync_list_view.html", function () {
            resolve();
        });
    });
};

function loadModuleMapHtml() {
    return new Promise((resolve, reject) => {
        $("#entityList_R").load("./module_map.html", function () {
            resolve();
        });
    });
};

function loadFieldMapHtml() {
    return new Promise((resolve, reject) => {
        $("#fieldMapList_R").load("./field_map.html", function () {
            resolve();
        });
    });
};

function loadAuthenticationPageHtml() {
    return new Promise((resolve, reject) => {
        $("#serviceList_R").load("./authorize.html", function () {
            resolve();
        });
    });
};
function populateOsyncDataObj(integId) {
    return new Promise((resolve, reject) => {
        var urlForModuleAPI = "/api/v1/integration/" + integId + "/modules?module=integ";
        getInvoker(urlForModuleAPI).then(function (data) {
            oSyncDataObj.setIntegrationId(integId);
            oSyncDataObj.setLeftModuleId(data.data.left_module_id);
            oSyncDataObj.setLeftModuleName(moduleInfo[data.data.left_module_id]);

            oSyncDataObj.setRightModuleId(data.data.right_module_id);
            oSyncDataObj.setRightModuleName(moduleInfo[data.data.right_module_id]);
            oSyncDataObj.setModuleSyncDirection(data.data.direction);
            oSyncDataObj.setSyncStatus(data.data.sync_status);
            oSyncDataObj.setMasterService(data.integProps.masterService);
        });
        resetAlreadyLoadedBoolean();
        resolve();
    });

}
function resetAlreadyLoadedBoolean() {
    $(".bg_dummy_class").attr("isAlreadyLoaded", "false");
}

function displayFailureMessage(message) {
    $('#showAlertMessage .alert').alert('close');
    var msgHtml = '<div class="alert alert-danger alert-dismissible fade show" role="alert"><strong>Attn!</strong> ' + message + '.<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button></div>';
    $("#showAlertMessage").append(msgHtml);
    $('#showAlertMessage .alert').alert('show').delay(5000).hide("slow");
    // $("html, body").animate({scrollTop: $('html, body').get(0).scrollHeight}, 2000); 
}

function showBreadCrumb() {
    if ($("#breadCrumbDiv").hasClass("d-none")) {
        $("#breadCrumbDiv").removeClass("d-none").addClass("d-flex");
    }
}

function hideBreadCrumb() {
    if ($("#breadCrumbDiv").hasClass("d-flex")) {
        $("#breadCrumbDiv").removeClass("d-flex").addClass("d-none");
    }
}

function getNameSpaceKey() {
    return new Promise((resolve, reject) => {
        var url_string = window.location.href;
        var url = new URL(url_string);
        nameSpace = url.searchParams.get("namespace");
        if (nameSpace == null) {
            nameSpace = "Default";
        }
        // osyncIdKey = nameSpace + "__oSyncId";
        // hashKey = nameSpace + "__hash";
        // leftServiceIdKey = nameSpace + "__leftServiceId";
        // rightServiceIdKey = nameSpace + "__rightServiceId";
        resolve();
    });
}

function showRevokeButton(serviceId, emailId) {
    var buttonHtmlObj = $("#authorizeTable button[name=\'" + serviceId + "\']");
    buttonHtmlObj.attr("data-target", "#revokeConfirmationBox");
    buttonHtmlObj.attr("data-toggle", "modal");
    buttonHtmlObj.attr("data-action", "revoke");
    buttonHtmlObj.removeAttr("onclick");
    buttonHtmlObj.text('Revoke').removeClass("btn btn-primary").addClass("btn btn-outline-danger");
    $("#authorizeTable button[name=\'" + serviceId + "\']").siblings("div.user_email_div").text(emailId).show();
}