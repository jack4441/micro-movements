package com.nttdata.bootcamp.movements.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.nttdata.bootcamp.movements.entity.CashHistory;

public interface CashHistoryRepository extends ReactiveMongoRepository<CashHistory, String> {

}
