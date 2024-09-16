package com.krishna.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public interface SnapShotService {

	List<Map<Object,Object>> getBuWiseMarginSnapShot(String accessToken, Long to, Long from,String businessVertical);

	Map<Object, Object> getBuWiseReserveSnapShot(String accessToken, Long to, Long from, String businessVertical);


	List<Object> getMarginSnapShot(String accessToken, long to, long from, long projectId, String businessVertical);

	void sendMailOnReserveChange(String accessToken);

}
