package com.nttdata.bootcamp.movements.service;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nttdata.bootcamp.movements.entity.CashHistory;
import com.nttdata.bootcamp.movements.entity.Movements;
import com.nttdata.bootcamp.movements.entity.ProductDto;
import com.nttdata.bootcamp.movements.entity.RequestClientDto;
import com.nttdata.bootcamp.movements.entity.RequestMovementsDto;
import com.nttdata.bootcamp.movements.entity.ResponseDelete;
import com.nttdata.bootcamp.movements.feignclient.ClientClient;
import com.nttdata.bootcamp.movements.feignclient.ProductClient;
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
	@Autowired
	ProductClient productClient; 

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
	public Flux<Movements> findTenMovementsDebCred(String idclient){
		return movementsdao.findAll().filter(value-> 
				value.getIdcliente().equals(idclient) 
				&&
				(value.getIdproduct()
				.equals(productClient.getAll().stream().filter(prod-> prod.getDescription().equals("tarjeta de debito")).findFirst().get().getId())
				||
				value.getIdproduct()
				.equals(productClient.getAll().stream().filter(prod-> prod.getDescription().equals("tarjeta de credito")).findFirst().get().getId())
				));
	}
	
	@Override
	public Flux<Movements> movementsFindAllCommissions(String idproduct, String initdate, String enddate) {
		// TODO Auto-generated method stub
		return movementsdao.findByIdproduct(idproduct).filter(value -> {
			try {
				return value.isPay_commission()
						&& (new SimpleDateFormat("dd-MM-yyyy").parse(value.getDate())
						.after(new SimpleDateFormat("dd-MM-yyyy").parse(initdate))
						||new SimpleDateFormat("dd-MM-yyyy").parse(value.getDate())
						.equals(new SimpleDateFormat("dd-MM-yyyy").parse(initdate)))
						&& new SimpleDateFormat("dd-MM-yyyy").parse(value.getDate())
						.before(new SimpleDateFormat("dd-MM-yyyy").parse(enddate))
						|| new SimpleDateFormat("dd-MM-yyyy").parse(value.getDate())
						.equals(new SimpleDateFormat("dd-MM-yyyy").parse(enddate));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		});
	}
	
	@Override
	public Flux<CashHistory> movementsFindAllAverageBalanceCreditBankAccountsPerMonth(String idclient)
	{
		return cashhistorydao.findAll().filter(filteridclient-> filteridclient.getIdcliente().equals(idclient))
				.filter(filterdate-> {
					try {
						return new SimpleDateFormat("MM").format(new SimpleDateFormat("dd-MM-yyyy").parse(filterdate.getDate()))
								.equals(new SimpleDateFormat("MM").format(new Date())) && filterdate.isLast_balance_day();
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
		log.info("Entrando al método movementsSave en el servicio ServiceMovements.");
		log.info("Obtenemos al destinatario de la transacción bancaria");
		//Aquí puden ser varios en caso el receptor tenga autorizados en sus cuentas
		RequestClientDto receiver = client.getReceiver(request.getMovements().getDestination());
		log.info("El valor obtenido es null?: " + (receiver==null));
		CashHistory resultSaveCashHistoryTransmitter=null;
		if(request.getMovements().getId()==null)
		{
			log.info("Obtenemos al cliente que realiza la operación.");
			var cli = client.getAll(request.getMovements().getIdcliente());
			log.info("El cliente obtenido: "+cli.getName()+" "+cli.getLastname());
			log.info("Configuración del movimiento financiero actual.");
			var result = request.getMovements().operationType(cli);
			if(result)
			{
				log.info("Actualizamos el saldo del cliente que realiza el movimiento.");
				var clientdto = client.update(cli);
				if(clientdto!=null)
				{
					log.info("Configuración del registro para el historial de saldos para el receptor.");
					
					CashHistory cashhistorysave = request.toCashHistory();
					CashHistory resultSaveCashHistoryReceiver = null;
					if(receiver!=null)
					{
						cashhistorysave.filterCasHistoryReceiver(receiver);
						if(cashhistorysave.getCurrent_balance()>0)
						{
							log.info("Se guarda el historial de saldos del receptor.");
							resultSaveCashHistoryReceiver = cashhistorydao.save(cashhistorysave).block();
							updateLastBalanceDay(cashhistorysave);					
						}
						RequestClientDto resultUpdateReceiver = new RequestClientDto();
						if(resultSaveCashHistoryReceiver!=null)
						{
							log.info("Se actualiza el saldo del receptor.");
							resultUpdateReceiver = client.update(receiver);
						}
					}
					log.info("Configuración del registro para el historial de saldos para el que transfiere.");
					cashhistorysave = request.toCashHistory();
					cashhistorysave.filterCasHistoryTransmitter(cli);
					if(cashhistorysave.getCurrent_balance()>0)
					{
						log.info("Se guarda el historial de saldos del que transfiere.");
						resultSaveCashHistoryTransmitter = cashhistorydao.save(cashhistorysave).block();	
						updateLastBalanceDay(cashhistorysave);
					}				
					if(resultSaveCashHistoryTransmitter!=null)
					{
						log.info("Se guarda el movimiento.");
						return movementsdao.save(request.toMovements());
					}
					else
						return Mono.just(Movements.builder().build());
				}				 								
				else
					return Mono.just(Movements.builder().build());
			}
			else
				return Mono.just(Movements.builder().build());
		}
			
		else
			return Mono.just(Movements.builder().build());
	}
	
	private void updateLastBalanceDay(CashHistory cashhistorysave) {
		var findCashHistory = cashhistorydao.findByIdclienteAndIddetailAndIdproductAndDate(cashhistorysave.getIdcliente(), cashhistorysave.getIddetail()
				, cashhistorysave.getIdproduct(), cashhistorysave.getDate()).collectList().defaultIfEmpty(new ArrayList<>()).block();
		if(findCashHistory.size()>1)
			findCashHistory.forEach(value1-> {
				findCashHistory.forEach(value2-> {
					if(!value1.getId().equals(value2.getId()))
					{
						if(ChronoUnit.SECONDS.between(LocalTime.parse(value1.getTime()), LocalTime.parse("00:00:00"))
							< ChronoUnit.SECONDS.between(LocalTime.parse(value2.getTime()), LocalTime.parse("00:00:00")))
						{
							value1.setLast_balance_day(true);
							cashhistorydao.save(value1).block();
						}
						else
						{
							value1.setLast_balance_day(false);
							cashhistorydao.save(value1).block();
						}
					}
				});
			});
		else
			findCashHistory.get(0).setLast_balance_day(true);
			cashhistorydao.save(findCashHistory.get(0)).block();
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
