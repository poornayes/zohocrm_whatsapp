var fieldMapDataPL = {};


function validateValues() {
    var leftMandatoryArr = [];
    $("#inputElementsDiv input[ismandatory='true']").each(function () {
        var thisObj = $(this);
        var isValuePresent = thisObj.parents("div.autoSuggestDummy").find("select").val() != "";
        if (!isValuePresent) {
            leftMandatoryArr.push($(this).val());
        }
    });

    if (leftMandatoryArr.length > 0) {
        displayFailureMessage("Missing mandatory fields for <b>" + oSyncDataObj.leftServiceName + "</b> Service : " + leftMandatoryArr);
        window.scrollTo(0, 0);
        hideCommonLoading();
        return false;
    }


    var rightMandatory = ($("#rightMandatoryFields").val()).split(",");
    if (rightMandatory == "") { return true; }
    $("#inputElementsDiv select.rightServiceSelect option:selected[value != '']").each(function () {
        var remove_Item = $(this).val();
        rightMandatory = $.grep(rightMandatory, function (value) {
            return value != remove_Item;
        });
    });

    if (rightMandatory.length > 0) {
        var display_name_arr = [];
        $.each(rightMandatory, function (k, d) {
            display_name_arr.push(rightMandatoryFieldsJson[d]);
        });
        displayFailureMessage("Missing mandatory fields for <b>" + oSyncDataObj.rightServiceName + "</b> Service : " + display_name_arr);
        window.scrollTo(0, 0);
        hideCommonLoading();
        display_name_arr = [];
        return false;
    }
    return true;
}
function saveFields() {
    var isValidated = validateValues();
    if (!isValidated) { return; }
    if (sync_status == 0 || fieldMapDataChanged) {
        fieldMapDataChanged = false;
        if (isValidated) {
            var fieldMapArr = [];
            var format = "";
            var dataTypeArr = ["text", "number", "boolean", "double", "date", "date_time", "pickList", "childPickList"];
            $.each(dataTypeArr, function (k, dataType) {
                var oneWayBoolean = false;
                var rightParentId = "";
                var leftParentId = "";

                $("input[type='text'][id$='_osyncLeftField_" + dataType + "']").each(function () {

                    if (dataType == "date" || dataType == "date_time") {
                        oneWayBoolean = true;
                    }



                    var rightFieldId = this.name + "_osyncRightField_" + dataType;

                    if(containsNumbers(rightFieldId)){
                        rightFieldId = escapeHtml(rightFieldId);
                    }
                    else {
                        rightFieldId = $.escapeSelector(rightFieldId);
                    }
                    
                    var rightFieldValue = $('#' + rightFieldId + ' :selected').val();

                    var leftType = $(this).attr("dataType");
                    var rightType = $('#' + rightFieldId + ' option:selected').attr("dataType");
                    if ((leftType == "date" && rightType == "date") || (leftType == "date_time" && rightType == "date_time")) { oneWayBoolean = false; }
                    if ((leftType == "pickList" && rightType == "pickList") || (leftType == "childPickList" && rightType == "childPickList")) {
                        rightParentId = $('#' + rightFieldId + ' :selected').attr("parentId");
                        leftParentId = $(this).attr("parentId");
                    }

                    if (rightFieldValue != "" && rightFieldValue != undefined) {
                        var fieldMapJson = {};
                        fieldMapJson["osync_id"] = oSyncId;
                        fieldMapJson["integ_id"] = integrationId;
                        fieldMapJson["leftColumnName"] = this.name;
                        fieldMapJson["rightColumnName"] = rightFieldValue;
                        fieldMapJson["leftColumnType"] = leftType;
                        fieldMapJson["rightColumnType"] = rightType;
                        fieldMapJson["one_way"] = oneWayBoolean;
                        fieldMapJson["parentId"] = leftParentId + "_" + rightParentId;


                        if (dataType === "date" || dataType === "date_time") {
                            fieldMapJson["format"] = $(this).attr("format");
                        }

                        fieldMapArr.push(fieldMapJson);
                    }
                });
            });
            var saveFieldsURL = "/api/v1/integration/" + integrationId + "/fields?module=integ";

            postInvoker(saveFieldsURL, fieldMapArr).then(function (data) {
                args = {
                    "left_service_name": oSyncDataObj.leftServiceName,
                    "right_service_name": oSyncDataObj.rightServiceName,
                    "left_service_id": oSyncDataObj.leftServiceId,
                    "right_service_id": oSyncDataObj.rightServiceId
                }
                if (oSyncDataObj.moduleDirection == 3) {
                    processPostResponse(args, "syncConfiguration");
                } else {
                    saveSyncConfiguration();
                }
            });
            return true;
        }
    } else {
        args = {
            "left_service_name": oSyncDataObj.leftServiceName,
            "right_service_name": oSyncDataObj.rightServiceName,
            "left_service_id": oSyncDataObj.leftServiceId,
            "right_service_id": oSyncDataObj.rightServiceId
        }
        if (oSyncDataObj.moduleDirection == 3) {
            processPostResponse(args, "syncConfiguration");
        } else {
            saveSyncConfiguration();
        }
    }
}
function getLeftFields(callback) { //Show All Fields

    showCommonLoading("Loading " + oSyncDataObj.leftServiceName + " fields for " + oSyncDataObj.leftModuleName + " module...");
    authHeader = oSyncDataObj.hash;
    var url = preURL + "/api/v1/all-fields?module=integ&module_id=" + oSyncDataObj.leftModuleId + "&service_id=" + leftServiceId + "&integ_id=" + integrationId + "&left_service=true";
    var showNotNullInfo = false;
    if (oSyncDataObj.leftServiceName == "Monday.com" || oSyncDataObj.rightServiceName == "Monday.com") {
        showNotNullInfo = true;
    }
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
            fieldMapJsonVal = {
                "showNotNullInfo": showNotNullInfo,
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
            var dir = oSyncDataObj.moduleDirection;
            if (dir != 3) { fieldMapJsonVal.show_sync_conf = false; }

            if (data.pickListFields != null) {
                data = formatPicklistFields(data);
            }
            fieldMapJsonVal.leftArgs.data = data;
            //leftfieldDataForPL = data;
            fieldMapJsonVal.leftArgs.mandatory = [];
            if (callback != undefined) {
                callback();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
        }
    });
}
function getRightFields(callback) { //Show All Fields
    showCommonLoading("Loading " + oSyncDataObj.rightServiceName + " fields for " + oSyncDataObj.rightModuleName + " module...");
    authHeader = oSyncDataObj.hash;
    var url = preURL + "/api/v1/all-fields?module=integ&module_id=" + oSyncDataObj.rightModuleId + "&service_id=" + rightServiceId + "&integ_id=" + integrationId + "&left_service=false";
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
            if (data.pickListFields != null) {
                data = formatPicklistFields(data);
            }
            fieldMapJsonVal.rightArgs.data = data;
            var rightMandatoryFields = [];

            $.each(data, function (i, val) {
                $.each(val, function (index, value) {
                    if (value.mandatory) {
                        rightMandatoryFields.push(value.id);
                        rightMandatoryFieldsJson[value.id] = value.displayName;
                    }
                });
            });
            fieldMapJsonVal.rightArgs.mandatory = rightMandatoryFields;
            args = fieldMapJsonVal;
            //   rightfieldDataForPL = fieldMapJsonVal;
            if (callback != undefined) {
                callback();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(jqXHR);
        }
    });
}
function showFieldMapPage() {
    showCommonLoading("Loading Field mapping page details");


    var rightCallBackFunction = function () {
        populateDetails("fieldMapList");
    }
    var leftCallBackFunction = function () {
        getRightFields(rightCallBackFunction);
    }
    getLeftFields(leftCallBackFunction);
    args = fieldMapJsonVal;
}
function showFieldPage() {
    $("#showModuleList").hide();
    showCommonLoading("Validating the values. Please hold on");
    saveModuleData();
}
function autoMatch(left, right) {
    return new Promise((resolve, reject) => {
        showCommonLoading("Auto-matching Fields...");
        var leftObj = left;
        var rightObj = right;

        match = {};

        $.each(leftObj, function (key, value) {
            if( key != "pickListFields" && key != "childListFields"){
            var leftFields = value;
            var rightFields = rightObj[key];
            var leftArr = [];
            var rightArr = [];
            var multipleChecker = ["perfectMatch", "startsWith", "containsMatch"];

            $.each(multipleChecker, function (k, checkerName) {
                $.each(leftFields, function (key, value) {
                   
                    var leftFieldsId = value.id;
                    leftFieldsIdstr = JSON.stringify(leftFieldsId);
                    leftFieldsIds = leftFieldsIdstr.replace(/[^a-zA-Z0-9_-]/g, "").replace(/[_\s]/g, '').toLowerCase().trim();

                    $.each(rightFields, function (key, value) {

                        var rightFieldsId = value.id;
                        if (jQuery.inArray(rightFieldsId, rightArr) == -1 && jQuery.inArray(leftFieldsId, leftArr) == -1) {
                            rightFieldsIdstr = JSON.stringify(rightFieldsId);
                            rightFieldsIds = rightFieldsIdstr.replace(/[^a-zA-Z0-9_-]/g, "").replace(/[_\s]/g, '').toLowerCase().trim();
                            if (checkerName == "perfectMatch") { perfectMatch(leftFieldsIds, rightFieldsIds, match, leftFieldsId, rightFieldsId, rightArr, leftArr); }
                            else if (checkerName == "startsWith") { startsWith(leftFieldsIds, rightFieldsIds, match, leftFieldsId, rightFieldsId, rightArr, leftArr); }
                            else if (checkerName == "containsMatch") { containsMatch(leftFieldsIds, rightFieldsIds, match, leftFieldsId, rightFieldsId, rightArr, leftArr); }
                            else if (checkerName == "mandFieldsMatch") { mandFieldsMatch(); }
                        }
                    });
                
                });
            });
        }
        });
    
        
        $.each(match, function (key, value) {

            if(containsNumbers(key)){
                key = escapeHtml(key);
            }
            else {
                key = $.escapeSelector(key); 
            }
            
            $("#inputElementsDiv input[name=" + key + "]").parents("div.autoSuggestDummy").addClass("selectedFieldRow").find("select.rightServiceSelect").val(value).select2({
                "width": "100%",
                "padding-top": "6px",
                "theme": "classic"
            });
        });
        disableSelectedOption();
        resolve();
    });
}
function perfectMatch(leftFieldsIds, rightFieldsIds, match, leftFieldsId, rightFieldsId, rightArr, leftArr) {
    if (leftFieldsIds === rightFieldsIds) {
        rightArr.push(rightFieldsId);
        leftArr.push(leftFieldsId);
        match[leftFieldsId] = rightFieldsId;
    }
}
function startsWith(leftFieldsIds, rightFieldsIds, match, leftFieldsId, rightFieldsId, rightArr, leftArr) {
    if (leftFieldsIds.startsWith(rightFieldsIds) || rightFieldsIds.startsWith(leftFieldsIds)) {
        rightArr.push(rightFieldsId);
        leftArr.push(leftFieldsId);
        match[leftFieldsId] = rightFieldsId;
    }
}
function containsMatch(leftFieldsIds, rightFieldsIds, match, leftFieldsId, rightFieldsId, rightArr, leftArr) {
    if (leftFieldsIds.indexOf(rightFieldsIds) > 0 || rightFieldsIds.indexOf(leftFieldsIds) > 0) {
        rightArr.push(rightFieldsId);
        leftArr.push(leftFieldsId);
        match[leftFieldsId] = rightFieldsId;
    }
}

