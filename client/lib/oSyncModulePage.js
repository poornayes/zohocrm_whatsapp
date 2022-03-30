

function saveModuleData() {
    var leftModuleIdVar = $('#leftModule :selected').attr("id");
    var rightModuleIdVar = $('#rightModule :selected').attr("id");

    var leftModuleNameVar = $('#leftModule :selected').val();
    var rightModuleNameVar = $('#rightModule :selected').val();

    var directionVar = $(".selectedSpan").attr("id");

    if(leftModuleIdVar == null || rightModuleIdVar == null){
        hideCommonLoading();
        displayFailureMessage("Ooops! Reconfigure your sync and continue...");
        return;
    }
    showCommonLoading("Saving the settings to map.....");

    var saveModuleJson = {
        "direction": directionVar,
        "left_module_id": leftModuleIdVar,
        "right_module_id": rightModuleIdVar
    };

    integrationId = oSyncDataObj.integrationId;

    oSyncDataObj.setLeftModuleId(leftModuleIdVar);
    oSyncDataObj.setLeftModuleName(leftModuleNameVar);
    oSyncDataObj.setRightModuleId(rightModuleIdVar);
    oSyncDataObj.setRightModuleName(rightModuleNameVar);
    oSyncDataObj.setModuleSyncDirection(directionVar);

    var url = "/api/v1/integration/" + integrationId + "/modules?module=integ";

    postInvoker(url, saveModuleJson).then(function (data) {
        fieldMapJsonVal = {
            "show_sync_conf": true,
            "leftArgs": {
                "serviceName": oSyncDataObj.leftServiceName,
                "moduleName": oSyncDataObj.leftModuleName,
                "serviceId": oSyncDataObj.leftServiceId,
                "data": {}
            },
            "rightArgs": {
                "serviceName": oSyncDataObj.rightServiceName,
                "moduleName": oSyncDataObj.rightModuleName,
                "serviceId": oSyncDataObj.rightServiceId,
                "data": {}
            }
        };
        processPostResponse(data, "fieldMapPage");
    });
}

function showModulePage() {
    showCommonLoading("Loading your modules. Please wait...");
    
    if( oSyncDataObj.integrationId != undefined && oSyncDataObj.integrationId != "null" ){
        showModuleListPage();
    } else {
        assignOsyncValues().then( function(){
            showModuleListPage();
        });
    }
    
}
function showModuleListPage(){
    integrationId = oSyncDataObj.integrationId;
    var urlForModuleAPI = "/api/v1/integration/" + integrationId + "/modules?module=integ";
    getInvoker(urlForModuleAPI).then(function (data) {
        if (oSyncDataObj.leftServiceName != undefined) {
            data.left.service_display_name = oSyncDataObj.leftServiceName;
            data.right.service_display_name = oSyncDataObj.rightServiceName;
        }
        if(data.data.left_module_id == "null") {
        removeChosenModules(data).then(function (response) {
            processGetResponse(response, "entityList");
            hideCommonLoading();
            $('[data-toggle="tooltip"]').tooltip();
        });
        } else{
            processGetResponse(data, "entityList");
            hideCommonLoading();
            $('[data-toggle="tooltip"]').tooltip();
        }

    });
}
function selectSavedFields(data) {
    saved_direction = data.data.direction;
    saved_leftModule = data.data.left_module_id;
    saved_rightModule = data.data.right_module_id;

    saved_masterservice = data.integProps.masterService;

    oSyncDataObj.setMasterService(saved_masterservice);

    $("#entityList_R span.directionSelector").removeClass("badge-secondary").addClass("badge-light");
    $("#entityList_R span.directionSelector").find("svg#" + saved_direction).parent("span").removeClass("badge-light").addClass("badge-secondary");

    var leftSelectedText = $("#entityList_R select#leftModule").attr("disabled","disabled").find("option#" + saved_leftModule).text();
    $("#entityList_R select#leftModule").val(leftSelectedText).select2({
        "width": "100%",
        "padding-top": "6px",
        "theme": "classic"
    });

    var rightSelectedText = $("#entityList_R select#rightModule").attr("disabled","disabled").find("option#" + saved_rightModule).text();
    $("#entityList_R select#rightModule").val(rightSelectedText).select2({
        "width": "100%",
        "padding-top": "6px",
        "theme": "classic"
    });
}

function removeChosenModules(data) {
    return new Promise((resolve, reject) => {
        var leftChosenModules = data.chosenModules.left_chosen_modules;
        var rightChosenModules = data.chosenModules.right_chosen_modules;

        var allLeftModules = data.left.modules;

        $.each(leftChosenModules, function (i, key) {
            allLeftModules = $.grep(allLeftModules, function (value) {
                var moduleKey = value.moduleId;
                return moduleKey != key;
            });
        });
        data.left.modules = allLeftModules;

        var allRightModules = data.right.modules;

        $.each(rightChosenModules, function (i, key) {
            allRightModules = $.grep(allRightModules, function (value) {
                var moduleKey = value.moduleId;
                return moduleKey != key;
            });
        });
        data.right.modules = allRightModules;
        resolve(data);
    });
}