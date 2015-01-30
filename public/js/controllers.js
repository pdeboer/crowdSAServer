/**
 * Created by Mattia on 23.01.2015.
 */

myApp.controller('QuestionCtrl', function($scope, $http){

    $scope.getQuestion = function() {
        var el = document.getElementById("startBtn");
        el.className = "btn btn-info";
        el.innerText = "Looking for a really good question...";
        $('#startBtn').prop('disabled', true);
        $scope.question = ""
        $http.get("/waiting/getQuestion")
            .success(function(data) {
                //TODO: pause the game
                // From now on the turker has 1 minute time to accept the assignment or reject it
                if (window.confirm('A question is ready for you! Do you want to answer?'))
                {
                    // Accepted
                    //TODO: Extend the assignment
                    window.location = '/viewer/' + data;
                    $scope.question = "The question is loading! Please wait a moment.";
                }
                else
                {
                    // Rejected
                    el.className = "btn btn-success";
                    el.innerText = "Start!";
                    $('#startBtn').prop('disabled', false);
                    $scope.question = "Don't want to answer any other question? Make a quick break and get some fresh air.";
                    //Delete the assignment and allow other turker to answer this question
                    $http.post("/rejectAssignment", {assignmentId: data.split("/",2)[1]})
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