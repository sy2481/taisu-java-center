package com.ruoyi.timer.mapper;



import com.ruoyi.timer.domain.DangerWork;
import com.ruoyi.timer.domain.TimInOutLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author 兴跃
 */
@Repository
public interface TimInOutLogMapper {

    List<TimInOutLog> getInOutLog(@Param("today") Date today, @Param("nextDay") Date nextDay,@Param("maxAid") Integer maxAid);
    List<TimInOutLog> getInOutLogExtend(@Param("today") Date today, @Param("nextDay") Date nextDay,@Param("maxAid") Integer maxAid);

    List<TimInOutLog> getInOutLogOrderByAid(@Param("today") Date today, @Param("nextDay") Date nextDay);

    List<DangerWork> getDangerData(@Param("oldTime") String oldTime,@Param("currentTime")  String currentTime);
}
