const catalyst = require('zcatalyst-sdk-node');

module.exports = (cronDetails, context) => {
	let ts = getRandomId();
	console.log('Hello from osync_cron_handler.js');

	console.log(catalyst);
	console.log(cronDetails);
	

	// let cronParams = cronDetails.getCronParam('');
	// let remainingExecutionCount = cronDetails.getRemainingExecutionCount();
	// let thisCronDetails = cronDetails.getCronDetails();
	// let projectDetails = cronDetails.getProjectDetails();

	// let remainingTime = context.getRemainingExecutionTimeMs();
	// let executionTime = context.getMaxExecutionTimeMs();
	const app = catalyst.initialize(context);

	console.log(app);

	let circuit = app.circuit();

	console.log(circuit);

	circuit
		.execute('4344000001214001', 'oSyncScheduler_'+ts, {})
		.then((result) => {
			console.log(result);
			context.closeWithSuccess(); //end of application with success
		})

	/* 
        CONTEXT FUNCTIONALITIES
    */
	
	// context.closeWithFailure(); //end of application with failure
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
