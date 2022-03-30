var allServices = [];
var allModules = [];
var servicesObj = {};
var modulesObj = {};
var osyncProductionDomain = "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";
var osyncDevelopmentDomain = "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";

// var osyncProductionDomain = "http://localhost:7000";
// var osyncDevelopmentDomain = "http://localhost:7000";
var syncStatus =
{
    "0": "Not Started",
    "1": "Running",
    "2": "Paused",
    "3": "Stopped"
}

var syncDirection =
{
    "1": "From Left to Right",
    "2": "From Right to Left",
    "3": "Two Way"
}

function getOsyncDomain() {
    return $("#deploymentEnvironment").is(":checked") ? osyncDevelopmentDomain : osyncProductionDomain;
}

function sendEmail() {
    var url = getOsyncDomain() + "/adminapi/v1/login?module=admin";
    var email = $("#admin-email").val();
    if (!email || email.indexOf("@") == -1 || email.indexOf(".") == -1) {
        alert("Enter a valid email address");
        return false;
    }
    
    var postBody = {
        "email": email
    }
    $("#login-submit").text("Processing...");
    invokePostRequest(url, postBody).then(function(response) {
        var code = response["code"];
        if (code === "500") {
            $("#login-post-response").text("Error occurred..");
            $("#login-post-response").show();
            $("#login-submit").text("Login");
        } else {
            $("#login-post-response").text("Please check your email inbox for the OTP");
            $("#login-form button").hide()
            $("#login-form input").prop("disabled", true);
            $("#otp-form").toggle("slide");
        }
    }).catch(function(error) {
        $("#login-post-response").text("Error occurred..");
        $("#login-post-response").show();
        $("#login-submit").text("Login");
        
    });
    return false;
}

function validateOtp() {
    var url = getOsyncDomain() + "/adminapi/v1/validate?module=admin";
    var email = $("#admin-email").val();
    if (!email || email.indexOf("@") == -1 || email.indexOf(".") == -1) {
        alert("Enter a valid email address");
        return false;
    }
    var otp = $("#admin-otp").val();
    if (!otp) {
        alert("Enter the OTP. Please check your inbox");
        return false;
    }
    
    var postBody = {
        "email": email,
        "otp": otp
    }
    $("#otp-submit").text("Processing...");
    invokePostRequest(url, postBody).then(function(response) {
        var code = response["code"];
        if (code === "500") {
            $("#login-post-response").text("Error occurred..");
            $("#login-post-response").show();
            $("#login-submit").text("Login");
            $("#login-form button").show();
            $("#login-form input").prop("disabled", false);
            $("#otp-form").hide();
            $("#login-form").show();
        } else {
            $("#loginbutton").hide();
            $("#service").hide();
            $("#debug").show();
        }
    }).catch(function(error) {
        $("#login-post-response").text("Error occurred..");
        $("#login-post-response").show();
        $("#login-submit").text("Login");
        $("#login-form button").show();
        $("#login-form input").prop("disabled", false);
        $("#otp-form").hide();
        $("#login-form").show();
        
    });
    return false;
}

function login() {
    var url = getOsyncDomain() + "/adminapi/v1/ping?module=admin";
    invokeGetRequest(url).then(function (response) {
        $("#loginbutton").hide();
        $("#loginmessage").hide();
    }).catch(function (error) {
        console.log("errrrr");
        $("#loginbutton").show();
        $("#service").hide();
        $("#debug").hide();
        $("#loginmessage").hide();
    });
}

function addModule() {
    var url = getOsyncDomain() + "/adminapi/v1/service";
    
    var serviceId = $("#serviceId").val();
    
    
    var moduleOrder = $("#moduleOrder").val();
    var uniqueColumn = $("#uniqueColumn").val();
    var name = $("#name").val();
    var emailColumn = $("#emailColumn").val();
    var primaryColumn = $("#primaryColumn").val();
    
    
    if (serviceId === "") {
        alert("Enter service id");
        return false;
    }
    url+="/"+serviceId+"/module";
    var postBody = {
        "moduleOrder": moduleOrder,
        "serviceId": serviceId,
        "uniqueColumn": uniqueColumn,
        "name": name,
        "emailColumn": emailColumn,
        "primaryColumn": primaryColumn
    };
    
    invokePostRequest(url, postBody).then(function (response) {
        console.log(response);
    }).catch(function (error) {
        console.log(error);
    });
}

