package test.job;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class MissedTaskJob implements SimpleJob {
    @SneakyThrows
    @Override
    public synchronized void execute(ShardingContext shardingContext) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = simpleDateFormat.format(new Date());
        log.info("this context is:" + shardingContext
                + ", now time is :" + date);        log.info("thread start sleep");
        long startTime = System.currentTimeMillis();
        while (true) {
            long endTime = System.currentTimeMillis();
            //等待两分钟
            if ((endTime - startTime) / 1000 > 120) {
                break;
            }
        }
        log.info("thread stop sleep");
    }
}
