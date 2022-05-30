package com.nttdata.bootcamp.movements.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Data;


@Data
public class ProductDto implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9097733611599368397L;
	private String iddetail;
	private String id;
	private double cash;
	private String modality;
	private String bank_account;
	private String inter_bank_account;
	private String company;
	private double credit;
	private String fee;
	private double payperfee;
	private String credit_card;
	private String ccv;
	private String expiration_date;
	private String state;
	// mínimo saldo de promedio dia
	private double counter_transaction;
	// mínimo saldo de promedio diario mensual
	private double balance_day;
	private String deposit_date;
	private double limit_transaction;
	private String payment_date;
	//contador de pagos vencidos
	private double count_overdue_installments;
	private List<Accounts> bank_accounts;
	
}
