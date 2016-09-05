idlemage.config(function($routeProvider) {

	$routeProvider.when('/', {
		templateUrl : 'js/home.html',
		controller : 'home',
		controllerAs: 'controller'
	}).when('/login', {
		templateUrl : 'js/auth/login.html',
		controller : 'navigation',
		controllerAs: 'controller'
	}).when('/signup', {
		templateUrl : 'js/auth/signup.html',
		controller : 'navigation',
		controllerAs: 'controller'
	}).otherwise('/');

});