package com.nttdata.bootcamp.movements.entity;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDelete implements Serializable {
private static final long serialVersionUID = 587154910569029836L;
private String response;
}
