package utils;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author liukaiyi
 * @date 2020年12月21日
 * @param <T>
 */
@Slf4j
public class MultiThreadUtil<T> {
	
	private int index = 0;
	private static int times = 0;
	
	private static int retryTimes = 0;
	private static int errorSize = -1;
	private static int maxRetryTimes; 
	private static long sleepTime;
	
	private List<T> list = new ArrayList<T>();
	private CopyOnWriteArrayList<T> errorList = new CopyOnWriteArrayList<T>();
	
	/** 期望线程数 */
	private int amount;
	
	private Function<T, Boolean> func;
	
	private static int corePoolSize;
    private static int maxPoolSize;
    private static int queueCapacity;
    private static final Long KEEP_ALIVE_TIME = 1L;
    
    /**
     * 
     * @param amount	线程数量
     * @param list		待处理list
     * @param slpTime	尝试时间隔时间
     * @param maxRT		尝试次数，不包含特征匹配首次
     * @param func		处理方法，false表示需要重新处理
     */
    public MultiThreadUtil(Integer amount, List<T> list, Long slpTime,  Integer maxRT, Function<T, Boolean> func) {
    	this.amount = amount;
    	corePoolSize = amount;
    	maxPoolSize = amount * 3;
    	queueCapacity = amount * 30;
    	this.list = list;
    	this.func = func;
    	sleepTime = slpTime;
    	maxRetryTimes = maxRT;
	}
    
    public MultiThreadUtil(Integer amount, List<T> list, Function<T, Boolean> func) {
    	this(amount, list, 0L, -1, func);
	}
    
    public MultiThreadUtil(Integer amount, List<T> list, Long slpTime, Function<T, Boolean> func) {
    	this(amount, list, slpTime, -1, func);
	}
    
	public void begin(){
		int listSize = list.size();
		
		/** 实际线程数 */
		int collectionSize = amount < listSize ? amount : listSize;
		List<List<T>> listCollection = new ArrayList<List<T>>(collectionSize);
		
		for(int i = 0 ; i < collectionSize; i ++) {
			listCollection.add(new ArrayList<T>(listSize / collectionSize + 1));
		}
		
		for(int i = 0 ; i < listSize ; i++) {
			listCollection.get(i % collectionSize).add(list.get(i));
		}
		
		
		//通过ThreadPoolExecutor构造函数自定义参数创建
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
        		corePoolSize,
        		maxPoolSize,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity),
                new ThreadPoolExecutor.CallerRunsPolicy());
        
        for(List<T> l : listCollection) {
            executor.execute(() -> {
            	for(T obj : l) {
            		
            		boolean flag = func.apply(obj);
            		if(!flag) {
						errorList.add(obj);
            		}
				}
				//发送完index+1
				synchronized (this) {
					index++;
					if(index == listCollection.size()) {//线程全部跑完
						
						boolean shutdown = false;
						int currentErrorSize = errorList.size();
						
						times++;
						log.info("第 "+times+" 次，剩余：{}", currentErrorSize);
						
							if(errorSize == currentErrorSize) {//表示此次没有进展
								retryTimes++;
								log.info("没有进展，尝试-"+retryTimes);
								if(maxRetryTimes >= 0) {//有限制次数控制
									if(retryTimes >= maxRetryTimes) {//达到重试次数，终止
										log.info("SHUTDOWN NOW!!!");
										log.error("BE SHUTDOWN!!!");
										executor.shutdownNow();
										shutdown = true;
									}
								}
//								else {//没有限制次数控制
//									
//								}
							}else {
								errorSize = currentErrorSize;
								retryTimes = 0;
							}
//						}
						
						if(!shutdown && currentErrorSize > 0) {//还有error数据，继续
							index = 0;
							list = errorList;
							errorList = new CopyOnWriteArrayList<T>();
							if(sleepTime > 0L && retryTimes > 0) {
								try {
									Thread.sleep(sleepTime);
								} catch (InterruptedException e) {
									log.error(PrintUtil.printExce(e));
								}
							}
							new MultiThreadUtil<T>(amount, list, sleepTime, maxRetryTimes, func).begin();
						}else {
							log.info("FINISH!!!");
						}
					}
				}
            });
        }
        //终止线程池
        executor.shutdown();
        //不使用强制终止线程，线程只能自行结束
//        try {
//            if(!executor.awaitTermination(5, TimeUnit.SECONDS)) {
//            	executor.shutdownNow();
//            }
//        } catch (InterruptedException e) {
//        	log.error(PrintUtil.printExce(e));
//        }
	}

}
