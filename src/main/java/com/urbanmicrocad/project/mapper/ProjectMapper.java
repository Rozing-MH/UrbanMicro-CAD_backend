package com.urbanmicrocad.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.urbanmicrocad.project.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 查询工程并加行锁（SELECT ... FOR UPDATE），用于写操作事务内防止并发修改。
     * 必须在 @Transactional 事务内调用，否则 FOR UPDATE 无意义。
     */
    @Select("SELECT * FROM prj_project WHERE id = #{id} AND user_id = #{userId} AND is_deleted = false FOR UPDATE")
    Project selectOneForUpdate(@Param("id") java.util.UUID id, @Param("userId") Long userId);
}
