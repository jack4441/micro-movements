package com.nttdata.bootcamp.movements.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nttdata.bootcamp.movements.entity.CashHistory;
import com.nttdata.bootcamp.movements.entity.Movements;
import com.nttdata.bootcamp.movements.entity.RequestMovementsDto;
import com.nttdata.bootcamp.movements.entity.ResponseDelete;
import com.nttdata.bootcamp.movements.service.IServiceMovements;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("movements/v1")
public class ControllerMovements {

	@Autowired
	IServiceMovements service;
	
	@GetMapping("/allmovements")
	public Flux<Movements> getAll()
	{
		return service.movementsFindAll();
	}
	
	@GetMapping("/allmovementsbyid/{idcliente}/{idproducto}")
	public Flux<Movements> getAll(@PathVariable String idcliente, @PathVariable String idproducto)
	{
		return service.findByIdcliente(idcliente, idproducto);
	}
	
	@PostMapping(path = "/savemovements", produces = MediaType.APPLICATION_JSON_VALUE
			, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Movements> save(@RequestBody RequestMovementsDto body)
	{
		return service.movementsSave(body);
	}
	
	@PutMapping(path = "/updatemovements", produces = MediaType.APPLICATION_JSON_VALUE
			, consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<Movements> update(@RequestBody RequestMovementsDto body)
	{
		return service.movementsUpdate(body);
	}
	
	@DeleteMapping(path = "/deletedetail/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseDelete> delete(@PathVariable String id)
	{
		return service.movementsDelete(id);
	}
	
	@GetMapping(path = "/getcommissionsperrange/{idproduct}/{initdate}/{enddate}")
	public Flux<Movements> movementsFindAllCommissions(@PathVariable String idproduct, @PathVariable String initdate, @PathVariable String enddate)
	{
		return service.movementsFindAllCommissions(idproduct, initdate, enddate);
	}
	
	@GetMapping(path = "/getaveragebalance/{idclient}")
	public Flux<CashHistory> movementsFindAllAverageBalanceCreditBankAccountsPerMonth(@PathVariable String idclient)
	{
		return service.movementsFindAllAverageBalanceCreditBankAccountsPerMonth(idclient);
	}
	
	@GetMapping(path = "/gettenmovements/{idclient}")
	public Flux<Movements> findTenMovementsDebCred(@PathVariable String idclient)
	{
		return service.findTenMovementsDebCred(idclient);
	}
	
}