function addService() {
    var url = getOsyncDomain() + "/adminapi/v1/service?module=admin";
    
    var authScopes = $("#authScopes").val();
    var clientId = $("#clientId").val();
    var authorizeUrl = $("#authorizeUrl").val();
    var tokenUrl = $("#tokenUrl").val();
    var displayName = $("#displayName").val();
    var serviceName = $("#name").val();
    var clientSecret = $("#clientSecret").val();
    var refreshTokenUrl = $("#refreshTokenUrl").val();
    var authType = $("#authType").val();
    var dynamicModule = $("#dynamicModule").val();
    var revokeTokenUrl = $("#revokeTokenUrl").val();
    
    
    if (serviceName === "") {
        alert("Enter service name");
        return false;
    }
    
    if (dynamicModule !== "true" && dynamicModule !== "false") {
        alert("Enter valid dynamicModule");
        return false;
    }
    
    var postBody = {
        "name": serviceName,
        "authType": authType,
        "authScopes": authScopes,
        "clientId": clientId,
        "clientSecret": clientSecret,
        "authorizeUrl": authorizeUrl,
        "tokenUrl": tokenUrl,
        "refreshTokenUrl": refreshTokenUrl,
        "revokeTokenUrl": revokeTokenUrl,
        "dynamicModule" : dynamicModule,
        "displayName" : displayName
    };
    
    invokePostRequest(url, postBody).then(function (response) {
        console.log(response);
    }).catch(function (error) {
        console.log(error);
    });
}

function bindClickEvents() {
    
    $("#navbarCollapse li").off().on("click", function () {
        $("main.container").hide();
        $("#navbarCollapse .active").removeClass("active");
        $(this).toggleClass("active");
        $("#" + $(this).find("a").attr("type")).show();
    });
    
    $("input[name='authTypeOption']").off().on("click", function () {
        var selValue = $(this).val();
        console.log(selValue);
        if (selValue === "apikey") {
            $("div.form-group[type='oauth']").hide();
        } else {
            $("div.form-group[type='oauth']").show();
        }
    });
    
    $("input[box-type='search']").off().on("keypress", function (event) {
        var keycode = (event.keyCode ? event.keyCode : event.which);
        if (keycode == '13') {
            $("#toolLoading").show();
            var searchType = $(this).attr("search-type");
            if (searchType === "osync") {
                var searchText = $("#emailOrOsyncId").val();
                searchText = $.trim(searchText);
                searchOsync(searchText);
            } else if (searchType === "authinfo") {
                var searchText = $("#authinfoId").val();
                searchText = $.trim(searchText);
                searchAuthinfo(searchText);
            } else if (searchType === "integ") {
                var searchText = $("#integId").val();
                searchText = $.trim(searchText);
                searchInteg(searchText);
            } else if (searchType === "field") {
                var searchText = $("#fieldIntegId").val();
                searchText = $.trim(searchText);
                fetchFields(searchText);
            } else if (searchType === "integstatus") {
                var searchText = $("#integstatusId").val();
                searchText = $.trim(searchText);
                searchintegstatus(searchText);
            } else if (searchType === "reports") {
                var searchText = $("#fieldReportsId").val();
                searchText = $.trim(searchText);
                displayReports(searchText);
                
            }
            
            
        }
    });
    
    
    $("#syncdropdown").off().on("change", function (event) { 
        var value = $(this).val();
        $("#myTable tr").filter(function() {
            $(this).toggle($(this).text().indexOf(value) > -1)
        });  
    });
    
    
    $("#syncdropdown").off().on("change", function (event) { 
        var value = $(this).val();
        $("#myTable tr").filter(function() {
            $(this).toggle($(this).text().indexOf(value) > -1)
        });  
    });
    
    $("#list-records-list").off().on("click", function () {
        fetchRecords();
    });
    
    $("button[type1='subServiceModule']").off().on("click", function () {
        var attrId = $(this).attr("id");
        $("div[group='addServiveSubModule']").hide();
        $("div[type='" + attrId + "']").show();
        
        if (attrId === "listService") {
            var url = getOsyncDomain() + "/adminapi/v1/services?module=admin";
            invokeGetRequest(url).then(function (response) {
                allServices = response;
                var template = $("#services_template").html();
                var text = Mustache.render(template, response);
                $("#allServices").html(text).show();
                $("#allServices").select2();
                
            }).catch(function (error) {
                console.log(error);
            });
        } else if (attrId === "listModule") {
            var url = getOsyncDomain() + "/adminapi/v1/services?module=admin";
            invokeGetRequest(url).then(function (response) {
                allServices = response;
                var template = $("#services_template").html();
                var text = Mustache.render(template, response);
                $("#allModules").html(text).show();
                $("#allModules").select2();
            }).catch(function (error) {
                console.log(error);
            });
        } else if (attrId === "addModule") {
            loadModuleInfoSchema();
        } else if (attrId === "addService") {
            loadServiceInfoSchema();
        }
    });
    
    $('#viewMoreContainer').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget);
        var details = button.data('details');
        details = JSON.parse(decodeURIComponent(details));
        var respArray = [];
        $.each(details, function (key, value) {
            var repArg = {
                "key": key,
                "value": value
            }
            respArray.push(repArg);
        });
        var template = $("#view_more_info_template").html();
        var text = Mustache.render(template, respArray);
        $("#viewMoreContainer .modal-body").html(text).show();
        
    });
}

