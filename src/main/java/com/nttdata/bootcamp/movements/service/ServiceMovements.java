package com.nttdata.bootcamp.movements.service;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.bootcamp.movements.entity.Movements;
import com.nttdata.bootcamp.movements.entity.ProductDto;
import com.nttdata.bootcamp.movements.entity.RequestClientDto;
import com.nttdata.bootcamp.movements.entity.RequestMovementsDto;
import com.nttdata.bootcamp.movements.entity.ResponseDelete;
import com.nttdata.bootcamp.movements.feignclient.ClientClient;
import com.nttdata.bootcamp.movements.repository.CashHistoryRepository;
import com.nttdata.bootcamp.movements.repository.movementsRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ServiceMovements implements IServiceMovements {

	@Autowired
	movementsRepository movementsdao;
	@Autowired
	ClientClient client;
	@Autowired
	CashHistoryRepository cashhistorydao;

	@Override
	public Flux<Movements> movementsFindAll() {
		// TODO Auto-generated method stub
		return movementsdao.findAll();
	}

	@Override
	public Flux<Movements> findByIdcliente(String idclient, String idproduct) {
		// TODO Auto-generated method stub
		return movementsdao.findByIdclienteAndIdproduct(idclient, idproduct);
	}
	
	@Override
	public Flux<Movements> movementsFindAllCommissions(String idproduct, String initdate, String enddate) {
		// TODO Auto-generated method stub
		return movementsdao.findByIdproduct(idproduct).filter(value -> {
			try {
				return value.isPay_commission()
						&& (new SimpleDateFormat("mm-dd-yyyy").parse(value.getDate())
						.after(new SimpleDateFormat("mm-dd-yyyy").parse(initdate))
						||new SimpleDateFormat("mm-dd-yyyy").parse(value.getDate())
						.equals(new SimpleDateFormat("mm-dd-yyyy").parse(initdate)))
						&& new SimpleDateFormat("mm-dd-yyyy").parse(value.getDate())
						.before(new SimpleDateFormat("mm-dd-yyyy").parse(enddate))
						|| new SimpleDateFormat("mm-dd-yyyy").parse(value.getDate())
						.equals(new SimpleDateFormat("mm-dd-yyyy").parse(enddate));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		});
	}

	@CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "fallbacksave")
	@Retry(name = "myRetry")
	@Override
	public Mono<Movements> movementsSave(RequestMovementsDto request) {
		// TODO Auto-generated method stub
		RequestClientDto receiver = client.getReceiver(request.getMovements().getDestination());
		ProductDto prodReceiver = receiver.getDetail().stream().filter(value-> value.getBank_account().equals(request.getMovements().getDestination())
				|| value.getCredit_card().equals(request.getMovements().getDestination())).findAny().orElse(null);
		RequestClientDto receiverUpdate;
		if(request.getMovements().getId()==null)
		{
			var cli = client.getAll(request.getMovements().getIdcliente());
			var result = request.getMovements().operationType(cli);
			if(result)
			{
				var clientdto = client.update(cli);
				if(clientdto!=null)
					return movementsdao.save(request.toMovements());
				/*{
					return movementsdao.save(request.toMovements());
					if(request.getMovements().getType().equals("D")
							|| request.getMovements().getType().equals("R"))
						receiver = client.getReceiver(request.getMovements().getDestination());
					if(receiver!=null)
					{
						if(request.getMovements().getType().equals("D"))
							receiver.getDetail().stream().forEach(value-> {
								if(value.getBank_account().equals(request.getMovements().getDestination())
										&& !value.getBank_account().equals(request.getMovements().getBank_account()))
								{
									value.setCash((value.getCash()+request.getMovements().getAmount())-cli.)
								}
							});;
						receiverUpdate = client.update(receiverUpdate);
					}
				}*/						
				else
					return Mono.just(Movements.builder().build());
			}
			else
				return Mono.just(Movements.builder().build());
		}
			
		else
			return Mono.just(Movements.builder().build());
	}
	
	@Override
	public Mono<Movements> movementsUpdate(RequestMovementsDto request) {
		// TODO Auto-generated method stub
		if(request.getMovements().getId()!=null)
			return movementsdao.findById(request.getMovements().getId()).map(e-> request.toMovements()).flatMap(movementsdao::save);
		else
			return Mono.just(Movements.builder().build());
	}

	@Override
	public Mono<ResponseDelete> movementsDelete(String id) {
		// TODO Auto-generated method stub
		movementsdao.deleteById(id).block();
		return Mono.just(ResponseDelete.builder().response("Operación completada").build());
	}
	
	public Mono<Movements> fallbacksave(Exception e)
	{
    	log.info("Entrando al método fallbackupdate en el servicio ServiceMovements");
    	log.info("message Error: " + e.getMessage());
    	return Mono.just(Movements.builder().build());
	}
	
}
