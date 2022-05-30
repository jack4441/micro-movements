package com.nttdata.bootcamp.movements.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Document("cash_history")
@Builder
public class CashHistory implements Serializable {
	private static final long serialVersionUID = 1143229964550970423L;
	
	@Transient
	private static final double commission = 1;	
	
	@Getter
	@Setter
	@Id
	private String id;
	@Getter
	@Setter
	@Field
	private String idcliente;
	@Getter
	@Setter
	@Field
	private String fullname;
	@Getter
	@Setter
	@Field
	private String iddetail;
	@Getter
	@Setter
	@Field
	private String idproduct;
	@Getter
	@Setter
	@Field
	private String bank_account;
	@Getter
	@Setter
	@Field
	private String credit_card;
	@Getter
	@Setter
	@Field
	private String type;
	@Getter
	@Setter
	@Field
	private double amount;
	@Getter
	@Setter
	@Field
	private double current_balance;
	@Getter
	@Setter
	@Field
	private String destination;
	@Getter
	@Setter
	@Field
	private String date;
	@Getter
	@Setter
	@Field
	private String time;
	@Getter
	@Setter
	@Field
	private String state;
	@Getter
	@Setter
	@Field
	private boolean last_balance_day;
	@Getter
	@Setter
	@Field
	private boolean pay_commission;
	@Getter
	@Setter
	@Field
	private double amount_commission;
	
	public void filterCasHistoryTransmitter(RequestClientDto cli_transmitter) {
		var result = false;
		log.info("Entrando al método filterCasHistoryTransmitter en la clase CashHistory.");
		
		this.idcliente = cli_transmitter.getId();
		this.current_balance = 0;
		this.state="transmitter";
		//Si es una transacción bancaria
		if(this.getType().equals("T"))
		{
			log.info("Se detectó el movimiento como un depósito a un tercero.");
			cli_transmitter.getDetail().stream().forEach(value-> {
				if(value.getBank_account().equals(this.getBank_account()))
				{
					log.info("Asignamos el saldo actual al registro del historial de saldo para el que transfiere.");
					this.current_balance = ((value.getCash()+this.getAmount())-this.amount_commission);
					this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
				}
			});
		}
		//Si es un depósito
		if(this.getType().equals("D"))
		{
			log.info("Se detectó el movimiento como un depósito.");
			cli_transmitter.getDetail().stream().forEach(value-> {
				if(value.getIddetail().equals(this.getIddetail()))
				{
					log.info("Asignamos el saldo actual al registro del historial de saldo para el que transfiere.");
					this.current_balance = ((value.getCash()+this.getAmount())-this.amount_commission);
					this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
				}
			});
		}
		//Si es una recarga
		if(this.getType().equals("R"))
			cli_transmitter.getDetail().stream().forEach(value-> {
				if(value.getIddetail().equals(this.getIddetail()))
				{
					this.current_balance = ((value.getCash()-this.getAmount())-this.amount_commission);
					this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
				}
			});
		//Si es un pago de un crédito
		if(this.type.equals("PC"))
			cli_transmitter.getDetail().stream().forEach(value-> {
				if(value.getIddetail().equals(this.getIddetail()))
				{
					this.current_balance = ((value.getCredit()-this.getAmount())-this.amount_commission);
					this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
				}
			});
		//Si es un consumo con una tarjeta de crédito
		if(this.type.equals("CCC"))
			cli_transmitter.getDetail().stream().forEach(value-> {
				if(value.getCredit_card().equals(this.credit_card)
						&& 
						value.getIddetail().equals(this.iddetail))
				{
					this.current_balance = ((value.getCredit()-this.getAmount())-this.amount_commission);
					this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
				}
			});
		//Si es un pago de un crédito con destino a un tercero mediante una tarjeta de débito
		if(this.type.equals("PCTD"))
			for (var value : cli_transmitter.getDetail()) {
				if(value.getIddetail().equals(this.getIddetail()))
				{
					log.info("Buscar la cuenta principal.");
					ProductDto prodCB = cli_transmitter.getDetail().stream().filter(valueFind -> 
					valueFind.getBank_account().equals(value.getBank_account())).findAny().orElseGet(null);
					result = resultBankDeposit(prodCB, cli_transmitter);
					if(!result)
					{
						log.info("Realizar el movimiento con las cuentas bancarias alternas.");
						if(value.getBank_accounts()!=null)
						{
							if(!value.getBank_accounts().isEmpty())
							{
								for (var account : value.getBank_accounts()) {
									ProductDto prodCBAlt = cli_transmitter.getDetail().stream().filter(valueFindCBAlt -> 
									valueFindCBAlt.getBank_account().equals(account.getAccount())).findAny().orElseGet(null);
									if(!result)
									{
										log.info("Realizar el movimiento con la cuenta bancaria alterna " + prodCBAlt.getBank_account());
										result = resultBankDeposit(prodCBAlt, cli_transmitter);
										if(result)
										{
											this.current_balance = ((prodCBAlt.getCash()-this.getAmount())-this.amount_commission);
											this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
										}
									}
								}
							}
						}							
					}
				}
			}
	}
	
