var osyncId = "";
var integrationId = "";
var hash = "";
var leftModuleId = "";
var rightModuleId ="";
var leftModuleName ="";
var rightModuleName="";
var moduleDirection = "";
var leftServiceId ="";
var rightServiceId ="";
var leftServiceName= "";
var rightServiceName ="";
var leftImgName = "";
var rightImgName = "";
var masterService = "";
var syncStatus = "";
var rightMandatoryFieldsJson = {};

var oSyncData = function(){
    this.setLeftModuleId = function(lmId){
        this.leftModuleId = lmId;
    };
    this.setRightModuleId = function(rtId){
        this.rightModuleId = rtId;
    };
    this.setLeftModuleName = function(lmName){
        this.leftModuleName = lmName;
    };
    this.setRightModuleName = function(rtName){
        this.rightModuleName = rtName;
    };
    this.setModuleSyncDirection = function(mDir){
        this.moduleDirection = mDir;
    };
    this.setLeftServiceId = function(lsId){
        this.leftServiceId = lsId;
    };
    this.setRightServiceId = function(rsId){
        this.rightServiceId = rsId;
    };
    this.setLeftServiceName = function(lsName){
        this.leftServiceName = lsName;
    };
    this.setRightServiceName = function(rsName){
        this.rightServiceName = rsName;
    };
    this.setLeftServiceImgName = function(lsImgName){
        this.leftImgName = lsImgName;
    };
    this.setRightServiceImgName = function(rsImgName){
        this.rightImgName = rsImgName;
    };
    this.setIntegrationId = function(integId){
        this.integrationId = integId;
    };
    this.setOsyncId = function(osyncId){
        this.osyncId = osyncId;
    };
    this.setHash = function(hash){
        this.hash = hash;
    };
    this.setMasterService = function(masterService){
        this.masterService = masterService;
    };
    this.setSyncStatus = function(syncStatus){
        this.syncStatus = syncStatus;
    };


    this.retrieveModuleObj = function () {
        var moduleObj = {
            leftModuleId: assignValue(this.leftModuleId),
            rightModuleId: assignValue(this.rightModuleId),
            leftModuleName: assignValue(this.leftModuleName),
            rightModuleName: assignValue(this.rightModuleName),
            moduleDirection: assignValue(this.moduleDirection),
            leftServiceId:assignValue(this.leftServiceId),
            rightServiceId:assignValue(this.rightServiceId),
            leftServiceName:assignValue(this.leftServiceName),
            rightServiceName:assignValue(this.rightServiceName),
            integrationId:assignValue(this.integrationId),
            osyncId:assignValue(this.osyncId),
            hash:assignValue(this.hash),
            masterService:assignValue(this.masterService),
        };
        //clean(moduleObj);
        return moduleObj;
    };
}




// function clean(obj) {
//     var propNames = Object.getOwnPropertyNames(obj);
//     for (var i = 0; i < propNames.length; i++) {
//         var propName = propNames[i];
//         if (obj[propName] ===  "" || obj[propName] === null || obj[propName] === undefined) {
//             delete obj[propName];
//         }
//     }
// }
function assignValue(valueToAssign){
    return (valueToAssign === undefined) ? "" : valueToAssign;
}
// function assignObjectValue(valueToAssign){
//     return (valueToAssign === undefined) ? {} : valueToAssign;
// }
// function assignArrayValue(valueToAssign){
//     return (valueToAssign === undefined) ? [] : valueToAssign;
// }