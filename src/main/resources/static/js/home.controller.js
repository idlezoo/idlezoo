idlezoo.controller('home', function($rootScope, $scope, $interval, $http) {
	if(!$rootScope.authenticated){
		return;
	}
	var socket = new SockJS("/game/ws");
	
	
	var self = this;
	
	socket.onopen = function () {
		// Socket open.. start the game loop.
		console.log('Info: WebSocket connection opened.');
		socket.send('me');
		setInterval(function() {
			// Prevent server read timeout.
			socket.send('ping');
		}, 5000);

		
		
		$scope.buy = function($buildingName) {
			socket.send('buy/' + $buildingName);
		};
		
		$scope.upgrade = function($buildingName) {
			socket.send('upgrade/' + $buildingName);
		};
		
		$scope.buyPerk = function($perkName) {
			socket.send('buyPerk/' + $perkName);
		};
		
		$scope.fight = function(){
			socket.send('fight');
		};
		
		socket.onmessage = function (message) {
			if(message.data == 'WIN'){
				toastr["success"]("You've won the fight!");
			}else if(message.data == 'LOSS'){
				toastr["error"]("You've lost the fight!");
			}else if(message.data == 'WAITING'){
				toastr["info"]("You've started waiting the fight.");
			}else{
				self.zoo = JSON.parse(message.data);
			}
			//console.log('Message from socket ' + message.data);
		};
		

		var stop;
	    // Don't start update if it is already defined
	    if ( !angular.isDefined(stop) ) {
	    	stop = $interval(function() {
	    		self.zoo.money += self.zoo.moneyIncome / 10;
	    		if(self.zoo.waitingForFight){
	    			self.zoo.championTime += 0.1;
	    		}
	    	}, 100);
		}

	    $scope.stopUpdate = function() {
	        if (angular.isDefined(stop)) {
	        	$interval.cancel(stop);
	        	stop = undefined;
	        }
	    };

		$scope.$on('$destroy', function() {
			console.log('Home controller scope destory');
	        $scope.stopUpdate();
	    });
	};


});

idlezoo.controller('about', function() {
	//TODO can I delete this?
});