var myProfile = angular.module('myApp');

myProfile.controller('myProfileCtrl', ['$scope', 'userFactory', 'profileFactory', 'modalService', function($scope, userFactory, profileFactory, modalService){
    
    localStorage.setItem("prevPage","myapp.html#/myprofile");
    
    var Address = '', Sublocality = '', Locality = '', Lat = 0.0, Lng = 0.0, image_url='',picOrientation=null;
    
    $scope.options = {
        country: 'in',
        sendToCarousel: false
    };
    
    $scope.details = '';
    
    if(userFactory.user == "" || userFactory.user == null || userFactory.user == "anonymous")
        window.location.replace("myapp.html");
    
    var unsaved = false; 
	
	//beginning of image display
	var canvasCtx = document.getElementById("panel").getContext("2d");
	
	$('#ifile').change(function(event) {
        EXIF.getData(event.target.files[0], function() {
		exif = EXIF.getAllTags(this);
		
		picOrientation = exif.Orientation;
		});
		
		this.imageFile = event.target.files[0];
		
		var reader = new FileReader();
		reader.onload =  function(event) {
			var img = new Image();
			img.onload = function() {
				drawImage(img);
			}
			img.src = event.target.result;
			
		}
		reader.readAsDataURL(this.imageFile);
    });
	
	var drawImage = function(img) {
		canvasCtx.width = 300;
		canvasCtx.height = 300;
		
		if(img.width>img.height){                      							//Landscape Image 
			canvasCtx.width = 300;
			canvasCtx.height = 300 / img.width * img.height;
		} else {                                                                  //Portrait Image
			canvasCtx.width = 300 / img.height * img.width;
			canvasCtx.height = 300;
		} 
		
		console.log("width: "+canvasCtx.width+"height: "+canvasCtx.height);
		
		if (picOrientation==2){
			canvasCtx.transform(-1, 0, 0, 1,canvasCtx.width, 0);
		}
		if (picOrientation==3){
			canvasCtx.transform(-1, 0, 0, -1,canvasCtx.width, canvasCtx.height);
		}
		if (picOrientation==4){
			canvasCtx.transform(1, 0, 0, -1, 0, canvasCtx.height );
		}
		if (picOrientation==5){
			canvasCtx.transform(0, 1, 1, 0, 0, 0);
		}
		if (picOrientation==6){
			canvasCtx.transform(0, 1, -1, 0, canvasCtx.height , 0);
		}
		if (picOrientation==7){
			canvasCtx.transform(0, -1, -1, 0, canvasCtx.height , canvasCtx.width);
		}
		if (picOrientation==8){
			canvasCtx.transform(0, -1, 1, 0, 0, canvasCtx.width);
		}
		
		canvasCtx.drawImage(img,0,0,canvasCtx.width, canvasCtx.height);
		image_url = canvasCtx.canvas.toDataURL();
	}			
	//end of image display
    
    var displayProfile = function(){
        profileFactory.getProfile(userFactory.user).then(
        function(response){
            if (response.data.code == 0) {
                $scope.userId = userFactory.user;
                $scope.fullname = response.data.fullName;
				$scope.mobile = response.data.mobile;
				$scope.location = response.data.address;
				$scope.credit = response.data.credit;
				$scope.referralCode = response.data.referralCode;
				$scope.label = response.data.photoIdVerified;
				url = response.data.photoId;
				var img = new Image();
				img.src = url;
				if(img.src != null && img.src != "null")
					drawImage(img);
            } else {
                $scope.userId = "";
                $scope.fullname = "";
				$scope.mobile = "";
				$scope.location = "";
				$scope.credit = "";
				$scope.referralCode = "";
            }
        },
        function(error){
            console.log("unable to get profile: " + error.message);
        });
    }
    
    // getting the profile
    displayProfile();
    
    $scope.updateProfile = function(){
        
        if($scope.location != '')
            getLocationData($scope.location);
        else
            editProfileData();
        
        unsaved = false;
    }
    
    $scope.cancel = function(){
        window.location.replace("myapp.html#/");
    }
    
    var getLocationData = function(location){
        $.ajax({
            url: 'https://maps.googleapis.com/maps/api/geocode/json',
            type: 'get',
            data: 'address='+location+"&key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g",
            success: function(response){
                if(response.status == 'OK'){
                    Address = response.results[0].formatted_address;
                    $scope.$apply(function(){
                        $scope.location = Address;
                    });
                    response.results[0].address_components.forEach(function(component){
                        if(component.types.indexOf("sublocality_level_1") != -1)
                            Sublocality = component.long_name;
                        if(component.types.indexOf("locality") != -1)
                            Locality = component.long_name;
                    });
                    Lat = response.results[0].geometry.location.lat;
                    Lng = response.results[0].geometry.location.lng;
                    
                }
                editProfileData();
            },
            error: function(){
                console.log("not able to get location data");
            }
        });
    }
    
    editProfileData = function(){
        var req = {
			userId : userFactory.user,
			fullName : $scope.fullname,
			mobile : $scope.mobile,
			location : $scope.location,
            address: Address,
            locality: Locality,
            sublocality: Sublocality,
            lat: Lat,
            lng: Lng,
			photoId: image_url
		}
		console.log(req);
		editProfile(req);
    }
    
    var editProfile = function(req){
        profileFactory.updateProfile(req).then(
        function(response){
            if (response.data.code == 0) {
                dialogText = 'Your Profile Has Been Updated!!';
            }else{
                dialogText = 'please try after sometime';
            }
            modalService.showModal({}, {bodyText:dialogText,showCancel: false,actionButtonText: 'OK'}).then(function(result){
				window.location.reload();
			}, function(){});
        },
        function(error){
            console.log("unable to edit profile: " + error.message);
        });
    }
    
    $scope.isChanged = function(){
        unsaved = true;
    }
    
    window.onbeforeunload = function(){
        if (unsaved) {
			return "You have unsaved changes on this page.";
        }
    }
    
}]);