function disableSelectedOption() {
    $("select option").removeAttr('disabled');
    $("select").each(function (i, s) {
        var a = $(s).val();
        if (a != "") {
            $("select").not(s).find("option[value='" + $(s).val() + "']").attr('disabled', 'disabled');
        }
    });
}

function fieldRetriver() {
    return new Promise((resolve, reject) => {
        integrationId = oSyncDataObj.integrationId;
        var savedFieldData = {};
        var getSavedFieldsURL = "/api/v1/integration/" + integrationId + "/fields?module=integ";
        getInvoker(getSavedFieldsURL).then(function (data) {
            savedFieldData = processGetResponse(data, "fieldRetriver");
            $.each(savedFieldData, function (i, row) {
                var fieldRow = {};
                leftColName = row.leftColumnName;
                rightColName = row.rightColumnName;
                syncOneWay = row.oneWay;
                parentId = row.parentId;
                leftColType = row.leftColumnType;

                if(containsNumbers(leftColName)){
                    leftColName = escapeHtml(leftColName);
                }
                else {
                    leftColName = $.escapeSelector(leftColName);
                }

                var selectObject = $("#inputElementsDiv input[name=" + leftColName + "]").parents("div.autoSuggestDummy").addClass("selectedFieldRow").find("select.rightServiceSelect");
                if (selectObject.find("option[value='" + rightColName + "']").length > 0) {

                    selectObject.val(rightColName).select2({
                        "width": "100%",
                        "padding-top": "6px",
                        "theme": "classic"
                    });

                    if(leftColType == "pickList"){populateChildFieldDataRetriver(selectObject,leftColName,rightColName,savedFieldData,parentId);}
                } else {
                    $("#inputElementsDiv input[name=" + leftColName + "]").parents("div.autoSuggestDummy").removeClass("selectedFieldRow");
                }
                
            });
            disableSelectedOption();
        });
        resolve();
    });


}