function loadServiceInfoSchema(){
    var url = getOsyncDomain() + "/adminapi/v1/service/schema";
    invokeGetRequest(url).then(function (response) {
        var respArray = [];
        $.each(response, function (key, value) {
            var placeholder = "";
            if(key ==="dynamicModule"){
                placeholder ="Value should be either true OR false";
            } else if(key ==="authScopes"){
                placeholder ="Value should be either comma separated OR space separated"
            }
            var repArg = {
                "key": key,
                "value": value,
                "placeholder" : placeholder
            }
            respArray.push(repArg);
        })
        
        var template = $("#service_schema_template").html();
        var text = Mustache.render(template, respArray);
        
        $("#serviceSchema").html(text).show();
        $("#serviceSchema #addServiceBtn").off().on("click", function () {
            addService();
        });
    }).catch(function (error) {
        console.log(error);
    });
}


function loadModuleInfoSchema(){
    var url = getOsyncDomain() + "/adminapi/v1/module/schema";
    invokeGetRequest(url).then(function (response) {
        var respArray = [];
        $.each(response, function (key, value) {
            var placeholder = "";
            if(key === "authType"){
                placeholder ="Value should be either oauth OR apikey";
            } else if(key ==="dynamicModule"){
                placeholder ="Value should be either true OR false";
            } else if(key ==="authScopes"){
                placeholder ="Value should be either comma separated OR space separated"
            }
            var repArg = {
                "key": key,
                "value": value,
                "placeholder" : placeholder
            }
            respArray.push(repArg);
        })
        
        var template = $("#service_schema_template").html();
        var text = Mustache.render(template, respArray);
        
        $("#moduleSchema").html(text).show();
        $("#moduleSchema #addModuleInformations").show();
        $("#moduleSchema #addServiceBtn").off().on("click", function () {
            addModule();
        });
    }).catch(function (error) {
        console.log(error);
    });
}
function loadServiceInfo(thisObj, callback) {
    var serviceId = $(thisObj).find("option:selected").attr("serviceid");
    var url = getOsyncDomain() + "/adminapi/v1/service/" + serviceId + "?module=admin";
    invokeGetRequest(url).then(function (response) {
        var respArray = [];
        $.each(response, function (key, value) {
            var repArg = {
                "key": key,
                "value": value
            }
            respArray.push(repArg);
        })
        
        var template = $("#service_info_template").html();
        var text = Mustache.render(template, respArray);
        
        $("#displayServiceInfo").html(text).show();
    }).catch(function (error) {
        console.log(error);
    });
}

function loadModuleInfo(thisObj, callback) {
    var serviceId = $(thisObj).find("option:selected").attr("serviceid");
    var url = getOsyncDomain() + "/adminapi/v1/service/" + serviceId + "/module";
    invokeGetRequest(url).then(function (response) {
        var respArray = [];
        $.each(response, function (key, value) {
            $.each(value, function (key1, value1) {
                var repArg = {
                    "key": key1,
                    "value": value1
                }
                respArray.push(repArg);
            });
            respArray.push({ "rowEnd": true });
        })
        
        var template = $("#module_info_template").html();
        var text = Mustache.render(template, respArray);
        
        $("#displayModuleInfo").html(text).show();
    }).catch(function (error) {
        console.log(error);
    });
}

