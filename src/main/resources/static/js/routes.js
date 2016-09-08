idlemage.config(function($stateProvider, $urlRouterProvider) {

	$stateProvider
		.state('app', {
			url: "/",
			templateUrl: "js/home.html",
			controller: 'home as controller'
		})
		.state('login', {
			url: "/login",
			templateUrl: "js/auth/login.html",
			controller: 'navigation as controller'
		})
		.state('signup', {
			url: "/signup",
			templateUrl: "js/auth/signup.html",
			controller: 'navigation as controller'
		})


	$urlRouterProvider.otherwise('/');

});