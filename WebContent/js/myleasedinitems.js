var myLeasedInItemsApp = angular.module('myApp');

myLeasedInItemsApp.controller('myLeasedInItemsCtrl', ['$scope', 
													    'userFactory', 
														'bannerService', 
														'modalService',
                                                        'logoutService',
														function($scope, 
														userFactory, 
														bannerService, 
														modalService,
                                                        logoutService){

    localStorage.setItem("prevPage","myapp.html#/myleasedinitems");
    
    var leaseStatusDesc = {'LeaseReady':'This item will be picked up by us from the owner soon.','PickedUpOut':'This item has been picked up and will be delivered to the requestor.', 'LeaseStarted':'This item has been delivered to the requestor.','LeaseEnded':'This item will be picked up by us from the requestor soon.','PickedUpIn':'This item has been picked and will be delivered to the owner.'};

    $scope.leases = [];
    
    var initialPopulate = function(){
        getLeasedInItems(0);
    }
    
    var getLeasedInItems = function(cookie){
        
        req = {
            cookie: cookie,
            leaseUserId: "",
            leaseReqUserId: userFactory.user,
            status: 'Active'
        }
        
        getLeasedInItemsSend(req);
        
    }
    
    var getLeasedInItemsSend = function(req){
        $.ajax({
			url: '/GetLeasesByX',
			type: 'post',
			data: JSON.stringify(req),
			contentType:"application/json",
			dataType:"json",
			
			success: function(response){
                if(response.code == 0){
                    response.statusDesc = leaseStatusDesc[response.status];
                    $scope.$apply(function(){
                        $scope.leases.unshift(response);
                    });
                    getLeasedInItems(response.cookie);
                }
            },
		
			error: function() {}
		});
    }
    
    initialPopulate();
    
    $scope.closeLease = function(ItemId, OwnerUserId, RequestorUserId, index){
        if($scope.leases[index].status == 'LeaseStarted' || $scope.leases[index].status == 'LeaseReady'){
            modalService.showModal({}, {bodyText: "Are you sure you want to close the lease on the Item?",actionButtonText: 'Yes'}).then(
                function(result){
                    req = {
                        itemId: ItemId,
                        userId: OwnerUserId,
                        reqUserId: RequestorUserId,
						userLoggedIn: userFactory.user,
                        flag: "close",
                        accessToken: userFactory.userAccessToken
                    };
                    closeLeaseSend(req, index);
                },function(){});
        }else{
            modalService.showModal({}, {bodyText: "Can only be closed when lease status is LeaseStarted or LeaseReady", showCancel:false, actionButtonText: 'Ok'}).then(
                function(result){
                },function(){});
        }
    }
    
    var closeLeaseSend = function(req, index){
        $.ajax({
            url: '/RenewLease',
            type:'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType: "json",
            success: function(response) {
				if(response.code==0){
					bannerService.updatebannerMessage(response.message,"");
					$("html, body").animate({ scrollTop: 0 }, "slow");
					$scope.leases = [];
                    initialPopulate();
				}else{
					modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'Ok'}).then(
						function(result){
                            if(response.code == 400)
                                logoutService.logout();
                            else{
                                $scope.leases = [];
                                initialPopulate();
                            }
					},function(){});
				}  
            },
            error: function() {
            }
        });
    }
    
    $scope.showItemDetails = function(uid){
        window.location.replace("ItemDetails?uid="+uid);
    }
    
    $scope.changeDeliveryPlan = function(plan, i){
        
        var p = "Are you sure you want to pick up the item?";
        
        if(plan=='FLS_OPS')
            p = "Are you sure you want frrndlease to pick up the item?";
        
        modalService.showModal({}, {bodyText: p, actionButtonText: 'Ok'}).then(function(result)  {
            req = {
                deliveryPlan: plan,
                leaseId: $scope.leases[i].leaseId
            }

            $.ajax({
                url: '/ChangeDeliveryPlan',
                type: 'post',
                data: JSON.stringify(req),
                contentType:"application/json",
                dataType:"json",

                success: function(response) {
                    if(response.code == 0){
                        window.location.reload();
                    }else{
                        modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'Ok'}).then(function(result){
                            window.location.reload();
                        },function(){});
                    }
                },

                error: function() {}
            });
            
        },function(){});
        
    }
    
    $scope.changePickupStatus = function(s, i){
        
        req = {
            owner: false,
            leaseId: $scope.leases[i].leaseId,
            pickupStatus: s
        }
        
        $.ajax({
            url: '/ChangePickupStatus',
            type: 'post',
            data: JSON.stringify(req),
            contentType:"application/json",
            dataType:"json",
        
            success: function(response){
                if(response.code != 0){
                    modalService.showModal({}, {bodyText: response.message, showCancel:false, actionButtonText: 'Ok'}).then(function(result){
                        if(s == true)
                            $scope.leases[i].leaseePickupStatus = false;
                        else
                            $scope.leases[i].leaseePickupStatus = true;
                    },function(){});
                }
            },
            error: function() {}
        });
    }
    
    $("#agreement").submit(function(e) {
        e.preventDefault();
    });
    
}]);