function assignServiceInfoObj() {
    return new Promise((resolve, reject) => {
        if (allServices.length > 0) {
            $.each(allServices, function (key, value) {
                servicesObj[value.service_id] = value.name;
            });
            resolve();
        } else {
            var url = getOsyncDomain() + "/adminapi/v1/services?module=admin";
            invokeGetRequest(url).then(function (response) {
                allServices = response;
                $.each(allServices, function (key, value) {
                    servicesObj[value.service_id] = value.name;
                });
                resolve();
            }).catch(function (error) {
                console.log(error);
            });
        }
        
    });
}

function assignModulesInfoObj() {
    return new Promise((resolve, reject) => {
        if (allModules.length > 0) {
            $.each(allModules, function (key, value) {
                modulesObj[value.module_id] = value.name;
            });
            resolve();
        } else {
            var url = getOsyncDomain() + "/adminapi/v1/modules?module=admin";
            invokeGetRequest(url).then(function (response) {
                allModules = response;
                $.each(allModules, function (key, value) {
                    console.log(value);
                    modulesObj[value.module_id] = value.name;
                });
                resolve();
            }).catch(function (error) {
                console.log(error);
            });
        }
        
    });
}

function searchOsync(searchText) {
    
    $("#debug #displayServiceInfo").empty();
    $("#displayServiceErrorInfo").hide();
    $("#toolLoading").show();
    
    var url = getOsyncDomain() + "/adminapi/v1/customer?module=admin&searchText=" + searchText;
    
    invokeGetRequest(url).then(function (response) {
        $("#toolLoading").hide();
        if (response.length > 0) {
            
            if (response.error != undefined) {
                $("#debug #displayServiceInfo").hide();
                $("#debug #displayServiceErrorInfo").show();
            } else {
                var respArray = [];
                $.each(response, function (index, respJson) {
                    $.each(respJson, function (key, value) {
                        var temp = false;
                        if (key === "osync_id") {
                            temp = true;
                        }
                        var repArg = {
                            "key": key,
                            "value": value,
                            "show_link": temp,
                            "show_type": "integ",
                            "show_type_text": "View Integration"
                        }
                        respArray.push(repArg);
                    });
                    respArray.push({ "rowEnd": true });
                });
                
                var template = $("#service_info_template").html();
                var text = Mustache.render(template, respArray);
                
                $("#debug #displayServiceInfo").html(text).show();
            }
            
        } else {
            $("#debug #displayServiceInfo").hide();
            $("#debug #displayServiceErrorInfo").show();
        }
    }).catch(function (error) {
        console.log(error);
    });
}

function searchAuthinfo(authinfoId) {
    
    assignServiceInfoObj().then(function () {
        assignModulesInfoObj().then(function () {
            fetchAuthinfo(authinfoId);
        })
    });
}
function fetchAuthinfo(authinfoId) {
    
    $("#debug #displayServiceAuthInfo").empty();
    $("#displayServiceAuthErrorInfo").hide();
    
    var url = getOsyncDomain() + "/adminapi/v1/customer/authinfo/" + authinfoId + "?module=admin";
    invokeGetRequest(url).then(function (response) {
        //console.log(response);
        $("#toolLoading").hide();
        if (response.error != undefined) {
            $("#debug #displayServiceAuthInfo").hide();
            $("#debug #displayServiceAuthErrorInfo").show();
        } else {
            var respArray = [];
            $.each(response, function (index, respJson) {
                $.each(respJson, function (key, value) {
                    if (key === "left_service_id" || key === "right_service_id" || key === "master_service") {
                        value = value + " (" + servicesObj[value] + ")";
                    }
                    var temp = false;
                    if (key === "integ_id") {
                        temp = true;
                    }
                    if (key === "left_module_id" || key === "right_module_id") {
                        value = value + " (" + modulesObj[value] + ")";
                    }
                    
                    if (key === "sync_status") {
                        value = value + " (" + syncStatus[value] + ")";
                    }
                    
                    if (key === "direction") {
                        value = value + " (" + syncDirection[value] + ")";
                    }
                    var repArg = {
                        "key": key,
                        "value": value,
                        "show_link": temp,
                        "show_type": "field",
                        "show_type_text": "View Fields"
                    }
                    respArray.push(repArg);
                });
                respArray.push({ "rowEnd": true });
            });
            
            if (respArray.length > 0) {
                var template = $("#serviceauth_info_template").html();
                var text = Mustache.render(template, respArray);
                $("#debug #displayServiceAuthInfo").html(text).show();
            } else {
                $("#debug #displayServiceAuthInfo").hide();
                $("#debug #displayServiceAuthErrorInfo").show();
            }
        }
    }).catch(function (error) {
        console.log(error);
    });
}


