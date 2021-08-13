package test.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class MyJob implements SimpleJob {

    @Override
    public void execute(ShardingContext shardingContext) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = simpleDateFormat.format(new Date());
        log.info("this context is:" + shardingContext
                + ", now time is :" + date);
    }
}
