package com.nttdata.bootcamp.movements.service;
import com.nttdata.bootcamp.movements.entity.CashHistory;
import com.nttdata.bootcamp.movements.entity.Movements;
import com.nttdata.bootcamp.movements.entity.RequestMovementsDto;
import com.nttdata.bootcamp.movements.entity.ResponseDelete;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
public interface IServiceMovements {
Flux<Movements> movementsFindAll();	
Flux<Movements> findByIdcliente(String idclient, String idproduct);	
Mono<Movements> movementsSave(RequestMovementsDto request);	
Mono<Movements> movementsUpdate(RequestMovementsDto request);	
Mono<ResponseDelete> movementsDelete(String id);	
Flux<Movements> movementsFindAllCommissions(String idproduct, String initdate, String enddate);	
Flux<CashHistory> movementsFindAllAverageBalanceCreditBankAccountsPerMonth(String idclient);
Flux<Movements> findTenMovementsDebCred(String idclient);	
}
