package com.educationcertificationsystem.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;


@Configuration(proxyBeanMethods = false)
@MapperScan(basePackages = {
    "com.educationcertificationsystem.mapper",
    "com.educationcertificationsystem.ai.mapper",
    "com.educationcertificationsystem.course.mapper",
    "com.educationcertificationsystem.eval.mapper",
    "com.educationcertificationsystem.file.mapper",
    "com.educationcertificationsystem.improve.mapper",
    "com.educationcertificationsystem.notice.mapper",
    "com.educationcertificationsystem.org.mapper",
    "com.educationcertificationsystem.program.mapper",
    "com.educationcertificationsystem.report.mapper",
    "com.educationcertificationsystem.role.mapper",
    "com.educationcertificationsystem.survey.mapper",
    "com.educationcertificationsystem.system.mapper",
    "com.educationcertificationsystem.user.mapper"
})
public class MybatisPlusConfig {

    @Bean
    @ConditionalOnMissingBean(SqlSessionFactory.class)
    public SqlSessionFactory sqlSessionFactory(
            DataSource dataSource,
            ObjectProvider<Interceptor[]> interceptorsProvider,
            @Value("${mybatis-plus.mapper-locations:classpath*:com/educationcertificationsystem/mapper/*.xml}")
            String mapperLocations) throws Exception {

        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources(mapperLocations));
        Interceptor[] interceptors = interceptorsProvider.getIfAvailable();
        if (interceptors != null && interceptors.length > 0) {
            factoryBean.setPlugins(interceptors);
        }

        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);
        factoryBean.setTypeAliasesPackage("com.educationcertificationsystem.model.entity");

        return factoryBean.getObject();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
