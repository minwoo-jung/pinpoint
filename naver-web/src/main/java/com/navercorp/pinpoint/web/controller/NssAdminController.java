package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.service.NssAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
@Controller
@RequestMapping("/admin/nss")
@PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.controller.AdminController).CALL_API_FOR_APP_AGENT_MANAGEMENT)")
public class NssAdminController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NssAuthService nssAuthService;

    @RequestMapping(value = "/authorizedPrefix", method = RequestMethod.GET)
    @ResponseBody
    public Collection<String> getAuthorizedPrefixes() {
        return nssAuthService.getAuthorizedPrefixes();
    }

    @RequestMapping(value = "/authorizedPrefix", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> addAuthorizedPrefix(@RequestParam("prefix") String prefix) {
        nssAuthService.addAuthorizedPrefix(prefix);
        return createSuccessResponse(prefix + " added");
    }

    @RequestMapping(value = "/authorizedPrefix", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> deleteAuthorizedPrefix(@RequestParam("prefix") String prefix) {
        nssAuthService.removeAuthorizedPrefix(prefix);
        return createSuccessResponse(prefix + " deleted");
    }

    @RequestMapping(value = "/overrideUserId", method = RequestMethod.GET)
    @ResponseBody
    public Collection<String> getOverrideUserIds() {
        return nssAuthService.getOverrideUserIds();
    }

    @RequestMapping(value = "/overrideUserId", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> addOverrideUserId(@RequestParam("userId") String userId) {
        nssAuthService.addOverrideUserId(userId);
        return createSuccessResponse(userId + " added.");
    }

    @RequestMapping(value = "/overrideUserId", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, String> removeOverrideUserId(@RequestParam("userId") String userId) {
        nssAuthService.removeOverrideUserId(userId);
        return createSuccessResponse(userId + " removed.");
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("message", message);
        return result;
    }

    private Map<String, String> createErrorResponse(String errorCode, String errorMessage) {
        Map<String, String> result = new HashMap<>();
        result.put("errorCode", errorCode);
        result.put("errorMessage", errorMessage);
        return result;
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseBody
    public Map<String, String> handleError(HttpServletRequest request, Exception e) {
        return createErrorResponse("500", e.getCause().getMessage());
    }

}
