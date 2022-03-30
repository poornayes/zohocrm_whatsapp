var SMSServiceController = {
    getAssigner: function (providerName) {
        var assigner;
        if (providerName === "zc"){  
            assigner = ZohoCRM;
        }
        return assigner;
    }
}
