package com.nttdata.bootcamp.movements.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.nttdata.bootcamp.movements.entity.Movements;

import reactor.core.publisher.Flux;

public interface movementsRepository extends ReactiveMongoRepository<Movements, String>  {

	Flux<Movements> findByIdclienteAndIdproduct(String idclient, String idproduct);
	
	Flux<Movements> findByIdproduct(String idproduct);
	
}
