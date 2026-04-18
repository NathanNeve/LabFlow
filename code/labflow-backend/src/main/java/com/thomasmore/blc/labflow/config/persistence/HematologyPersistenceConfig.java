package com.thomasmore.blc.labflow.config.persistence;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.thomasmore.blc.labflow.repository.hematology",
        entityManagerFactoryRef = "hematologyEntityManagerFactory",
        transactionManagerRef = "hematologyTransactionManager"
)
public class HematologyPersistenceConfig {

    @Value("${hematology.datasource.url}")
    private String jdbcUrl;

    @Value("${labflow.jpa.ddl-auto:update}")
    private String ddlAuto;

    @Value("${labflow.jpa.show-sql:true}")
    private boolean showSql;

    @Bean(name = "hematologyDataSource")
    @Primary
    public DataSource hematologyDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setDriverClassName("org.sqlite.JDBC");
        return ds;
    }

    @Bean(name = "hematologyEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean hematologyEntityManagerFactory(
            @Qualifier("hematologyDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("com.thomasmore.blc.labflow.entity.hematology");
        em.setPersistenceUnitName("hematology");
        HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendor);
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", ddlAuto);
        props.put("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");
        props.put("hibernate.show_sql", showSql);
        em.setJpaPropertyMap(props);
        return em;
    }

    @Bean(name = "hematologyTransactionManager")
    @Primary
    public PlatformTransactionManager hematologyTransactionManager(
            @Qualifier("hematologyEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
