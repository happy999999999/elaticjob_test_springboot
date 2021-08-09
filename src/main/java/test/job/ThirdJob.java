package test.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.dataflow.job.DataflowJob;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ThirdJob implements DataflowJob {
    @Override
    public List fetchData(ShardingContext shardingContext) {
        log.info("sharding item = {},start fetch data", shardingContext.getShardingItem());
        return Arrays.asList("jack", "mack", "dasi");
    }

    @Override
    public void processData(ShardingContext shardingContext, List list) {
        log.info("sharding item = {},list={}", shardingContext.getShardingItem(), list.toString());
    }
}
