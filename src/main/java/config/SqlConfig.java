package config;

import entity.CarEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import repository.CarRepository;

import javax.sql.DataSource;
import java.util.Properties;


@Configuration
@PropertySource("classpath:sql-config.properties")
@EnableJpaRepositories(basePackageClasses = CarRepository.class)
    public class SqlConfig {


        @Value("${database.host}")
        private String host;

        @Value("${database.database}")
        private String database;

        @Value("${database.user}")
        private String user;

        @Value("${database.password}")
        private String password;

    @Bean
    @Profile("testH2")
    public DataSource testDataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        builder.setType(EmbeddedDatabaseType.H2).addScripts("sql/structure.sql", "sql/data.sql");
        return builder.build();
    }

        @Bean
        @Profile("!testH2")
        public DataSource postgresql() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl("jdbc:postgresql://" + host + ":5432/" + database);
            dataSource.setUsername(user);
            dataSource.setPassword(password);
            return dataSource;
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

            entityManagerFactoryBean.setDataSource(dataSource);
            entityManagerFactoryBean.setPackagesToScan(CarEntity.class.getPackage().getName());
            entityManagerFactoryBean.setPersistenceUnitName("myPersistenceUnit");

            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);

            Properties properties = new Properties();
            properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            entityManagerFactoryBean.setJpaProperties(properties);
            entityManagerFactoryBean.afterPropertiesSet();

            return entityManagerFactoryBean;
        }

        @Bean
        public PlatformTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean entityManagerFactory) {
            JpaTransactionManager transactionManager = new JpaTransactionManager();
            transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());

            return transactionManager;
        }



    }
