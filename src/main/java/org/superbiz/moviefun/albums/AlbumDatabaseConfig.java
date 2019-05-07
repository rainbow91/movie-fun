package org.superbiz.moviefun.albums;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.superbiz.moviefun.DatabaseServiceCredentials;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class AlbumDatabaseConfig {

    @Bean
    public HibernateJpaVendorAdapter jpaVendorAdapter(DatabaseServiceCredentials serviceCredentials) {
        HibernateJpaVendorAdapter adapter =
                new HibernateJpaVendorAdapter();
        adapter.setDatabase(Database.MYSQL);
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setGenerateDdl(true);
        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBea(DataSource ds,  HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean factoryBean =
                new LocalContainerEntityManagerFactoryBean();
        factoryBean.setJtaDataSource(ds);
        factoryBean.setJpaVendorAdapter(adapter);
        factoryBean.setPackagesToScan("org.superbiz.moviefun.albums");
        factoryBean.setPersistenceUnitName("album-mysql");
        return factoryBean;
    }


}
