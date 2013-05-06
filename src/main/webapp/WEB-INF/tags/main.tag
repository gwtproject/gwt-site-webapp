<%@tag description="main navigation" pageEncoding="UTF-8"%>
<!DOCTYPE html>

<html>
<head>

<link href="css/main.css" rel="stylesheet" type="text/css" media="all">

</head>
<body>

	<div id="container">

		<header>

			<a href=""> <!-- TODO size --> <img src="images/gwt-logo.png"
				alt="GWT Logo" style="width: 30px; margin: 10px;">
			</a>




		</header>

		<div id="nav">
			<div>
				<ul>
					<li class="active"><a href="/">Home</a></li>
					<li class=""><a href="#">Download</a></li>
					<li class=""><a href="#">Get involved</a></li>
					<li class=""><a href="#">Getting started</a></li>
					<li class=""><a href="docs/">Documentation</a></li>
				</ul>
			</div>
		</div>



	</div>

	<div class="content">
		<jsp:doBody />
	</div>
	
	<div id="footer">
			<div>
				<ul>
					<li class="active"><a href="/privacy">Privacy Policy</a></li>
					<li class=""><a href="/termsofservice">Terms of Service</a></li>
				</ul>
			</div>
		</div>

</body>

</html>
