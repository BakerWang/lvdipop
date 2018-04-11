package com.bizdata.service;

import java.util.Date;
import java.util.List;

import com.bizdata.entity.CustomerCrm;

public interface CustomerCrmService {

	public List<CustomerCrm> queryList();

	public List<CustomerCrm> queryListByCmmaintdate(Date date);

	public List<String> queryAllCmmemid();
}
