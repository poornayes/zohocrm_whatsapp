var ServiceController = {
    getAssigner: function (providerName) {
        var assigner;
        if (providerName.indexOf("zohocrm") != -1){  
            assigner = ZohoCRM;
        } else if (providerName === "freshworks") {
            assigner = FreshDesk;
        } else {
            assigner = OsyncDefault;
        }
        return assigner;
    }
}
