package com.jeez.zp.platform.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.jeez.common.utils.InputValidationUtils;
import com.jeez.common.utils.PasswordUtil;
import com.jeez.zp.platform.dto.auth.AccountUpdateRequest;
import com.jeez.zp.platform.dto.auth.LoginRequest;
import com.jeez.zp.platform.dto.auth.RegisterRequest;
import com.jeez.zp.platform.entity.PmsMember;
import com.jeez.zp.platform.entity.PmsUser;
import com.jeez.zp.platform.exception.BusinessException;
import com.jeez.zp.platform.mapper.PlatformBootstrapMapper;
import com.jeez.zp.platform.mapper.PmsMemberMapper;
import com.jeez.zp.platform.mapper.PmsUserMapper;
import com.jeez.zp.platform.service.AuthService;
import com.jeez.zp.platform.vo.AuthLoginVO;
import com.jeez.zp.platform.vo.AuthMeVO;
import com.jeez.zp.platform.vo.CampSummaryVO;
import com.jeez.zp.platform.vo.CurrentUserBundleVO;
import com.jeez.zp.platform.vo.RegisterOptionsVO;
import com.jeez.zp.platform.vo.RoleSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEMO_LOGIN_PASSWORD = "demo-login";
    private static final long DEFAULT_REGISTER_CAMP_ID = 10001L;
    private static final int ACTIVE_STATUS = 1;
    private static final int NOT_DELETED = 0;
    private static final int INITIAL_VERSION = 0;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{2,31}$");

    private final PlatformBootstrapMapper platformBootstrapMapper;
    private final PmsUserMapper pmsUserMapper;
    private final PmsMemberMapper pmsMemberMapper;

    @Override
    public AuthLoginVO login(LoginRequest request) {
        if (request == null) {
            throw new BusinessException(40003, "账号不能为空");
        }

        String username = normalizeOptional(request.getUsername());
        String mobile = normalizeOptional(request.getMobile());
        String email = normalizeOptional(request.getEmail());
        if (!StringUtils.hasText(username) && !StringUtils.hasText(mobile) && !StringUtils.hasText(email)) {
            throw new BusinessException(40003, "账号不能为空");
        }

        CurrentUserBundleVO bundle = platformBootstrapMapper.selectUserByAccount(username, mobile, email);
        if (bundle == null) {
            throw new BusinessException(40001, "账号不存在");
        }
        if (!matchesCurrentPassword(bundle, request.getPassword())) {
            throw new BusinessException(40002, "密码错误");
        }

        StpUtil.login(bundle.getUserId());
        return AuthLoginVO.from(bundle, StpUtil.getTokenValue(), loadPermissionCodes(bundle));
    }

    @Override
    @Transactional
    public AuthLoginVO register(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException(40003, "注册信息不能为空");
        }

        String username = normalizeRequired(request.getUsername(), "登录账号");
        String password = normalizeRequired(request.getPassword(), "密码");
        String nickName = normalizeRequired(request.getNickName(), "姓名");
        String mobile = normalizeRequired(request.getMobile(), "手机号");
        String email = normalizeOptional(request.getEmail());
        Long campId = resolveRegisterCampId(request.getCampId());
        Long roleId = request.getRoleId();

        validateRegisterFields(username, password, nickName, mobile, email, roleId);
        ensureAccountUnique(username, mobile, email);

        RoleSummaryVO role = platformBootstrapMapper.selectActiveRole(campId, roleId);
        if (role == null) {
            throw new BusinessException(40003, "注册角色不存在");
        }

        Long userId = IdWorker.getId();
        PmsUser user = new PmsUser();
        user.setUserId(userId);
        user.setUsername(username);
        user.setMobile(mobile);
        user.setEmail(email);
        user.setNickName(nickName);
        user.setAreaCode("+86");
        user.setPasswordHash(PasswordUtil.encrypt(password));
        user.setStatus(ACTIVE_STATUS);
        user.setIsDeleted(NOT_DELETED);
        pmsUserMapper.insert(user);

        PmsMember member = new PmsMember();
        member.setMemberId(IdWorker.getId());
        member.setCampId(campId);
        member.setRoleId(roleId);
        member.setUserId(userId);
        member.setName(nickName);
        member.setMobile(mobile);
        member.setEmail(email);
        member.setStatus(ACTIVE_STATUS);
        member.setCreatedBy(userId);
        member.setUpdatedBy(userId);
        member.setIsDeleted(NOT_DELETED);
        member.setVersionNo(INITIAL_VERSION);
        pmsMemberMapper.insert(member);

        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(500, "注册账号上下文创建失败");
        }
        StpUtil.login(userId);
        return AuthLoginVO.from(bundle, StpUtil.getTokenValue(), loadPermissionCodes(bundle));
    }

    @Override
    public RegisterOptionsVO getRegisterOptions(Long campId) {
        Long resolvedCampId = resolveRegisterCampId(campId);
        CampSummaryVO camp = platformBootstrapMapper.selectCampSummary(resolvedCampId);
        if (camp == null) {
            throw new BusinessException(40003, "注册门店不存在");
        }
        return RegisterOptionsVO.builder()
                .campId(camp.getCampId())
                .campName(camp.getName())
                .roles(new ArrayList<>(platformBootstrapMapper.selectPublicRegisterRoles(resolvedCampId)))
                .build();
    }

    @Override
    public AuthMeVO getCurrentUser(Long userId) {
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }

        return AuthMeVO.from(bundle, loadPermissionCodes(bundle));
    }

    @Override
    @Transactional
    public AuthMeVO updateCurrentAccount(Long userId, AccountUpdateRequest request) {
        if (request == null) {
            throw new BusinessException(40003, "请求不能为空");
        }
        CurrentUserBundleVO bundle = platformBootstrapMapper.selectCurrentUserBundle(userId);
        if (bundle == null) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }

        String nickName = normalizeRequired(request.getNickName(), "nickName");
        String email = normalizeOptional(request.getEmail());
        String wechat = normalizeOptional(request.getWechat());
        String avatarUrl = normalizeOptional(request.getAvatarUrl());
        String passwordHash = null;

        if (!InputValidationUtils.isValidPersonName(nickName)) {
            throw new BusinessException(40003, "姓名格式不正确，请输入 2-30 个中文或英文字母");
        }
        if (!InputValidationUtils.isValidOptionalEmail(email)) {
            throw new BusinessException(40003, "邮箱格式不正确");
        }

        if (StringUtils.hasText(request.getNewPassword())) {
            if (!matchesCurrentPassword(bundle, request.getOldPassword())) {
                throw new BusinessException(40004, "原密码错误");
            }
            String newPassword = request.getNewPassword().trim();
            if (newPassword.length() < 6) {
                throw new BusinessException(40005, "新密码长度不能少于 6 位");
            }
            passwordHash = PasswordUtil.encrypt(newPassword);
        }

        int updated = platformBootstrapMapper.updateAccountProfile(
                userId,
                nickName,
                email,
                wechat,
                avatarUrl,
                passwordHash
        );
        if (updated != 1) {
            throw new BusinessException(40401, "当前用户上下文不存在");
        }

        platformBootstrapMapper.updateLinkedMemberProfile(userId, nickName, email);

        return getCurrentUser(userId);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    private boolean matchesCurrentPassword(CurrentUserBundleVO bundle, String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            return false;
        }
        if (StringUtils.hasText(bundle.getPasswordHash())) {
            return PasswordUtil.matches(rawPassword, bundle.getPasswordHash());
        }
        return DEMO_LOGIN_PASSWORD.equals(rawPassword);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(40003, fieldName + "不能为空");
        }
        return value.trim();
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void validateRegisterFields(
            String username,
            String password,
            String nickName,
            String mobile,
            String email,
            Long roleId
    ) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessException(40003, "登录账号需为 3-32 位字母、数字或下划线，且以字母开头");
        }
        if (password.length() < 6) {
            throw new BusinessException(40003, "密码长度不能少于 6 位");
        }
        if (!InputValidationUtils.isValidPersonName(nickName)) {
            throw new BusinessException(40003, "姓名格式不正确，请输入 2-30 个中文或英文字母");
        }
        if (!InputValidationUtils.isValidMainlandMobile(mobile)) {
            throw new BusinessException(40003, "手机号格式不正确");
        }
        if (!InputValidationUtils.isValidOptionalEmail(email)) {
            throw new BusinessException(40003, "邮箱格式不正确");
        }
        if (roleId == null) {
            throw new BusinessException(40003, "请选择角色");
        }
    }

    private void ensureAccountUnique(String username, String mobile, String email) {
        if (platformBootstrapMapper.countUsersByUsername(username) > 0) {
            throw new BusinessException(40003, "登录账号已存在");
        }
        if (platformBootstrapMapper.countUsersByMobile(mobile) > 0) {
            throw new BusinessException(40003, "手机号已存在");
        }
        if (email != null && platformBootstrapMapper.countUsersByEmail(email) > 0) {
            throw new BusinessException(40003, "邮箱已存在");
        }
    }

    private List<String> loadPermissionCodes(CurrentUserBundleVO bundle) {
        return platformBootstrapMapper.selectAuthorityCodesByRoleId(bundle.getRoleId());
    }

    private Long resolveRegisterCampId(Long campId) {
        if (campId != null) {
            return campId;
        }
        List<CampSummaryVO> camps = platformBootstrapMapper.selectAvailableCamps();
        if (camps == null || camps.isEmpty()) {
            return DEFAULT_REGISTER_CAMP_ID;
        }
        return camps.get(0).getCampId();
    }
}
