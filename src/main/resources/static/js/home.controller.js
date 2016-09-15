idlemage.controller('home', function($scope, $interval, $http) {
	
	var socket = new SockJS("/game/ws");
	
	socket.onopen = function () {
		// Socket open.. start the game loop.
		console.log('Info: WebSocket connection opened.');
		socket.send('me');
		setInterval(function() {
			// Prevent server read timeout.
			socket.send('ping');
		}, 5000);
	};
	
	
	var self = this;
//	$http.get('/game/me').then(function(response) {
//		self.mage = response.data;
//	})
	
	
	$scope.buy = function($buildingName) {
		socket.send('buy/' + $buildingName);
	};
	
	$scope.upgrade = function($buildingName) {
		socket.send('upgrade/' + $buildingName);
	};
	
	$scope.fight = function(){
		socket.send('fight');
	};
	
	socket.onmessage = function (message) {
		// _Potential_ security hole, consider using json lib to parse data in production.
		console.log('Message from socket ' + message.data);
		self.mage = JSON.parse(message.data);
	};
	

	var stop;
    // Don't start update if it is already defined
    if ( !angular.isDefined(stop) ) {
    	stop = $interval(function() {
    		self.mage.mana += self.mage.manaIncome / 10;
    		if(self.mage.waitingForFight){
    			self.mage.championTime += 0.1;
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
        $scope.stopUpdate();
    });

});