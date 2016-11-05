idlezoo.config(function($stateProvider, $urlRouterProvider) {

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
		.state('topincome', {
			url: "/topincome",
			templateUrl: "js/top/income.html",
			controller: 'topincome as controller'
		})
		.state('topwins', {
			url: "/topwins",
			templateUrl: "js/top/wins.html",
			controller: 'topwins as controller'
		})
		.state('toptime', {
			url: "/toptime",
			templateUrl: "js/top/time.html",
			controller: 'toptime as controller'
		})

	$urlRouterProvider.otherwise('/');

});