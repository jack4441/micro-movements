package com.nttdata.bootcamp.movements.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class RequestMovementsDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2002812484046211313L;
	private Movements movements;
	
	public Movements toMovements()
	{
		return Movements.builder()
				.id(this.movements.getId())
				.idcliente(this.movements.getIdcliente())
				.idproduct(this.movements.getIdproduct())
				.bank_account(this.movements.getBank_account())
				.fullname(this.movements.getFullname())
				.credit_card(this.movements.getCredit_card())
				.amount(this.movements.getAmount())
				.destination(this.movements.getDestination())
				.pay_commission(this.movements.isPay_commission())
				.amount_commission(this.movements.getAmount_commission())
				.type(this.movements.getType())
				.date(this.movements.getDate())
				.build();
	}
	
	public CashHistory toCashHistory() {
		return CashHistory.builder()
				.idcliente(this.movements.getIdcliente())
				.bank_account(this.movements.getBank_account())
				.fullname(this.movements.getFullname())
				.credit_card(this.movements.getCredit_card())
				.amount(this.movements.getAmount())
				.destination(this.movements.getDestination())
				.type(this.movements.getType())
				.date(this.movements.getDate())
				.pay_commission(this.movements.isPay_commission())
				.amount_commission(this.movements.getAmount_commission())
				.build();
	}
	
}
