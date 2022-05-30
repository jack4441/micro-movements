package com.nttdata.bootcamp.movements.feignclient;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;

import com.nttdata.bootcamp.movements.entity.ResponseProduct;

@FeignClient(name = "service-product")
public interface ProductClient {
@GetMapping(path = "/product/v1/allproduct", produces = MediaType.APPLICATION_JSON_VALUE)
public List<ResponseProduct> getAll();
}
