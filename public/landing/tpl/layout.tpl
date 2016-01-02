<!DOCTYPE html>
<html lang="en">
<head>  
  <meta charset="utf-8" />
  <title>Be Angular | Bootstrap Admin Web App with AngularJS</title>
  <meta name="description" content="Angularjs, Html5, Music, Landing, 4 in 1 ui kits package" />
  <meta name="keywords" content="AngularJS, angular, bootstrap, admin, dashboard, panel, app, charts, components,flat, responsive, layout, kit, ui, route, web, app, widgets" />
  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" />
  <link rel="stylesheet" href="../libs/assets/animate.css/animate.css" type="text/css" />
  <link rel="stylesheet" href="../libs/assets/font-awesome/css/font-awesome.min.css" type="text/css" />
  <link rel="stylesheet" href="../libs/assets/simple-line-icons/css/simple-line-icons.css" type="text/css" />
  <link rel="stylesheet" href="../libs/jquery/bootstrap/dist/css/bootstrap.css" type="text/css" />
{% if build %}
  <link rel="stylesheet" href="css/font.css" type="text/css" />
  <link rel="stylesheet" href="css/app.css" type="text/css" />
{% else %}
  <link rel="stylesheet" href="css/app.min.css" type="text/css" />
{%endif%}
</head>
<body>
	{% include 'header.tpl' %}
  <div id="content">
    {% block content %}
    {% endblock %}
  </div>
  {% include 'footer.tpl' %}
  <script src="../libs/jquery/jquery/dist/jquery.js"></script>
  <script src="../libs/jquery/bootstrap/dist/js/bootstrap.js"></script>
  <script src="../libs/jquery/jquery_appear/jquery.appear.js"></script>
  <script src="js/landing.js"></script>
</body>
</html>
