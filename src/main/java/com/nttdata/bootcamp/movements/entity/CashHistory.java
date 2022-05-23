package com.nttdata.bootcamp.movements.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Data;

@Document("cash_history")
@Data
@Builder
public class CashHistory {

	@Id
	private String id;
	@Field
	private String idcliente;
	@Field
	private String fullname;
	@Field
	private String bank_account;
	@Field
	private String credit_card;
	@Field
	private String type;
	@Field
	private double amount;
	@Field
	private double current_balance;
	@Field
	private String destination;
	@Field
	private String date;
	@Field
	private boolean pay_commission;
	@Field
	private double amount_commission;
	
	public void filterCasHistory(RequestClientDto cli_transmitter, RequestClientDto cli_receiver) {
		if(this.getType().equals("D")
				|| this.getType().equals("R"))
		{
			if(this.getType().equals("D"))
				cli_receiver.getDetail().stream().forEach(value-> {
					if(value.getBank_account().equals(this.getDestination())
							&& !value.getBank_account().equals(this.getBank_account()))
					{
						this.current_balance = ((value.getCash()+this.getAmount())-this.amount_commission);
					}
				});
			if(this.getType().equals("R"))
				cli_receiver.getDetail().stream().forEach(value-> {
					if(value.getBank_account().equals(this.getDestination())
							&& !value.getBank_account().equals(this.getBank_account()))
					{
						this.current_balance = ((value.getCash()-this.getAmount()));
					}
				});
			//if(this.type.equals("PC"))
				
		}
	}
	
}