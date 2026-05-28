package com.jeez.zp.order.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserCampMapper {

    @Select("""
            SELECT m.camp_id
            FROM pms_user u
            JOIN pms_member m ON m.user_id = u.user_id
                AND m.is_deleted = 0
            WHERE u.user_id = #{userId}
              AND u.is_deleted = 0
            LIMIT 1
            """)
    Long selectCurrentCampId(@Param("userId") Long userId);
}
