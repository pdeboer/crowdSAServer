/**
 * Created by Mattia on 23.01.2015.
 */

myApp.controller('QuestionCtrl', function($scope, $http){
    $scope.questions = [];

    $scope.getQuestions = function(paper_id){
        $http.get("/questions/"+paper_id)
            .success(function(data){
                $scope.questions = data;
            })
            .error(function(data) {
               console.error("Error: Cannot get the questions related to paper " + paper_id);
            });
    };

    $scope.getQuestion = function() {
        var el = document.getElementById("startBtn");
        el.className = "btn btn-info";
        el.innerText = "Looking for a question...";
        $('#startBtn').prop('disabled', true);
        $scope.question = "";
        $http.get("/waiting/getQuestion")
            .success(function(data) {
                //TODO: pause the game
                // From now on the turker has 1 minute time to accept the assignment or reject it
                if (window.confirm('A question is ready for you! Do you want to answer it?'))
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
                    $scope.question = "You rejected the question";
                    //Delete the assignment and allow other turker to answer this question
                    $http.post("/rejectAssignment", {assignmentId: data.split("/",2)[1]})
                }

            })
            .error(function() {
                $scope.question = "Error while getting the question!";
            });
    };

    $scope.showViewer = function(question_id){
        $http.get('/waiting/getDefinedQuestion/'+ question_id)
            .success(function(data){
                window.location.href = '/viewer/' + data;
            })
            .error(function(data){
               console.error("Error: " + data);
            });
    }
});

myApp.controller('PapersCtrl', function ($scope, $http) {
    $scope.papers =[ ] ;

    $scope.getPapers = function ( ) {
        $http.get("/papers")
            .success ( function ( data, status ) {
                $scope.papers = data ;
            } )
            .error ( function ( arg ) {
                alert ( "error " ) ;
            } ) ;
    } ;

    //Init papers
    $scope.getPapers() ;

    $scope.second_step = function(paper_id){
        window.location.href = "/waiting/secondStep/"+paper_id;
    };

    $scope.setLocation = function(url) {
        window.location.href = url;
    };

});

myApp.controller('ViewerCtrl', function($scope, $http, $timeout){

    $scope.cancel_assignment = function(assignment_id){
        window.location.href = '/viewer/cancel/'+assignment_id;
    };

    $scope.possible_answers = [];
    $scope.set_possible_answers = function(possibilities){
        var possibilities = possibilities.split("$$");
        $scope.possible_answers = possibilities;
    };

    $scope.dom_children = [];
    $scope.getDsToRefine = function(possibilities){
        var dss = possibilities.split("$$");
        if(dss==""){
            $scope.dom_children = [];
        } else {
            $scope.dom_children = dss;
        }
    };

    $scope.removeElementDS = function(ds) {
        $scope.dom_children.splice($scope.dom_children.indexOf(ds), 1);
        disableBorders(ds[1]);
    };

    $scope.counter_sec = 600;

    $scope.countdown = function(expiration_sec) {
        $scope.counter_sec = expiration_sec;
        $scope.start_countdown();
    };

    $scope.start_countdown = function() {
        $timeout(function () {
            $scope.counter_sec -= 1;
            if ($scope.counter_sec > 0) {
                $scope.start_countdown();
            } else {
                alert("The time is over!");
                $('#cancelAnswer').trigger('click');
            }
        }, 1000);
    };

});