function searchInteg(integId) {
    
    assignServiceInfoObj().then(function () {
        assignModulesInfoObj().then(function () {
            fetchIntegration(integId);
        })
    });
}
function fetchIntegration(integId) {
    
    $("#debug #displayIntegInfo").empty();
    $("#displayIntegErrorInfo").hide();
    
    var url = getOsyncDomain() + "/adminapi/v1/customer/integration/" + integId + "?module=admin";
    invokeGetRequest(url).then(function (response) {
        $("#toolLoading").hide();
        if (response.error != undefined) {
            $("#debug #displayIntegInfo").hide();
            $("#debug #displayIntegErrorInfo").show();
        } else {
            var respArray = [];
            $.each(response, function (index, respJson) {
                $.each(respJson, function (key, value) {
                    if (key === "leftServiceId" || key === "rightServiceId" || key === "master_service") {
                        value = value + " (" + servicesObj[value] + ")";
                    }
                    var temp = false;
                    if (key === "integ_id") {
                        temp = true;
                    }
                    if (key === "left_module_id" || key === "right_module_id") {
                        value = value + " (" + modulesObj[value] + ")";
                    }
                    
                    if (key === "sync_status") {
                        value = value + " (" + syncStatus[value] + ")";
                    }
                    
                    if (key === "direction") {
                        value = value + " (" + syncDirection[value] + ")";
                    }
                    var repArg = {
                        "key": key,
                        "value": value,
                        "show_link": temp,
                        "show_type": "field",
                        "show_type_text": "View Fields"
                    }
                    respArray.push(repArg);
                });
                respArray.push({ "rowEnd": true });
            });
            
            if (respArray.length > 0) {
                var template = $("#service_info_template").html();
                var text = Mustache.render(template, respArray);
                $("#debug #displayIntegInfo").html(text).show();
            } else {
                $("#debug #displayIntegInfo").hide();
                $("#debug #displayIntegErrorInfo").show();
            }
        }
    }).catch(function (error) {
        console.log(error);
    });
}

function fetchFields(integId) {
    var fetchFieldsInfo = function () {
        $("#debug #displayFieldInfo").empty();
        $("#displayFieldErrorInfo").hide();
        
        var url = getOsyncDomain() + "/adminapi/v1/customer/integration/" + integId + "/fields?module=admin";
        invokeGetRequest(url).then(function (response) {
            $("#toolLoading").hide();
            if (response.error != undefined) {
                $("#debug #displayFieldInfo").hide();
                $("#debug #displayFieldErrorInfo").show();
            } else {
                var respArray = [];
                
                $.each(response, function (index, respJson) {
                    var fieldJson = {};
                    $.each(respJson, function (key, value) {
                        
                        if (key === "fieldmap_id" || key === "left_column_name"
                        || key === "right_column_name" || key === "left_column_type"
                        || key === "right_column_type" || key === "left_column_format"
                        || key === "right_column_format" || key === "enabled") {
                            fieldJson[key] = value;
                        }
                        
                    });
                    fieldJson["full_info"] = encodeURIComponent(JSON.stringify(respJson));
                    respArray.push(fieldJson);
                });
                if (respArray.length > 0) {
                    var template = $("#field_info_template").html();
                    var text = Mustache.render(template, respArray);
                    $("#debug #displayFieldInfo").html(text).show();
                } else {
                    $("#debug #displayFieldInfo").hide();
                    $("#debug #displayFieldErrorInfo").show();
                }
            }
        }).catch(function (error) {
            console.log(error);
        });
    }
    assignServiceInfoObj().then(function () {
        assignModulesInfoObj().then(function () {
            fetchIntegrationInFieldSection(integId, "displayFieldInfo", "displayFieldErrorInfo", "displayFieldIntegInfo");
            fetchFieldsInfo(integId);
        })
    });
}

