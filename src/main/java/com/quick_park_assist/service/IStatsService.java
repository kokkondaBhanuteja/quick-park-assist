package com.quick_park_assist.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
@Service
public interface IStatsService {

     Map<String, Object> getStatsForUser(Long userId);
     Map<String,Object> getSpotOwnerStats(Long userId);
     List<Map<String, Object>> getRecentActivityForUser(Long userId, int limit);
     List<Map<String, Object>> getRecentActivityForOwner(Long userId, int limit);

}
