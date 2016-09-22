var myNotifications = angular.module('myApp');

myNotifications.controller('myNotificationsCtrl', ['$scope', 
													'userFactory',
													'modalService',
													'bannerService',
													'logoutService',
													'eventsCount',
													function($scope,
													userFactory,
													modalService,
													bannerService,
													logoutService,
													eventsCount){
    
    var offset = 0;
    var Limit = 5;
    
    var initialPopulate = function(){
        offset = 0;
        
        getNotifications();
        
        $scope.events = [];
        
        $scope.loadMore = true;
    }
    
    var getNotifications = function(){
        
        req = {
            userId: userFactory.user,
            limit: Limit,
            offset: offset
        }
        
        getNotificationsSend(req);
        
    }
    
    getNotificationsSend = function(req) {
		$.ajax({
			url: '/flsv2/GetNotifications',
			type: 'post',
			data: JSON.stringify(req),
			contentType:"application/json",
			dataType:"json",
			
			success: function(response) {
                if(response.code == 0){
                    $scope.$apply(function(){
                        $scope.events.push.apply($scope.events, response.resList);
                        if(response.resList.length < Limit)
                            $scope.loadMore = false;
                    });
                    offset = response.offset;
                    
                }else{
                    $scope.$apply(function(){
                        $scope.loadMore = false;
                    });
                }
			},
		
			error: function() {
			}
		});
	};
    
    $scope.loadmore = function(){
        getNotifications();
    }
    
    $scope.readEvent = function(index, s){
        $.ajax({
                url: '/flsv2/EventReadStatus',
                type: 'post',
                data: JSON.stringify({
                    eventId: $scope.events[index].eventId,
                    readStatus: s,
                    userId:userFactory.user,
                    accessToken: userFactory.userAccessToken
                }),
                contentType:"application/json",
                dataType:"json",

                success: function(response) {
                    if(response.code == 0){
                        eventsCount.updateEventsCount();
                        $scope.$apply(function(){
                            if($scope.events[index].readStatus == 'FLS_READ'){
                                $scope.events[index].readStatus = 'FLS_UNREAD';
                            }else{
                                $scope.events[index].readStatus = 'FLS_READ';
                            }
                        });
                    }
                },

                error: function() {
                }
            });
    }
	
	$scope.replyFriendMessage = function(index){
		modalService.showModal({}, {messaging: true, bodyText: 'Reply to this message', actionButtonText: 'Send'}).then(function(result){
            var message = result;
			var friend_name = $scope.events[index].fullName;
			var item_id=0;
            var friend_name = "";
            var item_id= $scope.events[index].itemId;
            var item_name = $scope.events[index].title;
            var item_uid = $scope.events[index].uid;
            if(message == "" || message == undefined)
                message = "";
            
			if(friend_name == "" || friend_name == undefined || friend_name=="-")
                friend_name = "";
			
			if($scope.events[index].fromUserId != '-')
				var friendId = $scope.events[index].fromUserId;
				
		   var req = {
                userId: userFactory.user,
                message: message,
				friendId: friendId,
				friendName: friend_name,
				itemId : item_id,
                title: item_name,
                uid: item_uid,
				accessToken: userFactory.userAccessToken
            }
           
			sendMessage(req);	
        }, function(){});
    }
	
	var sendMessage = function(req){
		
		$.ajax({
			url: '/flsv2/SendMessage',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code==0){
                    initialPopulate();
					bannerService.updatebannerMessage("Success, Message to Friend sent");
                    $("html, body").animate({ scrollTop: 0 }, "slow");
				}else{
					modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'OK'}).then(function(result){eventsCount.updateEventsCount();
						if(response.code == 400){
							logoutService.logout();
						}
					}, function(){});
				}
			},
		
			error: function() {
				console.log("Not able to send message");
			}
		});
	}
    
    initialPopulate();
    
}]);