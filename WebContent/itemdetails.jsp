<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
    
<html lang="en" ng-app="itemDetailsApp">
    
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!--Meta tags For Facebook start here-->
        <meta property="og:title" content="New Item Posted on Frrndlease"/>
        <meta property="og:type" content="website" />
        <meta property="og:image" content="images/fls-logo.png" />
        <meta property="og:url" content="http://www.frrndlease.com/ItemDetails?uid=${uid}" />
        <meta property="og:description" content="${title}" />
    <!--Meta tags For Facebook end here-->


    <!-- Google Apis Start -->
    <meta name="google-signin-scope" content="profile email">
    <meta name="google-signin-client_id" content="909447696017-ka0dc75ts261cua6d2ho5mvb7uuo9njc.apps.googleusercontent.com">
    <script src="https://apis.google.com/js/platform.js"></script>
    <script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=places"></script>
    <!-- Google Api End -->

    <!-- Angularjs api -->
    <script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
    <script src="js/ui-bootstrap-tpls-1.3.2.min.js"></script>
    <script src="js/header.js"></script>
    <!-- Angularjs api ends -->

    <link rel="shortcut icon" href="images/fls-favicon.ico" type="image/x-icon">

    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
    <title>Item Details</title>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="js/jquery-1.11.3.min.js"></script>
    <script type="text/javascript" src="js/jquery-1.11.3.min.js"></script>

    <!-- Bootstrap -->
    <link href="css/bootstrap.min.css" rel="stylesheet">
        <link href="css/gsdk.css" rel="stylesheet" />
    <link href="css/mystore.css" rel="stylesheet">
    <link href='https://fonts.googleapis.com/css?family=Happy+Monkey' rel='stylesheet' type='text/css'>

    <!--Loading Animation code CSS & JS Links  -->
    <link href="css/animation.css" type="text/css" rel="stylesheet">
    <script src="js/jquery.backstretch.js"></script>
    <script src="js/animation.js"></script>
    <!--Loading Animation code CSS & JS Links ends here  -->

    <!--Check whether user is logged in or not  -->
    <script src="js/logincheck.js"></script>
    <!--Check whether user is logged in or not ends here  -->

    <!--Correct Orientation of image while uploading  -->
    <script src="js/exif.js"></script>
    <!--Correct Orientation of image while uploading ends here  -->
	
	<link href="css/font-awesome.min.css" type="text/css" rel="stylesheet">
    <script src="js/bootstrap-datepicker.js"></script>
	<script src="js/bootstrap-select.js"></script>
	<script src="js/get-shit-done.js"></script>
	<script src="js/gsdk-bootstrapswitch.js"></script>
	<script src="js/gsdk-checkbox.js"></script>
	<script src="js/gsdk-morphing.js"></script>
	<script src="js/gsdk-radio.js"></script>
	<script src="js/jquery.flexisel.js"></script>
	<script src="js/jquery.tagsinput.js"></script>
	<script src="js/jquery-ui.custom.min.js"></script>
	<script src="js/retina.min.js"></script>    
</head>
    
