package com.nttdata.bootcamp.movements.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Document("movements")
@Builder
public class Movements implements Serializable {
	private static final long serialVersionUID = -796337333766845074L;
	@Transient
	private static final double commission = 1;
	@Transient
	private static final double  limit_transaction = 3;
	
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
	private String destination;
	@Getter
	@Setter
	@Field
	private String date;
	@Getter
	@Setter
	@Field
	private boolean pay_commission;
	@Getter
	@Setter
	@Field
	private double amount_commission;
	
	public boolean operationType(RequestClientDto cli)
	{
		boolean result = false;
		log.info("Entrando al método operationType en la clase Movements");
		//Transacciones bancarias
		if(this.type.equals("T"))
		{
			log.info("Se identificó el movimiento como una transacción bancaria");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getIddetail().equals(this.iddetail))
			.findAny().get();
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			return resultBankWithdrawals(prod, cli);
		}
		//Depósitos bancarios a la misma cuenta
		if(this.type.equals("D"))
		{
			log.info("Se identificó el movimiento como un depósito bancario");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getIddetail().equals(this.iddetail))
			.findAny().get();
			return resultBankDeposit(prod, cli);
		}
		//Retiros con tajeta de débito
		if(this.type.equals("RTD"))
		{
			log.info("Se identificó el movimiento como un retiro bancario mediante una tarjeta de crédito.");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getIddetail().equals(this.iddetail)).findAny().orElseGet(null);
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			log.info("Buscar la cuenta principal.");
			ProductDto prodCB = cli.getDetail().stream().filter(value -> 
			value.getBank_account().equals(prod.getBank_account())).findAny().orElseGet(null);
			log.info("Realizar el movimiento con la cuenta bancaria principal.");
			result = resultBankWithdrawals(prodCB, cli);
			if(!result)
			{
				log.info("Realizar el movimiento con las cuentas bancarias alternas.");
				if(prod.getBank_accounts()!=null)
				{
					if(!prod.getBank_accounts().isEmpty())
					{
						for (var account : prod.getBank_accounts()) {
							ProductDto prodCBAlt = cli.getDetail().stream().filter(value -> 
							value.getBank_account().equals(account.getAccount())).findAny().orElseGet(null);
							if(!result)
							{
								log.info("Realizar el movimiento con la cuenta bancaria alterna " + prodCBAlt.getBank_account());
								result = resultBankWithdrawals(prodCB, cli);
							}
						}
					}	
				}
			}
			return result;
		}
		//Retiros bancarios
		if(this.type.equals("R"))
		{
			log.info("Se identificó el movimiento como un retiro bancario");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getIddetail().equals(this.iddetail))
			.findAny().get();
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			return resultBankWithdrawals(prod, cli);
		}
		//Pagos de créditos con tarjeta de debito
		if(this.type.equals("PCTD"))
		{
			log.info("Se identificó el movimiento como un pago de crédito mediante una tarjeta de débito.");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getIddetail().equals(this.iddetail)).findAny().orElseGet(null);
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			log.info("Buscar la cuenta principal.");
			ProductDto prodCB = cli.getDetail().stream().filter(value -> 
			value.getBank_account().equals(prod.getBank_account())).findAny().orElseGet(null);
			result = resultBankWithdrawals(prodCB, cli);
			if(!result)
			{
				log.info("Realizar el movimiento con las cuentas bancarias alternas.");
				if(prod.getBank_accounts()!=null)
				{
					if(!prod.getBank_accounts().isEmpty())
					{
						for (var account : prod.getBank_accounts()) {
							ProductDto prodCBAlt = cli.getDetail().stream().filter(value -> 
							value.getBank_account().equals(account.getAccount())).findAny().orElseGet(null);
							if(!result)
							{
								log.info("Realizar el movimiento con la cuenta bancaria alterna " + prodCBAlt.getBank_account());
								result = resultBankWithdrawals(prodCBAlt, cli);
							}
						}
					}
				}
			}
			return result;
		}
		//Pagos de créditos
		if(this.type.equals("PC"))
		{
			log.info("Se identificó el movimiento como un pago de crédito");
			ProductDto prod = cli.getDetail().stream().filter(value -> 
			value.getId().equals(this.idproduct)
			&& 
			value.getIddetail().equals(this.iddetail))
			.findAny().get();
			return resultBankWithdrawalsPayCredits(prod, cli);
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
				log.info("Crédito actual: " + cli.getDetail().get(index).getCredit());
				cli.getDetail().remove(index);
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
	
	//Método auxiliar
	private boolean resultBankWithdrawalsPayCredits(ProductDto prod, RequestClientDto cli)
	{
		boolean result = false;
		var index = cli.getDetail().indexOf(prod);			
		if(filterTypeBankAccount(prod))
		{
			log.info("Nombre y Apellidos del registro del cliente que se modificará: " + cli.getName() + " " + cli.getLastname());
			if(prod.getCredit()-this.amount>=0)
			{
				prod.setCredit(prod.getCredit()-this.amount);
				log.info("Se modificó la cantidad de la deuda de su crédito");
				log.info("Crédito actual a pagar: " + cli.getDetail().get(index).getCredit());
				cli.getDetail().remove(index);
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
		return result;
	}
	
	//Método auxiliar
	private boolean resultBankWithdrawals(ProductDto prod, RequestClientDto cli)
	{
		boolean result = false;
		var index = cli.getDetail().indexOf(prod);			
		if(filterTypeBankAccount(prod))
		{
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
				cli.getDetail().remove(index);
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
		return result;
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
	
}
