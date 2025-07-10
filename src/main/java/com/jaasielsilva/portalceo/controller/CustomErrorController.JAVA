package com.jaasielsilva.portalceo.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            model.addAttribute("codigoErro", statusCode);

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return "errors/404";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                return "errors/403";
            } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
                return "errors/400";
            } else if (statusCode == HttpStatus.UNAUTHORIZED.value()) {
                return "errors/401";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "errors/500";
            } else {
                return "errors/generic";
            }

}
