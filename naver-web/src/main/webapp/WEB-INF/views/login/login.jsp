<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="ko">
<head>
	<meta charset="UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no">
    <title>Pinpoint : 로그인</title>
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
            <h1><img width="228px" height="36px" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAHIAAAASCAYAAACHKYonAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMy1jMDExIDY2LjE0NTY2MSwgMjAxMi8wMi8wNi0xNDo1NjoyNyAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNiAoV2luZG93cykiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RERGNjFCMjFBMENBMTFFNjgyN0JCMjUxRkQxOUExNkIiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RERGNjFCMjJBMENBMTFFNjgyN0JCMjUxRkQxOUExNkIiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpEREY2MUIxRkEwQ0ExMUU2ODI3QkIyNTFGRDE5QTE2QiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpEREY2MUIyMEEwQ0ExMUU2ODI3QkIyNTFGRDE5QTE2QiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PmmABf0AAAO5SURBVHja3FlNaBQxFJ4Rf9ZikYqC6EFc8a8X0YXiwWMLItSDWK8W0RYEPboVvYm0RQ8e9LALYhU96IJHRXY910MXxYNYtS0iigp2EWmrrXb8ot/UdLqTSTOTrvrgIzuTZPLe+/KSvKzreV6zU10qQNlRi9+3pKgbJpwEvmWiY6i4ZSeFYjnw2cs405rdtGx2XTfSZt/3aFvicwOKjIEpZRedvYhGBSAforjf11XUCUe30OlKuzS+ZaJjkLwmFCfo9LV8PQ70A9eA2yD1e0w9f9kMgioRRHok0pWILRoQ2SIT2RWoFLOjDUjzuZPOMnF+nv3jEmmioxx9l4Ej0hjfgEmgXmo6ABwEma9jTrg8+OmcJ5Fp2lMt0ps5UUtVJ7L3R5wQ5Fg/KkI/UKfqK2SI/YS0KcbQ+ZapjqDGWwoUAQ8YBy4AjVL9auAYMMI274FthnrOsjmKyLAVMfDdLJtmw3yg4yRHQUaUUUWgQ3Jy2hKRKh0FURdJ0DtgZ6gzBrx64B7bPgNSBnrOsXkhiFyku5myTBus33lCLIM5x55U1RFLqng+CfwADmDJfBzq2IzzhUvbILAdOG6oy0LZPCO6RKYDztKVZmlvG+Zz1pItYTq2A0uAGyDqUeRJMuOMoTjFxw4DPebYLCLpbyDSP0zEOeqLvof4u0eRTpiKSse9LG8GInUl0AcUgV2BPvdFSgJsRd16o9Tm94l1xmZFmmeVyAY6Wzj9jjTDKjHGKkunzhzHiCO6Om5k+TTw/jRwmN+4FYjKKRQv+bjBOE913Vk2M0+0Ios1crVhKlNIYLxe6Sidk2asrpjoOMlymWISTyvGmopjMMjsZTSa2jxvIruq1Jfi3JyEiDBkiMthluTqiomOb4B1YpkE3krvzwNrALF0ngksu2JP3Sz1T8xmsV8KchNnUvNon3TK0CYdzzOB9ME0/QhLKc4xncjPo89+9nlhanMVP8+yme9GFzr9SFoKjET5eF62NNZ1Lo/tvJ6LusKrQ9Et7eWJCKLQqs21ItLfL8u8JO6xNQjm/isUl5iC3AVROxQkruAkawSeA1ds2YwoStTmWhJZ4d1ohXtlxuJYZ4GH3A/7QVg3sEUicBUg7mCfAPuAj0ArJsHXJJVgSmLF5loS6S8tvVI6YSsqxcm1FbgKpHhoGgR5Y8Aofn9i3SZA3PzsZiQnLkxJEre51kT6y03B9iAgZgI4ip97gD7gA1BHZ04AD5hXNqHdiE1deGpN1GY3+u/I/1cQG+JP5RSIq/zrtvwUYAA22ObjzGRVAgAAAABJRU5ErkJggg==" alt="" /></h1>
        </div>
        <div id="container">
            <div id="content">
                <form name="loginForm" action="<c:url value='/j_spring_security_check.pinpoint' />" method="POST">
                    <fieldset>
                        <div class="input_row">
                            <span class="input_box">
                                <input type="text" name="company" placeholder="Company" maxlength="16">
                            </span>
                        </div>
                        <div class="input_row">
                            <span class="input_box">
                                <input type="text" name="username" placeholder="User Id" maxlength="41" value="">
                            </span>
                        </div>
                        <div class="input_row">
                            <span class="input_box">
                                <input type="password" name="password" placeholder="Password" maxlength="16">
                            </span>
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
</body>
</html>
