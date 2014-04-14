package com.hightail.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.Random;

/**
 * Created by hightail on 4/10/14.
 */
public class MockFileScanner {

    //measuring the dummy file scanner
    public static void scanFiles(MetricRegistry registry) {

        while(true) {

            Timer timer = registry.timer("com.hightail.kaspersky.scan.file");
            Timer.Context context = timer.time();

            try {
                scan();
            }
            catch (Exception ex) {}
            finally {
                context.stop();
            }
        }
    }

    //scanning a single file (dummy - sleeps randomly for [0 - 100)ms )
    private static void scan() throws InterruptedException{
        Random random = new Random();
        int sleepTime = random.nextInt(100);
        Thread.sleep(1+sleepTime);
    }
}
