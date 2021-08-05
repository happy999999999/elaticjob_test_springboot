package test.job;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class MissedTaskJob implements SimpleJob {
    @SneakyThrows
    @Override
    public void execute(ShardingContext shardingContext) {
        log.info(this.getClass().getName()+",this context is:"+ shardingContext+", now time is :"+new Date(System.currentTimeMillis()));
        log.info("thread start sleep");
        long startTime = System.currentTimeMillis();
        while(true){
            long endTime = System.currentTimeMillis();
            if ((endTime-startTime)/1000>6){
                break;
            }
        }
        log.info("thread stop sleep");
    }
}
