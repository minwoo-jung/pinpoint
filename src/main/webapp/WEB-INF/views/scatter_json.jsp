<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
{
	"scatter" : [
	<c:forEach items="${scatter}" var="dot" varStatus="status">
		{
			"x" : ${dot.timestamp},
			"y" : ${dot.executionTime},
			"traceId" : "${dot.traceId}",
			"type" : <c:choose><c:when test="${dot.exceptionCode == 1}">"Failed"</c:when><c:otherwise>"Success"</c:otherwise></c:choose> 
		}
	    <c:if test="${!status.last}">,</c:if>
	</c:forEach>
	]
}