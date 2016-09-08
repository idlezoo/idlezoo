idlemage.controller('home', function($scope, $http) {
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
});