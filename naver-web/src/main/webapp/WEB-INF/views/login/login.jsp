<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">
    <title>Pinpoint : Login</title>
    <style>
        @charset 'utf-8';
        body,button,dd,dl,dt,fieldset,form,h1,input,p,{
            margin:0;
            padding:0
        }
        body,button,input{
            font-family:Helvetica,Dotum,sans-serif;
            font-size:12px;
            -webkit-text-size-adjust:none
        }
        button,input{
            border:0;
            border-radius:0;
            background:0 0;
            -webkit-appearance:none;
            -moz-appearance:none;
            appearance:none;
            cursor:pointer
        }
        fieldset,img{
            border:0
        }
        html{
            height:100%
        }
        body{
            position:relative;
            height:100%;
            background:#f5f6f7
        }
        #wrap{
            position:relative;
            min-height:100%
        }
        #container{
            padding-bottom:50px;
            min-height:185px
        }
        #container, #header{
            width:768px;
            margin:0 auto
        }
        #content{
            width:460px;
            margin:0 auto;
            padding-bottom:30px
        }
        div[tabindex='-1']{
            outline:0
        }
        #header{
            width: 440px;
            height: 68px;
            margin: 62px auto 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #408dd4;
        }
        .input_row{
            position:relative;
            height:29px;
            margin:0 0 14px;
            padding:10px 35px 10px 15px;
            border:solid 1px #dadada;
            background:#fff
        }
        .input_box{
            display:block;
            overflow:hidden
        }
        input{
            font-size:15px;
            line-height:16px;
            position:relative;
            z-index:9;
            width:100%;
            height:16px;
            padding:7px 0 6px;
            color:#000;
            border:none;
            background:#fff;
            -webkit-appearance:none
        }
        .input_row .ly_v2{
            top:50px
        }
        ::-webkit-input-placeholder{
            color:#8e8e8e
        }
        ::-moz-placeholder{
            color:#4a4a4a
        }
        :-ms-input-placeholder{
            color:#8e8e8e
        }
        input:-moz-placeholder{
            color:#8e8e8e
        }
        .input_row.focus{
            border:solid 1px #08A600
        }
        input[type=submit]{
            display:block;
            width:100%;
            height:61px;
            margin:30px 0 14px;
            padding-top:1px;
            border:none;
            border-radius:0;
            background-color:#408dd4;
            cursor:pointer;
            text-align:center;
            color:#fff;
            font-size:20px;
            font-weight:700;
            line-height:61px;
            -webkit-appearance:none;
            font-size:21px;
            font-weight:400;
            padding-top:1px;
        }
        input[type=submit]:disabled {
            background-color: #AAA;
            cursor: default;
        }
        button:active,button:hover,button:link{
            text-decoration:none;
            color:#fff
        }
        .error{
            font-size:12px;
            line-height:16px;
            margin:-2px 0 12px;
            color:#ff1616
        }
        .error{
            font-size:12px;
            line-height:16px;
            margin:-2px 0 12px;
            color:#ff1616
        }
        .msg {
            font-size:12px;
            line-height:16px;
            margin:-2px 0 12px;
            color: #31708f;
        }
    </style>
</head>
<body onload="document.loginForm.username.focus()">
    <div id="wrap">
        <div id="header">
            <svg xmlns="http://www.w3.org/2000/svg" style="width:100%; height: 100%;">
                <style>
                    .pinpoint-out { stroke: #05c9fe; stroke-width: 3px; fill: none; }
                    .pinpoint-in { fill: #05c9fe; }
                    .pinp { font: 52px sans-serif; fill: #FFF; }
                    .int { font: 52px sans-serif; fill: #FFF; }
                </style>

                <text x="100" y="54" class="pinp">PINP</text>
                <circle cx="230" cy="34" r="15" class="pinpoint-out"/>
                <circle cx="230" cy="34" r="4" class="pinpoint-in"/>
                <text x="256" y="54" class="int">INT</text>
            </svg>
        </div>
        <div id="container">
            <div id="content">
                <form name="loginForm" action="<c:url value='/j_spring_security_check.pinpoint' />" method="POST" onsubmit="return submitForm()">
                    <fieldset>
                        <div class="input_row">
                            <span class="input_box">
                                <input type="text" name="organization" placeholder="Organization" maxlength="40">
                            </span>
                        </div>
                        <div class="error organization_msg" style="display:none">
                            You can enter only numbers, alphabets, and "-_" characters.( 3 ~ 40 )
                        </div>
                        <div class="input_row">
                            <span class="input_box">
                                <input type="text" name="username" placeholder="User Id" maxlength="24" value="">
                            </span>
                        </div>
                        <div class="error username_msg" style="display:none">
                            You can enter only numbers, alphabets, and "-_" characters.( 4 ~ 24 )
                        </div>
                        <div class="input_row">
                            <span class="input_box">
                                <input type="password" name="password" placeholder="Password" maxlength="24">
                            </span>
                        </div>
                        <div class="error password_msg" style="display:none">
                            You can enter only numbers, alphabets, and "!@#$%^&*()" characters.(8 ~ 24)<br/>
                            And you must enter at least one alphabet, one digit, and at least one special character.
                        </div>
                        <c:if test="${not empty error}">
                            <div class="error">${error}</div>
                        </c:if>
                        <c:if test="${not empty msg}">
                            <div class="msg">${msg}</div>
                        </c:if>
                        <input type="submit" value="LOG IN">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    </fieldset>
                </form>
            </div>
        </div>
    </div>
    <script>
        var $ = document;
		var STATUS = {
			VALID: 'valid',
			INVALID: 'invalid',
			EMPTY: 'empty'
		};
		var oData = {
			organization: {
				status: STATUS.EMPTY,
				el: $.querySelector("input[name='organization']"),
				elMsg: $.querySelector("div.organization_msg"),
                reg: /^[\w\-]{3,40}$/
            },
            username: {
				status: STATUS.EMPTY,
				el: $.querySelector("input[name='username']"),
				elMsg: $.querySelector("div.username_msg"),
                reg: /^[\w\-]{4,24}$/
            },
            password: {
				status: STATUS.EMPTY,
				el: $.querySelector("input[name='password']"),
				elMsg: $.querySelector("div.password_msg"),
		        reg: /^(?=.*[a-zA-Z])(?=.*[!@#$%^\&\*\(\)])(?=.*[0-9]).{8,24}/
            }
        };
		var type = ['organization', 'username', 'password'];
        (function() {
        	Object.keys(oData).forEach(function(key) {
				(function(target) {
					target.el.addEventListener("change", function() {
						checkValid(target);
					});
				})(oData[key]);
            });
		})();

        function checkAllStatus() {
			var allValid = true;
			for (var key in oData) {
				if (oData[key].status !== STATUS.VALID) {
					allValid = false;
					break;
				}
			}
			return allValid;
        }
        function checkValid(target) {
			var v = target.el.value.trim();
			if (v === '') {
				target.status = STATUS.EMPTY;
				target.elMsg.style.display = "block";
			} else if (target.reg.test(v)) {
				target.status = STATUS.VALID;
				target.elMsg.style.display = "none";
			} else {
				target.status = STATUS.INVALID;
				target.elMsg.style.display = "block";
			}
        }
        function submitForm() {
        	type.forEach(function(key) {
				checkValid(oData[key], key === "password");
            });
        	return checkAllStatus();
        }
    </script>
</body>
</html>