function populateChildFieldData(selectObject) {
    var optionResultJson = {};
    var rightFieldId = selectObject.id;
    var _childFieldData_id = rightFieldId.substring(0, rightFieldId.indexOf("_osyncRightField_pickList"));

    var rest = rightFieldId.substring(0, rightFieldId.lastIndexOf("_"));

    rightParentId = $('#' + rightFieldId + ' :selected').attr("parentId");
    $("#" + _childFieldData_id + "_childFieldData").show();

    $.each(fieldMapDataPL.pickListFields, function (count, jsonSet) {

        if (jsonSet.parentId == rightParentId) {
            optionResultJson["childs"] = jsonSet.childs;
        }
    });
    

    var template = $("#childOptionList").html();
    var txt = Mustache.render(template, optionResultJson);
    //$("#"+rest+"_childPickList").html(txt);
    var elements = document.querySelectorAll('[name=' + rest + '_childPickList]');
    $.each(elements, function (count, ele) {
        ele.innerHTML = txt
    });
}


function populateChildFieldDataRetriver(selectObject,leftColName,rightColName,savedFieldData,parentId) {

    var optionResultJson = {};
    var rest = leftColName+"_osyncRightField" //Industry_osyncRightField //leftColName_osyncRightField

    rightParentId = selectObject.attr("parentId"); //nummber
    $("#" + leftColName + "_childFieldData").show();
    pId = parentId.substring(parentId.lastIndexOf("_")+1, parentId.length);


    $.each(fieldMapDataPL.pickListFields, function (count, jsonSet) {
        if (jsonSet.parentId == pId) {
            optionResultJson["childs"] = jsonSet.childs;
        }
    });
    var template = $("#childOptionList").html();
    var txt = Mustache.render(template, optionResultJson);
    var elements = document.querySelectorAll('[name=' + rest + '_childPickList]');
    $.each(elements, function (count, ele) {
        ele.innerHTML = txt
    });

    $.each(savedFieldData, function (c, j) {
        if(j.leftColumnType == "childPickList"){ 
            if (j.parentId == parentId) {
                leftColName = j.leftColumnName;
                rightColName = j.rightColumnName;

                var selectObject = $("#inputElementsDiv input[name=" + leftColName + "]").parents("div.autoSuggestDummy").addClass("selectedFieldRow").find("select."+leftColName);
                if (selectObject.find("option[value='" + rightColName + "']").length > 0) {
                    selectObject.val(rightColName).select2({
                        "width": "100%",
                        "padding-top": "6px",
                        "theme": "classic"
                    });
                }
            }

        }
    });
}

function formatPicklistFields(data) {
    $.each(data.pickListFields, function (count, jsonSet) {
        var parentId = jsonSet.id;
        var childArr = [];
        $.each(data.childListFields, function (c_count, c_jsonSet) {
            if (c_jsonSet.parentId == parentId) {
                childArr.push(c_jsonSet);
            }
        });
        jsonSet["childs"] = childArr;
    });
    fieldMapDataPL = data;
    return data;
}

function escapeHtml(unsafe)
{
    return unsafe
         .replaceAll(/&/g, "&amp;")
         .replaceAll(/</g, "&lt;")
         .replaceAll(/>/g, "&gt;")
         .replaceAll(/"/g, "&quot;")
         .replaceAll(/'/g, "&#039;")
         .replaceAll(/ /g, "\\ ");
 }

 function containsNumbers(str){
    var regexp = /\d/g;
    return regexp.test(str);
  };
