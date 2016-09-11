idlemage.controller('home', function($scope, $interval, $http) {
	var self = this;
	$http.get('/game/me').then(function(response) {
		self.mage = response.data;
	})
	
	
	$scope.buy = function($buildingName) {
		$http.get('/game/buy?building=' + $buildingName).then(function(response){
			self.mage = response.data;
		})
	};
	
	$scope.upgrade = function($buildingName) {
		$http.get('/game/upgrade?building=' + $buildingName).then(function(response){
			self.mage = response.data;
		})
	};
	
	$scope.fight = function(){
		$http.get('/game/fight').then(function(response){
			self.mage = response.data;
		})
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