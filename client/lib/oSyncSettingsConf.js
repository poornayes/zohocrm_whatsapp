function saveSyncConfiguration() {
    showCommonLoading("Saving the settings. Please hold on.");
    var masterServiceId = "0";
    if(oSyncDataObj.moduleDirection == 3 ){
        masterServiceId = $('input[name="masterSlave"]:checked').attr("id");
    } else if(oSyncDataObj.moduleDirection == 1) 
    { 
        masterServiceId = oSyncDataObj.leftServiceId;
    } else if(oSyncDataObj.moduleDirection == 2) 
    { 
        masterServiceId = oSyncDataObj.rightServiceId;
    }
    
    var urlForModuleAPI = "/api/v1/integration/" + integrationId + "/start-sync?module=integ";
    var dataConf = {
        "masterService": masterServiceId,
        "syncDuration": "60",
        "leftServiceId": leftServiceId,
        "rightServiceId": rightServiceId,
        "osyncId": oSyncId
    }
    postInvoker(urlForModuleAPI,dataConf).then(function(data){ 
        processPostResponse(data,"");
    });
}

function showConfPage() {
    saveFields();
}

function showSettingsPage() {
        args = {
            "left_service_name": oSyncDataObj.leftServiceName,
            "right_service_name": oSyncDataObj.rightServiceName,
            "left_service_id": oSyncDataObj.leftServiceId,
            "right_service_id": oSyncDataObj.rightServiceId
        }
        populateDetails("syncConfiguration");
}