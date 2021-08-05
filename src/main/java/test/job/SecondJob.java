package test.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class SecondJob implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        //do something
        log.info(this.getClass().getName()+",this context is:"+ shardingContext
                +", now time is :"+new Date(System.currentTimeMillis()));
        //通过区分当前分片来执行不同的任务
        switch (shardingContext.getShardingItem()) {
            case 0:
                // do something by sharding item 0
                break;
            case 1:
                // do something by sharding item 1
                break;
            case 2:
                // do something by sharding item 2
                break;
            // case n: ...
        }
    }
}
