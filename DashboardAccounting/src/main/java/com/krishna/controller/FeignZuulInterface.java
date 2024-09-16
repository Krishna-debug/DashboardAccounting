package com.krishna.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;


// @FeignClient(name = "zuul", url = "http://" + "${DASHBOARD_ZUUL_HOST_ADDRESS}" + ":"
// 		+ "${DASHBOARD_ZUUL_PORT_NUMBER}")
@FeignClient(name = "zuul", url = "https://"+"${ENVIRONMENT_URL}"+"/zuul/")
public interface FeignZuulInterface {
    public final static String PREFIX = "api/v1";

    @GetMapping(PREFIX+"/cron/auth/getTestTokenFromZuul")
	public Object getTestTokenFromZuul();

}
