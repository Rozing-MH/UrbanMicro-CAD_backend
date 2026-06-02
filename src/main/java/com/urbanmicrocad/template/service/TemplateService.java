package com.urbanmicrocad.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.template.converter.TemplateConverter;
import com.urbanmicrocad.template.dto.SaveCustomTemplateRequest;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.entity.ProjectTemplate;
import com.urbanmicrocad.template.mapper.TemplateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TemplateService implements ITemplateService {
    private static final String ROAD_SECTION_CATEGORY = "ROAD_SECTION";
    private static final String CUSTOM_CATEGORY = "CUSTOM";
    private static final UUID DEFAULT_TWO_LANE_TEMPLATE_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID ARTERIAL_BUS_TEMPLATE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");
    private static final UUID COMPLETE_STREET_TEMPLATE_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");
    private static final Set<String> VALID_CATEGORIES = Set.of(
        "BASIC_INTERSECTION",
        "CLOVERLEAF",
        "TURBINE",
        "DIAMOND",
        "ROUNDABOUT",
        "CUSTOM"
    );

    private final TemplateMapper templateMapper;
    private final ObjectMapper objectMapper;
    private final TemplateConverter templateConverter;

    public TemplateService(TemplateMapper templateMapper, TemplateConverter templateConverter) {
        this.templateMapper = templateMapper;
        this.objectMapper = new ObjectMapper();
        this.templateConverter = templateConverter;
    }

    @Override
    public List<TemplateDTO> list(String category, CurrentUser user) {
        if (ROAD_SECTION_CATEGORY.equals(category)) {
            return listCrossSectionTemplates();
        }
        if (category != null && !category.isBlank() && !VALID_CATEGORIES.contains(category)) {
            return List.of();
        }
        LambdaQueryWrapper<ProjectTemplate> query = activeTemplateQuery();
        if (category != null && !category.isBlank()) {
            query.eq(ProjectTemplate::getCategory, category);
        }
        // 包含系统模板 (userId IS NULL) + 当前用户自定义模板
        if (user != null) {
            query.and(w -> w.isNull(ProjectTemplate::getUserId)
                .or(sub -> sub.eq(ProjectTemplate::getUserId, user.id())));
        } else {
            query.isNull(ProjectTemplate::getUserId);
        }
        return templateMapper.selectList(query).stream()
            .map(templateConverter::toDto)
            .toList();
    }

    @Override
    public TemplateDTO get(UUID id, CurrentUser user) {
        TemplateDTO crossSection = findCrossSectionTemplate(id);
        if (crossSection != null) {
            return crossSection;
        }
        LambdaQueryWrapper<ProjectTemplate> query = activeTemplateQuery().eq(ProjectTemplate::getId, id);
        if (user != null) {
            query.and(w -> w.isNull(ProjectTemplate::getUserId)
                .or(sub -> sub.eq(ProjectTemplate::getUserId, user.id())));
        } else {
            query.isNull(ProjectTemplate::getUserId);
        }
        ProjectTemplate template = templateMapper.selectOne(query);
        if (template == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "模板不存在");
        }
        return templateConverter.toDto(template);
    }

    /**
     * 保存用户自定义断面模板。category 强制设为 CUSTOM。
     */
    @Override
    public TemplateDTO saveCustomTemplate(CurrentUser user, SaveCustomTemplateRequest request) {
        validateProfile(request.profile());
        checkDuplicateName(user.id(), request.name());
        OffsetDateTime now = OffsetDateTime.now();
        ProjectTemplate template = new ProjectTemplate();
        template.setId(UUID.randomUUID());
        template.setName(request.name());
        template.setCategory(CUSTOM_CATEGORY);
        template.setSnapshotData(snapshotDataFromProfile(request.profile()));
        template.setThumbnailUrl("");
        template.setUserId(user.id());
        template.setCreatedAt(now);
        template.setUpdatedAt(now);
        template.setIsDeleted(false);
        templateMapper.insert(template);
        return templateConverter.toCustomDto(template, request.profile(), CUSTOM_CATEGORY);
    }

    private void validateProfile(JsonNode profile) {
        if (profile == null || !profile.isObject()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "profile 必须为有效 JSON 对象");
        }
        if (!profile.has("lanes") || !profile.get("lanes").isArray()) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "profile 必须包含 lanes 数组");
        }
    }

    private void checkDuplicateName(Long userId, String name) {
        boolean exists = templateMapper.selectCount(new LambdaQueryWrapper<ProjectTemplate>()
            .eq(ProjectTemplate::getUserId, userId)
            .eq(ProjectTemplate::getName, name)
            .eq(ProjectTemplate::getIsDeleted, false)) > 0;
        if (exists) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "已存在同名自定义模板");
        }
    }

    /**
     * 软删除用户自定义模板。仅模板所有者可删除，系统模板不可删除。
     */
    @Override
    @Transactional
    public void deleteCustomTemplate(CurrentUser user, UUID id) {
        ProjectTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<ProjectTemplate>()
            .eq(ProjectTemplate::getId, id)
            .eq(ProjectTemplate::getIsDeleted, false));
        if (template == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "模板不存在");
        }
        if (template.getUserId() == null) {
            throw new ApiException(ErrorCode.FORBIDDEN, "系统模板不可删除");
        }
        if (!template.getUserId().equals(user.id())) {
            throw new ApiException(ErrorCode.FORBIDDEN, "只能删除自己创建的模板");
        }
        template.setIsDeleted(true);
        template.setUpdatedAt(OffsetDateTime.now());
        templateMapper.updateById(template);
    }

    private JsonNode snapshotDataFromProfile(JsonNode profile) {
        ObjectNode snapshotData = objectMapper.createObjectNode();
        snapshotData.set("profile", profile.deepCopy());
        return snapshotData;
    }

    @Override
    public List<TemplateDTO> listCrossSectionTemplates() {
        return List.of(
            crossSectionTemplate(
                DEFAULT_TWO_LANE_TEMPLATE_ID,
                "默认双车道",
                profile(
                    "default-2lane",
                    "Default 2-lane road",
                    lanes(
                        lane("l1", 3.5, "CAR", "FORWARD"),
                        lane("l2", 3.5, "CAR", "BACKWARD")
                    ),
                    median(0, "NONE"),
                    sidewalk(1.5, 1.5),
                    10
                )
            ),
            crossSectionTemplate(
                ARTERIAL_BUS_TEMPLATE_ID,
                "城市主干路（含公交车道）",
                profile(
                    "arterial-4lane-bus",
                    "4-lane arterial with bus lanes",
                    lanes(
                        lane("l1", 3.5, "BUS", "FORWARD"),
                        lane("l2", 3.5, "CAR", "FORWARD"),
                        lane("l3", 3.5, "CAR", "BACKWARD"),
                        lane("l4", 3.5, "BUS", "BACKWARD")
                    ),
                    median(1.5, "GRASS"),
                    sidewalk(2, 2),
                    19.5
                )
            ),
            crossSectionTemplate(
                COMPLETE_STREET_TEMPLATE_ID,
                "完整街道（慢行友好）",
                profile(
                    "complete-street-2lane-bike",
                    "Complete street with bike lanes",
                    lanes(
                        lane("l1", 1.8, "BIKE", "FORWARD"),
                        lane("l2", 3.25, "CAR", "FORWARD"),
                        lane("l3", 3.25, "CAR", "BACKWARD"),
                        lane("l4", 1.8, "BIKE", "BACKWARD")
                    ),
                    median(0, "NONE"),
                    sidewalk(2.5, 2.5),
                    15.1
                )
            )
        );
    }

    @Override
    public List<JsonNode> listCrossSections() {
        return listCrossSectionTemplates().stream()
            .map(TemplateDTO::profile)
            .toList();
    }

    @Override
    public JsonNode getCrossSection(String id) {
        return listCrossSectionTemplates().stream()
            .map(TemplateDTO::profile)
            .filter(p -> id.equals(p.get("id").asText()))
            .findFirst()
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "断面模板不存在"));
    }

    private TemplateDTO findCrossSectionTemplate(UUID id) {
        return listCrossSectionTemplates().stream()
            .filter(template -> template.id().equals(id))
            .findFirst()
            .orElse(null);
    }

    private LambdaQueryWrapper<ProjectTemplate> activeTemplateQuery() {
        return new LambdaQueryWrapper<ProjectTemplate>()
            .eq(ProjectTemplate::getIsDeleted, false)
            .orderByDesc(ProjectTemplate::getUpdatedAt);
    }

    private TemplateDTO crossSectionTemplate(UUID id, String name, JsonNode profile) {
        ObjectNode snapshotData = objectMapper.createObjectNode();
        snapshotData.set("profile", profile.deepCopy());
        return new TemplateDTO(id, name, ROAD_SECTION_CATEGORY, snapshotData, "", profile);
    }

    private ObjectNode profile(
        String id,
        String name,
        ArrayNode lanes,
        ObjectNode median,
        ObjectNode sidewalk,
        double totalWidth
    ) {
        ObjectNode profile = objectMapper.createObjectNode();
        profile.put("id", id);
        profile.put("name", name);
        profile.set("lanes", lanes);
        profile.set("median", median);
        profile.set("sidewalk", sidewalk);
        profile.put("totalWidth", totalWidth);
        return profile;
    }

    private ArrayNode lanes(ObjectNode... lanes) {
        ArrayNode laneArray = objectMapper.createArrayNode();
        for (ObjectNode lane : lanes) {
            laneArray.add(lane);
        }
        return laneArray;
    }

    private ObjectNode lane(String id, double width, String type, String direction) {
        ObjectNode lane = objectMapper.createObjectNode();
        lane.put("id", id);
        lane.put("width", width);
        lane.put("type", type);
        lane.put("direction", direction);
        return lane;
    }

    private ObjectNode median(double width, String type) {
        ObjectNode median = objectMapper.createObjectNode();
        median.put("width", width);
        median.put("type", type);
        return median;
    }

    private ObjectNode sidewalk(double leftWidth, double rightWidth) {
        ObjectNode sidewalk = objectMapper.createObjectNode();
        sidewalk.put("leftWidth", leftWidth);
        sidewalk.put("rightWidth", rightWidth);
        return sidewalk;
    }
}
