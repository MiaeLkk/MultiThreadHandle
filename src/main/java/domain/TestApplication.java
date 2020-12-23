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
		List<Integer> list = new ArrayList<Integer>();
		try {
			for(int i = 0 ; i < 1000 ; i++) {
				list.add(i);
			}
			
			new MultiThreadUtil<Integer>(3, list, 1000L, 3, new Function<Integer, Boolean>() {

				@Override
				public Boolean apply(Integer obj) {
					int ran = new Random().nextInt(10);
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