function searchintegstatus(integstatusId) {
    
    assignServiceInfoObj().then(function () {
        assignModulesInfoObj().then(function () {
            fetchintegstatus(integstatusId);
        })
    });
}
function fetchintegstatus(integstatusId) {
    
    $("#debug #displayIntegStatusInfo").empty();
    $("#displayIntegStatusErrorInfo").hide();
    
    var url = getOsyncDomain() + "/adminapi/v1/customer/integstatus/" + integstatusId + "?module=admin";
    invokeGetRequest(url).then(function (response) {
        $("#toolLoading").hide();
        if (response.error != undefined) {
            $("#debug #displayIntegStatusInfo").hide();
            $("#debug #displayIntegStatusErrorInfo").show();
        } else {
            var respArray = [];
            $.each(response, function (index, respJson) {
                $.each(respJson, function (key, value) {
                    if (key === "left_service_id" || key === "right_service_id" || key === "master_service") {
                        value = value + " (" + servicesObj[value] + ")";
                    }
                    var temp = false;
                    if (key === "integ_id") {
                        temp = true;
                    }
                    if (key === "left_module_id" || key === "right_module_id") {
                        value = value + " (" + modulesObj[value] + ")";
                    }
                    
                    if (key === "sync_status") {
                        value = value + " (" + syncStatus[value] + ")";
                    }
                    
                    if (key === "direction") {
                        value = value + " (" + syncDirection[value] + ")";
                    }
                    var repArg = {
                        "key": key,
                        "value": value,
                        "show_link": temp,
                        "show_type": "field",
                        "show_type_text": "View Fields"
                    }
                    respArray.push(repArg);
                });
                respArray.push({ "rowEnd": true });
            });
            
            if (respArray.length > 0) {
                var template = $("#integstatus_info_template").html();
                var text = Mustache.render(template, respArray);
                $("#debug #displayIntegStatusInfo").html(text).show();
            } else {
                $("#debug #displayIntegStatusInfo").hide();
                $("#debug #displayIntegStatusErrorInfo").show();
            }
        }
    }).catch(function (error) {
        console.log(error);
    });
}


function fetchIntegrationInFieldSection(integId, tagId, tagErrorId, tagIntegInfo) {
    $("#debug #" + tagId).empty();//displayFieldInfo
    $("#" + tagErrorId).hide();//displayFieldErrorInfo
    
    var url = getOsyncDomain() + "/adminapi/v1/customer/integration/" + integId + "?module=admin";
    invokeGetRequest(url).then(function (response) {
        if (response.error != undefined) {
            $("#debug #" + tagIntegInfo).hide();//displayFieldIntegInfo
        } else {
            
            var respArray = [];
            $.each(response, function (index, respJson) {
                $.each(respJson, function (key, value) {
                    if (key === "left_service_id" || key === "right_service_id") {
                        value = value + " (" + servicesObj[value] + ")";
                        var repArg = {
                            "key": key,
                            "value": value
                        }
                        respArray.push(repArg);
                    }
                    if (key === "osync_id" || key === "integ_id") {
                        var repArg = {
                            "key": key,
                            "value": value
                        }
                        respArray.push(repArg);
                    }
                });
                respArray.push({ "rowEnd": true });
            });
            
            if (respArray.length > 0) {
                var template = $("#service_info_template").html();
                var text = Mustache.render(template, respArray);
                $("#debug #" + tagIntegInfo).html(text).show();
            } else {
                $("#debug #" + tagIntegInfo).hide();
            }
        }
    }).catch(function (error) {
        console.log(error);
    });
}
function showIntegrationDetails(thisObj) {
    var showType = $(thisObj).attr("show_type");
    if (showType === "integ") {
        $("#list-integ-list").trigger("click");
    } else if (showType === "field") {
        $("#list-field-list").trigger("click");
    }
    
    var integId = $.trim($(thisObj).siblings("strong").text());
    $("#toolLoading").show();
    assignServiceInfoObj().then(function () {
        assignModulesInfoObj().then(function () {
            if (showType === "integ") {
                fetchIntegration(integId);
            } else if (showType === "field") {
                fetchIntegrationInFieldSection(integId, "displayFieldInfo", "displayFieldErrorInfo", "displayFieldIntegInfo");
                fetchFields(integId);
            }
        })
    });
}

