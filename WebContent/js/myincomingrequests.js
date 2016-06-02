var myInComingRequests = angular.module('myApp');

myInComingRequests.controller('myInComingRequestsCtrl', ['$scope', 'userFactory', 'modalService', function($scope, userFactory, modalService){
  
    var itemNextId = "0";
    
    // to get all the incoming requests
    var itemNextRequestId = "0";
    
    // to initialise the requests array
    $scope.requests = [];
    
    var initialPopulate = function(){
        
        itemNextRequestId = 0;
        
        getInRequests(itemNextRequestId);
        
    }
    
    var getInRequests = function(token){
        
        if(token == '' || token == undefined)
            token = 0;
        
        var req = {
            userId: userFactory.user,
            cookie: token
        }
        
        displayInRequests(req);
    }
    
    var displayInRequests = function(req){
        $.ajax({
            url: '/flsv2/GetRequestsPlus',
            type:'POST',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "JSON",
            success: function(response) {
                if(response.title){
                    itemNextRequestId = response.requestId;
                    itemNextId = response.requestItemId;
                    $scope.$apply(function(){
                        $scope.requests.unshift(response);
                    });
                    
                    getInRequests(itemNextRequestId);
                }
            },
            error: function() {
            }
        });
    }
    
    // populate the requests list initally
    initialPopulate();
    
    $scope.grantLease = function(itemId, reqUserId){
        
        modalService.showModal({}, {bodyText: "Are you sure you want to lease the Item?",actionButtonText: 'YES'}).then(
            function(result){
                var req = {
                    reqUserId: reqUserId,
                    itemId: itemId,
                    userId: userFactory.user
                }

                grantLeaseSend(req);
            },function(){});
    }
    
    var grantLeaseSend = function(req){
        $.ajax({
            url: '/flsv2/GrantLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'OK'}).then(
                    function(result){
                        $scope.requests = [];
                        initialPopulate();
                    },function(){});	
            },
            error: function() {
            }
        });
    }
    
    $scope.rejectLease = function(itemId, reqUserId){
        
        modalService.showModal({}, {bodyText: "Are you sure you want to reject the Request?",actionButtonText: 'YES'}).then(
            function(result){
                if(itemId === '')
                    itemId = null;

                if(reqUserId === '')
                    reqUserId = null;

                var req = {
                    itemId: itemId+"",
                    userId: reqUserId,
                };

                rejectLeaseSend(req);
            },function(){});
        
        
    }
    
    var rejectLeaseSend = function(req){
        $.ajax({
            url: '/flsv2/RejectRequest',
            type:'get',
            data: {req: JSON.stringify(req)},
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
                modalService.showModal({}, {bodyText: response.Message, showCancel:false, actionButtonText: 'OK'}).then(
                    function(result){
                        $scope.requests = [];
                        initialPopulate();
                    },function(){});
            },
            error: function() {
            }
        });
    }
    
    $scope.showItemDetails = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
    
}]);