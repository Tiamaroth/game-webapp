package com.webapp.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.hazelcast.aws.AwsDiscoveryStrategy;
import com.hazelcast.aws.AwsDiscoveryStrategyFactory;
import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;

@Configuration
@ConfigurationProperties("aws.key")
public class HazelcastConfiguration {
	@Value("${aws.key.access}")
	String awsKey;
	
	@Value("${aws.key.secret}")
	String awsSecret;
	
	@Profile("dev")
	@Bean
    public Config hazelCastConfig(){
		Config config = new Config();
        config.setInstanceName("hazelcast-instance")
                .addMapConfig(
                        new MapConfig()
                                .setName("configuration")
                                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                                .setEvictionPolicy(EvictionPolicy.LRU)
                                .setTimeToLiveSeconds(-1));
        return config;
    }
	
	@Profile("test")
	@Bean
    public Config hazelCastConfigAws(){
		Config config = new Config();
        config.setInstanceName("hazelcast-instance")
                .addMapConfig(
                        new MapConfig()
                                .setName("configuration")
                                .setMaxSizeConfig(new MaxSizeConfig(200, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                                .setEvictionPolicy(EvictionPolicy.LRU)
                                .setTimeToLiveSeconds(-1));
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(true)
        .setProperty("access-key", awsKey)
        .setProperty("secret-key", awsSecret)
        .setProperty("region", "eu-west-3")
        .setProperty("hz-port", "5701-5708");
        
        return config;
    }
}
