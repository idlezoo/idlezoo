idlemage.controller('top', function($rootScope, $http, $state) {
	var self = this;
	$http.get('/top/income').then(function(response) {
		self.top = response.data;
	});
});