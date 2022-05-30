package com.nttdata.bootcamp.movements;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import  static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.nttdata.bootcamp.movements.controller.ControllerMovements;
import com.nttdata.bootcamp.movements.entity.Movements;
import com.nttdata.bootcamp.movements.entity.RequestMovementsDto;
import com.nttdata.bootcamp.movements.entity.ResponseDelete;
import com.nttdata.bootcamp.movements.repository.CashHistoryRepository;
import com.nttdata.bootcamp.movements.repository.movementsRepository;
import com.nttdata.bootcamp.movements.service.ServiceMovements;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@WebFluxTest(ControllerMovements.class)
class ApplicationTests {

	@Autowired
	private WebTestClient webTestClient;
	
	@MockBean
	movementsRepository movementsrepos;
	@MockBean
	CashHistoryRepository cashhistoryrepos;
	@MockBean
	ServiceMovements services;
	
	@Test
	//1.- Registrar un depósito a una misma cuenta bancaria.
	public void registerMovementsInTheSameBankAccount() {
		RequestMovementsDto movement = new RequestMovementsDto();
		var movem = Movements.builder()
		.idcliente("6293fff2e67c141cf04875de")
		.iddetail("79ae61df-3cc6-495d-bb0b-1b470a525fb0")
		.fullname("Jose Diestra")
		.idproduct("62853d353bb9de122e044c65")
		.bank_account("478547878457894444")
		.credit_card("-")
		.type("D")
		.amount(10.00)
		.destination("478547878457894444")
		.date("20-05-2022")
		.build();
		movement.setMovements(movem);
		when(services.movementsSave(movement)).thenReturn(Mono.just(movement.toMovements()));
		
		webTestClient.post().uri("/movements/v1/savemovements")
		.bodyValue(movement)
		.exchange()
		.expectStatus().isOk();
		
	}
	
	@Test
	//2.- Depósito a otra cuenta del mismo cliente.
	public void registerMovementsInTheOwnerBankAccount() {
		RequestMovementsDto movement = new RequestMovementsDto();
		var movem = Movements.builder()
		.idcliente("6293fff2e67c141cf04875de")
		.iddetail("79ae61df-3cc6-495d-bb0b-1b470a525fb0")
		.fullname("Jose Diestra")
		.idproduct("62853d353bb9de122e044c65")
		.bank_account("478547878457894444")
		.credit_card("-")
		.type("T")
		.amount(10.00)
		.destination("478547878457898654")
		.date("27-05-2022")
		.build();
		movement.setMovements(movem);
		when(services.movementsSave(movement)).thenReturn(Mono.just(movement.toMovements()));
		
		webTestClient.post().uri("/movements/v1/savemovements")
		.bodyValue(movement)
		.exchange()
		.expectStatus().isOk();
		
	}
	
	@Test
	//3.- Registrar pagos de créditos.
	public void registerMovementsPayCredit() {
		RequestMovementsDto movement = new RequestMovementsDto();
		var movem = Movements.builder()
		.idcliente("629406be748a356b955032bd")
		.iddetail("66d2d75b-e60c-4c74-bc9f-8c6fc72faf83")
		.fullname("Salvador Roma")
		.idproduct("62853d533bb9de122e044c67")
		.bank_account("-")
		.credit_card("-")
		.type("PC")
		.amount(2000.00)
		.destination("62853d533bb9de122e044c67-credit")
		.date("19-05-2022")
		.build();
		movement.setMovements(movem);
		when(services.movementsSave(movement)).thenReturn(Mono.just(movement.toMovements()));
		
		webTestClient.post().uri("/movements/v1/savemovements")
		.bodyValue(movement)
		.exchange()
		.expectStatus().isOk();
		
	}
	
	@Test
	//4.- Cargar consumos a tarjetas de crédito.
	public void registerMovementsConsumesCreditCard() {
		RequestMovementsDto movement = new RequestMovementsDto();
		var movem = Movements.builder()
		.idcliente("629406c3748a356b955032bf")
		.iddetail("f089ad03-aa4e-499e-aa61-f6b88d8184f2")
		.fullname("Juana Horcara")
		.idproduct("62853d6b3bb9de122e044c69")
		.bank_account("-")
		.credit_card("4555-4444-4444-4444")
		.type("CCC")
		.amount(100.00)
		.destination("62853d6b3bb9de122e044c69-credit")
		.date("22-05-2022")
		.build();
		movement.setMovements(movem);
		when(services.movementsSave(movement)).thenReturn(Mono.just(movement.toMovements()));
		
		webTestClient.post().uri("/movements/v1/savemovements")
		.bodyValue(movement)
		.exchange()
		.expectStatus().isOk();
		
	}
	
	@Test
	//5.- Registrar  pagos de créditos de terceros mediante una tarjeta de débito.
	public void registerMovementsPayCreditWithCreditCard() {
		RequestMovementsDto movement = new RequestMovementsDto();
		var movem = Movements.builder()
		.idcliente("6293fff2e67c141cf04875de")
		.iddetail("dc187e62-c29d-42f8-bf07-715dfd287ab2")
		.fullname("Jose Diestra")
		.idproduct("629189ef34033978a275432f")
		.bank_account("-")
		.credit_card("4555-4444-4444-8888")
		.type("PCTD")
		.amount(1.00)
		//Iddetail del producto del destinatario.
		.destination("946f8647-5938-42d5-affa-c1789eaa14af")
		.date("28-05-2022")
		.build();
		movement.setMovements(movem);
		when(services.movementsSave(movement)).thenReturn(Mono.just(movement.toMovements()));
		
		webTestClient.post().uri("/movements/v1/savemovements")
		.bodyValue(movement)
		.exchange()
		.expectStatus().isOk();
		
	}
	
	@Test
	//5.- Actualizar movimiento.
	public void updateMovements() {
		RequestMovementsDto movement = new RequestMovementsDto();
		var movem = Movements.builder()
		.id("6292da42b2533647ce28d710")
		.idcliente("6292c9d0cd3393418bd79ba4")
		.iddetail("23edf891-40f3-4f90-b28b-99a83e8c4f9f")
		.fullname("Jose Diestra")
		.idproduct("62853d353bb9de122e044c65")
		.bank_account("478547878457898654")
		.credit_card("-")
		.type("D")
		.amount(11.00)
		//Iddetail del producto del destinatario.
		.destination("478547878457898654")
		.date("20-05-2022")
		.build();
		movement.setMovements(movem);
		when(services.movementsSave(movement)).thenReturn(Mono.just(movement.toMovements()));
		
		webTestClient.put().uri("/movements/v1/updatemovements")
		.bodyValue(movement)
		.exchange()
		.expectStatus().isOk();
		
	}
	
	@Test
	//12.- Eliminar Movimientos.
	public void deleteMovements()
	{
		String idclient = "6292da42b2533647ce28d710";
		ResponseDelete reponse = ResponseDelete.builder().response("Operación completada").build();
		given(services.movementsDelete(any())).willReturn(Mono.just(reponse));
		webTestClient.delete().uri("/movements/v1/deletedetail/"+idclient)
			.exchange()
			.expectStatus().isOk();
		
	}

}
