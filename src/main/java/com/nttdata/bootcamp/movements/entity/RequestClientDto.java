package com.nttdata.bootcamp.movements.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Data;


@Data
public class RequestClientDto implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6180018095751335973L;
	private String id;
	private String name;
	private String lastname;
	private String dni;
	private String type;
	private List<ProductDto> detail;	
	
}
