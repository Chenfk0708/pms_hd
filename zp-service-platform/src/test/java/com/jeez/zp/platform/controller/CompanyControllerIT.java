package com.jeez.zp.platform.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CompanyControllerIT {

    private static final String AUTH_VERIFIED_HEADER = "X-Auth-Verified";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String CAMP_ID = "10001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Timeout(60)
    void companyInfoSave_shouldUpsertProfileAndGetSavedProfile() throws Exception {
        mockMvc.perform(post("/company/info/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"%s",
                                  "profile":{
                                    "name":"Platform Test Company",
                                    "type":"hotel",
                                    "phone":"13900000001",
                                    "city":"Shenzhen / Nanshan",
                                    "address":"Test Road 1001"
                                  }
                                }
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("Platform Test Company"))
                .andExpect(jsonPath("$.data.type").value("hotel"))
                .andExpect(jsonPath("$.data.phone").value("13900000001"))
                .andExpect(jsonPath("$.data.city").value("Shenzhen / Nanshan"))
                .andExpect(jsonPath("$.data.address").value("Test Road 1001"));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT company_name
                FROM company_profile
                WHERE camp_id = 10001 AND is_deleted = 0
                """, String.class)).isEqualTo("Platform Test Company");

        mockMvc.perform(post("/company/info/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"%s","includeImages":true}
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("Platform Test Company"))
                .andExpect(jsonPath("$.data.images").isArray());
    }

    @Test
    @Timeout(60)
    void companyQualificationSave_shouldPersistProfileAndLegalIdentity() throws Exception {
        mockMvc.perform(post("/company/qualification/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"%s",
                                  "profile":{
                                    "name":"Qualification Test Company",
                                    "type":"homestay",
                                    "phone":"13900000002",
                                    "city":"Guangzhou / Tianhe",
                                    "address":"Qualification Road 2002"
                                  },
                                  "legalIdentity":{
                                    "documentType":"resident_id_card",
                                    "documentNumber":"ID-TEST-2002"
                                  },
                                  "legalPersonName":"Legal Tester",
                                  "legalPersonIdNumber":"ID-TEST-2002"
                                }
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.profile.name").value("Qualification Test Company"))
                .andExpect(jsonPath("$.data.legalIdentity.documentType").value("resident_id_card"))
                .andExpect(jsonPath("$.data.legalIdentity.documentNumber").value("ID-TEST-2002"))
                .andExpect(jsonPath("$.data.businessLicenses[0].id").value("businessLicense"));

        assertThat(jdbcTemplate.queryForObject("""
                SELECT company_name
                FROM company_profile
                WHERE camp_id = 10001 AND is_deleted = 0
                """, String.class)).isEqualTo("Qualification Test Company");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT document_number
                FROM company_qualification
                WHERE camp_id = 10001 AND document_type = 'resident_id_card' AND is_deleted = 0
                """, String.class)).isEqualTo("ID-TEST-2002");
    }

    @Test
    @Timeout(60)
    void companyQualificationUpload_shouldCreateAssetAndReturnUpdatedViewModel() throws Exception {
        JsonNode qualification = saveQualification("Upload Test Company", "UPLOAD-ID-3003");
        assertThat(qualification.path("legalIdentity").path("documentNumber").asText()).isEqualTo("UPLOAD-ID-3003");

        MvcResult uploadResult = mockMvc.perform(post("/company/qualification/upload")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"%s",
                                  "target":"businessLicense",
                                  "fileName":"business-license-test.png",
                                  "kind":"image",
                                  "sizeLabel":"2.0MB"
                                }
                                """.formatted(CAMP_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.file.name").value("business-license-test.png"))
                .andExpect(jsonPath("$.data.file.kind").value("image"))
                .andExpect(jsonPath("$.data.viewModel.businessLicenses[0].files[0].name").value("business-license-test.png"))
                .andReturn();

        String assetId = objectMapper.readTree(uploadResult.getResponse().getContentAsByteArray())
                .at("/data/file/id")
                .asText();
        assertThat(assetId).isNotBlank();
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM company_qualification_asset
                WHERE camp_id = 10001 AND asset_type = 'businessLicense' AND file_name = 'business-license-test.png'
                """, Integer.class)).isEqualTo(1);
    }

    @Test
    @Timeout(60)
    void companyInfoGet_shouldRejectOtherCamp() throws Exception {
        mockMvc.perform(post("/company/info/get")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campId":"10002","includeImages":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40301));
    }

    private JsonNode saveQualification(String companyName, String documentNumber) throws Exception {
        MvcResult result = mockMvc.perform(post("/company/qualification/save")
                        .header(AUTH_VERIFIED_HEADER, "true")
                        .header(USER_ID_HEADER, "12001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "campId":"%s",
                                  "profile":{
                                    "name":"%s",
                                    "type":"homestay",
                                    "phone":"13900000003",
                                    "city":"Shenzhen / Futian",
                                    "address":"Upload Road 3003"
                                  },
                                  "legalIdentity":{
                                    "documentType":"resident_id_card",
                                    "documentNumber":"%s"
                                  }
                                }
                                """.formatted(CAMP_ID, companyName, documentNumber)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data");
    }
}