function displayReports(integId) {
    $("#toolLoading").show();
    assignServiceInfoObj().then(function () {
        assignModulesInfoObj().then(function () {
            fetchIntegrationInFieldSection(integId, "displayReportsInfo", "displayReportsErrorInfo", "displayReportsIntegInfo");
            fetchReports(integId);
        })
    });
}

function fetchAllSyncLogData(){
    var url = getOsyncDomain() + "/adminapi/v1/customer/rec?module=admin";
    invokeGetRequest(url).then(function (response){
        console.log("res : ", response);
    }).catch(function (error) {
        console.log(error);
    });
}



function fetchReports(integId) {
    $("#debug #displayReportsInfo").empty();
    $("#displayReportsIntegInfo").hide();
    
    var url = getOsyncDomain() + "/adminapi/v1/customer/integration/" + integId + "/reports?module=admin";
    invokeGetRequest(url).then(function (response) {
        $("#toolLoading").hide();
        if (response.error != undefined) {
            $("#debug #displayReportsInfo").hide();
            $("#debug #displayReportsErrorInfo").show();
        } else {
            var respArray = [];
            
            $.each(response, function (index, respJson) {
                var fieldJson = {};
                $.each(respJson, function (key, value) {
                    
                    if (key === "left_count_fetched" || key === "left_count_updated"
                    || key === "left_no_change_count" || key === "right_no_change_count"
                    || key === "right_count_fetched" || key === "matched_on_unique_column"
                    || key === "right_count_updated" || key === "left_count_created"
                    || key === "right_count_created" || key === "left_errors_count"
                    || key === "right_errors_count" || key === "duplicates_count"
                    || key === "conflict_count" || key === "start_time" || key === "end_time"
                    || key === "left_skipped_for_email_column" || key === "right_skipped_for_email_column") {
                        fieldJson[key] = value;
                    }
                    
                });
                fieldJson["full_info"] = encodeURIComponent(JSON.stringify(respJson));
                respArray.push(fieldJson);
            });
            if (respArray.length > 0) {
                var template = $("#reports_info_template").html();
                var text = Mustache.render(template, respArray);
                $("#debug #displayReportsInfo").html(text).show();
            } else {
                $("#debug #displayReportsInfo").hide();
                $("#debug #displayReportsErrorInfo").show();
            }
        }
    }).catch(function (error) {
        console.log(error);
    });
}


function fetchRecords() { 
    $("#debug #displayRecordsInfo").empty();
    $("#displayRecordsErrorInfo").hide();
    
    var url = getOsyncDomain() + "/adminapi/v1/customer/records?module=admin";
    
    invokeGetRequest(url).then(function (response) {
        $("#toolLoading").hide();
        
        if (response.error != undefined) {
            $("#debug #displayRecordsInfo").hide();
            $("#debug #displayRecordsErrorInfo").show();
        } else {
            var respArray = [];
            
            var successcount = 0;
            $.each(response, function (index, respJson) {
                var fieldJson = {};
                var success=0;
                var errors=0;
                $.each(respJson, function (key, value) {
                    if(key=="left_count_created" || key=="left_count_fetched" || key=="left_count_updated" || key=="left_no_change_count" || key=="right_count_created"
                    || key=="right_count_fetched" || key=="right_count_updated" || key=="right_no_change_count"){
                        
                        success+=parseInt(value);
                    }
                    if(key=="left_errors_count" || key=="right_errors_count"){
                        
                        errors+=parseInt(value);
                    }
                    fieldJson[key] = value;
                    
                });
                fieldJson['successvalue'] = success;
                fieldJson['errorsvalue'] = errors;
                
                fieldJson["full_info"] = encodeURIComponent(JSON.stringify(respJson));
                respArray.push(fieldJson);
                
            });
            if (respArray.length > 0) {
                var template = $("#fetchrecordscript").html();
                var text = Mustache.render(template, respArray);
                $("#fetchrecordsInfo").html(text).show();
            } else {
                $("#debug #fetchrecordsInfo").hide();
                $("#debug #fetchrecordsErrorInfo").show();
            }
        }
    });        
    
}
function showSpinner(thisObj){
    $(thisObj).find(".spinnerBox").addClass("spinner-border spinner-border-sm");
}

function hideSpinner(thisObj){
    $(thisObj).find(".spinnerBox").removeClass("spinner-border spinner-border-sm");
}