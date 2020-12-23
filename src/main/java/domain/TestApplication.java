package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import utils.MultiThreadUtil;
import utils.PrintUtil;

@Slf4j
public class TestApplication {

	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		try {
			for(int i = 0 ; i < 100000 ; i++) {
				list.add(""+i);
			}
			
			new MultiThreadUtil<String>(1000, list, 1000L, 3, new Function<String, Boolean>() {

				@Override
				public Boolean apply(String obj) {
					int ran = new Random().nextInt(3);
					log.info(obj+":"+ran);
					if(ran == 0) {
						return true;
					}
					return false;
				}
			}).begin();
		} catch (Exception e) {
			log.error(PrintUtil.printExce(e));
		}
	}

}
