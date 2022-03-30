function manipulateJson(args) {
    var syncServiceInfo = {
        "syncInfo": {
            "left_service_name": oSyncDataObj.leftServiceName,
            "right_service_name": oSyncDataObj.rightServiceName,
            "left_img_name": oSyncDataObj.leftImgName,
            "right_img_name": oSyncDataObj.rightImgName,
            "left_module_name": oSyncDataObj.leftModuleName,
            "right_module_name": oSyncDataObj.rightModuleName,
            "data": {},
            "sync_status": {},
            "sync_type": {},
            "integ_props" : {},
            "integ_error" : false
        }
    };
    syncServiceInfo.syncInfo.data = args;

    var sync_status = args.integProps.syncStatus;
    if (sync_status == "0") { showBreadCrumb();       return args; }
    else if (sync_status == "1") {
        syncServiceInfo.syncInfo.sync_status = { "sync_running": "true" }
        hideBreadCrumb();
    }
    else if (sync_status == "2") {
        syncServiceInfo.syncInfo.sync_status = { "sync_paused": "true" }
        showBreadCrumb();
    }
    else if (sync_status == "3") {
        syncServiceInfo.syncInfo.sync_status = { "sync_stopped": "true" }
        showBreadCrumb();
    }

    var sync_type = args.integProps.direction;
    if (sync_type == "2") { syncServiceInfo.syncInfo.sync_type = { "leftOnly": "true" } }
    else if (sync_type == "3") { syncServiceInfo.syncInfo.sync_type = { "bothWay": "true" } }
    else if (sync_type == "1") { syncServiceInfo.syncInfo.sync_type = { "rightOnly": "true" } }

    var integ_error_status = args.integStatus;
    if (integ_error_status != null) {
        integ_error_status = args.integStatus.i_status;
        if (integ_error_status != "COMPLETE" && integ_error_status != "RUNNING") {
            syncServiceInfo.syncInfo.integ_error = true;
        } 
        if(integ_error_status == "RUNNING" && sync_status == "1"){
            syncServiceInfo.syncInfo.integ_processing = true;
        }
        if(integ_error_status == "COMPLETE"){
            syncServiceInfo.syncInfo.integ_complete = true;
        }
    }
    return syncServiceInfo;
}


function pauseSync() {
    $("#pauseSync").find("small").addClass("spinner-border spinner-border-sm");
    var urlForPauseAPI = "/api/v1/integration/" + integrationId + "/pause-sync?module=sync";

    postInvoker(urlForPauseAPI, "").then(function (data) {
        processPostResponse(data, "");
    });
}
function forceSync() {
    var urlForForceSyncAPI = "/api/v1/run-sync?module=integ&osync_id=" + oSyncDataObj.osyncId + "&integ_id=" + oSyncDataObj.integrationId;

    setTimeout(() => {
        hideCommonLoading();
    }, 5000);
    getInvoker(urlForForceSyncAPI).then(function (data) {
        processGetResponse(data, "");
    });
}
function resumeSync() {
    $("#resumeSync").find("small").addClass("spinner-border spinner-border-sm");
    var urlForResumeAPI = "/api/v1/integration/" + integrationId + "/resume-sync?module=sync";
    postInvoker(urlForResumeAPI, "").then(function (data) {
        processPostResponse(data, "");
    });
}
function stopSync() {
    var urlForStopAPI = "/api/v1/integration/" + integrationId + "/stop-sync?module=sync";
    postInvoker(urlForStopAPI, "").then(function (data) {
        processPostResponse(data, "");
    });
}

function overAllSyncReport(fromIndex) {
    hideBreadCrumb();
    if(!fromIndex){
    loadOverAllReportData(oSyncDataObj.osyncId, oSyncDataObj.leftServiceId, oSyncDataObj.rightServiceId).then(function (data) {
        args = integData;
        populateDetails("overAllSyncReport");
        $("a#syncReportBreadCrumb").parent("li").addClass("active");
    });
    } else{
        args = integData;
        populateDetails("overAllSyncReport");
        $("a#syncReportBreadCrumb").parent("li").addClass("active");
    }
}

function syncReport(integrationId) {
    showCommonLoading("Generating OSync Overview.Please wait...");
    if (integrationId == null) { integrationId = oSyncDataObj.integrationId; }
    // populateIntegJson(integrationId);
    var urlForModuleAPI = "/api/v1/synchealthreport?module=sync&integ_id=" + integrationId;
    getInvoker(urlForModuleAPI).then(function (data) {
        processGetResponse(data, "syncReport");
        hideCommonLoading();
        $("a#syncReportBreadCrumb").parent("li").addClass("active");
    });
}

// function populateIntegJson(integrationId){
//     $.each(integData.integ, function (i, row) {
//         if(row.integrationId == integrationId){
//             integValues.leftModuleId = row.leftModuleId;
//             integValues.rightModuleId = row.rightModuleId;
//             integValues.masterService = row.masterService;
//         }
//     });
// }


function deleteIntegration(thisObj) {
    var integId = $(thisObj).attr("integrationId");
    if (integId != undefined && integId != "") {
        var deleteIntegAPIURL = "/api/v1/integration/" + integId + "?module=integ";
        $('#revokeConfirmationBox').modal('hide');
        showCommonLoading("Deleting the integration.");
        deleteInvoker(deleteIntegAPIURL).then(function (data) {
            $("div.card-body[integrationid=\'"+integId+"\']").parents(".card").remove();
            hideCommonLoading();
        });
    }
}
