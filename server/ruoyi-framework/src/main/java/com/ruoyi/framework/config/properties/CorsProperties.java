package com.ruoyi.framework.config.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * CORS 配置
 *
 * @author ruoyi
 */
@Component
@ConfigurationProperties(prefix = "security.cors")
public class CorsProperties
{
    /**
     * 是否启用 CORS
     */
    private boolean enabled = true;

    /**
     * 允许跨域的来源模式
     */
    private List<String> allowedOriginPatterns = new ArrayList<>();

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public List<String> getAllowedOriginPatterns()
    {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns)
    {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }
}
