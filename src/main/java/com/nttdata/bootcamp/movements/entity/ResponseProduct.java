package com.nttdata.bootcamp.movements.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseProduct {

	private static final long serialVersionUID = -9140856778152959609L;
	private String id;
	private String description;
	private String category;
	
	public ResponseProduct()
	{}
	
	public ResponseProduct(String id, String description, String category)
	{
		this.id=id;
		this.description=description;
		this.category=category;
	}
	
}
