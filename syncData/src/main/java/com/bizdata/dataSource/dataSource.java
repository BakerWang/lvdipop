package com.bizdata.dataSource;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class dataSource {
	final static Logger logger = LoggerFactory.getLogger(dataSource.class);

	@Bean(name = "sourceDataSource")
	@Qualifier("sourceDataSource")
	@Primary
	@ConfigurationProperties(prefix = "datasource.source")
	public DataSource destDataSource() {
		logger.info("sourceDataSource [oracle database] init ...");
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "destDataSource")
	@Qualifier("destDataSource")
	@ConfigurationProperties(prefix = "datasource.dest")
	public DataSource secondaryDataSource() {
		logger.info("destDataSource [mysql database] init ...");
		return DataSourceBuilder.create().build();
	}

	@Bean(name = "sourceJdbcTemplate")
	public JdbcTemplate sourceJdbcTemplate(@Qualifier("sourceDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean(name = "destJdbcTemplate")
	public JdbcTemplate destJdbcTemplate(@Qualifier("destDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

}
