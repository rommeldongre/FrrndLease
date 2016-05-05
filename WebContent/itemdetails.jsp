<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
    
<html lang="en" ng-app="itemDetailsApp">
    
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
    
<!-- Google Apis Start -->
<meta name="google-signin-scope" content="profile email">
<meta name="google-signin-client_id" content="909447696017-ka0dc75ts261cua6d2ho5mvb7uuo9njc.apps.googleusercontent.com">
<script src="https://apis.google.com/js/platform.js" async defer></script>
<script src="https://maps.googleapis.com/maps/api/js?v=3.exp&signed_in=true&libraries=places"></script>
<!-- Google Api End -->
    
<!-- Angularjs api -->
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src="js/ui-bootstrap-tpls-1.3.2.min.js"></script>
<!-- Angularjs api ends -->

<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
<title>Item Details</title>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="js/jquery-1.11.3.min.js"></script>
<script type="text/javascript" src="js/jquery-1.11.3.min.js"></script>
    
<!-- Bootstrap -->
<link href="css/bootstrap.min.css" rel="stylesheet">
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

</head>
    
<body onload="start()">
	<div id="loader"></div>
	<div id="main">

		<!--header starts---------------------------------------->
        <div ng-controller="headerCtrl">
		  <div ng-include="'header.html'"></div>
        </div>
		<!--header ends----------------------------------------->

		<div class="container-fluid" id="midcontainer" ng-controller="itemDetailsCtrl">
            
            <!-- Error Message Display starts -->
			<div class="row" ng-if="errorCheck()">
				<div id="heading">
					<span>{{message}}</span>
				</div>
			</div>
            <!-- Error Message Display ends -->
            
            <!-- Item Details starts -->
			<div class="row" ng-if="itemDetailsCheck()">
				<div class="col-md-6" id="outertable">
                    
					<div class="row">
						<div class="col-md-12">
							<form>
								<div class="form-group" id="ifile_div">
									<label for="item_image" id="image_label">Add Picture</label> <input type="file" id="ifile">
								</div>
							</form>
						</div>
					</div>
					<br />
                    
					<div class="row">
						<div class="col-md-12">
							<canvas id="panel"></canvas>
						</div>
					</div>
                    
					<form id="itemform">
                        
						<div class="row">
							<div class="col-md-12">
								<div class="form-group">
									<label for="title">Title</label>
                                    <input type="text" class="form-control" id="title" placeholder="Enter Title" required>
								</div>
							</div>
						</div>
                        
						<div class="row">
							<div class="col-md-6">
								<div class="input-group">
									<div class="input-group-button">
										<label for="category">Category</label><br />
										<button id="dropdownbuttoncategory" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false" required> Category <span class="caret"></span>
										</button>
										<ul id="dropdownmenucategory" class="dropdown-menu" role="menu"></ul>
									</div>
								</div>
							</div>
							<div class="col-md-6">
								<div class="form-group">
									<label for="location">Location</label>
                                    <input type="text" class="form-control" id="location" placeholder="Location">
								</div>
							</div>
						</div>
                        
						<div class="row">
							<div class="col-md-4">
								<div class="form-group">
									<label for="lease_value">Lease Value</label>
                                    <input type="number" class="form-control" id="lease_value" placeholder="Lease Value">
								</div>
							</div>
							<div class="col-md-4">
								<div class="input-group">
									<div class="input-group-button">
										<label for="lease_term">Lease Term</label><br />
										<button id="dropdownbuttonlease_term" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false"> Lease Term <span class="caret"></span>
										</button>
										<ul id="dropdownmenulease_term" class="dropdown-menu" role="menu"></ul>
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
								<div class="form-group">
									<label for="description">Description</label>
									<textarea rows="3" class="form-control" id="description" placeholder="Add Description"></textarea>
								</div>
							</div>
						</div>
                        
						<button class="btn btn-default" id="submit" type="submit">Submit</button>

					</form>
                    
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
                    
					<div id="lease_item_row" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="lease_item" class="btn btn-primary" onclick="lease_item()">Lease Item</button>
						</div>
					</div>

					<div id="request_item_row" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="request_item" class="btn btn-primary" onclick="request_item()">Request Item</button>
						</div>
					</div>

					<div id="add_item_row" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="add_item" class="btn btn-primary" onclick="add_item()">Add Item</button>
						</div>
					</div>

					<div id="edit_item_row" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="edit_item" class="btn btn-primary" onclick="edit_item()">Edit Item</button>
						</div>
					</div>

					<div id="delete_item_row" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="delete_item" class="btn btn-primary" onclick="delete_item()">Delete Item</button>
						</div>
					</div>

					<div id="cancel_row" class="row">
						<div class="col-md-12 col-sm-12 col-xs-12">
							<button id="cancel" class="btn btn-primary" onclick="cancel()">Cancel</button>
						</div>
					</div>
				</div>

			</div>
            <!-- Item Details ends -->


			<!-- Button trigger Alert Modal -->
			<button id="alertModalTriggerButton" type="button"
				class="btn btn-default btn-lg" data-toggle="modal"
				data-target="#myAlertModal" data-backdrop="static" data-keyboard="false">Launch demo modal</button>

			<!-- Alert Modal -->
			<div class="modal fade" id="myAlertModal" tabindex="-1" role="dialog"
				aria-labelledby="myModalLabel">
				<div class="modal-dialog" role="document">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
								aria-label="Close">
								<span aria-hidden="true">&times;</span>
							</button>
							<!--<h4 class="modal-title" id="myModalLabel">Modal title</h4>-->
						</div>
						<div class="modal-body" id="alertModalMsg"></div>
						<div class="modal-footer">
							<button type="button" class="btn btn-default"
								data-dismiss="modal" onclick="continueWork()">Yes</button>
							<button type="button" class="btn btn-default"
								data-dismiss="modal">No</button>
						</div>
					</div>
				</div>
			</div>

			<!-- Button trigger Confirmation Modal -->
			<button id="modalTriggerButton" type="button"
				class="btn btn-default btn-lg" data-toggle="modal"
				data-target="#myModal" data-backdrop="static" data-keyboard="false">Launch demo modal</button>

			<!-- Confirmation Modal -->
			<div class="modal fade" id="myModal" tabindex="-1" role="dialog"
				aria-labelledby="myModalLabel">
				<div class="modal-dialog" role="document">
					<div class="modal-content">
						<div class="modal-header">
							<button type="button" class="close" data-dismiss="modal"
								aria-label="Close">
								<span aria-hidden="true">&times;</span>
							</button>
							<h4 class="modal-title" id="myModalLabel"></h4>
						</div>
						<div class="modal-body" id="modalMsg"></div>
						<div class="modal-footer">
							<button type="button" class="btn btn-default"
								data-dismiss="modal" onclick="redirectToPrevPage()">Close</button>
						</div>
					</div>
				</div>
			</div>

            <!--The tawk.to widget code will get populated here from file chatbox.html.-->
            <div id="tawk_widget"></div>

			<!--Footer starts here-->
			<div ng-include="'footer.html'"></div>
            <!--Footer ends here-->

		</div>
	</div>

	<script type="text/javascript">
		var userloggedin, userLocation, reasonForGetItem, itemObj, reqObj, itemNo, leaseObj, picOrientation = null;
        
        var code = "${code}";
        var message = "${message}";

		function start() {
			
			$("#modalTriggerButton").hide();
			$("#alertModalTriggerButton").hide();

			load_Gapi();

			$('#error_row').hide();
			$('#submit').hide();

			getLocationWidth();
			getLeaseValueWidth();
			loadCategoryDropdown();
			loadLeaseTermDropdown();

			getLocation(); //to show default location of the user

			//checking if user is logged in or not
			userloggedin = localStorage.getItem("userloggedin");
			//userLocation = localStorage.getItem("userLocation"); 

			if(userloggedin == "${userId}") {
				getItemInfo();

				$("#description").css("margin-bottom", "35%");
				$("#lease_item").css("display", "none");
				$("#request_item_row").css("display", "none");
				$("#add_item_row").css("display", "none");
				$("#edit_item").css("margin-bottom", "5px");
				$("#delete_item").css("margin-bottom", "5px");
				$("#cancel").css("margin-bottom", "35%");
			} else{
				getItemInfo();
				makeFormReadOnly();
				
				$("#ifile_div").hide();
				$("#description").css("margin-bottom", "35%");
				$("#lease_item_row").css("display", "none");
				$("#request_item").css("margin-bottom", "5px");
				$("#cancel").css("margin-bottom", "35%");
				$("#add_item_row").css("display", "none");
				$("#edit_item_row").css("display", "none");
				$("#delete_item_row").css("display", "none");
			}

			document.getElementById("ifile").onchange = function(event) {

				EXIF.getData(event.target.files[0], function() {
					exif = EXIF.getAllTags(this);

					picOrientation = exif.Orientation;
				});

				this.imageFile = event.target.files[0];

				var reader = new FileReader();
				reader.onload = function(event) {
					var img = new Image();
					//img.style.width = "300px";
					//img.style.height = "300px";

					img.onload = function() {
						drawImage(img);
					}
					img.src = event.target.result;

				}
				reader.readAsDataURL(this.imageFile);
			}

		}

		function drawImage(img) {
			//beginning of image display
			canvasCtx = document.getElementById("panel").getContext("2d");

			this.canvasCtx.canvas.width = 300;//img.width;
			this.canvasCtx.canvas.height = 300;//img.height;

			if (img.width > img.height) { //Landscape Image 
				canvasCtx.width = 300;
				canvasCtx.height = 300 / img.width * img.height;
			} else { //Portrait Image
				canvasCtx.width = 300 / img.height * img.width;
				canvasCtx.height = 300;
			}

			if (picOrientation == 2) {
				this.canvasCtx.transform(-1, 0, 0, 1, canvasCtx.width, 0);
			}
			if (picOrientation == 3) {
				this.canvasCtx.transform(-1, 0, 0, -1, canvasCtx.width,
						canvasCtx.height);
			}
			if (picOrientation == 4) {
				this.canvasCtx.transform(1, 0, 0, -1, 0, canvasCtx.height);
			}
			if (picOrientation == 5) {
				this.canvasCtx.transform(0, 1, 1, 0, 0, 0);
			}
			if (picOrientation == 6) {
				this.canvasCtx.transform(0, 1, -1, 0, canvasCtx.height, 0);
			}
			if (picOrientation == 7) {
				this.canvasCtx.transform(0, -1, -1, 0, canvasCtx.height,
						canvasCtx.width);
			}
			if (picOrientation == 8) {
				this.canvasCtx.transform(0, -1, 1, 0, 0, canvasCtx.width);
			}

			this.canvasCtx.drawImage(img, 0, 0, canvasCtx.width,
					canvasCtx.height);
			url = canvasCtx.canvas.toDataURL();
		}
		//end of image display

		function load_Gapi() { //for google
			gapi.load('auth2', function() {
				gapi.auth2.init();
			});
		}

		$(window).resize(function() {
			getLocationWidth();
			getLeaseValueWidth();
		});

		function loadCategoryDropdown() { //for category dropdown
			catName = '';
			reasonForGetCategory = 'categoryDropdown';
			getNextCategory(catName);
		}

		function loadCategoryDropdownContinued(obj) {
			var ul = document.getElementById("dropdownmenucategory");

			var li = document.createElement("li");
			li.id = catName;
			li.className = "category";

			li.innerHTML = obj.catName;

			var lidivider = document.createElement("li");
			lidivider.className = "divider";

			ul.appendChild(lidivider);
			ul.appendChild(li);

			getNextCategory(catName);
		}

		function loadLeaseTermDropdown() { //for leaseterm dropdown
			leaseTermName = '';

			getNextLeaseTerm(leaseTermName);

		}

		function loadLeaseTermDropdownContinued(obj) {
			var ul = document.getElementById("dropdownmenulease_term");

			var li = document.createElement("li");
			li.id = leaseTermName;
			li.className = "leaseterm";

			li.innerHTML = obj.termName;

			var lidivider = document.createElement("li");
			lidivider.className = "divider";

			ul.appendChild(lidivider);
			ul.appendChild(li);

			getNextLeaseTerm(leaseTermName);
		}

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

		$(document).on("click", ".category", function(event) { //to see which option is selected from dropdown category
			var text = document.getElementById(event.target.id).innerHTML;
			document.getElementById("dropdownbuttoncategory").innerHTML = text;

		});

		$(document)
				.on(
						"click",
						".leaseterm",
						function(event) { //to see which option is selected from dropdown lease term
							var text = document.getElementById(event.target.id).innerHTML;
							document.getElementById("dropdownbuttonlease_term").innerHTML = text;
						});

		function add_item() { //called when Add Item is clicked then add the item to Items table and to Store table
			currentWork = 'addItem';
			$('#submit').click();
		}

		function request_item() {
			currentWork = 'requestItem';

			var div = document.getElementById("alertModalMsg");
			div.innerHTML = "Are you sure you want to request the Item?";

			$("#alertModalTriggerButton").click();

		}

		function lease_item() {
			currentWork = 'leaseItem';

			var div = document.getElementById("alertModalMsg");
			div.innerHTML = "Are you sure you want to lease the Item?";

			$("#alertModalTriggerButton").click();
		}

		function edit_item() { //editting the item-----------------------------------------------			
			currentWork = 'editItem';
			var div = document.getElementById("alertModalMsg");
			div.innerHTML = "Are you sure you want to save the edited values?";

			$("#alertModalTriggerButton").click();
		}

		function delete_item() { //deleting the item-----------------------------------------------
			currentWork = 'deleteItem';

			var div = document.getElementById("alertModalMsg");
			div.innerHTML = "Are you sure you want to delete this Item?";

			$("#alertModalTriggerButton").click();

		}

		function cancel() {
			redirectToPrevPage();
		}

		$("#itemform").submit(function(event) {
			event.preventDefault();

			if (currentWork == 'addItem') {
				itemSetValues();
				itemDbCreate();
			}
		});

		function continueWork() {
			if (currentWork == 'editItem') {
				editItemSetValues();
				editItemDbCreate();

			} else if (currentWork == 'deleteItem') {
				itemId = getItemToShow();
				userId = userloggedin;

				if (itemId == '')
					itemId = 0;
				if (userId == '')
					userId = null;

				deleteItemDbCreate();
			} else if (currentWork == 'requestItem') {
				itemId = itemUserId = null;

				if (userloggedin == "" || userloggedin == null || userloggedin == "anonymous")
					logInCheck(); //refer file logincheck.js 
				else
					requestItem();
			} else if (currentWork == 'leaseItem') {

				reqUserId = getRequestingUser();
				leaseItemSetValues(reqUserId);
			}

		}
		
		function requestItem(){
			var req = {
				itemId: "${itemId}",
				userId: userloggedin
			};

			reqItemSend(req);
		}

		function reqItemSend(req){
			
			$.ajax({
				url: '/flsv2/RequestItem',
				type:'get',
				data: {req: JSON.stringify(req)},
				contentType:"application/json",
				dataType: "json",
				
				success: function(response) {
					var heading = "Successful";
					
					var msg = response.Message;
					var objOwner = getItemOwner();
					
					confirmationIndex(heading, msg);
				},
				
				error: function() {
					var msg = "Not Working";
					confirmationIndex(msg);
				}
			});
		}

		function confirmationIndex(heading, msg) { //called from script_admin_v2.js
			var header = document.getElementById("myModalLabel");
			header.innerHTML = heading;
			var div = document.getElementById("modalMsg");
			div.innerHTML = msg;

			$("#modalTriggerButton").click();
		}

		function redirectToPrevPage() {
			prevPage = getPrevPage("prevPage");
			window.location.replace(prevPage);
		}

		//getting item info when user wants to view an existing item--------------------------------------------------------------------		
		function getItemInfo() {

			storeItemOwner("${userId}");

			$("#title").val("${title}");

			$("#dropdownbuttoncategory").text("${category}");

			$("#description").val("${description}");

			$("#lease_value").val("${leaseValue}");

			$("#dropdownbuttonlease_term").text("${leaseTerm}");

			//display the picture of the item begins here

			url = "${image}";
			var img = new Image();

			img.src = url;
			if (img.src != null && img.src != "null")
				drawImage(img);
			//display the picture of the item ends here	
		}

		//to get current location of the user and show it in the location by default
		function getLocation() {
			if (navigator.geolocation) {
				navigator.geolocation.getCurrentPosition(showPosition);
			} else {
				console.log("Geolocation is not supported by this browser.");
			}
		}

		function showPosition(position) {
			console.log("Latitude: " + position.coords.latitude
					+ "<br>Longitude: " + position.coords.longitude);

			latitude = position.coords.latitude;
			longitude = position.coords.longitude;
			coords = new google.maps.LatLng(latitude, longitude);

			var geocoder = new google.maps.Geocoder();
			var latLng = new google.maps.LatLng(latitude, longitude);
			geocoder
					.geocode(
							{
								'latLng' : latLng
							},
							function(results, status) {
								if (status == google.maps.GeocoderStatus.OK) {
									console.log(results[4].formatted_address);
									$("#location").val(
											results[4].formatted_address);
								} else {
									console
											.log("Geocode was unsucessfull in detecting your current location");
								}
							});
		}

		function makeFormReadOnly() {
			$("#title").attr("disabled", true);
			$("#dropdownbuttoncategory").attr("disabled", true);
			$("#location").attr("disabled", true);
			$("#description").attr("disabled", true);
			$("#lease_value").attr("disabled", true);
			$("#dropdownbuttonlease_term").attr("disabled", true);
			$("#quantity").attr("disabled", true);
		}
	</script>


	<!-- Include all compiled plugins (below), or include individual files as needed -->
	<script src="js/mystore.js"></script>
    <script src="js/itemdetails.js"></script>
    <script src="js/tawk.js"></script>
	<script src="js/script_admin_v2.js"></script>
	<script src="js/bootstrap.min.js"></script>

</body>
</html>