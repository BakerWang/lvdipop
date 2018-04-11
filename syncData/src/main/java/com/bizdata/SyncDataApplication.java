package com.bizdata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.bizdata.task.EntryTask;

@SpringBootApplication
@EnableScheduling
public class SyncDataApplication implements CommandLineRunner {

	@Autowired
	EntryTask entryTask;
	
	public static void main(String[] args) {
		SpringApplication.run(SyncDataApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		entryTask.initData();
	}
}
