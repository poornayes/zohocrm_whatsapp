var args = {};
var providerObj = {};
var ngrokURL = "";
var preURL =  "https://8wwnpsl8i1.execute-api.us-east-2.amazonaws.com/Prod";

var oSyncDataObj = new oSyncData();
var providerData = "";
var integData = {}; //json for overAllReport 
var integIdArr = []; //json for overAllReport 
var sync_status=0;
var moduleInfo = {};
var fieldMapDataChanged = false;

var nameSpace = "";

// var osyncIdKey = nameSpace + "__oSyncId";
// var hashKey = nameSpace+"__hash";
// var leftServiceIdKey = nameSpace +"__leftServiceId";
// var rightServiceIdKey = nameSpace +"__rightServiceId";

var osyncIdKey = "";
var hashKey = "";
var leftServiceIdKey = "";
var rightServiceIdKey = "";

var fieldMapJsonVal = {};
