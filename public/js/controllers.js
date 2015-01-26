/**
 * Created by Mattia on 23.01.2015.
 */

myApp.controller('QuestionCtrl', function($scope, $http){

    $scope.getQuestion = function() {
        var el = document.getElementById("startBtn");
        el.className = "btn btn-warning";
        el.innerText = "Waiting...";
        $('startBtn').prop('disabled', true);
        $http.get("/waiting/getQuestion")
            .success(function(data) {
                //TODO: pause the game
                if (window.confirm('A question is ready for you! Do you want to answer?'))
                {
                    // Accepted
                    //TODO: Extend the assignment
                    window.location = '/viewer/' + data;
                    $scope.question = data;
                }
                else
                {
                    // Rejected
                    el.className = "btn btn-success";
                    el.innerText = "Start!";
                    $('startBtn').prop('disabled', false);
                    $scope.question = "You refused to answer the question " + data;
                    //TODO: delete the assignment and allow other turker to answer this question
                }

            })
            .error(function() {
                $scope.question = "Error while getting the question!";
            });
    }
});

myApp.controller('jobController', function JobController($scope, $http) {
    $scope.jobs =[ ] ;
    $scope.input = "" ;

    $scope.doGetJobs = function ( ) {
        var httpRequest = $http ( {
            method : 'GET',
            url : "/jobs"
        } ).success ( function ( data, status ) {
            $scope.jobs = data ;
        } ).error ( function ( arg ) {
            alert ( "error " ) ;
        } ) ;
    } ;

    $scope.doSearch = function ( ) {
        var httpRequest = $http ( {
            method : 'GET',
            url : "/search/" + $scope.input
        } ).success ( function ( data, status ) {
            $scope.jobs = data ;
        } ).error ( function ( arg ) {
            alert ( "error " ) ;
        } ) ;
    } ;

    $scope.showViewer = function(questionId) {
        $http.get("/waiting/getDefinedQuestion/"+questionId)
            .success(function(data) {
                //TODO: pause the game
                if (window.confirm('A question is ready for you! Do you want to answer?')) {
                    // Accepted
                    //TODO: Extend the assignment
                    window.location = '/viewer/' + data;
                }
                else {
                    // Rejected
                    //TODO: delete the assignment and allow other turker to answer this question
                    alert("Question refused");
                }
            })
            .error(function() {
                alert("Error");
            });
    };

    // run the search when the page loads.
    $scope.doGetJobs() ;
    $scope.doSearch() ;

    $scope.setLocation = function(url) {
        window.location.href = url;
    };

});