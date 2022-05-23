package com.nttdata.bootcamp.movements.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Document("movements")
@Data
@Builder
public class Movements implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -796337333766845074L;
	@Transient
	private static final double commission = 1;
	@Transient
	private static final double  limit_transaction = 3;
	
	
	@Id
	private String id;
	@Field
	private String idcliente;
	@Field
	private String fullname;
	@Field 
	private String idproduct; 
	@Field
	private String bank_account;
	@Field
	private String credit_card;
	@Field
	private String type;
	@Field
	private double amount;
	@Field
	private String destination;
	@Field
	private String date;
	@Field
	private boolean pay_commission;
	@Field
	private double amount_commission;
	
	public boolean operationType(RequestClientDto cli)
	{
		var result = false;
		log.info("Entrando al método operationType en la clase Movements");

		//Depósitos bancarios
		if(this.type.equals("D"))
		{
			log.info("Se identificó el movimiento como un depósito bancario");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getId().equals(this.idproduct)
			&& 
			value.getBank_account().equals(this.bank_account))
			.findAny().get();
			var index = cli.getDetail().indexOf(prod);
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			prod.setCash(prod.getCash()+this.amount);			
			//Definir la comisión
			if(prod.getCounter_transaction()>commission)
			{
				log.info("Comisión a cobrar; se alcanzo el límite de transacciones: " + ((this.amount/100)*commission));
				prod.setCash(prod.getCash()-((this.amount/100)*commission));
				this.pay_commission = true;
				this.amount_commission = (this.amount/100)*commission;
			}
			//Aumentamos el contador de transacciones
			prod.setCounter_transaction(prod.getCounter_transaction()+1);
			log.info("Se modificó la cantidad del cash de su cuenta bancaria: " + cli.getDetail().get(index).getBank_account());
			log.info("Cash actual: " + cli.getDetail().get(index).getCash());
			cli.getDetail().add(index, prod);	
			log.info("El registro modificado es agregado a la lista de productos del cliente");
			result = true;
		}
		//Retiros bancarios
		if(this.type.equals("R"))
		{
			log.info("Se identificó el movimiento como un retiro bancario");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getId().equals(this.idproduct)
			&& 
			value.getBank_account().equals(this.bank_account))
			.findAny().get();
			var index = cli.getDetail().indexOf(prod);
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			if(prod.getCash()-this.amount>=0)
			{
				prod.setCash(prod.getCash()-this.amount);
				//Definir la comisión
				if(prod.getCounter_transaction()>commission)
				{
					log.info("Comisión a cobrar; se alcanzo el límite de transacciones: " + ((this.amount/100)*commission));
					prod.setCash(prod.getCash()-((this.amount/100)*commission));
					this.pay_commission = true;
					this.amount_commission = (this.amount/100)*commission;
				}
				//Aumentamos el contador de transacciones
				prod.setCounter_transaction(prod.getCounter_transaction()+1);
				log.info("Se modificó la cantidad del cash de su cuenta bancaria: " + cli.getDetail().get(index).getBank_account());
				log.info("Cash actual: " + cli.getDetail().get(index).getCash());
				cli.getDetail().add(index, prod);	
				log.info("El registro modificado es agregado a la lista de productos del cliente");
				result = true;
			}
			else
			{
				log.info("El cliente con saldo "+cli.getDetail().get(index).getCash()+" no tiene saldo suficiente para realizar el retiro ingresado de " + this.amount);
				result = false;
			}
		}
		//Pagos de créditos
		if(this.type.equals("PC"))
		{
			log.info("Se identificó el movimiento como un pago de crédito");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getId().equals(this.idproduct)
			&& 
			value.getBank_account().equals(this.bank_account))
			.findAny().get();
			var index = cli.getDetail().indexOf(prod);
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			if(prod.getCredit()-this.amount>=0)
			{
				prod.setCredit(prod.getCredit()-this.amount);
				log.info("Se modificó la cantidad de la deuda de su crédito");
				log.info("Crédito actual a pagar: " + cli.getDetail().get(index).getCredit());
				cli.getDetail().add(index, prod);	
				log.info("El registro modificado es agregado a la lista de productos del cliente");
				result = true;
			}
			else
			{
				log.info("El cliente con deuda "+cli.getDetail().get(index).getCredit()+" a ingresado un monto mayor a la deuda : " + this.amount);
				result = false;
			}
		}
		//Consumos a tarjetas de crédito
		if(this.type.equals("CCC"))
		{
			log.info("Se identificó el movimiento como un consumo a una tarjeta de crédito");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getId().equals(this.idproduct)
			&& 
			value.getCredit_card().equals(this.credit_card))
			.findAny().get();
			var index = cli.getDetail().indexOf(prod);
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			if(prod.getCredit()-this.amount>=0)
			{
				prod.setCredit(prod.getCredit()-this.amount);
				log.info("Se modificó la cantidad del crédito de su tarjeta");
				log.info("Crédito actual a pagar: " + cli.getDetail().get(index).getCredit());
				cli.getDetail().add(index, prod);	
				log.info("El registro modificado es agregado a la lista de productos del cliente");
				result = true;
			}
			else
			{
				log.info("El cliente con crédito "+cli.getDetail().get(index).getCredit()+" no tiene saldo suficiente para realizar el consumo con esta tarjeta. Monto ingresado : " + this.amount);
				result = false;
			}
		}
		return result;
	}
	
}
