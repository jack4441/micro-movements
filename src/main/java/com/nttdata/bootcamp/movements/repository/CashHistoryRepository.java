package com.nttdata.bootcamp.movements.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.nttdata.bootcamp.movements.entity.CashHistory;

import reactor.core.publisher.Flux;

public interface CashHistoryRepository extends ReactiveMongoRepository<CashHistory, String> {

	Flux<CashHistory> findByIdclienteAndIddetailAndIdproductAndDate(String idcliente, String iddetail, String idproduct, String date);
	
}
