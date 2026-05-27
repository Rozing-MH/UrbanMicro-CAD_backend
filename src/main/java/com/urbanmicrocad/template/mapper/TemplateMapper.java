package com.urbanmicrocad.template.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.urbanmicrocad.template.entity.ProjectTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TemplateMapper extends BaseMapper<ProjectTemplate> {
}
