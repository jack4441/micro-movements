package com.nttdata.bootcamp.movements.feignclient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.MediaType;
import com.nttdata.bootcamp.movements.entity.RequestClientDto;


@FeignClient(name = "service-client")
public interface ClientClient {

	@GetMapping(path = "/client/v1/findclient/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	RequestClientDto getAll(@PathVariable String id);
	
	@GetMapping(path = "/client/v1/getReceiver/{bank_account}", produces = MediaType.APPLICATION_JSON_VALUE)
	RequestClientDto getReceiver(@PathVariable String bank_account);
	
	@PutMapping(path = "/client/v1/updateclient", produces = MediaType.APPLICATION_JSON_VALUE)
	RequestClientDto update(@RequestBody RequestClientDto body);
}
