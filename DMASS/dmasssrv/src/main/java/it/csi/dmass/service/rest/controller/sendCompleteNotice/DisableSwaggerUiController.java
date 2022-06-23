package it.csi.dmass.service.rest.controller.sendCompleteNotice;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;


import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DisableSwaggerUiController {
	
	@RequestMapping(value = "swagger-ui.html", method = RequestMethod.GET)
    public void getSwagger(HttpServletResponse httpResponse) throws IOException {
        httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
    }

}
