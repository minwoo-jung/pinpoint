<!--
	Copied directly from this URL.
	http://www.journaldev.com/2573/spring-mvc-file-upload-example-tutorial-single-and-multiple-files 
-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
<title>Upload File Request Page</title>
</head>
<body>
 
    <form method="POST" action="uploadFile.pinpoint" enctype="multipart/form-data">
        File to upload: <input type="file" name="file"><br /> 
        Name: <input type="text" name="name"><br /> <br /> 
        <input type="submit" value="Upload"> Press here to upload the file!
    </form>
     
</body>
</html>
