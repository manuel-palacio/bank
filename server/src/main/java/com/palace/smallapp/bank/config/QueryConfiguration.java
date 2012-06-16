package com.palace.smallapp.bank.config;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.MultiMap;
import com.palace.smallapp.bank.Money;
import com.palace.smallapp.bank.Transaction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Uses a distributed map as the query model
 * @see "http://www.hazelcast.com/"
 */
@Configuration
public class QueryConfiguration {

    @Bean(name = "transactions")
    public MultiMap<String, Transaction> transactions() {
        return Hazelcast.getMultiMap("transactions");
    }

    @Bean(name = "accounts")
    public Map<String, Money> accounts() {
        return Hazelcast.getMap("accounts");
    }
}
