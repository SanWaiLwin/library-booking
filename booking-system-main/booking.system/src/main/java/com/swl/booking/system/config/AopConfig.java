package com.swl.booking.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.swl.booking.system.ascept.LoggingAspect;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.ComponentScan;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)   // Use CGLIB proxy instead of JDK dynamic proxy
@ComponentScan(basePackages = "com.swl.booking.system")
public class AopConfig {

    @Bean
    public DefaultAdvisorAutoProxyCreator autoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);  // Use CGLIB proxy (for class-based proxy)
        return creator;
    }
    
    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }
}
