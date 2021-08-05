package test.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class MyJob implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        log.info(this.getClass().getName() + ",this context is:" + shardingContext
                + ", now time is :" + new Date(System.currentTimeMillis()));
    }
}
