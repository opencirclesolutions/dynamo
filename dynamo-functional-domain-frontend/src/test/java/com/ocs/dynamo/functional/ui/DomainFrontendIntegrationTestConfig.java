package com.ocs.dynamo.functional.ui;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ocs.dynamo.dao.BaseDao;
import com.ocs.dynamo.dao.impl.DefaultDaoImpl;
import com.ocs.dynamo.functional.domain.Country;
import com.ocs.dynamo.functional.domain.QCountry;
import com.ocs.dynamo.functional.domain.QRegion;
import com.ocs.dynamo.functional.domain.Region;
import com.ocs.dynamo.service.BaseService;
import com.ocs.dynamo.service.impl.DefaultServiceImpl;
import com.ocs.dynamo.ui.FrontendIntegrationTestConfig;

@Configuration
public class DomainFrontendIntegrationTestConfig extends FrontendIntegrationTestConfig {

    @Bean
    public BaseDao<Integer, Country> countryDao() {
        return new DefaultDaoImpl<>(QCountry.country, Country.class);
    }

    @Bean
    public BaseService<Integer, Country> countryService(BaseDao<Integer, Country> dao) {
        return new DefaultServiceImpl<>(dao, "code");
    }

    @Bean
    public BaseDao<Integer, Region> regionDao() {
        return new DefaultDaoImpl<>(QRegion.region, Region.class);
    }

    @Bean
    public BaseService<Integer, Region> regionService(BaseDao<Integer, Region> dao) {
        return new DefaultServiceImpl<>(dao, "code");
    }
}
