package com.suraj.document_processing_service.config;

import com.suraj.document_processing_service.properties.ProcessingProperties;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@RequiredArgsConstructor
public class AsyncProcessingConfig implements AsyncConfigurer {

    public static final String DOCUMENT_PROCESSING_EXECUTOR = "documentProcessingExecutor";

    private final ProcessingProperties properties;

    @Override
    @Bean(name = DOCUMENT_PROCESSING_EXECUTOR)
    public Executor getAsyncExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