	//Método auxiliar
	private boolean resultBankDeposit(ProductDto prod, RequestClientDto cli)
	{
		boolean result = false;
		if(filterTypeBankAccount(prod))
		{
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
			cli.getDetail().remove(index);
			cli.getDetail().add(index, prod);	
			log.info("El registro modificado es agregado a la lista de productos del cliente");
			result = true;
		}
		return result;
	}
	
	//Método auxiliar
	private boolean filterTypeBankAccount(ProductDto prod)
	{
		var result = false;
		if(prod.getLimit_transaction() != prod.getCounter_transaction() 
				&& prod.getLimit_transaction()!=0)
		{
			if(!prod.getDeposit_date().equals("-"))
			{
				if(new SimpleDateFormat("dd-MM-yyyy").format(new Date()).equals(prod.getDeposit_date()))
				{
					result = true;
				}
			}
			if(prod.getDeposit_date().equals("-"))
			{
				result = true;
			}
		}
		if(prod.getLimit_transaction()==0)
			result = true;
		return result;
	}
	
	public void filterCasHistoryReceiver(RequestClientDto cli_receiver) {
		
		log.info("Entrando al método filterCasHistoryReceiver en la clase CashHistory.");

		this.idcliente = cli_receiver.getId();
		this.current_balance = 0;
		//Si es una transacción bancaria
		if(this.getType().equals("T"))
		{
			log.info("Se detectó el movimiento como un depósito a un tercero.");
			cli_receiver.getDetail().stream().forEach(value-> {
				if(value.getBank_account().equals(this.getDestination())
						&& !value.getBank_account().equals(this.getBank_account()))
				{
					log.info("Asignamos el saldo actual al registro del historial de saldo para el receptor.");
					this.current_balance = ((value.getCash()+this.getAmount())-this.amount_commission);
					this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
					cli_receiver.getDetail().forEach(prod-> {
						if(prod.getBank_account().equals(this.getDestination()))
							prod.setCash(this.current_balance);
							this.fullname = cli_receiver.getName()+" "+cli_receiver.getLastname();
							this.bank_account = this.destination;
							this.destination = "-";
							this.state="receiver";
							this.iddetail=prod.getIddetail();
							this.idproduct=prod.getId();
					});
				}
			});
		}
		//Si es un pago de un crédito
		if(this.type.equals("PC"))
			cli_receiver.getDetail().stream().forEach(value-> {
				if(value.getIddetail().equals(this.getIddetail()))
				{
					this.current_balance = ((value.getCredit()-this.getAmount())-this.amount_commission);
					this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
					cli_receiver.getDetail().forEach(prod-> {
						if(prod.getIddetail().equals(this.getDestination()))
							prod.setCredit(this.current_balance);
							this.fullname = cli_receiver.getName()+" "+cli_receiver.getLastname();
							this.bank_account = this.destination;
							this.destination = "-";
							this.state="receiver";
							this.iddetail=prod.getIddetail();
							this.idproduct=prod.getId();
					});
				}
			});
		//Si es un pago de un crédito con destino a un tercero mediante una tarjeta de débito
		if(this.type.equals("PCTD"))
			cli_receiver.getDetail().stream().forEach(value-> {
				if(value.getIddetail().equals(this.getDestination()))
				{
					this.current_balance = ((value.getCredit()-this.getAmount())-this.amount_commission);
					this.time=new SimpleDateFormat("HH:mm:ss").format(new Date());
					cli_receiver.getDetail().forEach(prod-> {
						if(prod.getIddetail().equals(this.getDestination()))
							prod.setCredit(this.current_balance);
							this.fullname = cli_receiver.getName()+" "+cli_receiver.getLastname();
							this.bank_account = this.destination;
							this.destination = "-";
							this.state="receiver";
							this.iddetail=prod.getIddetail();
							this.idproduct=prod.getId();
					});
				}
			});
			
	}
	
}