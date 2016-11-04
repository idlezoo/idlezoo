idlemage.controller('topincome', function($rootScope, $http, $state) {
	var self = this;
	$http.get('/top/income').then(function(response) {
		self.top = response.data;
	});
});

idlemage.controller('toptime', function($rootScope, $http, $state) {
	var self = this;
	$http.get('/top/championTime').then(function(response) {
		self.top = response.data;
	});
});

idlemage.controller('topwins', function($rootScope, $http, $state) {
	var self = this;
	$http.get('/top/wins').then(function(response) {
		self.top = response.data;
	});
});