<body onload="start()">
	<div id="loader"></div>
	<div id="main">

		<!--header starts---------------------------------------->
        <div>
		  <div ng-include="'header.html'"></div>
        </div>
		<!--header ends----------------------------------------->

		<div class="container-fluid" id="midcontainer" ng-controller="itemDetailsCtrl">
            
            <!-- Error Message Display starts -->
			<div class="row" ng-if="showError">
				<div id="heading">
					<span>${message}</span>
				</div>
			</div>
            <!-- Error Message Display ends -->
            
            <!-- Item Details starts -->
			<div class="row" ng-if="!showError">
				<div class="col-md-6" id="outertable">
					<br />
					<div class="row">
                        <div class="col-md-12">
                            <input type="file" ng-if="userMatch" accept="image/*" onchange="angular.element(this).scope().uploadImage(files[0])" />
                            <img ng-src="{{(item.image === '' || item.image === null || item.image === 'null') ? 'images/imgplaceholder.png' : item.image}}" width="300" height="300"/>
                        </div>
					</div>
                    
					<div id="itemform" ng-cloak>
                        
						<div class="row">
							<div class="col-md-12">
								<div class="form-group" ng-init="item.title='${title}'">
									<label for="title">Title</label>
                                    <input type="text" class="form-control" ng-model="item.title" ng-disabled="!userMatch" placeholder="Enter Title" required>
								</div>
							</div>
						</div>
                        
						<div class="row">
							<div class="col-md-6">
								<div class="input-group">
									<div class="input-group-button" ng-init="item.category='${category}'">
										<label for="category">Category</label><br />
										<button id="dropdownbuttoncategory" ng-disabled="!userMatch" ng-bind="item.category" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false" required> Category <span class="caret"></span>
										</button>
										<ul id="dropdownmenucategory" class="dropdown-menu" role="menu">
                                            <span ng-repeat="c in categories" ng-click="categorySelected(c)">
                                                <li id="{{c}}" class="category">{{c}}</li>
                                                <li class="divider"></li>
                                            </span>
                                        </ul>
									</div>
								</div>
							</div>
							<div class="col-md-6">
								<div class="form-group">
									<label for="location">Location</label>
                                    <input type="text" class="form-control" id="location" value="${sublocality},${locality}" placeholder="Location" disabled>
								</div>
							</div>
						</div>
                        
						<div class="row">
							<div class="col-md-4">
								<div class="form-group" ng-init="item.leaseValue=${leaseValue}">
									<label for="lease_value">Lease Value</label>
                                    <input type="number" class="form-control" id="lease_value" ng-model="item.leaseValue" ng-disabled="!userMatch" placeholder="Lease Value">
								</div>
							</div>
							<div class="col-md-4">
								<div class="input-group">
									<div class="input-group-button" ng-init="item.leaseTerm='${leaseTerm}'">
										<label for="lease_term">Lease Term</label><br />
										<button id="dropdownbuttonlease_term" ng-disabled="!userMatch" ng-bind="item.leaseTerm" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false"> Lease Term <span class="caret"></span>
										</button>
										<ul id="dropdownmenulease_term" class="dropdown-menu" role="menu">
                                            <span ng-repeat="l in leaseTerms" ng-click="leaseTermSelected(l)">
                                                <li id="{{l}}" class="leaseterm">{{l}}</li>
                                                <li class="divider"></li>
                                            </span>
                                        </ul>
									</div>
								</div>
							</div>
							<div class="col-md-4">
								<div class="form-group">
									<label for="quantity">Quantity</label>
                                    <input type="number" class="form-control" id="quantity" placeholder="Quantity">
								</div>
							</div>
						</div>
                        
						<div class="row">
							<div class="col-md-12">
								<div class="form-group" ng-init="item.description='${description}'">
									<label for="description">Description</label>
									<textarea rows="3" class="form-control" ng-model="item.description" ng-disabled="!userMatch" style="margin-bottom:35%;" placeholder="Add Description"></textarea>
								</div>
							</div>
						</div>

					</div>
                    
				</div>
                
				<div class="col-md-6">
					<br />
					<div id="error_row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<div class="alert alert-danger fade in">
								<a href="#" class="close" data-dismiss="alert">&times;</a> <strong>Error!</strong>Operation failed.
							</div>
						</div>
					</div>
					<br />

					<div ng-if="!userMatch" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="request_item" style="margin-bottom:5px;" class="btn btn-primary" ng-click="requestItem()">Request Item</button>
						</div>
					</div>
					
					<div ng-if="!userMatch" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="wish_item" style="margin-bottom:5px;" class="btn btn-primary" ng-click="wishItem()">Add to Wishlist</button>
						</div>
					</div>

					<div ng-if="userMatch" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="edit_item" style="margin-bottom:5px;" class="btn btn-primary" ng-click="editItem()">Edit Item</button>
						</div>
					</div>

					<div ng-if="userMatch" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="delete_item" style="margin-bottom:5px;" class="btn btn-primary" ng-click="deleteItem()">Delete Item</button>
						</div>
					</div>
					
					<div  id="cancel_row" class="row">	
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="cancel" style="margin-bottom:35%;" class="btn btn-primary" onclick="cancel()">Cancel</button>
						</div>
					</div>
				</div>

			</div>
            <!-- Item Details ends -->

            <!--The tawk.to widget code will get populated here from file chatbox.html.-->
            <div id="tawk_widget"></div>

		</div>
		<!--Footer starts here-->
		<div ng-include="'footer.html'"></div>
        <!--Footer ends here-->
	</div>

	<script type="text/javascript">
		var reasonForGetItem, itemObj, reqObj, itemNo, leaseObj;
        
        var code = "${code}";
        var userId = "${userId}";
        var image = "${image}";
        
        var item_id = "${itemId}";

		function start() {

			$('#error_row').hide();

			getLocationWidth();
			getLeaseValueWidth();

		}

		$(window).resize(function() {
			getLocationWidth();
			getLeaseValueWidth();
		});

		function getLocationWidth() { //just for a symmetrical look, to set width of dropdown button and menu equal to Location input field
			var width = $("#location").width();
			$("#dropdownbuttoncategory").width(width);
			$("#dropdownmenucategory").width(width);
		}

		function getLeaseValueWidth() {
			var width = $("#lease_value").width();
			$("#dropdownbuttonlease_term").width(width);
			$("#dropdownmenulease_term").width(width);
		}
		
		function cancel(){
			window.history.back();
		}
			
	</script>


	<!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="js/itemdetails.js"></script>
    <script src="js/tawk.js"></script>
	<script src="js/bootstrap.min.js"></script>
    <!-- For Autocomplete Feature -->
    <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key=AIzaSyAmvX5_FU3TIzFpzPYtwA6yfzSFiFlD_5g&libraries=places"></script>
    <script src="js/ngAutocomplete.js"></script>
</body>
</html>