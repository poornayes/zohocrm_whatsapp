function serviceAuth(buttonName) {
    var buttonId = buttonName; //$(thisObj).attr("id");
    var authUrl = $("#" + buttonId).attr("url");
    var authType = $("#" + buttonId).attr("auth_type");
    if (authType === "apikey" || authType === "baseurl_oauth" || authType === "baseurl_apikey") {
        $("#apiKeyModal").modal('show');
        $("#apiKeyModal").attr("serviceId", $("#" + buttonId).attr("name"));
        $("#apiKeyModal,#apiKeySave").attr("isLeft", $("#" + buttonId).data("isleft"));
        
        if(authType === "baseurl_oauth"){
            
            $("#domainUrlInputDiv").show();
            $("#apiKeyInputDiv").hide();
            $("#apiKeySave").text("Save and Authorize");
        } else if(authType === "baseurl_apikey"){
            $("#domainUrlInputDiv").show();
            $("#apiKeyInputDiv").show();
            $("#apiKeySave").text("Save");
         } else {
            $("#apiKeyInputDiv").show();
            $("#apiKeySave").text("Save");
        }
    } else {
        var windowName = buttonName;
        var btnText = $("#" + buttonId).text();
        if (btnText == "Revoke") { 
            revokeAction(buttonName); 
        } else { 
            openNewWindow(windowName, $("#" + buttonId).attr("name"), authUrl, buttonId, authType); 
        }
    }

}
function revokeAction(thisObj) {
    $('#revokeConfirmationBox').modal('hide');
    showCommonLoading("Revoking the authentication.");
    var serviceId = $(thisObj).data("serviceId");
    var isLeft = $(thisObj).data("isleft");
    var urlRevoke = "/api/v1/revoke?service_id=" + serviceId + "&osync_id=" + oSyncId + "&integ_id=" + integrationId + "&left_service=" + isLeft;
    var buttonName = "leftButton";
    if (!isLeft) {
        buttonName = "rightButton";
    }
    invokeAPI_Delete(urlRevoke, buttonName)
}

function openNewWindow(windowName, serviceName, authURL, buttonName, authType) {
    var winSize = 'height=620,width=600,top=200,left=300,resizable';
    windowName = window.open(authURL, serviceName, winSize);
    if (window.focus) { windowName.focus(); }
    // $("#" + buttonName).find("span").addClass("spinner-border spinner-border-sm");
}

function showAuthPage(thisObj) {
    showCommonLoading("Loading Home page.Take a breath...");
    var url = "/api/v1/integrate?module=integ";
    postInvoker(url, providerData).then(function (data) {
        processPostResponse(data, "serviceList");
        hideCommonLoading();
        $("#configPage").show();
        $("#landingPage").hide();
        $("a#serviceListBreadCrumb").parent("li").addClass("active");
    });

}
function getAuthPageData() {
    showCommonLoading("Loading Home page.Take a breath...");
    var url = "/api/v1/integration/" + oSyncDataObj.integrationId + "?module=integ";
    getInvoker(url).then(function (data) {
        processGetResponse(data, "serviceList");
        hideCommonLoading();
    });
}

function invokeAuthMethod() {
    providerObj.getUserData().then(function (data) {
        providerData = data;
        showAuthPage();
    }).catch(function (err) {
        console.log("providerData errror ::::: ", err);
        console.log(err);
    });
}
