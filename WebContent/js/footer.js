var footerApp = angular.module('footerApp', []);

footerApp.controller('footerCtrl', ['$scope', '$timeout', 'modalService', function($scope, $timeout, modalService){
	
	$scope.newsLetterLead = function(){
		var Lead_type="news_letter";
        var req = {
                lead_email: $scope.lead_email,
				lead_type: "news_letter"
            }
			console.log(req);
			sendLeadEmail(req);	
    }
          
    var sendLeadEmail = function(req){
		
		$.ajax({
			url: '/flsv2/AddLead',
			type: 'post',
			data: JSON.stringify(req),
			contentType: "application/x-www-form-urlencoded",
			dataType: "json",
			success: function(response) {
				if(response.code==225){
					modalService.showModal({}, {bodyText: response.message ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
					}, function(){});
				}else if(response.code==0){
					modalService.showModal({}, {bodyText: "You have succesfully signed up for updates" ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
					}, function(){});
				}else{
					modalService.showModal({}, {bodyText: "Error Occured. Please try later" ,showCancel: false,actionButtonText: 'Ok'}).then(function(result){
					}, function(){});
					
				}
			},
		
			error: function() {
				console.log("Not able to send message");
			}
		});
	}   
}]);