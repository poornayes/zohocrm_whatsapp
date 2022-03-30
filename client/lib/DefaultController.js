var OsyncDefault = {
    init: function () {
        return new Promise((resolve, reject) => {
            this.setKeys().then(function () {
                resolve();
            });
        });
    },
    save: function (data) {
        return new Promise((resolve, reject) => {
            $.each(data, function (key, val) {
                if (val != undefined) {
                    localStorage.setItem(key, val);
                }
            });
            resolve();
        });

    },
    get: function (key) {
        return new Promise((resolve, reject) => {
            resolve(localStorage.getItem(key));
        });

    },
    delete: function (key) {
        return new Promise((resolve, reject) => {
            if (localStorage.getItem(key) != undefined) {
                resolve(localStorage.removeItem(key));
            } else {
                reject();
            }

        });
    },
    getUserData: function () {
        return new Promise((resolve, reject) => {
            var default_leftservice_id = "f1ad428a-41ea-469a-8733-1071351ba6c7";
            var default_rightservice_id = "8c5ddf04-26ac-4440-9207-d7fe8064f905";


            console.log("From DefaultCTrl:");

            var crmUserName = "Andrew"; //data.users[0].full_name;
            var crmId = getRandomId()+"_"+default_leftservice_id+"_"+default_rightservice_id;//data.users[0].zuid;
            var email = "kavith@gmail.com"; //data.users[0].email;

            var userData = {
                "left_service_id": default_leftservice_id,
                "right_service_id": default_rightservice_id,
                "companyId": crmId,
                "name": crmUserName,
                "email": email,
                "planName": "standard"
            };
            resolve(userData);
        });
    },
    setKeys : function(){
        return new Promise((resolve, reject) => {
            osyncIdKey = nameSpace + "__oSyncId";
            hashKey = nameSpace + "__hash";
            leftServiceIdKey = nameSpace + "__leftServiceId";
            rightServiceIdKey = nameSpace + "__rightServiceId";
            resolve();
        });
    },
};

function getRandomId() {
    var result = '';
    var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var charactersLength = characters.length;
    for (var i = 0; i < 8; i++) {
        result += characters.charAt(Math.floor(Math.random() * charactersLength));
    }
    return result;
}