package com.nttdata.bootcamp.movements;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.nttdata.bootcamp.movements.entity.MessageTransactWallet;
import com.nttdata.bootcamp.movements.entity.Movements;
import com.nttdata.bootcamp.movements.entity.ProductDto;
import com.nttdata.bootcamp.movements.entity.RequestClientDto;
import com.nttdata.bootcamp.movements.entity.RequestMovementsDto;
import com.nttdata.bootcamp.movements.feignclient.ClientClient;
import com.nttdata.bootcamp.movements.service.IServiceMovements;
import com.nttdata.bootcamp.movements.service.ServiceMovements;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableFeignClients
@EnableEurekaClient
@SpringBootApplication
@RequiredArgsConstructor
public class Application {

	private final KafkaTemplate<Integer, String> template;
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@Autowired
	ClientClient clientClient;
	@Autowired
	IServiceMovements movementsdao;
	@Component
	class Consumer{
		
		@KafkaListener(topics={"client"}, groupId="spring-boot-kafka-client")
		public void consume(String message) throws JsonMappingException, JsonProcessingException
		{
			log.info("message recibido: " + message);

			MessageTransactWallet messageTransact = Util.objectMapper.readValue(message, MessageTransactWallet.class);
			RequestClientDto response = clientClient.getReceiver(messageTransact.getIddetail());
			ProductDto product = response.getDetail()
					.stream()
					.filter(prod-> prod.getIddetail().equals(messageTransact.getIddetail())).findFirst().get();
			RequestMovementsDto requestMovements = new RequestMovementsDto();
			requestMovements.setMovements(Movements.builder()
					.idcliente(response.getId())
					.fullname(response.getName()+" "+response.getLastname())
					.iddetail(messageTransact.getIddetail())
					.idproduct(product.getId())
					.bank_account("-")
					.credit_card(product.getCredit_card())
					.type("PCTD")
					.amount(messageTransact.getAmount())
					.destination(messageTransact.getDestination())
					.date(new SimpleDateFormat("dd-MM-yyyy").format(new Date()))
					.build());
			movementsdao.movementsSave(requestMovements).block();
			var receiver = clientClient.getReceiver(messageTransact.getDestination());
			receiver.getDetail().forEach(prod-> {
				if(prod.getBank_account().equals(messageTransact.getDestination())&&prod.getCredit_card().equals("-"))
					prod.setCash(prod.getCash()+messageTransact.getAmount());
			});
			clientClient.update(receiver);
			messageTransact.setStatus("Success");
			template.send("transferWallet", messageTransact.toString());
		}
	}
	
}
