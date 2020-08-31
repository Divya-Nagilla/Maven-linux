package testid.core.module.user.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tech.ibit.common.crypto.Md5Utils;
import testid.core.exception.UserErrorCode;
import testid.core.module.enterprise.dto.EnterpriseDto;
import testid.core.module.enterprise.service.EnterpriseService;
import testid.core.module.session.constant.SessionKey;
import testid.core.module.session.service.SessionService;
import testid.core.module.user.dto.UserLoginDto;
import testid.core.module.user.param.LoginParam;
import testid.core.module.user.service.UserLoginService;
import testid.core.module.user.service.UserService;
import testid.db.entity.User;
import testid.db.mapper.UserMapper;
import tech.ibit.web.springboot.exception.ApiException;
import tech.ibit.web.springboot.session.Session;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;

/**
 * 用户登陆实现
 *
 * @author IBIT TECH
 */
@Service
@Slf4j
public class UserLoginServiceImpl implements UserLoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private EnterpriseService enterpriseService;

    /**
     * 用户登陆
     *
     * @param loginParam 登陆参数
     * @return 用户
     */
    @Override
    public UserLoginDto login(HttpServletRequest request, LoginParam loginParam) {

        User user = userService.getByUsername(loginParam.getUsername());
        if (null == user) {
            throw new ApiException(UserErrorCode.UserNameOrPwdError.MESSAGE);
        }
        try {
            if (!Md5Utils.verify(loginParam.getPassword(), user.getPassword())) {
                throw new ApiException(UserErrorCode.UserNameOrPwdError.MESSAGE);
            }
        } catch (NoSuchAlgorithmException e) {
            // ignore
            return null;
        }

        // 将userId设置到session中
        Session session = sessionService.getSession(request, true);
        session.setAttribute(SessionKey.userId, user.getUserId());

        EnterpriseDto enterprise = enterpriseService.getDto(user.getEnterpriseId());
        return new UserLoginDto(user, enterprise);
    }

    /**
     * 获取登陆对象
     *
     * @param request 请求
     * @return 登陆对象
     */
    @Override
    public UserLoginDto getLoginInfo(HttpServletRequest request) {
        Session session = sessionService.getSession(request);
        Integer userId = (Integer) session.getAttribute(SessionKey.userId);
        User user = userMapper.getById(userId);
        EnterpriseDto enterprise = enterpriseService.getDto(user.getEnterpriseId());
        return new UserLoginDto(user, enterprise);
    }

    /**
     * 注销登陆
     *
     * @param request 请求
     */
    @Override
    public void logout(HttpServletRequest request) {
        Session session = sessionService.getSession(request);
        session.invalid();
    }
}
